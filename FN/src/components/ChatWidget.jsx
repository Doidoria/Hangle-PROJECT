import { useEffect, useState, useCallback, useRef } from "react";
import api from "../api/axiosConfig";
import { useAuth } from "../api/AuthContext";
import "../css/ChatWidget.scss";

function createSessionId() {
  if (typeof crypto !== "undefined" && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return "sess-" + Date.now() + "-" + Math.random().toString(16).slice(2);
}

const ChatWidget = () => {
  const { role, userid, isLoading } = useAuth();
  const isDev = role === "ROLE_ADMIN" || role === "ROLE_MANAGER";

  const [isOpen, setIsOpen] = useState(false);
  const [mode, setMode] = useState("user"); // user | dev
  const [sessionId, setSessionId] = useState("");
  const [messages, setMessages] = useState([
    {
      role: "assistant",
      text: "안녕하세요! 😊\n무엇을 도와드릴까요?",
      links: [],
    },
  ]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);

  const messageEndRef = useRef(null);

  const appendMessage = useCallback((msg) => {
    setMessages((prev) => [...prev, msg]);
  }, []);

  // 모드별 세션ID 분리 저장
  useEffect(() => {
    if (isLoading) return;
    const keyPrefix = userid ? `${userid}_` : "guest_";
    const key = mode === "user"
      ? `${keyPrefix}chat_session_user`
      : `${keyPrefix}chat_session_dev`;
    let sid = localStorage.getItem(key);

    if (!sid) {
      sid = createSessionId();
      localStorage.setItem(key, sid);
    }
    setSessionId(sid);
  }, [mode, userid, isLoading]);

  // 모드별 메시지 로드
  useEffect(() => {
    if (isLoading) return;
    const keyPrefix = userid ? `${userid}_` : "guest_";
    const msgKey = mode === "user"
      ? `${keyPrefix}chat_messages_user`
      : `${keyPrefix}chat_messages_dev`;
    const savedMessages = localStorage.getItem(msgKey);

    if (savedMessages) {
      setMessages(JSON.parse(savedMessages));
    } else {
      // 새로운 세션이면 초기 시스템 메시지 세팅
      setMessages([
        {
          role: "assistant",
          text: "안녕하세요! 😊\n무엇을 도와드릴까요?",
          links: [],
        },
      ]);
    }
  }, [mode, userid, isLoading]);

  // 모드별 메시지 저장
  useEffect(() => {
    if (isLoading) return;

    const keyPrefix = userid ? `${userid}_` : "guest_";
    const msgKey =
      mode === "user"
        ? `${keyPrefix}chat_messages_user`
        : `${keyPrefix}chat_messages_dev`;

    localStorage.setItem(msgKey, JSON.stringify(messages));
  }, [messages, mode, userid, isLoading]);

  // 자동 스크롤
  const scrollToBottom = () => {
    messageEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, loading]);

  // 스크롤 항상 아래로
  useEffect(() => {
    if (isOpen) {
      setTimeout(() => {
        scrollToBottom();
      }, 0);
    }
  }, [isOpen]);

  // 추천 질문
  const userRecommendedQuestions = [
    "진행 중인 대회는 어디서 확인해?",
    "대회는 어떻게 참여해?",
    "문의는 어디서 보낼 수 있어?",
    "비밀번호는 어디서 변경해?",
    "회원 탈퇴는 어떻게 해?",
    "이메일 수정은 어디서 해?",
  ];
  const devRecommendedQuestions = [
    "로그인 API 알려줘",
    "Access Token 검증 API 경로는?",
    "회원 정보 수정 API는?",
    "대회 생성 API 경로 알려줘",
    "대회 삭제 API는 어디 있어?",
    "관리자 문의 전체 조회 API는?",
  ];
  const recommendedQuestions =
    mode === "user"
      ? userRecommendedQuestions
      : isDev
        ? devRecommendedQuestions
        : userRecommendedQuestions;

  const handleToggle = () => setIsOpen((prev) => !prev);

  // 실제 전송 함수 (예전 sendMessage 역할)
  const handleSend = async (overrideText) => {
    const raw = overrideText !== undefined ? overrideText : input;
    const trimmed = raw.trim();
    if (!trimmed || loading) return;

    appendMessage({ role: "user", text: trimmed });

    if (overrideText === undefined) {
      setInput("");
    }
    setLoading(true);

    const url = mode === "user" ? "/api/v1/chat/user" : "/api/v1/chat/dev";

    try {
      const resp = await api.post(url, {
        sessionId,
        message: trimmed,
      });

      const newSessionId = resp.data.sessionId || sessionId;
      if (!sessionId && newSessionId) {
        setSessionId(newSessionId);
      }

      appendMessage({
        role: "assistant",
        text: resp.data.reply || "응답을 처리하는 중 문제가 발생했습니다.",
        link: resp.data.link || null,
        // links: resp.data.links || null,
      });
    } catch (error) {
      console.error("챗봇 오류:", error);

      const status = error.response?.status;
      let msg = "오류가 발생했습니다. 잠시 후 다시 시도해주세요.";

      if (status === 401) {
        msg = "로그인이 필요합니다.";
      } else if (status === 403) {
        msg =
          "접근 권한이 없습니다. 관리자 또는 매니저 계정으로 로그인해주세요.";
      }

      appendMessage({
        role: "assistant",
        text: msg,
        link: null,
        // links: null,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleQuestionClick = (q) => {
    handleSend(q);
  };

  const handleExitChat = () => {
    const keyPrefix = userid ? `${userid}_` : "guest_";

    const sessionKey = mode === "user"
      ? `${keyPrefix}chat_session_user`
      : `${keyPrefix}chat_session_dev`;

    const msgKey = mode === "user"
      ? `${keyPrefix}chat_messages_user`
      : `${keyPrefix}chat_messages_dev`;

    // 삭제
    localStorage.removeItem(sessionKey);
    localStorage.removeItem(msgKey);

    // 신규 세션 생성
    const newSid = createSessionId();
    localStorage.setItem(sessionKey, newSid);
    setSessionId(newSid);

    setMessages([
      {
        role: "assistant",
        text: "안녕하세요! 😊\n무엇을 도와드릴까요?",
        links: [],
      },
    ]);

    setInput("");
    setLoading(false);
  };

  if (isLoading || !userid) {
    return <div style={{ display: "none" }} />;
  }

  return (
    <div className="chat-widget-root">
      {/* 플로팅 버튼 */}
      <button type="button" className="chat-widget-toggle"
        onClick={handleToggle} aria-label="챗봇 열기">
        <span className="chat-widget-icon"><img src="/image/icon-ChatBot.png" alt="챗봇-아이콘" /></span>
        {!isOpen && <span className="chat-widget-label">도움이 필요하신가요?</span>}
      </button>

      {isOpen && (
        <div className="chat-widget-panel">
          {/* 헤더 */}
          <div className="chat-widget-header">
            <div>
              <div className="title">Hangle 챗봇</div>
              <div className="subtitle">사용자용 / 개발자용 모드 지원</div>
            </div>
            <button className="exit-btn" onClick={handleExitChat}>초기화</button>
            <button className="close-btn" onClick={handleToggle}>×</button>
          </div>

          {/* 모드 전환 버튼 */}
          <div className="chat-mode-switch">
            <button onClick={() => setMode("user")} className={mode === "user" ? "active" : ""}>
              사용자용
            </button>
            {isDev && (
              <button onClick={() => setMode("dev")} className={mode === "dev" ? "active" : ""}>
                개발자용
              </button>
            )}
          </div>

          {/* 추천 질문 */}
          <div className="chat-recommendations">
            {recommendedQuestions.map((q, i) => (
              <button key={i} className="recommend-chip" onClick={() => handleQuestionClick(q)}>
                {q}
              </button>
            ))}
          </div>

          {/* 메시지 */}
          <div className="chat-widget-messages">
            {messages.map((m, idx) => {
              {/* URL 제거(만약 URL 삽입됬을때 대비) */ }
              const cleanText = m.text
                .replace(/\{.*"link".*\}/, "")     // JSON 제거
                .replace(/\[.*?\]\(.*?\)/g, "")    // 완전한 Markdown 제거
                .replace(/\[[^\]]*?\]/g, (match) => match.replace(/\[|\]/g, ""))
                .replace(/\[.*?\]/g, "")           // 대괄호 텍스트 제거
                .trim();
              return (
                <div key={idx}
                  className={`chat-message ${m.role === "user" ? "user" : "assistant"}`}>
                  <div className="bubble">
                    {/* 줄바꿈 처리 */}
                    {cleanText.split("\n").map((line, i) => (
                      <p key={i}>{line}</p>
                    ))}
                    {/* React 페이지 링크 안내 (사용자 모드만 표시됨) */}
                    {mode === "user" && m.link && (
                      <div className="chat-link-wrap">
                        <a className="chat-link-btn" href={m.link}>
                          바로가기
                        </a>
                      </div>
                    )}

                    {/* 개발자 모드에서만 API 링크 노출 */}
                    {mode === "dev" && m.links?.length > 0 && (
                      <div className="api-links">
                        {m.links.map((link, i) => (
                          <a key={i} className="api-link-chip" target="_blank"
                            rel="noreferrer" href={link.url}>
                            {link.method && <span className="method">{link.method}</span>}
                            <span className="path">{link.title || link.path}</span>
                          </a>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
            {/* 로딩 */}
            {loading && (
              <div className="chat-message assistant">
                <div className="bubble typing">
                  <span className="dot" />
                  <span className="dot" />
                  <span className="dot" />
                </div>
              </div>
            )}
            <div ref={messageEndRef} />
          </div>

          {/* 입력창 */}
          <div className="chat-widget-input">
            <textarea
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder={
                mode === "user"
                  ? "예) 비밀번호는 어디서 바꿔?\n예) 진행중인 대회 어디서 확인해?\n예) 문의는 어떻게 보내?"
                  : "예) 로그인 API 알려줘\n예) 대회 생성 API 경로는?\n예) 사용자 정보 수정 API?"
              } rows={2} />
            <button type="button" className="send-btn"
              disabled={loading || !input.trim()} onClick={handleSend}>전송
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChatWidget;
