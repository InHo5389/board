package board.outboxmessagerelay;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox,Long> {

    // 이벤트가 아직 전송되지 않은 것들을 주기적으로 폴링해서 보낸다고 했는데 그러한 이벤트들을 조회하기 위한 메소드
    List<Outbox> findAllByShardKeyAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
            Long shardKey,
            LocalDateTime from,
            Pageable pageable
    );
}
