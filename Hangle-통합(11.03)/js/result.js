// 탭-패널 매핑
const tabs = Array.from(document.querySelectorAll('.nb-tabs .tab'));
const panels = Array.from(document.querySelectorAll('.tab-panel'));

// 패널 표시
function showPanel(key, push = true){
  tabs.forEach(t => {
    const active = t.dataset.tab === key;
    t.classList.toggle('active', active);
    t.setAttribute('aria-selected', active ? 'true' : 'false');
  });
  panels.forEach(p => {
    p.hidden = p.dataset.panel !== key;
  });
  // URL 해시 동기화 (ex: #tab=output)
  if (push) {
    const url = new URL(location.href);
    url.hash = `tab=${key}`;
    history.replaceState(null, '', url);
  }
}

// 탭 클릭
tabs.forEach(t => {
  t.addEventListener('click', () => showPanel(t.dataset.tab));
});

// 해시로 초기 탭 결정
(function initFromHash(){
  const m = location.hash.match(/tab=([a-z]+)/i);
  const key = m ? m[1] : 'notebook';
  const exists = panels.some(p => p.dataset.panel === key);
  showPanel(exists ? key : 'notebook', false);
})();

// 키보드 접근성(좌/우 이동)
document.querySelector('.nb-tabs')?.addEventListener('keydown', e => {
  if(!['ArrowLeft','ArrowRight','Home','End'].includes(e.key)) return;
  e.preventDefault();
  const idx = tabs.findIndex(t => t.classList.contains('active'));
  let next = idx;
  if (e.key === 'ArrowRight') next = (idx + 1) % tabs.length;
  if (e.key === 'ArrowLeft') next = (idx - 1 + tabs.length) % tabs.length;
  if (e.key === 'Home') next = 0;
  if (e.key === 'End') next = tabs.length - 1;
  tabs[next].focus(); showPanel(tabs[next].dataset.tab);
});
