(function () {
  const barCanvas = document.getElementById('barChart');
  const donutCanvas = document.getElementById('donutChart');
  if (!barCanvas || !donutCanvas || typeof Chart === 'undefined') return;

  const labelsMeses = JSON.parse(barCanvas.dataset.labels || '[]');
  const contagemMeses = JSON.parse(barCanvas.dataset.valores || '[]');
  const statusData = [
    parseInt(donutCanvas.dataset.pendente || '0', 10),
    parseInt(donutCanvas.dataset.andamento || '0', 10),
    parseInt(donutCanvas.dataset.concluido || '0', 10),
    parseInt(donutCanvas.dataset.cancelado || '0', 10)
  ];

  Chart.defaults.font.family = "'Inter', system-ui, sans-serif";
  Chart.defaults.color = '#64748b';

  new Chart(barCanvas, {
    type: 'bar',
    data: {
      labels: labelsMeses,
      datasets: [{
        label: 'Orçamentos',
        data: contagemMeses,
        backgroundColor: 'rgba(26,112,43,.15)',
        borderColor: '#1a702b',
        borderWidth: 2,
        borderRadius: 6,
        borderSkipped: false,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#0f172a',
          titleColor: '#f8fafc',
          bodyColor: '#94a3b8',
          padding: 10,
          callbacks: {
            label: ctx => ` ${ctx.parsed.y} orçamento${ctx.parsed.y !== 1 ? 's' : ''}`
          }
        }
      },
      scales: {
        x: { grid: { display: false }, border: { display: false },
             ticks: { font: { size: 12 } } },
        y: { beginAtZero: true, grid: { color: '#f1f5f9' }, border: { display: false },
             ticks: { precision: 0, font: { size: 12 } } }
      }
    }
  });

  new Chart(donutCanvas, {
    type: 'doughnut',
    data: {
      labels: ['Pendente', 'Em andamento', 'Concluído', 'Cancelado'],
      datasets: [{
        data: statusData,
        backgroundColor: ['#f59e0b', '#3b82f6', '#22c55e', '#f43f5e'],
        borderColor: '#ffffff',
        borderWidth: 3,
        hoverOffset: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '68%',
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#0f172a',
          titleColor: '#f8fafc',
          bodyColor: '#94a3b8',
          padding: 10,
        }
      }
    }
  });
})();
