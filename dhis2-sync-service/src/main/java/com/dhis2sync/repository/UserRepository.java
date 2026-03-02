package com.dhis2sync.repository;

import com.dhis2sync.model.UserWithRolesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for reading user records with their associated role codes
 * from the {@code users_ic_mj} and {@code user_rolejc_mj} tables.
 *
 * <p>Uses an inner JOIN so that users without any role assignment are
 * excluded — a user without roles cannot be meaningfully synced to DHIS2.</p>
 *
 * <p>Performs a full table scan each sync cycle (no WHERE clause) per the
 * architecture decision of "每次全量查询 DB".</p>
 */
@Repository
public class UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor injection of the JdbcTemplate bean provided by DataSourceConfig.
     *
     * @param jdbcTemplate the configured JdbcTemplate instance
     */
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Queries all users joined with their role assignments and aggregates
     * the results so that each unique user appears once with a collected
     * list of role codes.
     *
     * <p>Column mapping:
     * <ul>
     *   <li>{@code u.username} → {@link UserWithRolesDTO#getUsername()}</li>
     *   <li>{@code u.orgcode} → {@link UserWithRolesDTO#getOrgcode()}</li>
     *   <li>{@code u.id} (aliased {@code user_id}) → {@link UserWithRolesDTO#getUserId()}</li>
     *   <li>{@code ur.role_id} → appended to {@link UserWithRolesDTO#getRoleCodes()}</li>
     * </ul>
     *
     * @return list of all users with aggregated role codes; may be empty
     */
    public List<UserWithRolesDTO> findAllUsersWithRoles() {
        String sql = "SELECT u.username, u.orgcode, u.id AS user_id, ur.role_id "
                   + "FROM users_ic_mj u "
                   + "JOIN user_rolejc_mj ur ON u.id = ur.user_id";

        // LinkedHashMap preserves insertion order for deterministic iteration
        Map<String, UserWithRolesDTO> userMap = new LinkedHashMap<>();

        jdbcTemplate.query(sql, (rs, rowNum) -> {
            String userId   = rs.getString("user_id");
            String username = rs.getString("username");
            String orgcode  = rs.getString("orgcode");
            String roleId   = rs.getString("role_id");

            // Create a new DTO entry if this user has not been seen yet
            UserWithRolesDTO dto = userMap.get(userId);
            if (dto == null) {
                dto = new UserWithRolesDTO(username, orgcode, userId, new ArrayList<>());
                userMap.put(userId, dto);
            }

            // Append the current row's role code to the user's role list
            dto.addRoleCode(roleId);

            // Return value is unused; we aggregate into userMap directly
            return dto;
        });

        logger.info("从数据库查询到 {} 条用户记录（含角色关联）", userMap.size());

        return new ArrayList<>(userMap.values());
    }
}