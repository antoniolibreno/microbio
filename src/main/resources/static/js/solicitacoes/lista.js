function filtrarTabela() {
    const termo = document.getElementById('searchInput').value.toLowerCase();
    const linhas = document.querySelectorAll('#tabelaSolicitacoes tbody tr');
    linhas.forEach(tr => {
        const texto = tr.textContent.toLowerCase();
        tr.style.display = texto.includes(termo) ? '' : 'none';
    });
}
