# Advanced-spring-aop

---
# 250611
## [Spring AOP implementation]
### AOP Test 설정 및 실행

Spring AOP를 활용한 `OrderService`와 `OrderRepository`의 프록시 생성 및 로그 출력 기능을 구현하고 테스트합니다.

### 프로젝트 구조

```
src
 └─ main
     └─ java
         └─ hello
             ├─ aop
             │   └─ order
             │       ├─ OrderService.java
             │       ├─ OrderServiceImpl.java
             │       ├─ OrderRepository.java
             │       ├─ OrderRepositoryImpl.java
             │       └─ aop
             │           ├─ AspectV1.java
             │           └─ AspectV2.java
             └─ AopTest.java
```

### AopTest.java

* `@SpringBootTest` 환경에서 AOP 프록시 적용 여부 확인
* `isAopProxy(orderService)`, `isAopProxy(orderRepository)` 로그 출력
* 정상 호출 테스트: `orderService.orderItem("hello")`
* 예외 발생 테스트: `orderService.orderItem("ex")` 시 `IllegalStateException` 검증

### AspectV1 (직접 포인트컷 설정)

* `@Aspect` 어노테이션으로 AOP 설정 클래스 지정
* `@Around` 어드바이스로 `hello.aop.order` 패키지 하위 모든 메서드에 적용
* 메서드 시그니처와 파라미터를 로그로 출력 후, 실제 비즈니스 메서드 실행

### AspectV2 (공유 포인트컷 필드 사용)

* `@Pointcut`으로 공통 포인트컷 정의
* 다른 어드바이스에서도 재사용 가능 (필드를 `public`으로 선언하면 외부 참조 가능)
* 반환 타입은 `Object`

### 테스트 실행 결과

* 서비스와 리포지토리 메서드가 호출될 때마다 로그 출력
* 프록시 적용 확인: `orderService`, `orderRepository` 모두 `true`

로그 예시:

```
[log] void hello.aop.order.OrderService.orderItem(String)
[orderService] 실행
[log] String hello.aop.order.OrderRepository.save(String)
[orderRepository] 실행
```

### 주요 포인트

* AspectJ 표현식: `execution(* 패키지경로..*(..))`
* `@Around` 어드바이스 사용 시 `ProceedingJoinPoint`로 비즈니스 로직 호출 제어
* `@Pointcut`으로 포인트컷 재활용 가능
* 테스트 환경에서 `AopUtils.isAopProxy`로 프록시 여부 확인
---

# 250623
## [Spring AOP implementation] (cont)
### AOP를 활용한 트랜잭션 및 로깅 처리

트랜잭션의 기본 흐름은 다음과 같다:
핵심 비즈니스 로직 실행 → 예외가 없으면 `commit` → 예외 발생 시 `rollback`

Spring AOP를 활용하여 트랜잭션과 로깅을 처리하는 예제로, `AspectV3` 클래스를 생성하여 테스트를 진행한다.


### AspectV3 클래스 구성

* 위치: `src > … > order > aop`
* 사용 어노테이션: `@Aspect`, `@Slf4j`

#### Pointcut 정의

1. **allOrder**

    * 대상: `hello.aop.order` 하위의 모든 클래스의 모든 메서드
    * 표현식: `execution(* hello.aop.order..*(..))`

2. **allService**

    * 대상: 이름이 `Service`로 끝나는 모든 클래스의 모든 메서드
    * 표현식: `execution(* *..*Service.*(..))`

#### Advice 메서드

1. **doLog**

    * 대상: `allOrder`
    * 설명: 메서드 호출 전후 로그 출력
    * 주요 로직:

2. **doTransaction**

    * 대상: `allOrder && allService`
    * 설명: 트랜잭션 처리 담당
    * 주요 로직:



### 실행 로그 결과 요약

Spring AOP가 적용된 서비스와 저장소 컴포넌트에 대해 AOP 프록시가 적용되고, 로그 및 트랜잭션 처리가 정상 수행된다:

