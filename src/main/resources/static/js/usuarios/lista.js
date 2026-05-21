function filtrar() {
  const t = document.getElementById('searchInput').value.toLowerCase();
  const rows = document.querySelectorAll('#userTable tbody tr');
  let vis = 0, total = 0;
  rows.forEach(r => {
    if (r.querySelector('.empty-state')) return;
    total++;
    const show = r.textContent.toLowerCase().includes(t);
    r.style.display = show ? '' : 'none';
    if (show) vis++;
  });
  const label = document.getElementById('countLabel');
  if (label) label.textContent = t ? vis + ' resultado(s)' : total + ' usuário(s)';
}
