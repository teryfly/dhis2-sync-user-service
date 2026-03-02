package com.dhis2sync.service;

import com.dhis2sync.client.Dhis2RoleClient;
import com.dhis2sync.model.RoleDTO;
import com.dhis2sync.model.SyncItemOutcome;
import com.dhis2sync.model.SyncResult;
import com.dhis2sync.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business service for synchronizing roles from the database to DHIS2.
 * Performs deduplication checks and handles per-item errors gracefully
 * without halting the overall sync process.
 */
@Service
public class RoleSyncService {

    private static final Logger logger = LoggerFactory.getLogger(RoleSyncService.class);

    private final RoleRepository roleRepository;
    private final Dhis2RoleClient dhis2RoleClient;

    /**
     * Constructor injection of repository and client dependencies.
     *
     * @param roleRepository   repository for reading roles from database
     * @param dhis2RoleClient  client for DHIS2 role API operations
     */
    public RoleSyncService(RoleRepository roleRepository, Dhis2RoleClient dhis2RoleClient) {
        this.roleRepository = roleRepository;
        this.dhis2RoleClient = dhis2RoleClient;
    }

    /**
     * Executes the full role synchronization workflow.
     * Reads all roles from the database, checks for duplicates in DHIS2,
     * and creates missing roles.
     *
     * @return summary of sync operation with success/skipped/failed counts
     */
    public SyncResult syncRoles() {
        logger.info("开始同步角色...");

        List<RoleDTO> roles = roleRepository.findAllRoles();
        int total = roles.size();
        int success = 0;
        int skipped = 0;
        int failed = 0;

        for (RoleDTO role : roles) {
            SyncItemOutcome outcome = processRole(role);
            switch (outcome) {
                case SUCCESS -> success++;
                case SKIPPED -> skipped++;
                case FAILED -> failed++;
            }
        }

        logger.info("角色同步完成: total={}, success={}, skipped={}, failed={}", 
                    total, success, skipped, failed);

        return new SyncResult(total, success, skipped, failed);
    }

    /**
     * Processes a single role: checks existence in DHIS2, creates if missing.
     * All exceptions are caught and logged; the method returns an outcome
     * indicating success, skip, or failure.
     *
     * @param role the role to process
     * @return the outcome of processing this role
     */
    private SyncItemOutcome processRole(RoleDTO role) {
        try {
            // Check if role already exists in DHIS2
            Optional<String> existing = dhis2RoleClient.findRoleByCode(role.getCode());

            if (existing.isPresent()) {
                logger.info("角色已存在,跳过: code={}, name={}", role.getCode(), role.getRoleName());
                return SyncItemOutcome.SKIPPED;
            }

            // Create new role in DHIS2
            // Note: both name and description use roleName per architecture spec
            dhis2RoleClient.createRole(role.getRoleName(), role.getRoleName(), role.getCode());
            logger.info("角色创建成功: code={}, name={}", role.getCode(), role.getRoleName());
            return SyncItemOutcome.SUCCESS;

        } catch (Exception e) {
            logger.error("角色同步失败: code={}, name={}, error={}", 
                        role.getCode(), role.getRoleName(), e.getMessage(), e);
            return SyncItemOutcome.FAILED;
        }
    }
}