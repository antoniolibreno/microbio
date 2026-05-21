function trocarAba(nome, botao) {
  document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
  document.getElementById('painel-' + nome).classList.add('active');
  botao.classList.add('active');
}
