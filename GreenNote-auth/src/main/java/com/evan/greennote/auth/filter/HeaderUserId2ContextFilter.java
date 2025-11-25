package com.evan.greennote.auth.filter;

import com.evan.framework.common.constant.GlobalConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//提取请求头中用户ID保存到上下文，方便后续使用
@Component
@Slf4j
public class HeaderUserId2ContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException{
        //从请求头中获取用户ID
        String userId=request.getHeader(GlobalConstants.USER_ID);
        log.info("## HeaderUserId2ContextFilter, 用户 ID：{}", userId);

//        //判断请求头中是否存在用户ID
//        if(StringUtils.isBlank(userId)){
//            //若为空则直接放行
//            chain.doFilter(request, response);
//            return;
//        }

        if(StringUtils.isNotBlank(userId)){
            //如果请求头中存在用户ID，则设置到ThreadLocal中
            log.info("===== 设置 userId 到 ThreadLocal 中，用户 ID：{}", userId);
            LoginUserContextHolder.setUserId(userId);
        }
        //始终放行请求
        chain.doFilter(request, response);

        //如果请求头中存在用户ID，则设置到ThreadLocal中
        log.info("===== 设置 userId 到 ThreadLocal 中，用户 ID：{}", userId);
        LoginUserContextHolder.setUserId(userId);

        try{
            chain.doFilter(request, response);
        }finally{
            //删除 ThreadLocal，防止内存泄漏
            LoginUserContextHolder.remove();
            //log.info("===== 删除 ThreadLocal，userId: {}",userId);
            if(StringUtils.isNotBlank(userId)) {
                log.info("===== 删除 ThreadLocal，userId: {}", userId);
            }
        }
    }
}
