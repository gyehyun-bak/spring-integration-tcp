## Spring Integration 기반 통신 패턴별 TCP 전문 통신 구현

### 프로젝트 구성

1. `simple-echo` - 최소 코드로 구현한 TCP ECHO 클라이언트-서버
2. `fixed-length` - BeanIO 기반 커스텀 고정길이(Fixed-Length) EUC-KR 전문 직렬화/역직렬화(marshal/unmarshal)
3. `masking` - Jackson `ValueSerializer`를 활용한 로그 마스킹 구현
4. `async` - 비동기 통신 패턴(한 소켓에서 동시에 다수의 요청-응답. Correlation 패턴)
5. `two-way` - 요청 포트와 응답 포트를 별개로
6. `ack` - 전문 수신에 대한 수신확인응답(ACK) 반환