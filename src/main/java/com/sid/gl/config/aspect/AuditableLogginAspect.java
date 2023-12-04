package com.sid.gl.config.aspect;

import com.sid.gl.model.ApplicationLog;
import com.sid.gl.services.impl.ApplicationLogService;
import com.sid.gl.utils.SecurityHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZoneId;

@Aspect
@Configuration
@RequiredArgsConstructor
public class AuditableLogginAspect {

    private final ApplicationLogService logService;
    Logger log = LoggerFactory.getLogger(AuditableLogginAspect.class);

    @Pointcut("@annotation(com.sid.gl.config.aspect.Auditable)")
    public void pointcut(){

    }
    @Around("execution(* com.sid.gl.controllers.*.*(..))")
    public Object around(ProceedingJoinPoint point){
     Object result = null;
     long beginTime = System.currentTimeMillis();
     try{
         //execution
         result = point.proceed();
     } catch (Throwable e){

     }
      //save audit
      audit(point,beginTime);
      return result;
    }

    private void audit(ProceedingJoinPoint point,Long time){
        MethodSignature signature= (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        ApplicationLog applicationLog=
                new ApplicationLog();
        String className = point.getTarget().getClass().getName();
        String methodName = signature.getName();
        applicationLog.setMethod(className + "."+methodName+"()");
        Object[] args = point.getArgs();

        DefaultParameterNameDiscoverer u = new DefaultParameterNameDiscoverer();
        String[] paramNames = u.getParameterNames(method);
        if (args != null && paramNames != null) {
            String params = "";
            for (int i = 0; i < args.length; i++) {
                params += "  " + paramNames[i] + ": " + args[i];
            }
            applicationLog.setParams(params);
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        applicationLog.setEndPoint(request.getServletPath());
        //check username
        applicationLog.setUsername(SecurityHelper.loadUserAuthenticated(request));
        applicationLog.setOperation(request.getMethod());
        //zone id

        applicationLog.setRequestTime(Instant.ofEpochMilli(time)
                .atZone(ZoneId.of("Africa/Casablanca")).toLocalDateTime());
        //save application log
        logService.saveLog(applicationLog);

    }

   /* @Before("execution(* com.sid.gl.repositories.*.*(..))")
    public void logMethodCall(JoinPoint jp) {
        String methodName = jp.getSignature().getName();
        log.info("Before " + methodName);
    }*/

}