* `orderService`와 `orderRepository`가 AOP 프록시 적용 여부 확인 → `true`
* 정상 케이스:

  ```
  [log] void OrderService.orderItem(String)
  [transaction start] void OrderService.orderItem(String)
  [orderService] 실행
  [log] String OrderRepository.save(String)
  [orderRepository] 실행
  [transaction end] void OrderService.orderItem(String)
  [resource release] void OrderService.orderItem(String)
  ```
* 예외 발생 케이스:

  ```
  [log] void OrderService.orderItem(String)
  [transaction start] void OrderService.orderItem(String)
  [orderService] 실행
  [log] String OrderRepository.save(String)
  [orderRepository] 실행
  [transaction rollback] void OrderService.orderItem(String)
  [resource release] void OrderService.orderItem(String)
  ```

→ 이처럼 AOP 설정에 따라 트랜잭션 시작, 커밋/롤백, 자원 해제 로그가 메서드 실행 흐름에 따라 출력된다.


### Pointcut 표현식 문법 요약

**기본 구조**
`execution([접근제한자] [반환타입] [패키지+클래스명].[메서드명]([파라미터]))`

예시 설명:

* `public *`: 접근자 및 반환 타입
* `hello.aop..*Service.*(..)`: `hello.aop` 하위의 `Service`로 끝나는 클래스의 모든 메서드
* `(..)`: 파라미터 무관
* `(String, ..)`: 첫 번째 인자가 `String`, 이후는 무관


### 디렉토리 패턴 예시

| 패턴                          | 설명                                        |
| --------------------------- | ----------------------------------------- |
| `*..*`                      | 루트부터 모든 패키지·클래스 이름 일치                     |
| `com..*`                    | `com` 하위 모든 서브패키지·클래스                     |
| `com.*.*`                   | `com` 아래 정확히 두 단계 구조                      |
| `com.*..*`                  | 첫 단계는 정확히 하나, 이후 무제한                      |
| `*.service..*`              | 루트 어딘가에 `service` 패키지 포함                  |
| `*..service.*`              | `service` 패키지 아래 클래스들                     |
| `org.example..*Controller`  | `Controller`로 끝나는 클래스                     |
| `*..*Repository`            | 클래스명이 `Repository`로 끝남                    |
| `hello.aop.*Service`        | `hello.aop` 바로 아래 `Service`로 끝나는 클래스      |
| `hello.aop..*Service.*(..)` | `hello.aop` 하위 구조 내 모든 `Service` 클래스의 메서드 |

이러한 포인트컷 표현식을 통해 세밀하고 효율적인 AOP 적용이 가능하다.

---
## [Spring AOP implementation] (cont)
### Pointcut 클래스 분리 및 AOP 순서 제어

#### Pointcut 클래스 분리

* `src > ... > aop` 디렉토리에 `Pointcuts` 클래스 생성
* 공통으로 사용할 포인트컷 정의



#### AspectV4Pointcut 클래스 구성

* `src > ... > order > aop` 디렉토리에 `AspectV4Pointcut` 클래스 생성
* `Pointcuts` 클래스에서 정의한 포인트컷을 경로 기반으로 참조
* 각 advice의 내용은 이전 `AspectV3`와 동일


#### advice 순서 보장 - AspectV5 클래스 구성

* `src > ... > order > aop` 디렉토리에 `AspectV5Order` 클래스 생성
* 내부에 static class로 `LogAspect`, `TxAspect` 정의
* `@Aspect`, `@Order` 어노테이션으로 순서 명시

   * `@Order(1)`이 먼저 실행됨
* 테스트 시 `@Import({AspectV5Order.TxAspect.class, AspectV5Order.LogAspect.class})` 로 명시


#### 주의사항

* 내부 클래스로 선언한 Aspect들을 모두 `@Import` 해야 동작
* 예: `@Import({AspectV5Order.TxAspect.class, AspectV5Order.LogAspect.class})`
* 하나만 import하면 나머지 Aspect는 스프링 컨테이너에 등록되지 않아 실행되지 않음

#### 테스트 로그 예시

```
[transaction start] ...
[log] ...
[orderService] 실행
[log] ...
[orderRepository] 실행
[transaction end] ...
[resource release] ...
```

