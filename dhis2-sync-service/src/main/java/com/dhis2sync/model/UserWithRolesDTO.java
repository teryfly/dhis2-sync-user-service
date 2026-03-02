package com.dhis2sync.model;

import java.util.List;

/**
 * Mutable data carrier representing a single user and their associated role codes,
 * aggregated from the {@code users_ic_mj} / {@code user_rolejc_mj} join query.
 *
 * <p>The {@code roleCodes} list is mutable to support incremental aggregation
 * in {@code UserRepository} as joined result rows are iterated.</p>
 *
 * <p>Field mapping:
 * <ul>
 *   <li>{@code users_ic_mj.username} → {@code username}</li>
 *   <li>{@code users_ic_mj.orgcode} → {@code orgcode}</li>
 *   <li>{@code users_ic_mj.id} → {@code userId}</li>
 *   <li>{@code user_rolejc_mj.role_id} → entries in {@code roleCodes}</li>
 * </ul>
 */
public class UserWithRolesDTO {

    private final String username;
    private final String orgcode;
    private final String userId;
    private final List<String> roleCodes;

    /**
     * All-args constructor.
     *
     * @param username  user login name
     * @param orgcode   organisation unit code for DHIS2 lookup
     * @param userId    user identifier from source database
     * @param roleCodes mutable list of role codes associated with this user
     */
    public UserWithRolesDTO(String username, String orgcode, String userId, List<String> roleCodes) {
        this.username = username;
        this.orgcode = orgcode;
        this.userId = userId;
        this.roleCodes = roleCodes;
    }

    public String getUsername() {
        return username;
    }

    public String getOrgcode() {
        return orgcode;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    /**
     * Appends a role code to this user's role list.
     * Used during row-by-row aggregation in {@code UserRepository}.
     *
     * @param roleCode the role code to add
     */
    public void addRoleCode(String roleCode) {
        roleCodes.add(roleCode);
    }

    @Override
    public String toString() {
        return "UserWithRolesDTO{username='" + username + "', orgcode='" + orgcode
                + "', userId='" + userId + "', roleCodes=" + roleCodes + "}";
    }
}