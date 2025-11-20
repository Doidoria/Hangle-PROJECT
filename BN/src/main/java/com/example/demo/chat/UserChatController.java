package com.example.demo.chat;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

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

    private String detectPageLink(String userMessage) {
        String msg = userMessage.toLowerCase();

        if (msg.contains("대회") || msg.contains("competition") || msg.contains("참여")) {return "/competitions";}
        if (msg.contains("문의") || msg.contains("질문") || msg.contains("help")) {return "/inquiry/write";}
        if (msg.contains("리더보드") || msg.contains("leaderboard") || msg.contains("순위")) {return "/leaderboard";}
        if (msg.contains("설정") || msg.contains("비밀번호") || msg.contains("계정") || msg.contains("탈퇴") || 
                msg.contains("계정 삭제") || msg.contains("이메일 수정")) {return "/setting";}
        if (msg.contains("프로필") || msg.contains("내 정보") || msg.contains("마이페이지")) {return "/myprofile";}

        return null;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody UserChatRequest request, Authentication authentication) {

        String userid = authentication.getName();
        String raw = request.sessionId();
        String sessionId = userid + "_" + (raw == null || raw.isBlank() ? java.util.UUID.randomUUID() : raw);
        String question = request.message();

        // 이전 대화 히스토리 로드
        var history = sessionService.getHistory(sessionId);

        // 사용자용 프롬프트 (기술 용어 금지)
        var systemMessage = new SystemMessage("""
            당신은 'Hangle 서비스 고객지원 챗봇'입니다.
            
            ### 답변 스타일 규칙
            - 답변은 따뜻하고 친절한 말투로 작성합니다.
            - 답변은 한 문장으로 끝내지 말고 2~3문장으로 자연스럽게 구성합니다.
              - 1문장: 사용자가 궁금한 기능을 설명합니다.
              - 2문장: 사용자가 참고할 수 있는 부가 정보를 제공합니다.
              - 3문장: "아래 버튼을 눌러 이용해보세요!"와 같이 버튼 클릭을 유도합니다.
            - 답변은 반드시 두 문장이 끝난 뒤 줄바꿈(빈 줄 1칸)을 넣고, 마지막 3번째 문장을 작성합니다.
            - 답변 텍스트에는 링크나 링크처럼 보이는 표현을 절대 포함하지 않습니다.
            
            ### 링크 관련 금지 규칙 (매우 중요)
            - 어떠한 형태로든 텍스트 내에 링크 구조를 넣지 마십시오.
            - 아래 모든 형태 금지:
              - Markdown 링크: [텍스트](링크)
              - 텍스트만 있는 대괄호: [텍스트]
              - 미완성 Markdown: [텍스트](
              - 괄호로 감싼 텍스트: (텍스트)
              - URL, 도메인, 경로(/something), 숫자로 시작하는 경로 등 링크로 보이는 모든 표현
            - “이곳에서 클릭”, “여기를 누르면”처럼 링크 클릭을 암시하는 표현도 금지합니다.
            - 답변 텍스트에는 링크 대신 자연어만 작성하고, 실제 링크는 오직 응답 JSON의 link 필드를 사용하여 제공합니다.
            - 괄호로 감싼 텍스트를 사용하지 않습니다.
            - 답변 텍스트에서는 괄호( ) 자체를 사용하지 않습니다.
            - 문장 안에서 괄호를 활용한 구조를 만들지 않습니다.
            
            ### 링크 안내 방식
            - 특정 페이지 안내 시 다음 형식으로 마무리합니다:
              - "아래 버튼을 이용하여 이동해보세요 ↓"
              - "아래 바로가기 버튼을 통해 쉽게 이동하실 수 있어요! 😊"
            - 문장 안에서 링크를 암시하거나 링크처럼 보이는 단어를 생성하지 마십시오.
            
            ### 금지사항
            - 기술 용어(API, HTTP, Swagger, JSON 등) 금지
            - 시스템 내부 동작·처리 과정 설명 금지
            - 답변 텍스트 안에 URL, 경로, JSON 구조, 링크 흉내 표현 모두 금지
            
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
        reply = reply.replaceAll("https?://\\S+", "");
        reply = reply.replaceAll("www\\.[a-zA-Z0-9./_-]+", "");
        reply = reply.replaceAll("[a-zA-Z0-9._%+-]+\\.(com|net|io|kr|co)\\S*", "");

        // 혹시라도 기술 단어가 포함되면 제거(이중 보호)
        String[] bannedWords = { "API", "Swagger", "엔드포인트", "HTTP", "JSON", "프론트엔드", "백엔드" };
        for (String word : bannedWords) { reply = reply.replace(word, ""); }

        // 길이 제한 (필요시)
        if (reply.length() > 500) {reply = reply.substring(0, 480) + "... (간단히 안내해드렸습니다)";}

        // 세션 히스토리 저장
        sessionService.appendUserMessage(sessionId, question);
        sessionService.appendAssistantMessage(sessionId, reply);
        String detectedLink = detectPageLink(question);

        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "reply", reply,
                "link", detectedLink
        ));
    }

    public record UserChatRequest(String sessionId, String message) {}
}
