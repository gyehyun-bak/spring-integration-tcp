## @Mask 어노테이션 적용

```java
@Record
@Getter
@Setter
@ToString(callSuper = true)
public class EchoRequestMessage extends HostMessage {

    @Field(ordinal = 4, length = 6, rid = true, literal = "ECHO")
    private TrxCode trxCode = TrxCode.ECHO;

    @Field(ordinal = 6, length = 100)
    @Mask(start = 2, end = 6, maskChar = '*') // '*'가 default지만 API 이해를 위해 표기
    private String message;
}
```

## 테스트 응답

```json
{
  "code": "S00000",
  "message": "hello world",
  "messageUuid": "4d77b569-f5ac-4341-af17-bb0840ea8aa5",
  "sender": "HOST",
  "sentAt": "20260920163122677",
  "trxCode": "ECHO"
}
```

## 요청 전문 로그

```json
{
  "code": null,
  "message": "he****world",
  "messageUuid": "39ccfa2d-7d07-418b-b373-7d77b5143d4b",
  "sender": "CLIENT",
  "sentAt": "20260920163408917",
  "trxCode": "ECHO"
}
```