/**
 * orcamento.js — Modal de Solicitação de Orçamento
 * Servido pelo Spring Boot em /js/orcamento.js
 * Front e back no mesmo domínio → API_BASE vazio.
 */

// ─── Abertura / Fechamento ───────────────────────────────────────────────────
function abrirModal() {
    const overlay = document.getElementById('modalOrcamento');
    overlay.classList.add('ativo');
    overlay.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
    limparFormulario();
    setTimeout(() => document.getElementById('orcNome').focus(), 100);
}

function fecharModal() {
    const overlay = document.getElementById('modalOrcamento');
    overlay.classList.remove('ativo');
    overlay.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
}

document.addEventListener('DOMContentLoaded', () => {
    const overlay = document.getElementById('modalOrcamento');
    if (!overlay) return;

    // Fechar clicando fora
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) fecharModal();
    });

    // Fechar com ESC
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') fecharModal();
    });

    // Submit
    document.getElementById('formOrcamento').addEventListener('submit', async (e) => {
        e.preventDefault();
        await enviarSolicitacao();
    });
});

// ─── Validação ───────────────────────────────────────────────────────────────
function validarCampos() {
    let valido = true;
    const nome    = document.getElementById('orcNome');
    const email   = document.getElementById('orcEmail');
    const servico = document.getElementById('orcServico');

    ['orcNome','orcEmail','orcServico'].forEach(id =>
        document.getElementById(id).classList.remove('erro'));
    ['errNome','errEmail','errServico'].forEach(id =>
        document.getElementById(id).textContent = '');

    if (!nome.value.trim()) {
        nome.classList.add('erro');
        document.getElementById('errNome').textContent = 'Por favor, informe seu nome.';
        valido = false;
    }
    if (email.value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
        email.classList.add('erro');
        document.getElementById('errEmail').textContent = 'E-mail inválido.';
        valido = false;
    }
    if (!servico.value) {
        servico.classList.add('erro');
        document.getElementById('errServico').textContent = 'Selecione o tipo de serviço.';
        valido = false;
    }
    return valido;
}

// ─── Envio ───────────────────────────────────────────────────────────────────
async function enviarSolicitacao() {
    if (!validarCampos()) return;

    const btn    = document.getElementById('btnEnviar');
    const texto  = document.getElementById('btnTexto');
    const loader = document.getElementById('btnLoader');

    btn.disabled = true;
    texto.style.display  = 'none';
    loader.style.display = 'inline';
    document.getElementById('modalFeedback').style.display = 'none';

    const payload = {
        nome:        document.getElementById('orcNome').value.trim(),
        email:       document.getElementById('orcEmail').value.trim() || null,
        telefone:    document.getElementById('orcTelefone').value.trim() || null,
        tipoServico: document.getElementById('orcServico').value
    };

    try {
        const response = await fetch('/api/solicitacoes', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(payload)
        });
        const data = await response.json();

        if (response.ok && data.sucesso) {
            exibirFeedback('ok', '✅ Solicitação enviada! Entraremos em contato em breve.');
            document.getElementById('formOrcamento')
                .querySelectorAll('input, select, button[type=submit]')
                .forEach(el => el.disabled = true);
        } else {
            exibirFeedback('err', data.mensagem || 'Erro ao enviar. Tente novamente.');
            reativarBtn(btn, texto, loader);
        }
    } catch {
        exibirFeedback('err', '❌ Não foi possível conectar ao servidor. Verifique sua conexão.');
        reativarBtn(btn, texto, loader);
    }
}

function exibirFeedback(tipo, msg) {
    const el = document.getElementById('modalFeedback');
    el.className = `modal-feedback ${tipo}`;
    el.textContent = msg;
    el.style.display = 'flex';
    el.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function reativarBtn(btn, texto, loader) {
    btn.disabled = false;
    texto.style.display  = 'inline';
    loader.style.display = 'none';
}

function limparFormulario() {
    const form = document.getElementById('formOrcamento');
    form.reset();
    ['orcNome','orcEmail','orcServico'].forEach(id =>
        document.getElementById(id).classList.remove('erro'));
    ['errNome','errEmail','errServico'].forEach(id =>
        document.getElementById(id).textContent = '');
    document.getElementById('modalFeedback').style.display = 'none';
    form.querySelectorAll('input, select, button').forEach(el => el.disabled = false);
    const texto  = document.getElementById('btnTexto');
    const loader = document.getElementById('btnLoader');
    if (texto)  texto.style.display  = 'inline';
    if (loader) loader.style.display = 'none';
}