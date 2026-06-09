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
    carregarAnalises();
    setTimeout(() => document.getElementById('orcNome').focus(), 100);
}

let analisesCarregadas = false;
async function carregarAnalises() {
    const select = document.getElementById('orcServico');
    if (!select) return;
    if (analisesCarregadas) return;

    try {
        const response = await fetch('/api/analises/ativas');
        if (!response.ok) throw new Error('Falha ao carregar análises');
        const analises = await response.json();

        select.innerHTML = '';
        const placeholder = document.createElement('option');
        placeholder.value = '';
        placeholder.disabled = true;
        placeholder.selected = true;
        placeholder.textContent = analises.length
            ? 'Selecione uma análise...'
            : 'Nenhuma análise disponível';
        select.appendChild(placeholder);

        analises.forEach(a => {
            const opt = document.createElement('option');
            opt.value = a.nome;
            opt.textContent = a.nome;
            select.appendChild(opt);
        });
        analisesCarregadas = true;
    } catch {
        select.innerHTML = '<option value="" disabled selected>Erro ao carregar análises</option>';
    }
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

    // Máscara de telefone (BR): (XX) XXXX-XXXX ou (XX) XXXXX-XXXX
    const telInput = document.getElementById('orcTelefone');
    if (telInput) {
        telInput.addEventListener('input', (e) => {
            e.target.value = formatarTelefone(e.target.value);
        });
    }

    // Submit
    document.getElementById('formOrcamento').addEventListener('submit', async (e) => {
        e.preventDefault();
        await enviarSolicitacao();
    });
});

function formatarTelefone(valor) {
    const digitos = valor.replace(/\D/g, '').slice(0, 11);
    const len = digitos.length;
    if (len === 0) return '';
    if (len < 3)  return `(${digitos}`;
    if (len < 7)  return `(${digitos.slice(0,2)}) ${digitos.slice(2)}`;
    if (len <= 10) return `(${digitos.slice(0,2)}) ${digitos.slice(2,6)}-${digitos.slice(6)}`;
    return `(${digitos.slice(0,2)}) ${digitos.slice(2,7)}-${digitos.slice(7)}`;
}

function telefoneValido(valor) {
    const d = valor.replace(/\D/g, '');
    return d.length === 10 || d.length === 11;
}

// ─── Validação ───────────────────────────────────────────────────────────────
function validarCampos() {
    let valido = true;
    const nome     = document.getElementById('orcNome');
    const email    = document.getElementById('orcEmail');
    const telefone = document.getElementById('orcTelefone');
    const servico  = document.getElementById('orcServico');

    ['orcNome','orcEmail','orcTelefone','orcServico'].forEach(id =>
        document.getElementById(id).classList.remove('erro'));
    ['errNome','errEmail','errTelefone','errServico'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.textContent = '';
    });

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
    if (telefone.value && !telefoneValido(telefone.value)) {
        telefone.classList.add('erro');
        const err = document.getElementById('errTelefone');
        if (err) err.textContent = 'Telefone inválido. Use (XX) XXXXX-XXXX.';
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
            fecharModal();
            notify('Solicitação enviada! Entraremos em contato em breve.', 'success');
            reativarBtn(btn, texto, loader);
        } else {
            exibirFeedback('err', data.mensagem || 'Erro ao enviar. Tente novamente.');
            reativarBtn(btn, texto, loader);
        }
    } catch {
        exibirFeedback('err', 'Não foi possível conectar ao servidor. Verifique sua conexão.');
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

function notify(mensagem, tipo) {
    if (typeof window.toast === 'function') {
        window.toast(mensagem, tipo || 'info');
    }
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