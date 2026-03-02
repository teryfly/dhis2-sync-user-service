package com.dhis2sync.model;

/**
 * Immutable data carrier representing a single role record
 * read from the {@code role_jc_mj} database table.
 *
 * <p>Field mapping:
 * <ul>
 *   <li>{@code role_jc_mj.role_name} → {@code roleName}</li>
 *   <li>{@code role_jc_mj.id} → {@code code} (used as DHIS2 role code for deduplication)</li>
 * </ul>
 */
public class RoleDTO {

    private final String roleName;
    private final String code;

    /**
     * All-args constructor.
     *
     * @param roleName human-readable role name
     * @param code     unique role identifier used as DHIS2 code
     */
    public RoleDTO(String roleName, String code) {
        this.roleName = roleName;
        this.code = code;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "RoleDTO{roleName='" + roleName + "', code='" + code + "'}";
    }
}