* `@Order` 설정을 통해 트랜잭션 관련 로직이 로그보다 먼저 실행됨을 확인 가능.

---
## [Spring AOP implementation] (cont)
### 다양한 Advice 유형 적용 - AspectV6Advice

#### Around Advice

* `@Around`는 메서드 실행 전·후를 모두 제어할 수 있는 가장 강력한 advice
* 메서드 실행 여부 결정, 반환값 조작, 예외 변환 등 가능
* `ProceedingJoinPoint` 사용 → `proceed()`를 호출해야 다음 advice 또는 실제 메서드 실행됨


#### Before Advice

* `@Before`는 메서드 실행 전에 호출됨
* 메서드 흐름에는 영향을 주지 않음
* `JoinPoint` 사용


#### AfterReturning Advice

* `@AfterReturning`은 정상적으로 메서드 실행 완료 후 호출됨
* 결과값을 읽을 수 있음 (`returning` 속성 필요)


#### AfterThrowing Advice

* `@AfterThrowing`은 메서드가 예외를 던질 경우 실행됨
* 예외 객체를 받아 로그 출력 가능 (`throwing` 속성 필요)



#### After Advice

* `@After`는 메서드가 정상 종료되거나 예외 발생 여부와 관계없이 항상 실행됨



#### 요약

* `Around`는 흐름을 제어할 수 있는 유일한 advice로 반드시 `proceed()` 호출 필요
* 나머지 4개 advice(`Before`, `AfterReturning`, `AfterThrowing`, `After`)는 흐름 제어 X
* 모두 `JoinPoint`를 통해 실행 메서드의 시그니처 정보 등을 로그 출력 가능
* `@AfterReturning`과 `@AfterThrowing`은 결과 및 예외 객체 전달을 위해 `returning`, `throwing` 속성 필요

#### 테스트 로그 예시

```
[transaction start] ...
[before] ...
[orderService] 실행
[orderRepository] 실행
[return] ... return = null
[after] ...
[transaction end] ...
[resource release] ...
```

또는 예외 상황:

```
[transaction start] ...
[before] ...
[orderService] 실행
[orderRepository] 실행
[ex] ... message = exception occurs!!
[after] ...
[transaction rollback] ...
[resource release] ...
```
---
# 250624
## [Spring AOP implementation - Pointcut]
### 커스텀 어노테이션 기반 Pointcut 테스트 - @Target, @Retention

### Target 지정의 의미

`@Target`은 어노테이션이 적용될 위치를 지정한다

* `ElementType.TYPE`: 클래스, 인터페이스, enum에 적용
* `ElementType.METHOD`: 메서드에 적용

`@Retention(RetentionPolicy.RUNTIME)`은 런타임에도 어노테이션 정보를 유지하여 리플렉션 및 AOP에 활용 가능하게 한다

### 어노테이션 정의

#### ClassAop

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassAop {
}
```

#### MethodAop

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodAop {
    String value();
}
```

### MemberService 인터페이스 및 구현

#### MemberService (Interface)


#### MemberServiceImpl (Class)

* `@ClassAop`: 클래스 단에 선언하여 클래스 전체를 타겟팅
* `@MethodAop("test")`: 메서드 단에 선언하여 메서드 개별 포인트컷 처리 가능

### ExecutionTest 구성

#### 목적

AOP 포인트컷 표현식 테스트를 위한 리플렉션 기반 메서드 추출 및 정보 확인



### 출력 예시

```
helloMethod = public java.lang.String hello.aop.member.MemberServiceImpl.hello(java.lang.String)
```

### 요약

* `@Target`은 어노테이션의 사용 위치를 결정
* `@Retention(RUNTIME)`을 통해 AOP 처리에 활용 가능
* `MemberServiceImpl`에 커스텀 어노테이션을 적용하고 리플렉션으로 메서드 정보를 추출해 AOP 테스트를 준비함
---
# 250625
## [Spring AOP implementation - Pointcut] (cont)
### Execution Pointcut 테스트 - 다양한 표현식 실습

