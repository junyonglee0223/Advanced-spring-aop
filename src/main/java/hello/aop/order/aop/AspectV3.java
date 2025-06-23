package hello.aop.order.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Slf4j
@Aspect
public class AspectV3 {
    @Pointcut("execution(* hello.aop.order..*(..))")
    private void allOrder(){}
    @Pointcut("execution(* *..*Service.*(..))")
    private void allService(){}

    @Around("allOrder()")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[log] {}", joinPoint.getSignature());
        return joinPoint.proceed();
    }
    @Around("allOrder() && allService()")
    public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        try{
            log.info("[transaction start] {}", joinPoint.getSignature());
            Object result = joinPoint.proceed();
            log.info("[transaction end] {}", joinPoint.getSignature());

            return result;
        } catch (Throwable e){
            log.info("[transaction rollback] {}", joinPoint.getSignature());
            throw e;
        } finally {
            log.info("[resource release] {}", joinPoint.getSignature());
        }
    }
}
