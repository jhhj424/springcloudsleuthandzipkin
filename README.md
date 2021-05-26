# MSA 환경에서 분산 트랜잭션 추적

MSA 구조에서는 하나의 HTTP 호출이 내부적으로 여러개의 서비스를 거쳐서 일어나게 되고, 트랜잭션이 여러 컴포넌트의 조합을 통해서 발생하기 때문에 전통적인 APM(Application Perfomance Monitoring) 도구를 이용해서 추적이 어렵다.

→ 이런 문제를 해결하기 위해, 별도의 분산 로그 추적 시스템이 필요하다.

## OpenTracing이란

애플리케이션 간에 분산된 호출 흐름을 공개적으로 추적하기 위한 표준

해당 OpenTracing spec을 기준으로 여러 Tracer들(Zipkin, Jaeger)등이 존재함

## 분산 로그 추적 시스템의 작동 원리

통상적으로 Trace 와 Span 이라는 개념을 사용함

- Trace: 클라이언트가 서버로 호출한 하나의 호출
- Span: 서비스 컴포넌트간의 호출

각 서비스 컴포넌트들은 하나의 클라이언트 호출을 추적하기 위해서 같은 Trace Id를 사용하고, 각 서비스간의 호출은 각각 다른 Span Id를 사용함

→ 이를 통해 전체 트랜잭션 시간을 Trace로 추적이 가능하고, 각 서비스별 구간 시간은 Span으로 추적할 수 있음

# Spring Cloud Sleuth & Zipkin

**Zipkin은 [B3-Propagation](https://github.com/openzipkin/b3-propagation)을 통해서 OpenTracing을 구현**

**Brave 는 B3-Propagation의 Java 구현체**

**Spring Cloud Sleuth는 BraveTracer를 Spring 프레임워크에서 쉽게 사용하기 위한 라이브러리**

B3 propagation은 간단히 말해 ‘X-B3-‘으로 시작하는 X-B3-TraceId와 X-B3-ParentSpanId, X-B3-SpanId, X-B3-Sampled, 이 4개 값을 전달하는 것을 통해서 트레이스 정보를 관리합니다. 서버에서는 이 값들을 TraceContext에서 관리하는데, Spring 프레임워크와 SLF4J(Simple Logging Facade for Java)를 사용하면 MDC(Mapped Diagnostic Context)에서 해당 값을 꺼내서 사용할 수 있습니다. HTTP를 통해 다른 서버로 전달하는 경우에는 HTTP 헤더를 통해서 전달하고, Kafka 메시지를 통해 전달하는 경우에는 Kafka 헤더를 통해서 전달합니다.

참고 링크 : https://www.youtube.com/

## [Spring Cloud Sleuth](https://spring.io/projects/spring-cloud-sleuth)

MSA환경에서 클라이언트의 호출은 내부적으로 여러 마이크로서비스를 거쳐서 일어나기 때문에 추적이 어렵다. 때문에 이를 추적하기 위해서는 연관된 ID가 필요한데, 이런 ID를 자동으로 생성해주는 것이 **Spring Cloud Sleuth**이다.

Spring Cloud Sleuth는 Spring에서 공식적으로 지원하는 Zipkin Client Library로 Spring과의 연동이 매우 쉬우며, 호출되는 서비스에 Trace(추적) ID와 Span(구간) ID를 부여한다. Trace ID는 클라이언트 호출의 시작부터 끝날때까지 동일한 ID로 처리되며, Span ID는 마이크로서비스당 1개의 ID가 부여된다. 이 두 ID를 활용하면 클라이언트 호출을 쉽게 추적할 수 있다.

TraceId, SpanId를 생성하면 이러한 정보를 서비스 호출을 위한 헤더나 MDC(Mapped Diagnostic Context)에 추가한다. 이는 Zipkin이나 ELK 같은 도구에서 인덱스나 프로세스 로그 파일을 저장하기 위해서 사용된다. **Spring Cloud 제품군의 CLASSPATH에 추가되면 다음과 같은 공통 커뮤니케이션 채널에 자동으로 통합된다.**

- RestTemplate 등에 의한 request
- Netfix Zuul microproxy를 통과하는 request
- Spring MVC 컨트롤러에서 수신된 HTTP header
- Apache Kafka나 RabbitMQ와 같은 메세징 기술을 통한 request