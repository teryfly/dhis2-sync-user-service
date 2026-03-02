package com.dhis2sync.service;

import com.dhis2sync.client.Dhis2OrgUnitClient;
import com.dhis2sync.client.Dhis2RoleClient;
import com.dhis2sync.client.Dhis2UserClient;
import com.dhis2sync.config.Dhis2Properties;
import com.dhis2sync.model.SyncItemOutcome;
import com.dhis2sync.model.SyncResult;
import com.dhis2sync.model.UserWithRolesDTO;
import com.dhis2sync.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Business service for synchronizing users from the database to DHIS2.
 * Resolves role and organisation unit UIDs, performs deduplication checks,
 * and handles per-item errors gracefully without halting the overall sync process.
 */
@Service
public class UserSyncService {

    private static final Logger logger = LoggerFactory.getLogger(UserSyncService.class);

    private final UserRepository userRepository;
    private final Dhis2UserClient dhis2UserClient;
    private final Dhis2RoleClient dhis2RoleClient;
    private final Dhis2OrgUnitClient dhis2OrgUnitClient;
    private final Dhis2Properties dhis2Properties;

    /**
     * Constructor injection of repository, client, and configuration dependencies.
     *
     * @param userRepository      repository for reading users from database
     * @param dhis2UserClient     client for DHIS2 user API operations
     * @param dhis2RoleClient     client for DHIS2 role API operations (for UID resolution)
     * @param dhis2OrgUnitClient  client for DHIS2 org unit API operations (for UID resolution)
     * @param dhis2Properties     configuration properties (for default password)
     */
    public UserSyncService(UserRepository userRepository,
                           Dhis2UserClient dhis2UserClient,
                           Dhis2RoleClient dhis2RoleClient,
                           Dhis2OrgUnitClient dhis2OrgUnitClient,
                           Dhis2Properties dhis2Properties) {
        this.userRepository = userRepository;
        this.dhis2UserClient = dhis2UserClient;
        this.dhis2RoleClient = dhis2RoleClient;
        this.dhis2OrgUnitClient = dhis2OrgUnitClient;
        this.dhis2Properties = dhis2Properties;
    }

    /**
     * Executes the full user synchronization workflow.
     * Reads all users with their role associations from the database,
     * resolves DHIS2 UIDs for roles and organisation units,
     * checks for duplicates in DHIS2, and creates missing users.
     *
     * @return summary of sync operation with success/skipped/failed counts
     */
    public SyncResult syncUsers() {
        logger.info("开始同步用户...");

        List<UserWithRolesDTO> users = userRepository.findAllUsersWithRoles();
        int total = users.size();
        int success = 0;
        int skipped = 0;
        int failed = 0;

        for (UserWithRolesDTO user : users) {
            SyncItemOutcome outcome = processUser(user);
            switch (outcome) {
                case SUCCESS -> success++;
                case SKIPPED -> skipped++;
                case FAILED -> failed++;
            }
        }

        logger.info("用户同步完成: total={}, success={}, skipped={}, failed={}", 
                    total, success, skipped, failed);

        return new SyncResult(total, success, skipped, failed);
    }

    /**
     * Processes a single user: checks existence in DHIS2, resolves role and
     * organisation unit UIDs, creates user if missing.
     * Missing role/org references are handled gracefully with warnings.
     * All exceptions are caught and logged; the method returns an outcome
     * indicating success, skip, or failure.
     *
     * @param user the user to process
     * @return the outcome of processing this user
     */
    private SyncItemOutcome processUser(UserWithRolesDTO user) {
        try {
            // Check if user already exists in DHIS2
            Optional<String> existing = dhis2UserClient.findUserByUsername(user.getUsername());

            if (existing.isPresent()) {
                logger.info("用户已存在,跳过: username={}", user.getUsername());
                return SyncItemOutcome.SKIPPED;
            }

            // Resolve role UIDs from role codes
            List<String> roleUids = new ArrayList<>();
            for (String roleCode : user.getRoleCodes()) {
                Optional<String> roleUidOpt = dhis2RoleClient.findRoleByCode(roleCode);
                if (roleUidOpt.isPresent()) {
                    roleUids.add(roleUidOpt.get());
                } else {
                    logger.warn("角色未找到,跳过该角色: roleCode={}, username={}", 
                               roleCode, user.getUsername());
                }
            }

            // Resolve organisation unit UID from orgcode
            List<String> orgUnitUids;
            Optional<String> orgUidOpt = dhis2OrgUnitClient.findOrgUnitByCode(user.getOrgcode());
            if (orgUidOpt.isPresent()) {
                orgUnitUids = List.of(orgUidOpt.get());
            } else {
                logger.warn("机构未找到,创建用户时机构列表为空: orgcode={}, username={}", 
                           user.getOrgcode(), user.getUsername());
                orgUnitUids = List.of();
            }

            // Create user in DHIS2
            // firstName and surname both use username (no separate name fields in source DB)
            String defaultPassword = dhis2Properties.getDefaultUserPassword();
            dhis2UserClient.createUser(
                user.getUsername(),
                defaultPassword,
                user.getUsername(),  // firstName
                user.getUsername(),  // surname
                roleUids,
                orgUnitUids
            );

            logger.info("用户创建成功: username={}, roleCount={}, orgUnitAssigned={}", 
                       user.getUsername(), roleUids.size(), !orgUnitUids.isEmpty());
            return SyncItemOutcome.SUCCESS;

        } catch (Exception e) {
            logger.error("用户同步失败: username={}, error={}", 
                        user.getUsername(), e.getMessage(), e);
            return SyncItemOutcome.FAILED;
        }
    }
}