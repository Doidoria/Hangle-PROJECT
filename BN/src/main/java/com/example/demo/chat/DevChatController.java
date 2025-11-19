package com.example.demo.chat;

/*
============================================================
ChatController.java 파일 전체 용도
============================================================
- LLM(스프링 AI)과 OpenAPI 문서 인덱스를 활용해 API 상담을 제공하는 REST 컨트롤러.
- 입력(사용자 질문)을 받아 Swagger 문서 요약 컨텍스트를 포함한 프롬프트를 구성하고, LLM 호출 후 결과를 검증/정제하여 반환.
- 응답 내 언급된 경로를 추출해 Swagger UI 등으로 연결 가능한 링크를 제공합니다.
============================================================
각 구성요소 및 주요 메서드 개요
============================================================
- 생성자: 의존성(ChatModel/세션/벡터검색)과 기본 baseUrl 초기화
- chat(): 상담 메인 플로우(문서확인→컨텍스트→LLM호출→검증→링크)
- buildSwaggerContext(): 인덱싱된 문서들을 제한 길이 내에서 짧은 텍스트로 요약
- removeDuplicateLines(): LLM 응답에서 중복 라인을 제거(번호 프리픽스 무시)
- validateResponseRelevance(): 질문 키워드와 응답의 API 일치 여부를 검증
- extractKeywords(): 질문에서 도메인 키워드 추출
- extractApiPaths(): 응답 텍스트에서 /api/... 경로 추출
- buildLinks(): 언급된 문서만 링크로 가공(없으면 Swagger UI 링크)
============================================================
각 라인별 상세 주석은 각 함수 블록 상단/내부에 포함
============================================================
*/

import com.example.demo.chat.support.VectorSearchService;
import com.example.demo.chat.support.VectorSearchService.Document;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat/dev")
public class DevChatController {

    private static final Logger log = LoggerFactory.getLogger(DevChatController.class);
    private static final int MAX_CONTEXT_CHARS = 3000; // 컨텍스트 최대 길이 제한(LLM 프롬프트 크기 관리)
    private static final List<String> DOMAIN_KEYWORDS = List.of( // 질문 키워드 추출용 도메인 사전
            "상품", "제품", "product", "item", "goods",
            "주문", "order", "구매", "purchase",
            "회원", "사용자", "유저", "user", "member",
            "파일", "file", "업로드", "upload", "다운로드", "download",
            "인증", "auth", "로그인", "login", "로그아웃", "logout",
            "장바구니", "cart", "basket",
            "결제", "payment", "pay",
            "배송", "delivery", "shipping"
    );
    private static final java.util.regex.Pattern API_CODE_PATTERN = java.util.regex.Pattern.compile("`(/api/[^`\\s]+)`"); // 백틱 포함 경로 패턴
    private static final java.util.regex.Pattern API_PLAIN_PATTERN = java.util.regex.Pattern.compile("(?:^|\\s)(/api/[^\\s,.:;)]+)"); // 일반 경로 패턴

    private final ChatModel chatModel; // 스프링 AI LLM 모델
    private final ChatSessionService sessionService; // 세션/히스토리 서비스
    private final VectorSearchService vectorSearchService; // OpenAPI 인덱스 검색 서비스
    private final String apiBaseUrl; // Swagger UI 등 링크 기반 URL

    /*
    ============================================================
    생성자 용도
    ============================================================
    - LLM 모델/세션/벡터검색 의존성 주입, 기본 API base URL 정규화
    ============================================================
    라인별 주석
    - chatModel/sessionService/vectorSearchService: 컨트롤러 핵심 의존성
    - apiBaseUrl: 말미 슬래시 제거하여 링크 생성 시 중복 "//" 방지
    ============================================================
    */
    public DevChatController(ChatModel chatModel, ChatSessionService sessionService,
                             VectorSearchService vectorSearchService,
                             @Value("${chatbot.api-base-url:http://localhost:8080}") String apiBaseUrl) {
        this.chatModel = chatModel; // 모델 보관
        this.sessionService = sessionService; // 세션 서비스 보관
        this.vectorSearchService = vectorSearchService; // 검색 서비스 보관
        this.apiBaseUrl = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl; // 말미 슬래시 정리
    }

    /*
    ============================================================
    chat() 메서드 용도
    ============================================================
    - 상담 메인 엔드포인트: 질문 수신 → 문서 확인/컨텍스트 생성 → LLM 호출 → 결과 정제/검증 → 링크 생성 → 응답
    ============================================================
    라인별 주석(핵심)
    - sessionId 보장 및 사용자 메시지 저장
    - 문서 없으면 폴백 메시지 반환
    - buildSwaggerContext로 제한 길이 컨텍스트 생성
    - System/User Prompt 구성 후 chatModel.call
    - removeDuplicateLines로 응답 정제
    - validateResponseRelevance로 키워드-API 일치 확인(불일치 시 폴백)
    - extractApiPaths로 경로 추출 → buildLinks로 링크 만들기(없으면 Swagger UI 링크)
    ============================================================
    */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        var sessionId = sessionService.ensureSessionId(request.sessionId()); // 세션 ID 확보
        log.info("=== 세션 [{}] 질문 수신: {} ===", sessionId, request.message()); // 수신 로그

