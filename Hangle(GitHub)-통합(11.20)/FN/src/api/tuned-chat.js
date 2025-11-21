// ============================================================
// 고급 챗봇 (벡터스토어 + 히스토리 연동)
// - 백엔드: /api/v1/chat (ChatController)
// - 응답: { sessionId, reply, links[{title, method, path, url}] }
// - UI: index.html 의 고급 챗봇 패널 요소 사용
// ============================================================
(function () {
	//-------------------------------
	// 화면 요소
	//-------------------------------
	const launcher = document.getElementById('chatLauncher');
	const panel = document.getElementById('chatPanel');
	const closeBtn = document.getElementById('chatClose');
	const quickSelect = document.getElementById('chatQuickSelect');
	const suggestions = document.getElementById('chatSuggestions');
	const bodyEl = panel ? panel.querySelector('.chat-body') : null;
	const inputEl = panel ? panel.querySelector('.chat-input') : null;
	const sendBtn = panel ? panel.querySelector('.chat-send') : null;

	// 상태
	const state = {
		sessionId: null,
		loadingIndicator: null
	};

	//-------------------------------
	// 유틸
	//-------------------------------
	function appendMessage(role, text) {
		const el = document.createElement('div');
		el.className = `chat-message ${role}`;
		if (role === 'bot') {
			const badge = document.createElement('span');
			badge.className = 'bot-badge';
			badge.textContent = 'AI 상담';
			el.appendChild(badge);
		}
		el.appendChild(document.createTextNode(text));
		bodyEl.appendChild(el);
		bodyEl.scrollTop = bodyEl.scrollHeight;
		return el;
	}

	function appendMultiline(text) {
		const el = appendMessage('bot', text);
		el.innerHTML = el.innerHTML.replaceAll('\n', '<br>');
	}

	function appendLinks(links) {
		if (!Array.isArray(links) || !links.length) return;
		const container = document.createElement('div');
		container.className = 'chat-message bot bot-link';
		const badge = document.createElement('span');
		badge.className = 'bot-badge';
		badge.textContent = 'AI 상담';
		container.appendChild(badge);
		links.slice(0, 8).forEach(link => {
			const btn = document.createElement('button');
			btn.className = 'chat-suggestion';
			btn.type = 'button';
			btn.textContent = link.title || (link.method ? `${link.method} ${link.path}` : (link.path || '열기'));
			if (link.url) {
				btn.addEventListener('click', () => window.open(link.url, '_blank', 'noopener'));
			} else {
				btn.disabled = true;
			}
			container.appendChild(btn);
		});
		bodyEl.appendChild(container);
		bodyEl.scrollTop = bodyEl.scrollHeight;
	}

	function setLoading(isLoading) {
		if (isLoading) {
			sendBtn.disabled = true;
			inputEl.disabled = true;
			state.loadingIndicator = appendMessage('bot', '답변 생성 중...');
			state.loadingIndicator.classList.add('loading');
		} else {
			sendBtn.disabled = false;
			inputEl.disabled = false;
			if (state.loadingIndicator && state.loadingIndicator.parentNode) {
				state.loadingIndicator.parentNode.removeChild(state.loadingIndicator);
				state.loadingIndicator = null;
			}
			inputEl.focus();
		}
	}

	function togglePanel() {
		panel.classList.toggle('visible');
		if (panel.classList.contains('visible')) {
			setTimeout(() => inputEl && inputEl.focus(), 200);
		}
	}

	function onEscClose() {
		document.addEventListener('keydown', (e) => {
			if (e.key === 'Escape' && panel.classList.contains('visible')) {
				panel.classList.remove('visible');
			}
		});
	}

	function onEnterSend() {
		inputEl.addEventListener('keydown', (e) => {
			if (e.key === 'Enter' && !e.shiftKey) {
				e.preventDefault();
				send();
			}
		});
	}

	function getErrorMessage(error) {
		let msg = '죄송합니다. ';
		if (error && error.name === 'AbortError') {
			msg += '응답 시간이 초과되었습니다. 다시 시도해주세요.';
		} else if (error && String(error.message || '').includes('status: 500')) {
			msg += 'AI 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
		} else if (error && String(error.message || '').includes('status: 404')) {
			msg += '챗봇 API를 찾을 수 없습니다. 관리자에게 문의하세요.';
		} else {
			msg += '현재 상담이 지연되고 있습니다. 잠시 후 다시 시도해주세요.';
		}
		return msg;
	}

	async function fetchWithTimeout(url, options, timeout = 35000) {
		const controller = new AbortController();
		const id = setTimeout(() => controller.abort(), timeout);
		try {
			const res = await fetch(url, { ...options, signal: controller.signal });
			clearTimeout(id);
			return res;
		} catch (e) {
			clearTimeout(id);
			throw e;
		}
	}

	//-------------------------------
	// 전송 로직
	//-------------------------------
	async function send() {
		const text = (inputEl.value || '').trim();
		if (!text || sendBtn.disabled) return;

		appendMessage('user', text);
		inputEl.value = '';
		setLoading(true);
		try {
			const res = await fetchWithTimeout('/api/v1/chat', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					sessionId: state.sessionId,
					message: text
				})
			}, 35000);
			if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
			const data = await res.json();
			if (data.sessionId && !state.sessionId) state.sessionId = data.sessionId;
			if (data.reply) appendMultiline(data.reply);
			if (data.links) appendLinks(data.links);
		} catch (err) {
			console.error('고급 챗봇 API 호출 오류:', err);
			appendMessage('bot', getErrorMessage(err));
		} finally {
			setLoading(false);
		}
	}

	//-------------------------------
	// 초기 Quick Select/Suggestions (선택)
	//-------------------------------
	function renderQuickSelect() {
		if (!quickSelect) return;
		quickSelect.innerHTML = '';
		const mk = (label, value) => {
			const b = document.createElement('button');
			b.className = 'chat-suggestion';
			b.type = 'button';
			b.textContent = label;
			b.addEventListener('click', () => {
				inputEl.value = value;
				send();
			});
			return b;
		};
		quickSelect.appendChild(mk('사용 가능한 모든 API', '사용 가능한 모든 API'));
		quickSelect.appendChild(mk('POST 기능만', 'POST 기능만'));
		quickSelect.appendChild(mk('회원 관리 API', '회원 관리 API'));
	}

	function renderSuggestions() {
		if (!suggestions) return;
		suggestions.innerHTML = '';
	}

	//-------------------------------
	// 바인딩
	//-------------------------------
	function bind() {
		if (!launcher || !panel || !closeBtn || !bodyEl || !inputEl || !sendBtn) {
			return;
		}
		launcher.addEventListener('click', togglePanel);
		closeBtn.addEventListener('click', togglePanel);
		sendBtn.addEventListener('click', send);
		onEnterSend();
		onEscClose();
		renderQuickSelect();
		renderSuggestions();
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', bind);
	} else {
		bind();
	}
})();



