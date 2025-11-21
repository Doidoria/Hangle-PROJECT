// ============================================================
// API 문서 조회 챗봇 (DOC)
// ============================================================
(function () {
	/** @type {{ openapi:any, paths:any, tags:any, servers:any, components:any } | null} */
	let openApiDoc = null;
	const state = {
		operationsIndex: [],
		loadingIndicator: null
	};
	//-------------------------------
	// 함수용도: CSS 선택자를 받아 단일 DOM 요소를 반환합니다.
	// - 입력: selector(string)
	// - 출력: Element|null (첫 번째 매칭 요소)
	// - 비고: document.querySelector 래퍼로 가독성을 높입니다.
	// 실행되는 순서 : -
	//-------------------------------
	function qs(selector) {
		return document.querySelector(selector);
	}
	//-------------------------------
	// 함수용도: 채팅 메시지 DOM을 역할(role)에 맞게 생성합니다.
	// - 입력: role('user'|'bot'|'bot-loading'|'bot-link'), payload(string|{url,label})
	// - 출력: 생성된 HTMLElement (메시지 컨테이너)
	// - 비고: 봇 로딩/링크/일반 메시지에 필요한 내부 요소를 동적으로 추가합니다.
	// 실행되는 순서 : -
	//-------------------------------
	function createChatMessage(role, payload) {
		const message = document.createElement('div');
		message.classList.add('chat-message');
		if (role === 'bot-loading') {
			message.classList.add('bot', 'loading');
			const badge = document.createElement('span');
			badge.className = 'bot-badge';
			badge.textContent = 'AI 답변 중';
			const spinner = document.createElement('div');
			spinner.className = 'loading-spinner';
			const label = document.createElement('span');
			label.textContent = payload;
			message.appendChild(badge);
			message.appendChild(spinner);
			message.appendChild(label);
		} else if (role === 'bot-link') {
			message.classList.add('bot', 'bot-link');
			const linkButton = document.createElement('button');
			linkButton.className = 'chat-suggestion';
			linkButton.type = 'button';
			const targetUrl = typeof payload === 'string' ? payload : payload?.url;
			const label = typeof payload === 'object' && payload !== null && payload.label ? payload.label : 'Swagger 문서 열기';
			linkButton.textContent = label;
			if (targetUrl) {
				linkButton.addEventListener('click', () => window.open(targetUrl, '_blank', 'noopener'));
			} else {
				linkButton.disabled = true;
			}
			message.appendChild(linkButton);
		} else {
			message.classList.add(role);
			if (role === 'bot') {
				const badge = document.createElement('span');
				badge.className = 'bot-badge';
				badge.textContent = 'AI 상담';
				message.appendChild(badge);
			}
			const textNode = document.createTextNode(payload);
			message.appendChild(textNode);
		}
		return message;
	}

	//-------------------------------
	// 함수용도: 생성한 메시지를 채팅 본문에 추가하고 스크롤을 하단으로 이동합니다.
	// - 입력: chatBody(Element), role(string), payload(any)
	// - 출력: 추가된 메시지(Element)
	// - 비고: 사용/봇 메시지 공통 부착 지점입니다.
	// 실행되는 순서 : -
	//-------------------------------
	function appendChatMessage(chatBody, role, payload) {
		const message = createChatMessage(role, payload);
		chatBody.appendChild(message);
		chatBody.scrollTop = chatBody.scrollHeight;
		return message;
	}

	//-------------------------------
	// 함수용도: 로딩 시작/종료 시 입력/버튼 활성화와 로딩 메시지 표시를 제어합니다.
	// - 입력: isLoading(boolean), sendButton, input, chatBody, st(state)
	// - 출력: 없음
	// - 비고: fetch 전후 UX 피드백을 일관되게 제공합니다.
	// 실행되는 순서 : -
	//-------------------------------
	function setChatLoading(isLoading, sendButton, input, chatBody, st) {
		if (isLoading) {
			sendButton.disabled = true;
			input.disabled = true;
			st.loadingIndicator = appendChatMessage(chatBody, 'bot-loading', '답변 생성 중...');
		} else {
			sendButton.disabled = false;
			input.disabled = false;
			if (st.loadingIndicator) {
				chatBody.removeChild(st.loadingIndicator);
				st.loadingIndicator = null;
			}
			input.focus();
		}
	}

	//-------------------------------
	// 함수용도: DOC 패널의 열림/닫힘을 토글하고 열릴 때 입력 포커스를 부여합니다.
	// - 입력: panel(Element), input(Element)
	// - 출력: 없음
	// - 비고: CSS 클래스 'visible'로 표시 상태를 제어합니다.
	// 실행되는 순서 : -
	//-------------------------------
	function toggleChatPanel(panel, input) {
		panel.classList.toggle('visible');
		if (panel.classList.contains('visible')) {
			setTimeout(() => input.focus(), 200);
		}
	}

	//-------------------------------
	// 함수용도: 네트워크/시간초과/상태코드 오류를 사용자 친화적 문구로 변환합니다.
	// - 입력: error(Error)
	// - 출력: string (표시용 메시지)
	// - 비고: 공통 에러 메시지 정책을 한 곳에서 유지합니다.
	// 실행되는 순서 : -
	//-------------------------------
	function getChatErrorMessage(error) {
		let errorMessage = '죄송합니다. ';
		if (error.name === 'AbortError') {
			errorMessage += '응답 시간이 초과되었습니다. 다시 시도해주세요.';
		} else if ((error.message || '').includes('status: 500')) {
			errorMessage += 'AI 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
		} else if ((error.message || '').includes('status: 404')) {
			errorMessage += '챗봇 API를 찾을 수 없습니다. 관리자에게 문의하세요.';
		} else {
			errorMessage += '현재 상담이 지연되고 있습니다. 잠시 후 다시 시도해주세요.';
		}
		return errorMessage;
	}

	//-------------------------------
	// 함수용도: Enter(Shift 미포함) 키로 전송 콜백을 실행하게 설정합니다.
	// - 입력: input(Element), sendCallback(Function)
	// - 출력: 없음
	// - 비고: 텍스트 영역/인풋에서 자연스러운 전송 UX를 구현합니다.
	// 실행되는 순서 : -
	//-------------------------------
	function setupEnterKeyHandler(input, sendCallback) {
		input.addEventListener('keydown', (event) => {
			if (event.key === 'Enter' && !event.shiftKey) {
				event.preventDefault();
				sendCallback();
			}
		});
	}

	//-------------------------------
	// 함수용도: ESC 키로 열린 패널을 닫도록 설정합니다.
	// - 입력: panel(Element)
	// - 출력: 없음
	// - 비고: 페이지 전역 키 이벤트로 동작합니다.
	// 실행되는 순서 : -
	//-------------------------------
	function setupEscapeKeyHandler(panel) {
		document.addEventListener('keydown', (event) => {
			if (event.key === 'Escape' && panel.classList.contains('visible')) {
				panel.classList.remove('visible');
			}
		});
	}

	//-------------------------------
	// 함수용도: fetch에 타임아웃(기본 35초)을 부여하고 중단 신호를 관리합니다.
	// - 입력: url(string), options(RequestInit), timeout(ms)
	// - 출력: Response (성공 시)
	// - 비고: 항상 타이머를 해제하여 누수를 방지합니다.
	// 실행되는 순서 : -
	//-------------------------------
	async function fetchWithTimeout(url, options, timeout = 35000) {
		const controller = new AbortController();
		const timeoutId = setTimeout(() => controller.abort(), timeout);
		try {
			const response = await fetch(url, { ...options, signal: controller.signal });
			clearTimeout(timeoutId);
			return response;
		} catch (error) {
			clearTimeout(timeoutId);
			throw error;
		}
	}

	//-------------------------------
	// 함수용도: 화면 요소를 수집하고 클릭/키 입력 등 이벤트를 바인딩합니다.
	// - 입력: 없음
	// - 출력: 없음
	// - 비고: 초기 1회 실행, 런처/닫기/전송/키보드 단축키 연결
	// 실행되는 순서 : 1
	//-------------------------------
	function bindEvents() {
		const openBtn = qs('#docChatLauncher');
		const closeBtn = qs('#docChatClose');
		const sendBtn = qs('#docChatSend');
		const input = qs('#docChatInput');
		const panel = qs('#docChatPanel');
		if (!openBtn || !closeBtn || !sendBtn || !input || !panel) return;
		openBtn.addEventListener('click', openPanel);
		closeBtn.addEventListener('click', closePanel);
		sendBtn.addEventListener('click', handleQuery);
		setupEnterKeyHandler(input, handleQuery);
		setupEscapeKeyHandler(panel);
	}

	//-------------------------------
	// 함수용도: DOC 패널을 열고 입력 포커스를 부여하며 문서가 없다면 로드합니다.
	// - 입력: 없음
	// - 출력: 없음
	// - 비고: 패널 표시 토글 후 loadOpenApiIfNeeded 호출
	// 실행되는 순서 : 2
	//-------------------------------
	function openPanel() {
		const panel = qs('#docChatPanel');
		const input = qs('#docChatInput');
		toggleChatPanel(panel, input);
		loadOpenApiIfNeeded();
	}

	//-------------------------------
	// 함수용도: DOC 패널을 즉시 닫습니다.
	// - 입력: 없음
	// - 출력: 없음
	// - 비고: 'visible' 클래스를 제거하여 숨깁니다.
	// 실행되는 순서 : -
	//-------------------------------
	function closePanel() {
		const panel = qs('#docChatPanel');
		panel.classList.remove('visible');
	}

	//-------------------------------
	// 함수용도: OpenAPI 문서를 최초 1회 로드하고 요약/추천 UI를 구성합니다.
	// - 입력: 없음
	// - 출력: 없음
	// - 비고: 로딩 표시 → /v3/api-docs 요청 → 인덱스/드롭다운/추천 생성 → 기본 선택
	// 실행되는 순서 : 3
	//-------------------------------
	async function loadOpenApiIfNeeded() {
		if (openApiDoc) return;
		const body = qs('#docChatBody');
		const suggestions = qs('#docChatSuggestions');
		const sendBtn = qs('#docChatSend');
		const input = qs('#docChatInput');

		setChatLoading(true, sendBtn, input, body, state);
		try {
			const res = await fetchWithTimeout('/v3/api-docs', { method: 'GET' }, 20000);
			if (!res.ok) throw new Error('Failed to load OpenAPI docs, status: ' + res.status);
			openApiDoc = await res.json();
			appendChatMessage(body, 'bot', `OpenAPI ${openApiDoc.openapi || ''} 문서를 불러왔습니다. 무엇을 조회할까요?`);
			appendChatMessage(body, 'bot-link', { url: '/swagger-ui/index.html', label: 'Swagger 문서 열기' });
			buildOperationsIndex();
			populateSummaryDropdown();
			renderSuggestions(suggestions);
			selectFirstSummaryIfExists();
		} catch (error) {
			appendChatMessage(body, 'bot', getChatErrorMessage(error));
		} finally {
			setChatLoading(false, sendBtn, input, body, state);
		}
	}

	//-------------------------------
	// 함수용도: 다중행 문자열을 <br>로 표시합니다.
	// - 입력: bodyEl(Element), text(string)
	// - 출력: 없음
	// - 비고: '\n' 문자를 실제 개행으로 렌더링하여 읽기성을 높입니다.
	// 실행되는 순서 : -
	//-------------------------------
	function appendMultiline(bodyEl, text) {
		const el = appendChatMessage(bodyEl, 'bot', text);
		el.innerHTML = el.innerHTML.replaceAll('\n', '<br>');
	}

	//-------------------------------
	// 함수용도: 하단에 Swagger UI 이동 버튼을 추가합니다.
	// - 입력: bodyEl(Element)
	// - 출력: 없음
	// - 비고: 클릭 시 새 탭으로 '/swagger-ui/index.html'을 엽니다.
	// 실행되는 순서 : -
	//-------------------------------
	function appendGoToSwagger(bodyEl) {
		appendChatMessage(bodyEl, 'bot-link', { url: '/swagger-ui/index.html', label: 'Swagger UI로 이동' });
	}

	//-------------------------------
	// 함수용도: 추천 쿼리 버튼(paths/tags/servers 등)을 렌더링합니다.
	// - 입력: container(Element)
	// - 출력: 없음
	// - 비고: 클릭 시 입력창에 값 설정 후 handleQuery 동작
	// 실행되는 순서 : -
	//-------------------------------
	function renderSuggestions(container) {
		if (!container) return;
		container.innerHTML = '';
		const mkBtn = (label, value) => {
			const b = document.createElement('button');
			b.className = 'chat-suggestion';
			b.type = 'button';
			b.textContent = label;
			b.addEventListener('click', () => {
				const input = qs('#docChatInput');
				input.value = value;
				handleQuery();
			});
			return b;
		};
		container.appendChild(mkBtn('paths 목록', 'paths'));
		container.appendChild(mkBtn('tags 목록', 'tags'));
		container.appendChild(mkBtn('servers 목록', 'servers'));
	}

	//-------------------------------
	// 함수용도: OpenAPI paths를 인덱싱(HTTP 메서드만)합니다.
	// - 입력: 없음
	// - 출력: 없음
	// - 비고: get/post/put/delete/patch/head/options/trace 만 대상으로 summary, tags, op 수집
	// 실행되는 순서 : -
	//-------------------------------
	function buildOperationsIndex() {
		const index = [];
		const paths = openApiDoc?.paths || {};
		const httpMethods = new Set(['get','post','put','delete','patch','head','options','trace']);
		for (const [pathKey, pathItem] of Object.entries(paths)) {
			for (const [method, op] of Object.entries(pathItem)) {
				if (!httpMethods.has(method.toLowerCase())) continue;
				index.push({
					path: pathKey,
					method: method.toUpperCase(),
					summary: (op && (op.summary || op.operationId)) || '',
					tags: (op && Array.isArray(op.tags)) ? op.tags.join(', ') : '',
					op: op || {}
				});
			}
		}
		index.sort((a, b) => (a.summary || '').localeCompare(b.summary || ''));
		state.operationsIndex = index;
	}

	//-------------------------------
	// 함수용도: Summary 드롭다운을 채웁니다.
	// - 입력: 없음
	// - 출력: 없음
	// - 비고: 첫 옵션은 placeholder. 각 항목은 "요약 — METHOD /path" 형식으로 표기
	// 실행되는 순서 : -
	//-------------------------------
	function populateSummaryDropdown() {
		const select = qs('#docSummarySelect');
		if (!select) return;
		select.innerHTML = '';
		if (!state.operationsIndex.length) {
			const opt = document.createElement('option');
			opt.value = '';
			opt.textContent = '요약 항목이 없습니다';
			select.appendChild(opt);
			return;
		}
		const placeholder = document.createElement('option');
		placeholder.value = '';
		placeholder.textContent = '요약 항목 선택';
		select.appendChild(placeholder);
		state.operationsIndex.forEach((item, idx) => {
			const label = item.summary || `${item.method} ${item.path}`;
			const opt = document.createElement('option');
			opt.value = String(idx);
			opt.textContent = `${label} — ${item.method} ${item.path}`;
			select.appendChild(opt);
		});
		select.addEventListener('change', onSummaryChange);
	}

	//-------------------------------
	// 함수용도: 드롭다운 변경 시 상세 출력합니다.
	// - 입력: e(Event)
	// - 출력: 없음
	// - 비고: 선택된 인덱스로 operationsIndex에서 아이템을 찾아 상세 렌더링
	// 실행되는 순서 : 4
	//-------------------------------
	function onSummaryChange(e) {
		const body = qs('#docChatBody');
		const value = e.target.value;
		if (!value) return;
		const idx = Number(value);
		const item = state.operationsIndex[idx];
		if (!item) return;
		renderOperation(item, body);
	}

	//-------------------------------
	// 함수용도: 첫 항목 자동 선택합니다.
	// - 입력: 없음
	// - 출력: 없음
	// - 비고: placeholder 다음(인덱스 1)을 선택하고 change 이벤트를 트리거
	// 실행되는 순서 : -
	//-------------------------------
	function selectFirstSummaryIfExists() {
		const select = qs('#docSummarySelect');
		if (!select || select.options.length <= 1) return;
		select.selectedIndex = 1;
		select.dispatchEvent(new Event('change'));
	}

	//-------------------------------
	// 함수용도: Operation 핵심 정보를 출력합니다.
	// - 입력: item(operationsIndex 항목), bodyEl(Element)
	// - 출력: 없음
	// - 비고: 요약/경로/태그/파라미터/요청본문/응답코드 요약 후 개행 처리 및 Swagger 버튼 추가
	// 실행되는 순서 : -
	//-------------------------------
	function renderOperation(item, bodyEl) {
		const op = item.op || {};
		const lines = [];
		lines.push(`요약: ${item.summary || '-'}`);
		lines.push(`경로: ${item.method} ${item.path}`);
		if (item.tags) lines.push(`태그: ${item.tags}`);
		if (op.parameters && op.parameters.length) {
			const params = op.parameters.map(p => `${p.name}${p.required ? ' (필수)' : ''} [${p.in}]`).join(', ');
			lines.push(`파라미터: ${params}`);
		}
		if (op.requestBody) lines.push('요청 본문: 있음');
		if (op.responses) {
			const codes = Object.keys(op.responses).slice(0, 6).join(', ');
			lines.push(`응답 코드: ${codes}${Object.keys(op.responses).length > 6 ? ' …' : ''}`);
		}
		appendMultiline(bodyEl, lines.join('\n'));
		appendGoToSwagger(bodyEl);
	}

	//-------------------------------
	// 함수용도: 특정 경로의 메서드 요약을 출력합니다.
	// - 입력: pathKey(string)
	// - 출력: 없음
	// - 비고: 해당 path의 HTTP 메서드별 summary를 나열합니다.
	// 실행되는 순서 : -
	//-------------------------------
	function respondForPath(pathKey) {
		const body = qs('#docChatBody');
		if (!pathKey) return appendChatMessage(body, 'bot', '조회할 경로를 입력하세요. 예: path /users');
		const pathItem = openApiDoc.paths && openApiDoc.paths[pathKey];
		if (!pathItem) return appendChatMessage(body, 'bot', `경로를 찾을 수 없습니다: ${pathKey}`);
		const methods = Object.keys(pathItem);
		const summaryByMethod = methods
			.filter(m => ['get','post','put','delete','patch','head','options','trace'].includes(m.toLowerCase()))
			.map(m => {
				const op = pathItem[m];
				const sum = (op && (op.summary || op.operationId)) || '';
				return `${m.toUpperCase()} - ${sum}`.trim();
			}).join('\n');
		appendMultiline(body, `경로: ${pathKey}\n${summaryByMethod}`);
		appendGoToSwagger(body);
	}

	//-------------------------------
	// 함수용도: 특정 메서드의 모든 경로를 나열합니다.
	// - 입력: method(string, 예: GET/POST)
	// - 출력: 없음
	// - 비고: 일치하는 모든 경로를 최대 12개까지 미리보기로 출력합니다.
	// 실행되는 순서 : -
	//-------------------------------
	function respondForMethod(method) {
		const body = qs('#docChatBody');
		if (!method) return appendChatMessage(body, 'bot', '메서드를 입력하세요. 예: method GET');
		const matched = [];
		const paths = openApiDoc.paths || {};
		for (const [p, item] of Object.entries(paths)) {
			for (const [m, op] of Object.entries(item)) {
				if (m.toLowerCase() === method.toLowerCase()) {
					const sum = (op && (op.summary || op.operationId)) || '';
					matched.push(`${m.toUpperCase()} ${p} - ${sum}`.trim());
				}
			}
		}
		if (!matched.length) return appendChatMessage(body, 'bot', `${method.toUpperCase()} 메서드를 가진 경로가 없습니다.`);
		const preview = matched.slice(0, 12).join('\n');
		appendMultiline(body, `총 ${matched.length}개 결과\n${preview}${matched.length > 12 ? '\n…' : ''}`);
		appendGoToSwagger(body);
	}

	//-------------------------------
	// 함수용도: 공백/특수문자를 제거한 느슨한 비교를 위해 문자열을 정규화합니다.
	// - 입력: str(any)
	// - 출력: 소문자+공백제거+허용문자만 남긴 string
	// - 비고: "회원 가입" vs "회원가입" 같은 케이스를 동일시합니다.
	// 실행되는 순서 : -
	//-------------------------------
	function normalizeForLooseMatch(str) {
		if (!str) return '';
		return String(str).toLowerCase().replace(/\s+/g, '').replace(/[^0-9a-z가-힣_\\/.-]/g, '');
	}

	//-------------------------------
	// 함수용도: 느슨한 포함(정규화 후 포함) 점수를 계산합니다.
	// - 입력: text, needle
	// - 출력: 0~1 사이 가중 점수
	// - 비고: 길이 대비 매치 길이를 기반으로 단순 가중치 부여
	// 실행되는 순서 : -
	//-------------------------------
	function scoreLooseContains(text, needle) {
		if (!text || !needle) return 0;
		const t = normalizeForLooseMatch(text);
		const n = normalizeForLooseMatch(needle);
		if (!t || !n || !t.includes(n)) return 0;
		return Math.min(1, n.length / Math.max(4, t.length)) * 1.5;
	}

	//-------------------------------
	// 함수용도: 원문 기준 부분 포함 점수를 계산합니다.
	// - 입력: text, needle
	// - 출력: 0~1 사이 점수
	// - 비고: 간단한 포함 여부와 길이 비를 이용합니다.
	// 실행되는 순서 : -
	//-------------------------------
	function scoreContains(text, needle) {
		if (!text || !needle) return 0;
		const t = String(text).toLowerCase();
		const n = String(needle).toLowerCase();
		if (!t.includes(n)) return 0;
		return Math.min(1, n.length / Math.max(4, t.length));
	}

	//-------------------------------
	// 함수용도: 요약/경로/태그/operationId에 대해 유사도 점수를 계산해 상위 후보를 찾습니다.
	// - 입력: query(string)
	// - 출력: [{item, score}] 점수 내림차순 배열
	// - 비고: 느슨/정확 매칭을 혼합 가중하여 실제 검색 의도에 가깝게 정렬합니다.
	// 실행되는 순서 : -
	//-------------------------------
	function searchSimilarOperations(query) {
		if (!state.operationsIndex.length) return [];
		const results = [];
		for (const item of state.operationsIndex) {
			let score = 0;
			score += scoreContains(item.summary, query) * 2.5;
			score += scoreLooseContains(item.summary, query) * 3;
			score += scoreContains(item.path, query) * 1.2;
			score += scoreLooseContains(item.path, query) * 1.8;
			score += scoreContains(item.tags, query) * 0.8;
			score += scoreLooseContains(item.tags, query) * 1.2;
			const opId = item.op?.operationId;
			score += scoreContains(opId, query) * 1.5;
			score += scoreLooseContains(opId, query) * 2;
			if (score > 0) results.push({ item, score });
		}
		results.sort((a, b) => b.score - a.score);
		return results;
	}

	//-------------------------------
	// 함수용도: 사용자의 쿼리를 해석하여 적절한 응답을 출력합니다.
	// - 입력: 없음(내부에서 입력 요소 조회)
	// - 출력: 없음
	// - 비고: 'path /...' / 'method GET' / 'paths|tags|servers' / 유사검색 순서로 처리
	// 실행되는 순서 : -
	//-------------------------------
	async function handleQuery() {
		const input = qs('#docChatInput');
		const body = qs('#docChatBody');
		const sendBtn = qs('#docChatSend');
		const text = (input.value || '').trim();
		if (!text) return;
		appendChatMessage(body, 'user', text);
		input.value = '';

		// 문서 미로딩 시 로딩 유도
		if (!openApiDoc) {
			setChatLoading(true, sendBtn, input, body, state);
			try {
				await loadOpenApiIfNeeded();
			} finally {
				setChatLoading(false, sendBtn, input, body, state);
			}
		}

		// 명령어 파싱
		const lower = text.toLowerCase();
		if (lower.startsWith('path ')) {
			const pathKey = text.slice(5).trim();
			return respondForPath(pathKey);
		}
		if (lower.startsWith('method ')) {
			const method = text.slice(7).trim();
			return respondForMethod(method);
		}
		if (lower === 'paths') {
			const paths = Object.keys(openApiDoc?.paths || {});
			const preview = paths.slice(0, 20).join('\n');
			appendMultiline(body, `총 ${paths.length}개 경로\n${preview}${paths.length > 20 ? '\n…' : ''}`);
			return appendGoToSwagger(body);
		}
		if (lower === 'tags') {
			const tags = Array.isArray(openApiDoc?.tags) ? openApiDoc.tags.map(t => t.name || '').filter(Boolean) : [];
			appendMultiline(body, tags.length ? `태그: ${tags.join(', ')}` : '태그 정보가 없습니다.');
			return appendGoToSwagger(body);
		}
		if (lower === 'servers') {
			const servers = Array.isArray(openApiDoc?.servers) ? openApiDoc.servers.map(s => s.url || '').filter(Boolean) : [];
			appendMultiline(body, servers.length ? `서버: ${servers.join('\n')}` : '서버 정보가 없습니다.');
			return appendGoToSwagger(body);
		}

		// 유사 검색
		const results = searchSimilarOperations(text);
		if (!results.length) {
			appendChatMessage(body, 'bot', '관련 API를 찾지 못했습니다. 예: "path /users", "method GET"');
			return;
		}
		results.slice(0, 3).forEach(r => renderOperation(r.item, body));
	}

	if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', bindEvents);
	else bindEvents();

	/*
	실행 순서(주요 흐름):
	1) bindEvents
	   - 역할: 화면 요소(ID: docChatLauncher, docChatClose, docChatSend, docChatInput, docChatPanel, docChatBody)를 조회하고 이벤트를 바인딩합니다.
	   - 세부: DOC 버튼 클릭 → openPanel, 닫기 버튼 클릭 → closePanel, 전송 버튼 및 Enter 키 → handleQuery, ESC 키 → 패널 닫기.
	   - 시점: DOMContentLoaded 시점에 1회 수행되어 인터랙션을 준비합니다.

	2) openPanel
	   - 역할: DOC 패널을 열고 입력창에 포커스를 부여합니다.
	   - 세부: 패널에 visible 클래스를 토글하고, 최초 1회에 한해 OpenAPI 문서를 로드하도록 loadOpenApiIfNeeded를 호출합니다.
	   - 결과: 사용자가 패널을 열자마자 입력 가능하며, 백그라운드에서 문서가 준비됩니다.

	3) loadOpenApiIfNeeded
	   - 역할: OpenAPI 문서를 서버에서 가져오고 초기 UI를 구성합니다.
	   - 세부: /v3/api-docs를 타임아웃(기본 20초)으로 요청합니다. 성공 시 안내 메시지와 Swagger 바로가기 버튼을 출력하고, 경로/메서드 인덱스를 생성하여 Summary 드롭다운을 채웁니다. 추천 버튼도 함께 렌더링합니다.
	   - 부가: 최초 로딩 동안 로딩 스피너(봇 로딩 메시지)를 표시하고, 완료/실패 시 스피너를 제거합니다. 실패하면 사용자 친화적 에러 메시지를 보여줍니다.
	   - 후속: 첫 번째 Summary 항목이 존재하면 selectFirstSummaryIfExists로 자동 선택하여 기본 정보를 즉시 표시합니다.

	4) onSummaryChange
	   - 역할: 사용자가 Summary 드롭다운에서 항목을 선택했을 때 해당 Operation의 핵심 정보를 채팅 본문에 표시합니다.
	   - 세부: 요약, 경로(HTTP 메서드 포함), 태그, 파라미터(이름/필수/위치), 요청 본문 유무, 대표 응답 코드들을 다중 행으로 출력하고, 하단에 Swagger UI로 이동 버튼을 함께 제공합니다.
	   - 결과: 사용자가 선택만으로도 핵심 스펙을 빠르게 확인하고, 필요 시 Swagger UI로 상세 탐색을 이어갈 수 있습니다.
	*/
})();