이 테스트 클래스는 `AspectJExpressionPointcut`을 활용해 다양한 `execution` 표현식이 실제 메서드에 어떻게 매칭되는지를 검증한다. 대상 메서드는 `MemberServiceImpl` 클래스의 `hello(String)`이며, 이를 기준으로 메서드 시그니처, 이름, 패키지, 타입, 파라미터 등 여러 기준으로 Pointcut의 동작을 확인한다.

### 메서드 시그니처 정확 매칭

* `execution(public String hello.aop.member.MemberServiceImpl.hello(String))`
  → 전체 시그니처를 정확히 입력하면 일치함
* 가장 기본적인 정확 매칭 방식이며, 실제로는 잘 사용되지 않는다

### 이름(name) 매칭

* `execution(* hello(..))`
  → 메서드 이름이 정확히 "hello"일 경우 매칭
* `execution(* hel*(..))`, `execution(* *el*(..))`
  → 와일드카드를 이용한 이름 일부 일치 테스트
* `execution(* nono(..))`
  → 존재하지 않는 이름으로 false 반환 확인

### 패키지(package) 매칭

* `execution(* hello.aop.member.MemberServiceImpl.hello(..))`
  → 클래스 이름까지 명확하게 지정해 정확한 매칭
* `execution(* hello.aop.member.*.*(..))`
  → member 패키지 내 모든 클래스의 모든 메서드
* `execution(* hello.aop.*.*(..))`
  → member 하위가 아니므로 false
* `execution(* hello.aop.member..*.*(..))`, `execution(* hello.aop..*.*(..))`
  → 하위 패키지까지 포함하는 점검

### 타입(type) 매칭

* `execution(* hello.aop.member.MemberServiceImpl.*(..))`
  → 구체 클래스 기준 매칭
* `execution(* hello.aop.member.MemberService.*(..))`
  → 부모 인터페이스 기준 매칭도 가능

### 구현체에만 있는 메서드 매칭 여부

* `MemberServiceImpl.internal(String)` 메서드로 테스트
* 구현체 기준으로는 매칭되지만, 인터페이스 기준 표현식에서는 매칭되지 않음
  → 인터페이스에는 존재하지 않기 때문

### 파라미터(args) 매칭

* `execution(* *(String))`
  → String 하나만 있을 경우 정확히 일치
* `execution(* *())`
  → 파라미터가 없으면 false
* `execution(* *(*))`, `execution(* *(..))`, `execution(* *(String, ..))`
  → 와일드카드 또는 가변인자 형태의 표현식으로 유연하게 매칭 가능

### 요약

* execution 표현식은 메서드의 다양한 정보를 기반으로 정밀한 매칭이 가능
* `*`, `..` 같은 와일드카드와 패턴 매칭을 통해 유연한 적용이 가능
* 타입 및 파라미터 매칭에서는 상속 구조와 가변 인자에 대한 이해가 중요
* 인터페이스 기준의 pointcut은 구현체의 내부 메서드에 적용되지 않음을 주의해야 함
---
## [Spring AOP implementation - Pointcut] (cont)
### Within Pointcut 테스트 - 클래스 범위 기반 포인트컷

이 테스트 클래스는 `AspectJExpressionPointcut`의 `within` 지시자를 활용하여 클래스의 범위를 기준으로 포인트컷을 설정하는 방식에 대해 확인한다. 테스트 대상 메서드는 `MemberServiceImpl` 클래스의 `hello(String)`이며, 클래스 이름이나 패키지 경로를 기준으로 매칭 결과를 검증한다.

### within 지시자의 특징

* `within`은 **클래스의 선언 위치**를 기준으로 포인트컷을 매칭한다.
* 구현 클래스 자체에 선언된 메서드만을 대상으로 하며, **인터페이스나 상위 타입**을 기준으로 매칭하지 않는다.
* 리플렉션 기준으로 해당 클래스에 물리적으로 존재하는지 여부를 판단한다.

### 정확한 클래스 이름 매칭

* `within(hello.aop.member.MemberServiceImpl)`
  → 구현 클래스 이름을 정확하게 입력하면 true 반환
  → 해당 클래스에 선언된 메서드이기 때문에 매칭 성공

### 와일드카드를 이용한 클래스 이름 매칭

