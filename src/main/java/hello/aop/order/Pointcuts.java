package hello.aop.order;

import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {
    @Pointcut("execution(* hello.aop.order..*(..))")
    public void allOrder(){}
    @Pointcut("execution(* *..*Service.*(..))")
    public void allService(){}
    @Pointcut("allOrder() && allService()")
    public void orderAndService(){}
}
