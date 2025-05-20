// !!!!! do not enable, interferes with tool functionality

//package harvestLog.advice;
//
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.*;
//import org.springframework.ai.tool.annotation.Tool;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//
//@Aspect
//@Component
//@Slf4j
//public class ToolCallLogger {
//
//    @Pointcut("@annotation(toolAnnotation)")
//    public void toolMethod(Tool toolAnnotation) {}
//
//    @Before("toolMethod(toolAnnotation)")
//    public void logToolCall(JoinPoint joinPoint, Tool toolAnnotation) {
//        String methodName = joinPoint.getSignature().getName();
//        Object[] args = joinPoint.getArgs();
//
//        log.info("🛠️ Tool called: '{}'", methodName);
//        log.info("🔍 Description: {}", toolAnnotation.description());
//        log.info("📥 Arguments: {}", Arrays.toString(args));
//    }
//}