* `within(hello.aop.member.*Service*)`
  → 클래스명이 Service를 포함하면 매칭됨
  → `MemberServiceImpl`이 이름에 해당하므로 true

### 하위 패키지까지 포함하는 매칭

* `within(hello.aop..*)`
  → `hello.aop` 및 하위 패키지까지 포함하여 범위를 넓힘
  → 구현 클래스가 해당 경로에 포함되므로 매칭 성공

### 인터페이스 기반 매칭 실패

* `within(hello.aop.member.MemberService)`
  → 인터페이스는 실제 구현 클래스의 선언 위치가 아니므로 false
  → **주의사항**: `within`은 인터페이스 기반으로 매칭되지 않음

### execution 표현식과의 비교

* `execution(* hello.aop.member.MemberService.*(..))`
  → execution은 시그니처 기준으로 판단하기 때문에 인터페이스의 메서드도 매칭 가능
  → 같은 메서드라도 `execution`을 사용하면 true 반환됨

### 요약

* `within`은 클래스 정의 위치 기준으로 정확히 매칭하기 때문에 **구현 클래스명** 기준으로 작성해야 함
* `execution`은 메서드 시그니처 기준으로 **인터페이스, 부모 타입까지 포함**해 매칭됨
* 인터페이스 기반 AOP 적용이 필요한 경우에는 `execution`을 사용해야 하며, `within`은 주로 **특정 클래스 단위의 타깃 제어가 필요할 때** 활용된다
* 하위 패키지를 포함한 경로 제어도 `within`을 통해 유연하게 가능하다

---
## [Spring AOP implementation - Pointcut] (cont)
### Args Pointcut 테스트 - 파라미터 타입 기반 매칭

이 테스트 클래스는 `args` 포인트컷 표현식이 메서드의 파라미터 타입을 기준으로 어떻게 동작하는지를 확인한다. 또한 `args`와 `execution` 표현식의 차이점도 함께 비교해 본다. 테스트 대상은 `MemberServiceImpl` 클래스의 `hello(String)` 메서드이다.

### args 지시자의 특징

* `args`는 **런타임에 전달되는 인수 객체의 실제 타입**을 기반으로 포인트컷을 매칭한다.
* 따라서 부모 타입, 인터페이스 타입도 모두 허용된다.
* 매우 유연한 포인트컷으로, 동적 프록시에도 사용 가능하다.

### args 표현식 테스트

* `args(String)`
  → 메서드가 `String` 타입 파라미터를 가지므로 true
* `args(Object)`, `args(java.io.Serializable)`
  → `String`은 이들의 하위 타입이므로 모두 true
* `args()`
  → 파라미터가 없다는 의미로, 해당 메서드는 `String`을 받기 때문에 false
* `args(..)`, `args(*)`, `args(String, ..)`
  → 가변 파라미터 또는 와일드카드 표현으로 유연하게 매칭되어 모두 true

### execution과의 비교

* `execution(* *(String))`
  → 메서드 시그니처에 `String` 명시 → 정확히 일치하므로 true
* `execution(* *(Object))`, `execution(* *(java.io.Serializable))`
  → 메서드 선언부가 `String`이기 때문에 `Object`나 `Serializable`로는 매칭되지 않음 → false

### args vs execution 차이점

* `args`는 **런타임 객체 타입** 기준으로 판단한다. 따라서 부모 타입까지 허용되어 유연하다.
* `execution`은 **정적 선언 시점의 시그니처**를 기준으로 한다. 메서드 선언부의 정확한 타입만 매칭된다.
* `args`는 프록시 객체에도 잘 동작하지만, `execution`은 CGLIB 기반 프록시 등에서 제한될 수 있다.

### 요약

* `args`는 런타임 인수 타입 기준의 매우 유연한 포인트컷이며, 부모 타입, 인터페이스 타입 매칭이 가능하다.
* `execution`은 메서드의 선언된 정적 타입 기준이기 때문에 명확하고 정밀하지만 유연성은 떨어진다.
* 상황에 따라 두 표현식을 적절히 선택하여 사용해야 하며, 프록시 방식에 따라 호환성 차이가 있을 수 있다.
