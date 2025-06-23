package hello.aop.order.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

@Slf4j
public class AspectV5 {

    @Aspect
    @Order(2)
    public static class LogAspect{
        @Around("hello.aop.order.Pointcuts.allOrder()")
        public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
            log.info("[log] {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }
    }
    @Aspect
    @Order(1)
    public static class TxAspect{
        @Around("hello.aop.order.Pointcuts.orderAndService()")
        public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
            try{
                log.info("[transaction start] {}", joinPoint.getSignature());
                Object result = joinPoint.proceed();
                log.info("[transaction end] {}", joinPoint.getSignature());
                return result;
            } catch (Throwable e) {
                log.info("[transaction rollback] {}", joinPoint.getSignature());
                throw e;
            } finally {
                log.info("[resource release] {}", joinPoint.getSignature());
            }
        }
    }
}
