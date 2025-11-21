package com.example.demo.chat;

/*
============================================================
ChatSessionService.java 파일 전체 용도
============================================================
- 세션별 대화 히스토리를 메모리에 보관/조회/갱신/만료하는 서비스.
- 단순히 최근 N개의 메시지를 유지하여 LLM 호출 시 컨텍스트로 사용.
============================================================
구성요소 및 흐름 개요
============================================================
- conversations: 세션ID → 대화(메시지 목록, 마지막 접근 시각)
- ensureSessionId(): 유효한 세션ID 보장
- getHistory(): 세션 히스토리 조회 및 마지막 접근 시각 갱신
- appendUserMessage()/appendAssistantMessage(): 메시지 추가(내부 appendMessage 사용)
- evictExpiredSessions(): TTL 기준 만료 세션 제거
============================================================
주의사항
============================================================
- 서버 재시작 시 히스토리는 사라짐(인메모리).
- 동시성: ConcurrentHashMap 사용, 메시지 리스트는 단순 ArrayList로 최소 사용 시나리오 가정.
============================================================
*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatSessionService {

    private static final Logger log = LoggerFactory.getLogger(ChatSessionService.class);

    // 세션ID → 대화(메시지 목록, 마지막 접근 시각) 저장소
    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();

    // 세션 TTL(만료 기준)
    private final Duration ttl = Duration.ofHours(3);

    // 히스토리 최대 보관 개수 (최근 N개만 유지)
    // 히스토리 축소 (6 → 2) - LLM 처리 속도 향상 목적
    private static final int MAX_HISTORY = 2;

    /*
    ============================================================
    함수용도: 세션ID가 없거나 공백이면 신규 생성하여 반환
    입력: sessionId(문자열 또는 null)
    출력: 유효한 세션ID(문자열)
    비고: 클라이언트가 세션을 유지하지 않을 때 신규 세션 발급
    ============================================================
    */
    public String ensureSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return sessionId;
    }

    /*
    ============================================================
    함수용도: 세션 히스토리 조회 및 마지막 접근 시각 갱신
    입력: sessionId
    출력: 메시지 목록(복사본)
    비고: 세션이 없으면 생성; 최대 개수 제한은 append 단계에서 관리
    ============================================================
    */
    public List<Message> getHistory(String sessionId) {
        var conversation = conversations.computeIfAbsent(sessionId, id -> new Conversation(new ArrayList<>(), Instant.now()));
        conversation.touch();
        log.debug("세션 [{}] 히스토리 로드 (메시지 {}건)", sessionId, conversation.messages().size());
        return new ArrayList<>(conversation.messages());
    }

    /*
    ============================================================
    함수용도: 사용자 메시지 추가
    입력: sessionId, content
    출력: 없음
    비고: 내부적으로 appendMessage 사용
    ============================================================
    */
    public void appendUserMessage(String sessionId, String content) {
        appendMessage(sessionId, new UserMessage(content));
        log.info("세션 [{}] 사용자 메시지 저장: {}", sessionId, content);
    }

    /*
    ============================================================
    함수용도: 어시스턴트(봇) 메시지 추가
    입력: sessionId, content
    출력: 없음
    비고: 내부적으로 appendMessage 사용
    ============================================================
    */
    public void appendAssistantMessage(String sessionId, String content) {
        appendMessage(sessionId, new AssistantMessage(content));
        log.info("세션 [{}] 챗봇 응답 저장: {}", sessionId, content);
    }

    /*
    ============================================================
    함수용도: 공통 메시지 추가 및 히스토리 개수 제한 적용
    입력: sessionId, message
    출력: 없음
    비고: MAX_HISTORY 초과 시 가장 오래된 메시지 제거
    ============================================================
    */
    private void appendMessage(String sessionId, Message message) {
        var conversation = conversations.computeIfAbsent(sessionId, id -> new Conversation(new ArrayList<>(), Instant.now()));
        conversation.messages().add(message);
        if (conversation.messages().size() > MAX_HISTORY) {
            conversation.messages().remove(0);
            log.debug("세션 [{}] 히스토리 최대 {}건으로 절단", sessionId, MAX_HISTORY);
        }
        conversation.touch();
    }

    /*
    ============================================================
    함수용도: TTL 기준으로 만료된 세션 제거
    입력: 없음
    출력: 없음
    비고: 주기적(스케줄링) 호출을 권장
    ============================================================
    */
    public void evictExpiredSessions() {
        var now = Instant.now();
        conversations.entrySet().removeIf(entry -> Duration.between(entry.getValue().lastAccessed(), now).compareTo(ttl) > 0);
        log.debug("만료된 세션 정리 완료. 현재 세션 수: {}", conversations.size());
    }

    /*
    ============================================================
    Conversation 내부 클래스 용도
    ============================================================
    - 단일 세션의 메시지 목록과 마지막 접근 시각을 보관.
    - touch() 호출 시점에 마지막 접근 시각을 갱신.
    ============================================================
    */
    private static final class Conversation {
        private final List<Message> messages;
        private Instant lastAccessed;

        // messages, lastAccessed 초기화
        private Conversation(List<Message> messages, Instant lastAccessed) {
            this.messages = messages;
            this.lastAccessed = lastAccessed;
        }

        // 메시지 목록 접근자
        public List<Message> messages() {
            return messages;
        }

        // 마지막 접근 시각 접근자
        public Instant lastAccessed() {
            return lastAccessed;
        }

        // 마지막 접근 시각 갱신
        public void touch() {
            this.lastAccessed = Instant.now();
        }
    }
}


