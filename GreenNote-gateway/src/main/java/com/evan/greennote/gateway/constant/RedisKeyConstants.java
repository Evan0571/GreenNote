package com.evan.greennote.gateway.constant;

public class RedisKeyConstants {
    private static final String USER_ROLES_KEY_PREFIX="user:roles:";
    private static final String ROLE_PERMISSION_KEY_PREFIX="role:permissions:";

    public static String buildRolePermissionKey(String roleKey){
        return USER_ROLES_KEY_PREFIX+roleKey;
    }
    public static String buildUserRoleKey(Long userId){
        return USER_ROLES_KEY_PREFIX+userId;
    }
}
