package outboxmessagerelay;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// 샤딩이 되어 있는 상황으로 가정하여 샤드가 분산되어서 이벤트 전송을 수행하는 걸 볼 수 있도록 하기위한 상수 클래스
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageRelayConstants {

    public static final int SHARD_COUNT = 4; // 임의의 값
}
