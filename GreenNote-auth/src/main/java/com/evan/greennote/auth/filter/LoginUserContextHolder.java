package com.evan.greennote.auth.filter;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.evan.framework.common.constant.GlobalConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginUserContextHolder {
    //初始化ThreadLocal变量
    private static final ThreadLocal<Map<String, Object>> LOGIN_USER_CONTEXT_THREAD_LOCAL
            = TransmittableThreadLocal.withInitial(HashMap::new);
    //设置用户ID
    public static void setUserId(Object value){
        LOGIN_USER_CONTEXT_THREAD_LOCAL.get().put(GlobalConstants.USER_ID, value);
    }
    //获取用户ID
    public static Long getUserId(){
        Object value=LOGIN_USER_CONTEXT_THREAD_LOCAL.get().get(GlobalConstants.USER_ID);
        if(Objects.isNull(value)){
            return null;
        }
        return Long.valueOf(value.toString());
    }
    //删除ThreadLocal
    public static void remove(){
        LOGIN_USER_CONTEXT_THREAD_LOCAL.remove();
    }
}
