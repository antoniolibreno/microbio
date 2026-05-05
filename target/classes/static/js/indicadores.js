(() => {
    'use strict';

    const $  = (sel, root = document) => root.querySelector(sel);
    const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

    function toast(message, type = 'info') {
        const root = $('#toastRoot');
        if (!root) { console.log(`[toast:${type}]`, message); return; }

        const el = document.createElement('div');
        el.className = `toast toast--${type}`;
        el.setAttribute('role', type === 'error' ? 'alert' : 'status');
        el.innerHTML = `
      <span class="material-symbols-outlined">${
            type === 'success' ? 'check_circle'
                : type === 'error' ? 'error'
                    : 'info'
        }</span>
      <span>${message}</span>
    `;
        root.appendChild(el);

        setTimeout(() => {
            el.classList.add('is-leaving');
            el.addEventListener('animationend', () => el.remove(), { once: true });
        }, 2600);
    }

    function withLoading(btn, fn) {
        if (!btn) return Promise.resolve(fn());
        btn.classList.add('is-loading');
        btn.disabled = true;
        return Promise.resolve()
            .then(fn)
            .finally(() => {
                btn.classList.remove('is-loading');
                btn.disabled = false;
            });
    }


    function bindAutoSubmitFilters() {
        $$('select[data-autosubmit]').forEach(select => {
            select.addEventListener('change', () => {
                const form = select.closest('form');
                if (form) form.submit();
            });
        });
    }

    function clearFilters() {
        const form = $('#filtersForm');
        if (!form) return;
        form.querySelectorAll('select').forEach(s => (s.selectedIndex = 0));
        toast('Filtros limpos', 'info');
        setTimeout(() => form.submit(), 200);
    }

    function openMoreFilters() {
        // TODO integrar: abrir <dialog> ou rota /indicadores/filtros
        toast('Mais filtros (em breve)', 'info');
    }

    function setView(view) {
        $$('.view-toggle__btn').forEach(b => {
            const active = b.dataset.view === view;
            b.classList.toggle('is-active', active);
            b.setAttribute('aria-selected', active ? 'true' : 'false');
        });

        toast(`Visualização: ${view === 'kanban' ? 'Kanban' : 'Lista'}`, 'info');
    }

    function goToNovaSolicitacao(ev, btn) {
        ev.preventDefault();
        withLoading(btn, () => {
            // TODO integrar: window.location.href = btn.getAttribute('href');
            console.log('[ação] nova-solicitacao');
            toast('Abrindo nova solicitação...', 'info');
        });
    }

    function goToNovaAnalise(ev, btn) {
        ev.preventDefault();
        withLoading(btn, () => {
            console.log('[ação] nova-analise');
            toast('Abrindo nova análise...', 'info');
        });
    }

    function abrirSolicitacao(card) {
        const id = card.dataset.id || '(sem id)';
        // TODO integrar: window.location.href = `/solicitacoes/${id}`;
        console.log('[ação] abrir-solicitacao', id);
        toast(`Abrindo solicitação ${id}`, 'info');
    }

    function verLaudo(ev, link) {
        ev.preventDefault();
        ev.stopPropagation();
        const card = link.closest('.card');
        const id = card?.dataset.id || '(sem id)';
        // TODO integrar: window.open(`/solicitacoes/${id}/laudo`, '_blank');
        console.log('[ação] ver-laudo', id);
        toast(`Abrindo laudo ${id}`, 'success');
    }

    function abrirNotificacoes(btn) {
        // TODO integrar: dropdown / fetch('/api/notificacoes')
        console.log('[ação] notificacoes');
        toast('Você não tem notificações novas', 'info');
    }

    function abrirPerfil(btn) {
        // TODO integrar: menu dropdown / window.location.href = '/perfil'
        console.log('[ação] perfil');
        toast('Abrindo perfil', 'info');
    }

    function bindActions() {
        document.addEventListener('click', (ev) => {
            const target = ev.target.closest('[data-action]');
            if (!target) return;

            const action = target.dataset.action;
            switch (action) {
                case 'nova-solicitacao': return goToNovaSolicitacao(ev, target);
                case 'nova-analise':     return goToNovaAnalise(ev, target);
                case 'mais-filtros':     return openMoreFilters();
                case 'limpar-filtros':   return clearFilters();
                case 'notificacoes':     return abrirNotificacoes(target);
                case 'perfil':           return abrirPerfil(target);
                case 'ver-laudo':        return verLaudo(ev, target);
                case 'abrir-solicitacao':

                    if (ev.target.closest('a, button')) return;
                    return abrirSolicitacao(target);
            }
        });

        document.addEventListener('keydown', (ev) => {
            if (ev.key !== 'Enter' && ev.key !== ' ') return;
            const card = ev.target.closest('.card[data-action="abrir-solicitacao"]');
            if (!card) return;
            ev.preventDefault();
            abrirSolicitacao(card);
        });
    }

    function bindViewToggle() {
        $$('.view-toggle__btn').forEach(btn => {
            btn.addEventListener('click', () => setView(btn.dataset.view));
        });
    }

    function init() {
        bindAutoSubmitFilters();
        bindViewToggle();
        bindActions();
        console.info('[indicadores] UI pronta');
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
