package com.evan.greennote.user.biz.constant;

public class RedisKeyConstants {
    //小绿书 ID 全局生成器
    public static final String GREENNOTE_ID_GENERATOR_KEY="greennote.id.generator";
    //用户角色数据
    private static final String USER_ROLES_KEY_PREFIX="user:roles:";
    //用户对应权限集合
    private static final String ROLE_PERMISSIONS_KEY_PREFIX="role:permissions:";
    //用户信息数据KEY前缀
    private static final String USER_INFO_KEY_PREFIX="user:info:";
    //角色对应权限集合KEY
    public static String buildUserInfoKey(Long userId){
        return USER_INFO_KEY_PREFIX + userId;
    }
    //用户对应角色集合
    public static String buildUserRolesKey(Long userId){
        return USER_ROLES_KEY_PREFIX + userId;
    }
    //角色对应权限集合
    public static String buildRolePermissionsKey(String  roleKey){
        return ROLE_PERMISSIONS_KEY_PREFIX + roleKey;
    }
}





