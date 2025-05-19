package harvestLog.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ControllerLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ControllerLoggingAspect.class);

    // Pointcut for all public methods in controllers
    @Before("execution(public * harvestLog.controller..*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        Long farmerId = getFarmerIdFromArgs(args);
        logger.info("Entering {} with farmerId: {} and arguments: {}",
                methodName, farmerId != null ? farmerId : "unknown", Arrays.toString(args));
    }

    // Log after successful execution
    @AfterReturning(pointcut = "execution(public * harvestLog.controller..*.*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        logger.info("Exiting {} with result: {}", methodName, result);
    }

    // Log exceptions
    @AfterThrowing(pointcut = "execution(public * harvestLog.controller..*.*(..))", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().toShortString();
        logger.error("Exception in {}: {}", methodName, exception.getMessage(), exception);
    }

    // Helper method to extract farmerId (customize based on your logic)
    private Long getFarmerIdFromArgs(Object[] args) {
        try {
            // Assuming farmerId is extracted via FarmerIdExtractor in controllers
            // This is a placeholder; you may need to adjust based on how farmerId is passed
            return harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId();
        } catch (Exception e) {
            return null;
        }
    }
}