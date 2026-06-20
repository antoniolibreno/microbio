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
    const botoes = document.querySelectorAll('.btn-detalhes');

    botoes.forEach(botao => {
        botao.addEventListener('click', function() {
            const cardPai = this.closest('.card2');
            
            // 1. (Opcional) Fecha outros cards que possam estar abertos
            document.querySelectorAll('.card2').forEach(outroCard => {
                if (outroCard !== cardPai) {
                    outroCard.classList.remove('active');
                    // Reseta o texto dos outros botões para 'DETALHES'
                    const btnOutro = outroCard.querySelector('.btn-detalhes');
                    if(btnOutro) btnOutro.textContent = 'DETALHES';
                }
            });

            // 2. Abre ou fecha o card atual
            cardPai.classList.toggle('active');

            // 3. Muda o texto do botão atual
            if (cardPai.classList.contains('active')) {
                this.textContent = 'FECHAR';
            } else {
                this.textContent = 'DETALHES';
            }
        });
    });
});