function filtrar() {
  const q = document.getElementById('searchInput').value.toLowerCase();
  const rows = document.querySelectorAll('#listContainer .list-row');
  let vis = 0;
  rows.forEach(r => {
    const show = r.textContent.toLowerCase().includes(q);
    r.style.display = show ? '' : 'none';
    if (show) vis++;
  });
  const label = document.getElementById('countLabel');
  if (label && q) label.textContent = vis;
}
