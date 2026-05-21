function recountAmostras() {
  const rows = document.querySelectorAll('#amostrasBody tr:not(#emptyRow)');
  document.getElementById('countAmostras').textContent = rows.length;
  const empty = document.getElementById('emptyRow');
  if (empty) empty.hidden = rows.length > 0;
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
  recountAmostras();
}
function removeAmostra(btn) {
  btn.closest('tr').remove();
  recountAmostras();
}
