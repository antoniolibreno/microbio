(function () {
  const css = getComputedStyle(document.documentElement);
  const token = (name, fallback) => (css.getPropertyValue(name).trim() || fallback);

  const primary      = token('--primary',      '#16a34a');
  const primaryGlow  = token('--primary-glow', 'rgba(22,163,74,.15)');
  const colorPend    = token('--chart-pendente',  '#92400e');
  const colorAnd     = token('--chart-andamento', '#1e40af');
  const colorConcl   = token('--chart-concluido', '#15803d');
  const colorCanc    = token('--chart-cancelado', '#991b1b');
  const textMuted    = token('--text-muted',   '#64748b');
  const borderFaint  = token('--border-faint', '#f1f5f9');
  const surface      = token('--surface',      '#ffffff');
  const textDark     = token('--text-dark',    '#0f172a');

  if (typeof Chart !== 'undefined') {
    Chart.defaults.font.family = token('--font', "'Inter', system-ui, sans-serif");
    Chart.defaults.color = textMuted;
  }

  /* ── Gráfico de área: orçamentos por mês ── */
  const areaCanvas = document.getElementById('areaChart');
  if (areaCanvas && typeof Chart !== 'undefined') {
    const labels = JSON.parse(areaCanvas.dataset.labels || '[]');
    const valores = JSON.parse(areaCanvas.dataset.valores || '[]');
    const ctx = areaCanvas.getContext('2d');
    const grad = ctx.createLinearGradient(0, 0, 0, 180);
    grad.addColorStop(0, primaryGlow);
    grad.addColorStop(1, 'rgba(22,163,74,0)');

    new Chart(areaCanvas, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: 'Orçamentos',
          data: valores,
          borderColor: primary,
          backgroundColor: grad,
          borderWidth: 2.5,
          fill: true,
          tension: 0.4,
          pointRadius: 0,
          pointHoverRadius: 5,
          pointHoverBackgroundColor: primary,
          pointHoverBorderColor: surface,
          pointHoverBorderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: textDark, titleColor: '#f8fafc', bodyColor: '#cbd5e1', padding: 10,
            callbacks: { label: c => ` ${c.parsed.y} orçamento${c.parsed.y !== 1 ? 's' : ''}` }
          }
        },
        scales: {
          x: { grid: { display: false }, border: { display: false }, ticks: { font: { size: 12 } } },
          y: { beginAtZero: true, grid: { color: borderFaint }, border: { display: false },
               ticks: { precision: 0, maxTicksLimit: 5, font: { size: 12 } } }
        }
      }
    });
  }

  /* ── Donut: distribuição por status ── */
  const donutCanvas = document.getElementById('donutChart');
  if (donutCanvas && typeof Chart !== 'undefined') {
    const data = [
      parseInt(donutCanvas.dataset.pendente  || '0', 10),
      parseInt(donutCanvas.dataset.andamento || '0', 10),
      parseInt(donutCanvas.dataset.concluido || '0', 10),
      parseInt(donutCanvas.dataset.cancelado || '0', 10)
    ];
    new Chart(donutCanvas, {
      type: 'doughnut',
      data: {
        labels: ['Pendente', 'Em andamento', 'Concluído', 'Cancelado'],
        datasets: [{
          data,
          backgroundColor: [colorPend, colorAnd, colorConcl, colorCanc],
          borderColor: surface, borderWidth: 3, hoverOffset: 6
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false, cutout: '72%',
        plugins: {
          legend: { display: false },
          tooltip: { backgroundColor: textDark, titleColor: '#f8fafc', bodyColor: '#cbd5e1', padding: 10 }
        }
      }
    });
  }

  /* ── Abas da atividade recente ── */
  const tabs = document.querySelectorAll('.tab[data-tab]');
  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const key = tab.dataset.tab;
      tabs.forEach(t => {
        const active = t === tab;
        t.classList.toggle('is-active', active);
        t.setAttribute('aria-selected', String(active));
      });
      document.querySelectorAll('.tab-panel[data-panel]').forEach(p => {
        p.hidden = p.dataset.panel !== key;
      });
      document.querySelectorAll('.see-all[data-seeall]').forEach(a => {
        a.hidden = a.dataset.seeall !== key;
      });
    });
  });
})();