        sessionService.appendUserMessage(sessionId, request.message()); // 히스토리에 사용자 메시지 추가

        var allDocs = vectorSearchService.getAllDocuments(); // 인덱싱된 문서 전량 조회
        log.info("세션 [{}] Swagger 문서 {}개 로드됨", sessionId, allDocs.size()); // 문서 개수 로그
        if (allDocs.isEmpty()) { // 문서 없으면 즉시 폴백
            var noApiMessage = "죄송합니다. 현재 등록된 API 문서가 없습니다.";
            sessionService.appendAssistantMessage(sessionId, noApiMessage);
            return ResponseEntity.ok(new ChatResponse(sessionId, noApiMessage, List.of()));
        }

        var swaggerContext = buildSwaggerContext(allDocs); // 컨텍스트 생성
        log.info("세션 [{}] Swagger 컨텍스트 생성 완료 ({}자)", sessionId, swaggerContext.length());

        var systemMessage = new SystemMessage("""
            당신은 'Hangle API 도우미 챗봇'입니다.
            
            역할:
            - 사용자의 질문이 자연어로 되어 있어도 의도를 분석하여 적절한 API를 찾아 설명합니다.
            - Swagger 문서(Available APIs)에 존재하는 엔드포인트만 사용합니다.
            - Swagger 문서에 없는 API는 절대 생성하거나 추측하지 않습니다(환각 금지).
            - 질문이 모호해도 의미를 파악하여 가장 관련된 API를 제시합니다.
            - 최대 8개의 관련 API만 출력합니다.
            - 답변은 반드시 한국어로 작성합니다.
            
            작업 방식:
            1. 사용자의 질문을 자연어로 해석하여 핵심 목적/기능/동사를 추출합니다.
               예: "대회 만들려면?" → "대회 생성 API"
                   "비밀번호 어떻게 바꿔?" → "비밀번호 변경 API"
            
            2. 추출한 키워드를 Swagger 엔드포인트(summary/description)와 비교하여 가장 관련성 높은 API를 찾습니다.
            
            3. 관련 API가 없으면 아래 문장을 정확히 출력합니다.
               "죄송합니다. 해당 내용을 Swagger 문서에서 찾을 수 없습니다."
            
            4. API를 나열할 때는 다음 형식을 사용합니다:
               METHOD /path - Summary
               (가능하면 1줄 설명도 추가)
            
            5. 중복된 API는 절대 출력하지 않습니다.
            6. Swagger 문서에 존재하지 않는 경로/메서드는 절대 언급하지 않습니다.
            7. 응답은 깔끔하고 간결하게 구성합니다.
            
            출력 형식 예시:
            GET /api/user/me - 내 정보 조회
            Explanation: 로그인된 사용자의 정보를 반환합니다.
            
            DELETE /api/inquiry/{id} - 문의 삭제
            Explanation: 본인이 작성한 문의만 삭제할 수 있습니다.
            """);

        var userPrompt = String.format("""
            아래는 현재 시스템에서 사용 가능한 모든 Swagger 기반 API 목록입니다.
            
            [Available APIs]
            %s
            
            
            [사용자 질문]
            "%s"
            
            
            당신의 작업 지침:
            
            1. 사용자의 질문을 자연어로 분석하여 핵심 의도(예: 로그인, 조회, 생성, 수정, 삭제 등)를 파악하세요.
            2. 의도와 가장 관련된 Swagger API들을 찾아 나열하세요.
            3. Swagger 문서에 존재하지 않는 API는 절대 언급하지 마세요.
            4. API는 다음 형식으로만 출력하세요:
            
               METHOD /path - Summary
               Explanation: (가능할 경우 한 줄 설명)
            
            5. 최대 8개의 API까지만 보여 주세요.
            6. 중복 API는 절대 출력하지 마세요.
            7. 의미가 불분명한 질문이라도 가능한 범위에서 가장 적절한 API를 추천하세요.
            8. 하지만 정말 관련 API가 없을 경우 다음 문장을 정확히 출력하세요:
               "죄송합니다. 해당 내용을 Swagger 문서에서 찾을 수 없습니다."
            
            9. 답변은 반드시 한국어로 작성하세요.
            """, swaggerContext, request.message());

