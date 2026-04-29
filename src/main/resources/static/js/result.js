document.addEventListener('DOMContentLoaded', () => {
    
    // Captura o botão de nova análise
    const btnNovaAnalise = document.getElementById('btnNovaAnalise');

    if(btnNovaAnalise) {
        btnNovaAnalise.addEventListener('click', () => {
            // Aqui você pode redirecionar para a rota do Spring Boot 
            // que renderiza o formulário de nova análise
            console.log('Iniciando fluxo de nova análise...');
            window.location.href = '/analises/nova';
        });
    }

    // Exemplo de como você poderia adicionar interatividade futura:
    // Filtro de cards, animações de hover, ou até drag and drop (se virar um Kanban real).
    const cards = document.querySelectorAll('.card');
    cards.forEach(card => {
        card.addEventListener('mouseenter', () => {
            card.style.transform = 'translateY(-2px)';
            card.style.transition = 'transform 0.2s ease, box-shadow 0.2s ease';
            card.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.08)';
        });
        
        card.addEventListener('mouseleave', () => {
            card.style.transform = 'translateY(0)';
            card.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.05)';
        });
    });
});