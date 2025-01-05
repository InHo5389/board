package board.comment.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 *  페이지 번호 활성화에 필요한 카운트 계산
 * util성이라 private 생성자와 final로 지정
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PageLimitCalculator {

    public static Long calculatorPageLimit(Long page, Long pageSize, Long movablePageCount) {
        return (((page-1) / movablePageCount) + 1) * pageSize * movablePageCount + 1;
    }
}
