package com.evan.framework.biz.operationlog.aspect;
import com.evan.framework.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

@Aspect
@Slf4j
public class ApiOperationLogAspect {
    @Pointcut("@annotation(com.evan.framework.biz.operationlog.aspect.ApiOperationLog)")
    public void apiOperationLog(){}
    @Around("apiOperationLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String argsJsonStr= Arrays.stream(args).map(toJsonStr()).collect(Collectors.joining(", "));
        String description=getApiOperationLogDescription(joinPoint);
        log.info("====== 请求开始: [{}], 入参: {}, 请求类: {}, 请求方法: {} =======",description,argsJsonStr,className,methodName);
        Object result = joinPoint.proceed();
        long executionTime=System.currentTimeMillis()-startTime;
        log.info("====== 请求结束: [{}], 耗时: {}ms, 出参: {}======",description,executionTime,JsonUtils.toJsonString(result));
        return result;
    }
    private String getApiOperationLogDescription(ProceedingJoinPoint joinPoint) {
        MethodSignature signature=(MethodSignature)joinPoint.getSignature();
        Method method=signature.getMethod();
        ApiOperationLog apiOperationLog=method.getAnnotation(ApiOperationLog.class);
        return apiOperationLog.description();
    }
    private Function<Object, String> toJsonStr(){
        return JsonUtils::toJsonString;
    }
}
