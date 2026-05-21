const nome  = document.getElementById('inputNome');
const valor = document.getElementById('inputValor');
const btn   = document.getElementById('btnSalvar');
function validate() {
  btn.disabled = !(nome.value.trim() && parseFloat(valor.value) > 0);
}
nome.addEventListener('input',  validate);
valor.addEventListener('input', validate);

function recount() {
  document.getElementById('countAmostras').textContent =
    document.querySelectorAll('#amostrasBody tr').length;
}
function addAmostra() {
  const tbody = document.getElementById('amostrasBody');
  const tr = document.createElement('tr');
  tr.innerHTML = `
    <td><input class="table-input" name="tipo" placeholder="Ex: Alimento sólido"></td>
    <td><input class="table-input" name="quantidade" placeholder="Ex: 250g mínimo"></td>
    <td><input class="table-input" name="conservacao" placeholder="Ex: Refrigerado (2-8°C)"></td>
    <td>
      <button type="button" class="icon-btn-danger" onclick="removeAmostra(this)">
        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </td>`;
  tbody.appendChild(tr);
  recount();
}
function removeAmostra(btn) {
  const tbody = document.getElementById('amostrasBody');
  if (tbody.querySelectorAll('tr').length <= 1) return;
  btn.closest('tr').remove();
  recount();
}
