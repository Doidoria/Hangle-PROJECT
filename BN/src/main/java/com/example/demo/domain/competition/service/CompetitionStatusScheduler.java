package com.example.demo.domain.competition.service;

import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.Status;
import com.example.demo.domain.competition.repository.CompetitionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CompetitionStatusScheduler {

    private final CompetitionRepository competitionRepository;

    /**
     * 매 1분마다 대회 상태를 자동으로 갱신한다.
     *
     * 규칙:
     *  - now < startAt            -> UPCOMING
     *  - startAt <= now <= endAt  -> OPEN
     *  - endAt < now              -> CLOSED
     */
    @Scheduled(cron = "0 * * * * *") // 매 분 0초마다 실행
    @Transactional
    public void updateCompetitionStatuses() {
        LocalDateTime now = LocalDateTime.now();

        List<Competition> competitions = competitionRepository.findAll();

        for (Competition competition : competitions) {
            LocalDateTime start = competition.getStartAt();
            LocalDateTime end = competition.getEndAt();

            // 시작/종료 시간이 비어 있는 데이터는 스킵
            if (start == null || end == null) {
                continue;
            }

            Status newStatus;
            if (now.isBefore(start)) {
                newStatus = Status.UPCOMING;
            } else if (now.isAfter(end)) {
                newStatus = Status.CLOSED;
            } else {
                newStatus = Status.OPEN;
            }

            // 상태가 바뀔 때만 업데이트
            if (newStatus != competition.getStatus()) {
                competition.setStatus(newStatus);
                // @Transactional 덕분에 메서드 종료 시점에 DB에 반영됨
            }
        }
    }
}
