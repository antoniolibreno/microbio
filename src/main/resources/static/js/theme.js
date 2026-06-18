/* Alternância de tema light/dark. O tema inicial já foi aplicado pelo
   script anti-flash no <head>; aqui só tratamos o clique no botão. */
(function () {
  function current() {
    return document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
  }

  function apply(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    try { localStorage.setItem('theme', theme); } catch (e) {}
    var meta = document.querySelector('meta[name="theme-color"]');
    if (meta) meta.setAttribute('content', theme === 'dark' ? '#0f1220' : '#fcfcfc');
    document.querySelectorAll('[data-theme-toggle]').forEach(syncButton);
  }

  function syncButton(btn) {
    var dark = current() === 'dark';
    btn.setAttribute('aria-pressed', String(dark));
    btn.setAttribute('aria-label', dark ? 'Mudar para tema claro' : 'Mudar para tema escuro');
    btn.setAttribute('title', dark ? 'Tema claro' : 'Tema escuro');
    var sun = btn.querySelector('[data-icon-sun]');
    var moon = btn.querySelector('[data-icon-moon]');
    if (sun)  sun.style.display  = dark ? 'block' : 'none';
    if (moon) moon.style.display = dark ? 'none' : 'block';
  }

  document.addEventListener('click', function (e) {
    var btn = e.target.closest('[data-theme-toggle]');
    if (!btn) return;
    e.preventDefault();
    apply(current() === 'dark' ? 'light' : 'dark');
  });

  document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-theme-toggle]').forEach(syncButton);
  });
})();
