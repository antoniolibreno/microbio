function initMap() {
  const localizacao = { lat: -24.9463583, lng: -53.4468717 };

  const mapa = new google.maps.Map(document.getElementById("map"), {
    zoom: 17,
    center: localizacao,
    mapTypeId: "roadmap"
  });

  new google.maps.Marker({
    position: localizacao,
    map: mapa,
    title: "Casa de Francisco"
  });
}

document.addEventListener('DOMContentLoaded', () => {
    // Usa event delegation no container pai em vez de buscar os botões diretamente
    const container = document.querySelector('.cards2-container');

    if (!container) return;

    container.addEventListener('click', function(e) {
        const botao = e.target.closest('.btn-detalhes');
        if (!botao) return;

        const cardPai = botao.closest('.card2');
        const isActive = cardPai.classList.contains('active');

        // Fecha todos
        document.querySelectorAll('.card2').forEach(card => {
            card.classList.remove('active');
            const btn = card.querySelector('.btn-detalhes');
            if (btn) btn.textContent = 'DETALHES';
        });

        // Abre só o clicado se não estava ativo
        if (!isActive) {
            cardPai.classList.add('active');
            botao.textContent = 'FECHAR';
        }

    });
});