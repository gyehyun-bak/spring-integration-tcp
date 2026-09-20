## Spring Integration 기반 통신 패턴별 TCP 전문 통신 구현

### 프로젝트 구성

1. `simple-echo` - 최소 코드로 구현한 TCP ECHO 클라이언트-서버
2. `fixed-length` - BeanIO 기반 커스텀 고정길이(Fixed-Length) EUC-KR 전문 직렬화/역직렬화(marshal/unmarshal)
3. `masking` - Jackson `ValueSerializer`를 활용한 로그 마스킹 구현