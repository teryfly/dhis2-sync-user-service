package com.dhis2sync.repository;

import com.dhis2sync.model.RoleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for reading role records from the {@code role_jc_mj} database table.
 * Performs a full table scan each sync cycle (no WHERE clause) per the
 * architecture decision of "每次全量查询 DB".
 */
@Repository
public class RoleRepository {

    private static final Logger logger = LoggerFactory.getLogger(RoleRepository.class);

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor injection of the JdbcTemplate bean provided by DataSourceConfig.
     *
     * @param jdbcTemplate the configured JdbcTemplate instance
     */
    public RoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Queries all role records from {@code role_jc_mj} and maps each row
     * to a {@link RoleDTO}.
     *
     * <p>Column mapping:
     * <ul>
     *   <li>{@code role_name} → {@link RoleDTO#getRoleName()}</li>
     *   <li>{@code id} → {@link RoleDTO#getCode()} (used as DHIS2 role code for deduplication)</li>
     * </ul>
     *
     * @return list of all roles; may be empty if the table has no rows
     */
    public List<RoleDTO> findAllRoles() {
        String sql = "SELECT role_name, id FROM role_jc_mj";

        List<RoleDTO> roles = jdbcTemplate.query(sql,
                (rs, rowNum) -> new RoleDTO(
                        rs.getString("role_name"),
                        rs.getString("id")
                )
        );

        logger.info("从数据库查询到 {} 条角色记录", roles.size());

        return roles;
    }
}