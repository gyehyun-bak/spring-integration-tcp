## Spring Integration 기반 TCP 전문 통신 구현

### 소개

Spring Integration과 몇 가지 오픈소스를 활용하여 TCP 전문 통신에서의 주요 패턴과 트러블슈팅을 구현하고 설명한 블로그 시리즈의 소스 코드입니다.

### 프로젝트 구성

아래는 각 프로젝트별 블로그 포스트 링크입니다.

| 순서 | 패키지명       | 블로그 포스트                                                                                                                                                                                                                                                                                                                                                        |
|:-----|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1    | `simple-echo`  | <ol><li>[Spring Integration 소개와 간단한 TCP Echo Client/Server 구현하기](https://velog.io/@gyehyunbak/spring-integration-tcp-echo)</li><li>[Spring Integration의 MessagingTemplate과 @MessagingGateway, 그리고 Channel Adapter와 Messaging Gateways](https://velog.io/@gyehyunbak/how-to-use-spring-integration-and-how-to-integrate-with-other-systems)</li></ol> |
| 2    | `framing`      | [Spring Integration TCP Connection Factory와 메시지 경계 구분(Framing), 그리고 길이 필드가 정수가 아닌 문자열(ASCII)인 경우 대처하기](https://velog.io/@gyehyunbak/spring-integration-connection-factory-message-framing)                                                                                                                                            |
| 3    | `fixed-length` | (작성 중) BeanIO 기반 고정길이(Fixed-Length) 데이터 포맷 직렬화/역직렬화(Marshalling/Unmarshalling) 구현하기                                                                                                                                                                                                                                                         |
| 4    | `masking`      | (작성 중) 커스텀 Jackson ValueSerializer와 어노테이션으로 전문 로그에서 민감정보 필드 마스킹하기                                                                                                                                                                                                                                                                     |