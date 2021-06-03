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

Spring Sleuth는 HTTP request가 들어오는 시점과 HTTP request가 다른 서비스로 나가는 부분을 랩핑하여 Trace와 Span Context를 전달하고, 자바 애플리케이션에서는 Thread Local 변수를 통해서 정보를 유지 및 전달한다.

위의 그림과 같이 x-b3로 시작하는 헤더들과 x-span-name 등을 이용하여 컨택스트를 전달한다.

HTTP로 들어오는 요청의 경우에는 Servlet filter를 이용하여, Trace Id와 Span Id를 받고 (만약에 이 서비스가 맨 처음 호출되는 서비스라서 Trace Id와 Span Id가 없을 경우에는 이를 생성한다.)

RestTemplate을 통해 다른 서비스로 호출을 할 경우에는 RestTemplate 을 랩핑하여, Trace Id와 Span Id와 같은 Context 정보를 실어서 보낸다.

이렇게 ServletFilter와 RestTemplate을 Spring 프레임웍단에서 랩핑해줌으로써, 개발자는 별도의 트레이스 코드를 넣을 필요 없이 Spring을 이용한다면 분산 트랜잭션을 추적할 수 있도록 해준다.

## [Brave](https://github.com/openzipkin/brave)

**B3-Propagation 의 java 구현체로 트레이스 정보를 관리할 수 있음**

Spring Cloud Slueth 2.0.0 버전부터는 Brave 라이브러리를 사용한다.

대부분의 경우 Slueth가 제공하는 Brave의 Tracer 또는 SpanCustomizer Bean만 사용하면 된다.

Brave는 분산 작업에 대한 정보를 캡쳐하고 Zipkin에게 보낼때 사용하는 라이브러리이다.

대부분의 경우 Brave를 직접 사용하지 않는다.

- brave.tracer: Zipkin에게 분산 데이터를 보내는 역할을 한다.

## Slf4j + Logback

Slf4j MDC는 자동 설정된다.

기본값으로는 logging.pattern.level를 %5p,

`[${spring.zipkin.service.name:${spring.application.name:-}},%X{X-B3-TraceId:-},%X{X-B3-SpanId:-},%X{X-Span-Export:-}]`으로 설정한다. (이는 logback 사용자를 위한 스프링 부트가 제공하는 기능이다.)

Slf4j를 사용하지 않는다면 위 패턴은 자동으로 적용되지 않는다.

## [Zipkin](https://zipkin.io/)

- 트위터에서 개발된 오픈소스
- 추적 가능한 분산 트랜잭션: HTTP, gRPC ..

분산 환경에서 로그 트레이싱을 하는 오픈소스로 trace를 수집하고 저장, 조회를 모두 관리한다.

수집한 trace를 분석하여 **예상대로 수행되지않는 구성 요소를 결정하고 수정할 수 있다.**

Zipkin 라이브러리는 수집된 트랜잭션 정보를 zipkin 서버의 collector 모듈로 전송한다. 이 때 다양한 프로토콜을 사용할 수 있는데, 일반적으로 HTTP를 사용하고, 시스템의 규모가 클 경우에는 Kafka 큐를 넣어서 Kafka 프로토콜로 전송이 가능함

수집된 정보는 대쉬 보드를 이용하여 시각화가 가능하다. Zipkin 서버의 대쉬보드([http://localhost:9411/zipkin/](http://localhost:9411/zipkin/))를 사용할 수 있고, Elastic Search 백앤드를 이용한 경우에는 Kibana를 이용하여 시각화가 가능하다.

- **Zipkin 구성요소**

  **1. Zipkin Client Library** : 서비스에서 트레이스 정보를 수집하여 Zipkin Server의 Collector 모듈로 전송하며, 지원하는 언어는 Java, Javascript, Go, C# 등이 있다. Collector로 전송할 때는 다양한 프로토콜을 사용할 수 있지만 일반적으로 HTTP를 사용하고, 시스템이 클 경우 Kafka 큐를 통해서도 전송을 한다.

  **2. Collector** : Zipkin Client Library로부터 전달된 트레이스 정보 유효성을 검증하고 검색 가능하게 저장 및 색인화 한다.

  **3. Storage** : Zkipkin Collector로 보내진 트레이스 정보는 Storage에 저장된다. Zipkin은 초창기에는 Cassandra에 데이터를 저장하도록 만들어졌지만(Cassandra가 확장 가능하고 유연한 스키마를 가지고 있기 때문에 Twitter 내에서 많이 사용되었음), 그 뒤로 ElasticSearch나 MySQL도 지원 가능하게 구성되었다. 그 외에 In-Memory도 지원 가능하기 때문에 간단히 로컬에서 테스트할 때는 In-Memory, 소규모는 MySQL, 운영환경에 적용은 Cassandra나 ElasticSearch를 저장소로 사용하는 것이 좋다.

  **4. API(Zipkin Query Service)** : 저장되고 색인화된 트레이스 정보를 검색하기 위한 JSON API이며, 주로 Web UI에서 호출된다.

  **5. Web UI** : 수집된 트레이스 정보를 확인할 수 있는 GUI로 만들어진 대쉬보드이며, 서비스 / 시간 / 어노테이션 기반으로 데이터 확인이 가능하다. Zipkin 서버의 대쉬보드를 사용할 수도 있고, ElasticSearch 백앤드를 이용한 경우는 Kibana 활용도 가능하다.