package ru.region_stat.aspects;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import java.lang.annotation.Annotation;
import java.util.Arrays;

@Aspect
@Component
public class LoggingHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut("execution(* ru.region_stat.controller..*(.., @org.springframework.web.bind.annotation.RequestBody (*), ..))")
    public void restController() {
    }

    @Pointcut("execution(* *.*(..))")
    public void allMethod() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PostMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.GetMapping) ||"
            + "@annotation(org.springframework.web.bind.annotation.PutMapping) ||"
            + "@annotation(org.springframework.web.bind.annotation.DeleteMapping) ||"
            + "@annotation(org.springframework.web.bind.annotation.ExceptionHandler)")
    public void logRequestMapping() {
    }

    @Before("restController() || logRequestMapping())"
    )
    public void logRequestBody(JoinPoint thisJoinPoint) {
        MethodSignature methodSignature = (MethodSignature) thisJoinPoint.getSignature();
        Annotation[][] annotationMatrix = methodSignature.getMethod().getParameterAnnotations();
        int index = -1;
        for (Annotation[] annotations : annotationMatrix) {
            index++;
            for (Annotation annotation : annotations) {
                if (!(annotation instanceof RequestBody)) {
                    continue;
                }
                Object requestBody = thisJoinPoint.getArgs()[index];
                log.info(String.valueOf(thisJoinPoint));
                log.info("  Request body = " + requestBody);
            }
        }
    }

    @AfterReturning(pointcut = "restController() && allMethod()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {

        log.info("{}|{}|{}|{}",
                "Entering in Method : " + joinPoint.getSignature().getName(),
                "Class Name :  " + joinPoint.getSignature().getDeclaringTypeName(),
                "Arguments :  " + Arrays.toString(joinPoint.getArgs()),
                "Target class : " + joinPoint.getTarget().getClass().getName()
        );

        log.info("Method Return value : " + this.getValue(result));
    }

    @AfterThrowing(pointcut = "restController() && allMethod()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {

        log.error("{}|{}",
                "An exception has been thrown in : " + joinPoint.getSignature().getName() + "()",
                "Message : " + exception.getLocalizedMessage() + "()");
        if (exception.getCause() != null) {
            log.error("Cause : " + exception.getCause());
        }
    }

    @Around("restController() && allMethod()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();
        try {
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;
            log.info("Method " + className + "." + methodName + " ()" + " execution time : " + elapsedTime + " ms");
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument " + Arrays.toString(joinPoint.getArgs()) + " in " + joinPoint.getSignature().getName() + "()");
            throw e;
        }
    }

    private String getValue(Object result) {
        String returnValue = null;
        if (null != result) {
            if (result.toString().endsWith("@" + Integer.toHexString(result.hashCode()))) {
                returnValue = ReflectionToStringBuilder.toString(result);
            } else {
                returnValue = result.toString();
            }
        }
        return returnValue;
    }
}