package com.example.demo.chat;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat/user")
public class UserChatController {

    private final ChatModel chatModel;
    private final ChatSessionService sessionService;

    public UserChatController(ChatModel chatModel, ChatSessionService sessionService) {
        this.chatModel = chatModel;
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody UserChatRequest request) {

        String sessionId = sessionService.ensureSessionId(request.sessionId());
        String question = request.message();

        // 이전 대화 히스토리 로드
        var history = sessionService.getHistory(sessionId);

        // 사용자용 프롬프트 (기술 용어 금지)
        var systemMessage = new SystemMessage("""
            당신은 'Hangle 서비스 고객지원 챗봇'입니다.

            ### 역할
            - 사용자의 질문 의도를 추론하여 가장 적절한 도움을 제공합니다.
            - 기술 용어(API, HTTP, Swagger 등)는 절대 사용하지 않습니다.
            - React 페이지 기준으로 어떤 메뉴에서 해결 가능한지 안내합니다.
            - 답변은 꼭 3~5줄로 짧고 명확하게 유지합니다.
            - 마지막 줄에는 {"link": "..."} 형식의 JSON을 반드시 포함합니다.
            - 해당되는 페이지가 없으면 {"link": null} 이라고 적습니다.

            ### Hangle React 페이지 링크
            - 홈: /
            - 내 프로필: /myprofile
            - 설정: /setting
            - 비밀번호 변경: /setting
            - 문의 작성: /inquiry/write
            - 내 문의 목록: /myprofile/inquiries
            - 관리자 문의 관리: /admin/inquiries
            - 전체 대회 목록: /competitions
            - 대회 생성: /competitions/new
            - 대회 상세: /competitions/:id
            - 리더보드: /leaderboard

            ### 금지사항
            - 기술 용어(API, HTTP 등) 금지
            - 서버 구조 설명 금지
            - 너무 긴 설명 금지

            항상 한국어로 답변합니다.
        """);

        // 사용자 질문 메시지
        var userMessage = new UserMessage("사용자 질문: \"" + question + "\"");

        // 프롬프트 구성: System + 이전 히스토리 + 현재 질문
        var promptMessages = new java.util.ArrayList<org.springframework.ai.chat.messages.Message>();
        promptMessages.add(systemMessage);
        promptMessages.addAll(history);  // 이전 user/assistant 메시지들
        promptMessages.add(userMessage);

        var response = chatModel.call(new Prompt(promptMessages));
        String reply = response.getResult().getOutput().getText();

        // 과도한 줄바꿈 제거
        reply = reply.replaceAll("\\n{3,}", "\n\n");
        reply = reply.replace("API", "")
                .replace("Swagger", "")
                .replace("HTTP", "")
                .replace("서버", "");

        // 혹시라도 기술 단어가 포함되면 제거(이중 보호)
        String[] bannedWords = {
                "API", "Swagger", "엔드포인트", "HTTP", "JSON", "서버", "프론트엔드", "백엔드"
        };
        for (String word : bannedWords) {
            reply = reply.replace(word, "");
        }

        // 길이 제한 (필요시)
        if (reply.length() > 500) {
            reply = reply.substring(0, 480) + "... (간단히 안내해드렸습니다)";
        }

        // 세션 히스토리 저장
        sessionService.appendUserMessage(sessionId, question);
        sessionService.appendAssistantMessage(sessionId, reply);

        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "reply", reply
        ));
    }

    public record UserChatRequest(String sessionId, String message) {}
}
