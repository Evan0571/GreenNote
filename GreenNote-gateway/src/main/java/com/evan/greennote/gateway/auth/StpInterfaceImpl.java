package com.evan.greennote.gateway.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.collection.CollUtil;
import com.evan.greennote.gateway.constant.RedisKeyConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public List<String> getPermissionList(Object loginId, String loginType){
        log.info("## 获取用户权限列表, loginId: {}",loginId);
        String userRolesKey=RedisKeyConstants.buildUserRoleKey(Long.valueOf(loginId.toString()));

        String useRolesValue=redisTemplate.opsForValue().get(userRolesKey);

        if(StringUtils.isBlank(useRolesValue)){
            return null;
        }

        List<String> userRoleKeys=objectMapper.readValue(useRolesValue, new TypeReference<>(){});

        if(CollUtil.isNotEmpty(userRoleKeys)){
            List<String> rolePermissionsKeys=userRoleKeys.stream()
                    .map(RedisKeyConstants::buildRolePermissionKey)
                    .toList();
            List<String> rolePermissionsValues=redisTemplate.opsForValue().multiGet(rolePermissionsKeys);
            if(CollUtil.isNotEmpty(rolePermissionsValues)){
                List<String> permissions= Lists.newArrayList();
                rolePermissionsValues.forEach(jsonValue->{
                   try{
                       List<String> rolePermissions=objectMapper.readValue(jsonValue, new TypeReference<>(){});
                       permissions.addAll(rolePermissions);
                   }catch(JsonProcessingException e){
                       log.error("==> JSON 解析错误：",e);
                   }
                });
                return permissions;
            }
        }
        return null;
    }

    @SneakyThrows
    @Override
    public List<String> getRoleList(Object loginId, String loginType){
        log.info("## 获取用户角色列表, loginId: {}",loginId);
        String userRolesKey= RedisKeyConstants.buildUserRoleKey(Long.valueOf(loginId.toString()));
        String userRolesValue=redisTemplate.opsForValue().get(userRolesKey);
        if(StringUtils.isBlank(userRolesValue)){
            return null;
        }
        return objectMapper.readValue(userRolesValue, new TypeReference<>(){});
    }
}
