package com.evan.greennote.auth.constant;

public class RedisKeyConstants {
    private static final String VERIFICATION_CODE_KEY_PREFIX="verification_code";
    public static String buildVerificationCodeKey(String email){
        return VERIFICATION_CODE_KEY_PREFIX+":"+email;
    }
    public static final String GREENNOTE_ID_GENERATOR_KEY="greennote.id.generator";
    private static final String USER_ROLES_KEY_PREFIX="user:roles:";
    private static final String ROLE_PERMISSIONS_KEY_PREFIX="role:permissions:";
    public static String buildUserRolesKey(Long userId){
        return USER_ROLES_KEY_PREFIX + userId;
    }
    public static String buildRolePermissionsKey(String  roleKey){
        return ROLE_PERMISSIONS_KEY_PREFIX + roleKey;
    }
}
