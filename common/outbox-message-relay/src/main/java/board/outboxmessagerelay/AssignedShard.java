package board.outboxmessagerelay;

import lombok.Getter;

import java.util.List;
import java.util.stream.LongStream;

/**
 * Shard를 각 애플리케이션에 균등하게 할당하기 위한 그런 클래스
 */
@Getter
public class AssignedShard {

    private List<Long> shards;

    // 애플리케이션 아이디, 코디네이터에 의해서 지금 실행되어 있는 애플리케이션 목록, 샤드 개수
    public static AssignedShard of(String appId, List<String> appIds, long shardCount) {
        AssignedShard assignedShard = new AssignedShard();
        assignedShard.shards = assign(appId, appIds, shardCount);
        return assignedShard;
    }

    /**
     * 애플리케이션에 인덱스를 찾기
     * <p>
     * 애플리케이션 아이디가 지금 실행된 애플리케이션 목록을 이제 정렬된 상태로 가지고 있을텐데 (appIds)
     * 기에서 이 애플리케이션 아이디가 몇 번째 있는지 반환
     * <p>
     * 그렇게 되면 여기 들어가 있는 애플리케이션 아이디들은 각각 독립적인 인덱스를 반환해서
     * 인덱스를 통해서 범위를 만들어 줄 수 있음
     */
    private static List<Long> assign(String appId, List<String> appIds, long shardCount) {
        int appIndex = findAppIndex(appId, appIds);
        if (appIndex == -1) {
            return List.of(); // 할당할 샤드가 없는걸 의미하여 빈 리시트 반환
        }

        // Start와 End 사이에 있는 범위가 이 애플리케이션이 할당된 Shard
        long start = appIndex * shardCount / appIds.size();
        long end = (appIndex + 1) * shardCount / appIds.size() - 1;

        return LongStream.rangeClosed(start,end).boxed().toList();
    }

    private static int findAppIndex(String appId, List<String> appIds) {
        for (int i = 0; i < appIds.size(); i++) {
            if (appIds.get(i).equals(appId)) {
                return i;
            }
        }
        return -1;
    }
}
