(function () {
    'use strict';

    function ensureRoot() {
        var root = document.getElementById('toastRoot');
        if (!root) {
            root = document.createElement('div');
            root.id = 'toastRoot';
            root.className = 'toast-root';
            root.setAttribute('aria-live', 'polite');
            document.body.appendChild(root);
        }
        return root;
    }

    function iconFor(type) {
        if (type === 'success') return 'fa-circle-check';
        if (type === 'error')   return 'fa-circle-exclamation';
        return 'fa-circle-info';
    }

    window.toast = function (message, type) {
        type = type || 'info';
        if (!message) return;
        var root = ensureRoot();
        var el = document.createElement('div');
        el.className = 'toast toast--' + type;
        el.setAttribute('role', type === 'error' ? 'alert' : 'status');

        var icon = document.createElement('i');
        icon.className = 'fa-solid ' + iconFor(type);

        var span = document.createElement('span');
        span.className = 'toast-msg';
        span.textContent = message;

        var close = document.createElement('button');
        close.className = 'toast-close';
        close.setAttribute('aria-label', 'Fechar');
        close.innerHTML = '<i class="fa-solid fa-xmark"></i>';
        close.addEventListener('click', function () { dismiss(el); });

        el.appendChild(icon);
        el.appendChild(span);
        el.appendChild(close);
        root.appendChild(el);

        requestAnimationFrame(function () { el.classList.add('is-visible'); });

        var timer = setTimeout(function () { dismiss(el); }, 4000);
        el.addEventListener('mouseenter', function () { clearTimeout(timer); });
    };

    function dismiss(el) {
        if (!el || el.classList.contains('is-leaving')) return;
        el.classList.add('is-leaving');
        el.addEventListener('transitionend', function () {
            if (el.parentNode) el.parentNode.removeChild(el);
        }, { once: true });
        setTimeout(function () {
            if (el.parentNode) el.parentNode.removeChild(el);
        }, 500);
    }

    document.addEventListener('DOMContentLoaded', function () {
        var ok  = document.querySelector('meta[name="flash-sucesso"]');
        var err = document.querySelector('meta[name="flash-erro"]');
        if (ok  && ok.content)  window.toast(ok.content,  'success');
        if (err && err.content) window.toast(err.content, 'error');
    });
})();
