/* ── Busca ── */
function filtrar() {
  const t = document.getElementById('searchInput').value.toLowerCase();
  const rows = document.querySelectorAll('#userTable tbody tr.user-row');
  let vis = 0, total = 0;
  rows.forEach(r => {
    total++;
    const show = r.textContent.toLowerCase().includes(t);
    r.style.display = show ? '' : 'none';
    if (show) vis++;
  });
  const label = document.getElementById('countLabel');
  if (label) label.textContent = t ? vis + ' resultado(s)' : total + ' usuário(s)';
}

/* ── Menu de ações (ellipsis) ── */
function fecharMenus() {
  document.querySelectorAll('.row-menu:not([hidden])').forEach(m => {
    m.hidden = true;
    m.classList.remove('open-up');
  });
}

function toggleRowMenu(btn, ev) {
  ev.stopPropagation();
  const menu = btn.parentElement.querySelector('.row-menu');
  const isOpen = !menu.hidden;
  fecharMenus();
  if (isOpen) return;
  menu.hidden = false;
  // abre para cima se não houver espaço abaixo
  const rect = btn.getBoundingClientRect();
  const espacoAbaixo = window.innerHeight - rect.bottom;
  menu.classList.toggle('open-up', espacoAbaixo < menu.offsetHeight + 16);
}

document.addEventListener('click', fecharMenus);
document.addEventListener('keydown', e => {
  if (e.key === 'Escape') { fecharMenus(); fecharModaisAbertos(); }
});

/* ── Abas (modais) ── */
function trocarAba(btn) {
  const alvo = btn.dataset.tab;
  const tabs = btn.closest('.tabs');
  const body = btn.closest('.modal-body');
  tabs.querySelectorAll('.tab-btn').forEach(b => b.classList.toggle('active', b === btn));
  body.querySelectorAll('.tab-panel').forEach(p => p.classList.toggle('active', p.id === alvo));
}

/* ── Modais ── */
function abrirModal(id) {
  fecharMenus();
  const ov = document.getElementById(id);
  ov.classList.add('ativo');
  ov.setAttribute('aria-hidden', 'false');
  document.body.style.overflow = 'hidden';
}

function fecharModal(id) {
  const ov = document.getElementById(id);
  ov.classList.remove('ativo');
  ov.setAttribute('aria-hidden', 'true');
  document.body.style.overflow = '';
}

function fecharModaisAbertos() {
  document.querySelectorAll('.modal-overlay.ativo').forEach(ov => fecharModal(ov.id));
}

function abrirModalNovo() {
  abrirModal('modalNovo');
}

function abrirModalEditar(menuBtn) {
  const row = menuBtn.closest('tr.user-row');
  const d = row.dataset;
  const isAdmin = d.admin === 'true';

  // ações dos forms apontam para o usuário selecionado
  document.getElementById('formEditAcesso').action = '/usuarios/' + d.id + '/atualizar-acesso';
  document.getElementById('formEditCompleto').action = '/usuarios/' + d.id + '/atualizar-completo';

  // popula ambos os forms
  document.querySelectorAll('#modalEditar form').forEach(form => {
    form.reset();
    form.querySelectorAll('[data-edit]').forEach(el => {
      el.value = d[el.dataset.edit] || '';
    });
    const radio = form.querySelector('input[name="admin"][value="' + isAdmin + '"]');
    if (radio) radio.checked = true;
  });

  // volta sempre para a primeira aba
  const primeira = document.querySelector('#modalEditar .tab-btn');
  if (primeira) trocarAba(primeira);

  abrirModal('modalEditar');
}

/* fecha modal ao clicar no overlay (fora da caixa) */
document.querySelectorAll('.modal-overlay').forEach(ov => {
  ov.addEventListener('click', e => { if (e.target === ov) fecharModal(ov.id); });
});
