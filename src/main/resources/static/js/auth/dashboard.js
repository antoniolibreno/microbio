(function () {
  const barCanvas = document.getElementById('barChart');
  const donutCanvas = document.getElementById('donutChart');
  if (!barCanvas || !donutCanvas || typeof Chart === 'undefined') return;

  const css = getComputedStyle(document.documentElement);
  const token = (name, fallback) => {
    const v = css.getPropertyValue(name).trim();
    return v || fallback;
  };
  const primary       = token('--primary',       '#16a34a');
  const primaryGlow   = token('--primary-glow',  'rgba(22,163,74,.15)');
  const colorPendente   = token('--chart-pendente',   '#92400e');
  const colorAndamento  = token('--chart-andamento',  '#1e40af');
  const colorConcluido  = token('--chart-concluido',  '#15803d');
  const colorCancelado  = token('--chart-cancelado',  '#991b1b');
  const textMuted     = token('--text-muted',    '#64748b');
  const borderFaint   = token('--border-faint',  '#f1f5f9');
  const textDark      = token('--text-dark',     '#0f172a');

  const labelsMeses = JSON.parse(barCanvas.dataset.labels || '[]');
  const contagemMeses = JSON.parse(barCanvas.dataset.valores || '[]');
  const statusData = [
    parseInt(donutCanvas.dataset.pendente || '0', 10),
    parseInt(donutCanvas.dataset.andamento || '0', 10),
    parseInt(donutCanvas.dataset.concluido || '0', 10),
    parseInt(donutCanvas.dataset.cancelado || '0', 10)
  ];

  Chart.defaults.font.family = "'Inter', system-ui, sans-serif";
  Chart.defaults.color = textMuted;

  new Chart(barCanvas, {
    type: 'bar',
    data: {
      labels: labelsMeses,
      datasets: [{
        label: 'Orçamentos',
        data: contagemMeses,
        backgroundColor: primaryGlow,
        borderColor: primary,
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
          backgroundColor: textDark,
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
        y: { beginAtZero: true, grid: { color: borderFaint }, border: { display: false },
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
        backgroundColor: [colorPendente, colorAndamento, colorConcluido, colorCancelado],
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
          backgroundColor: textDark,
          titleColor: '#f8fafc',
          bodyColor: '#94a3b8',
          padding: 10,
        }
      }
    }
  });
})();