        try {
            var prompt = new Prompt(List.of(systemMessage, new UserMessage(userPrompt))); // 프롬프트 구성
            log.info("세션 [{}] LLM 호출 시작", sessionId);
            var result = chatModel.call(prompt); // LLM 호출
            var reply = result.getResult().getOutput().getText(); // 답변 텍스트 추출
            log.info("세션 [{}] LLM 응답 완료", sessionId);

            reply = removeDuplicateLines(reply); // 응답 중복 제거
            log.debug("세션 [{}] 중복 제거 후 응답: {}", sessionId, reply);

            if (!validateResponseRelevance(request.message(), reply, allDocs)) { // 응답-질문 불일치 시 폴백
                log.warn("세션 [{}] 응답이 질문과 관련 없음 - 폴백 메시지 반환", sessionId);
                var fallbackMessage = "죄송합니다. 해당 내용을 Swagger 문서에서 찾을 수 없습니다.";
                sessionService.appendAssistantMessage(sessionId, fallbackMessage);
                var links = List.of(new ApiLink("Swagger 문서 전체 보기", null, null,
                        apiBaseUrl + "/swagger-ui/index.html"));
                return ResponseEntity.ok(new ChatResponse(sessionId, fallbackMessage, links));
            }

            sessionService.appendAssistantMessage(sessionId, reply); // 최종 응답 저장

            var mentionedPaths = extractApiPaths(reply); // 응답에서 /api/... 경로 추출
            log.info("세션 [{}] 응답에서 추출된 API 경로: {}", sessionId, mentionedPaths);

            var relevantDocs = allDocs.stream()
                    .filter(doc -> mentionedPaths.contains(doc.path())) // 언급된 경로만 필터
                    .limit(8) // 최대 8개
                    .toList();

            var links = buildLinks(relevantDocs); // 링크 구성
            if (links.isEmpty()) { // 경로 언급 없으면 Swagger UI 링크만 제공
                links = List.of(new ApiLink("Swagger 문서 전체 보기", null, null,
                        apiBaseUrl + "/swagger-ui/index.html"));
            }

            log.info("세션 [{}] 생성된 링크 개수: {}", sessionId, links.size());
            return ResponseEntity.ok(new ChatResponse(sessionId, reply, links)); // 최종 응답

        } catch (Exception ex) {
            log.error("세션 [{}] LLM 호출 중 오류 발생", sessionId, ex); // 예외 로그
            var fallback = "죄송합니다. 현재 상담이 지연되고 있습니다. 잠시 후 다시 시도해주세요.";
            sessionService.appendAssistantMessage(sessionId, fallback);
            return ResponseEntity.ok(new ChatResponse(sessionId, fallback, List.of())); // 폴백 응답
        }
    }

    /*
    ============================================================
    buildSwaggerContext() 용도
    ============================================================
    - 인덱싱된 문서 리스트를 최대 글자수 내에서 "[METHOD] /path - summary" 형태로 이어붙여 컨텍스트 생성
    ============================================================
    */
    private String buildSwaggerContext(List<Document> documents) {
        var builder = new StringBuilder(); // 누적 버퍼
        int charCount = 0; // 현재 누적 길이
        for (var doc : documents) {
            if (charCount >= MAX_CONTEXT_CHARS) break; // 길이 한도 초과 시 종료
            var line = String.format("[%s] %s", doc.httpMethod(), doc.path()); // 기본 포맷
            if (doc.summary() != null && !doc.summary().isBlank()) line += " - " + doc.summary(); // 요약 추가
            line += "\n"; // 줄바꿈
            if (charCount + line.length() > MAX_CONTEXT_CHARS) break; // 다음 추가 시 한도 초과면 종료
            builder.append(line); // 추가
            charCount += line.length(); // 길이 갱신
        }
        return builder.toString().trim(); // 트림 후 반환
    }

    /*
    ============================================================
    removeDuplicateLines() 용도
    ============================================================
    - LLM 응답 문자열에서 번호 프리픽스(예: "1. ")를 제거한 후 중복 라인을 제거
    ============================================================
    */
    private String removeDuplicateLines(String text) {
        if (text == null || text.isBlank()) return text; // 빈 값 처리
        var lines = text.split("\n"); // 줄 단위 분리
        var uniqueLines = new java.util.LinkedHashSet<String>(); // 순서 유지 + 중복 제거
        var normalizedToOriginal = new java.util.HashMap<String, String>(); // 정규화→원문 매핑
        for (var line : lines) {
            if (line.trim().isEmpty()) continue; // 빈 줄 스킵
            var normalized = line.replaceAll("^\\d+\\.\\s*", "").trim(); // 번호 프리픽스 제거
            if (!normalizedToOriginal.containsKey(normalized)) { // 신규 라인만 추가
                normalizedToOriginal.put(normalized, line);
                uniqueLines.add(line);
            }
        }
        return String.join("\n", uniqueLines); // 재조합
    }

    /*
    ============================================================
    validateResponseRelevance() 용도
    ============================================================
    - 질문 키워드와 LLM 응답이 언급한 API 문서가 의미적으로 일치하는지 검증(불일치 시 폴백 유도)
    ============================================================
    */
    private boolean validateResponseRelevance(String question, String reply, List<Document> allDocs) {
        if (reply.contains("찾을 수 없습니다")) return true; // 폴백 메시지는 통과
        var mentionedPaths = extractApiPaths(reply); // 응답에서 경로 추출
        if (mentionedPaths.isEmpty()) return true; // 경로 언급 없으면 일반 응답으로 통과
        var questionKeywords = extractKeywords(question); // 질문 키워드 추출
        if (questionKeywords.isEmpty()) return true; // 키워드 없음 → 통과
        for (var path : mentionedPaths) { // 언급 경로들 순회
            for (var doc : allDocs) {
                if (doc.path().equals(path)) { // 동일 경로 문서 찾기
                    var apiText = (doc.summary() + " " + doc.description() + " " + doc.path()).toLowerCase(); // 비교 텍스트
                    for (var keyword : questionKeywords) {
                        if (apiText.contains(keyword.toLowerCase())) return true; // 하나라도 매칭되면 관련 있음
                    }
                }
            }
        }
        log.warn("질문 키워드 [{}]가 응답 API [{}]와 일치하지 않음", questionKeywords, mentionedPaths); // 불일치 로그
        return false; // 관련 없음
    }

    /*
    ============================================================
    extractKeywords() 용도
    ============================================================
    - 사전 정의된 도메인 키워드 목록을 기준으로 질문 문자열에 포함된 키워드만 선별
    ============================================================
    */
    private List<String> extractKeywords(String question) {
        var keywords = new java.util.ArrayList<String>(); // 결과 리스트
        var lower = question == null ? "" : question.toLowerCase(); // 소문자 변환
        for (var keyword : DOMAIN_KEYWORDS) { // 도메인 사전 순회
            if (lower.contains(keyword.toLowerCase())) keywords.add(keyword); // 포함되면 추가
        }
        return keywords; // 반환
    }

    /*
    ============================================================
    extractApiPaths() 용도
    ============================================================
    - 응답 텍스트에서 `/api/...` 경로를 정규식으로 추출(백틱 감싸진 경우/일반 텍스트 모두 처리)
    ============================================================
    */
    private java.util.Set<String> extractApiPaths(String text) {
        if (text == null || text.isBlank()) return java.util.Set.of(); // 빈 입력 처리
        var paths = new java.util.LinkedHashSet<String>(); // 중복 제거 위해 Set 사용
        var m1 = API_CODE_PATTERN.matcher(text); // 백틱 포함 패턴
        while (m1.find()) paths.add(m1.group(1)); // 그룹 캡처 추가
        var m2 = API_PLAIN_PATTERN.matcher(text); // 일반 패턴
        while (m2.find()) paths.add(m2.group(1)); // 그룹 캡처 추가
        return paths; // 경로 집합 반환
    }

    /*
    ============================================================
    buildLinks() 용도
    ============================================================
    - 관련 문서 리스트를 UI에서 사용 가능한 링크(title/method/path/url)로 변환.
    - 요약이 없으면 "METHOD /path" 형식으로 제목 대체, 링크는 baseUrl + path.
    ============================================================
    */
    private List<ApiLink> buildLinks(List<Document> documents) {
        if (documents == null || documents.isEmpty()) return List.of(); // 빈 입력 처리
        var unique = new java.util.LinkedHashMap<String, ApiLink>(); // 중복(title/경로) 방지 위해 맵 사용
        for (var doc : documents) {
            if (doc == null || doc.path() == null || doc.path().isBlank()) continue; // 무효 문서 스킵
            var path = doc.path();
            var method = doc.httpMethod();
            var summary = doc.summary();
            var title = (summary != null && !summary.isBlank()) ? summary : ((method != null ? method + " " : "") + path); // 제목 결정
            var url = apiBaseUrl + path; // 실제 이동 URL
            unique.computeIfAbsent(path + "|" + method, k -> new ApiLink(title, method, path, url)); // 키 중복 방지
        }
        return List.copyOf(unique.values()); // 링크 리스트 반환
    }

    // 요청/응답 레코드: 컨트롤러의 입출력 최소 표현
    public record ChatRequest(String sessionId, @NotBlank String message) {}
    public record ChatResponse(String sessionId, String reply, List<ApiLink> links) {}
    public record ApiLink(String title, String method, String path, String url) {}
}

