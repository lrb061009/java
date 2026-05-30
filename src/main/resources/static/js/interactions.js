/**
 * Interaction Design System v2
 * Skeleton, Ripple, Scroll Reveal, Page Transitions, Toasts, Form Feedback
 */
(function () {
  'use strict';

  /* ==========================================================
     Skeleton Manager
     ========================================================== */
  var SkeletonManager = {
    show: function (container, options) {
      options = options || {};
      var type = options.type || 'card';
      var count = options.count || 1;

      if (typeof container === 'string') {
        container = document.querySelector(container);
      }
      if (!container) return;

      if (!container._skeletonOriginal) {
        container._skeletonOriginal = container.innerHTML;
      }
      container.setAttribute('aria-busy', 'true');
      container.classList.add('skeleton-loading');

      var html = '';
      for (var i = 0; i < count; i++) {
        html += this._generate(type);
      }
      container.innerHTML = html;
    },

    hide: function (container) {
      if (typeof container === 'string') {
        container = document.querySelector(container);
      }
      if (!container || !container._skeletonOriginal) return;

      container.removeAttribute('aria-busy');
      container.classList.remove('skeleton-loading');
      container.innerHTML = container._skeletonOriginal;
      delete container._skeletonOriginal;
      ScrollReveal.refresh();
    },

    _generate: function (type) {
      switch (type) {
        case 'card':
          return '<div class="skeleton skeleton-card"></div>';
        case 'text':
          return '<div class="skeleton skeleton-text"></div><div class="skeleton skeleton-text"></div><div class="skeleton skeleton-text"></div>';
        case 'table-row':
          return '<div class="skeleton-table-row"><div class="skeleton skeleton-text"></div><div class="skeleton skeleton-text"></div><div class="skeleton skeleton-text"></div><div class="skeleton skeleton-text"></div></div>';
        case 'stat':
          return '<div class="skeleton skeleton-stat"></div>';
        case 'title-text':
          return '<div class="skeleton skeleton-title"></div><div class="skeleton skeleton-text"></div><div class="skeleton skeleton-text"></div>';
        case 'menu-cards':
          var h = '';
          for (var i = 0; i < 3; i++) {
            h += '<div class="skeleton skeleton-card" style="height:280px;margin-bottom:16px;"></div>';
          }
          return h;
        default:
          return '<div class="skeleton skeleton-card"></div>';
      }
    }
  };

  /* ==========================================================
     Button Ripple
     ========================================================== */
  function initRipple() {
    document.addEventListener('pointerdown', function (e) {
      var btn = e.target.closest('.btn-ripple');
      if (!btn) return;

      var ripple = document.createElement('span');
      var rect = btn.getBoundingClientRect();
      var size = Math.max(rect.width, rect.height) * 2.5;

      ripple.className = 'ripple-effect';
      ripple.style.width = size + 'px';
      ripple.style.height = size + 'px';
      ripple.style.left = (e.clientX - rect.left - size / 2) + 'px';
      ripple.style.top = (e.clientY - rect.top - size / 2) + 'px';

      btn.appendChild(ripple);
      ripple.addEventListener('animationend', function () {
        ripple.remove();
      });
    });
  }

  /* ==========================================================
     Scroll Reveal (IntersectionObserver)
     ========================================================== */
  var ScrollReveal = {
    observer: null,

    init: function () {
      if (this.observer) return;

      var self = this;
      this.observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
          if (entry.isIntersecting) {
            entry.target.classList.add('revealed');
            self.observer.unobserve(entry.target);
          }
        });
      }, {
        threshold: 0.08,
        rootMargin: '0px 0px -30px 0px'
      });

      this._observeAll();
    },

    _observeAll: function () {
      var self = this;
      var els = document.querySelectorAll('.reveal, .reveal-stagger');
      els.forEach(function (el) {
        self.observer.observe(el);
      });
    },

    refresh: function () {
      if (!this.observer) return;
      this._observeAll();
    }
  };

  /* ==========================================================
     Page Transitions
     ========================================================== */
  function initPageTransitions() {
    var overlay = document.createElement('div');
    overlay.className = 'page-transition-overlay';
    overlay.setAttribute('aria-hidden', 'true');
    document.body.appendChild(overlay);

    document.addEventListener('click', function (e) {
      var link = e.target.closest('a[href]');
      if (!link) return;

      var href = link.getAttribute('href');
      if (!href) return;
      if (href === '#') return;
      if (href.startsWith('#')) return;
      if (href.startsWith('javascript:')) return;
      if (href.startsWith('mailto:')) return;
      if (href.startsWith('tel:')) return;
      if (link.getAttribute('target') === '_blank') return;
      if (link.hasAttribute('download')) return;
      if (link.hasAttribute('data-no-transition')) return;
      if (e.ctrlKey || e.metaKey) return;
      if (/\/api\//.test(href)) return;

      if (/^https?:\/\//.test(href)) {
        try {
          var u = new URL(href);
          if (u.host !== window.location.host) return;
        } catch (_) { return; }
      }

      e.preventDefault();
      overlay.classList.add('active');

      setTimeout(function () {
        window.location.href = href;
      }, 280);
    });

    window.addEventListener('pageshow', function (e) {
      if (e.persisted) {
        overlay.classList.remove('active');
      }
    });
  }

  /* ==========================================================
     Toast Notifications
     ========================================================== */
  var Toast = {
    _container: null,

    _ensureContainer: function () {
      if (this._container) return;
      this._container = document.createElement('div');
      this._container.className = 'toast-container';
      this._container.setAttribute('role', 'status');
      this._container.setAttribute('aria-live', 'polite');
      document.body.appendChild(this._container);
    },

    show: function (message, type, duration) {
      type = type || 'success';
      duration = (duration === undefined) ? 3000 : duration;

      this._ensureContainer();

      var icons = {
        success: '✓',
        error: '✕',
        warning: '⚠',
        info: 'ℹ',
        loading: ''
      };
      var toast = document.createElement('div');
      toast.className = 'toast toast-' + type;
      toast.setAttribute('role', 'alert');
      toast.innerHTML =
        '<span class="toast-icon">' + (icons[type] || icons.success) + '</span>' +
        '<span class="toast-message">' + escapeHtml(message) + '</span>';

      this._container.appendChild(toast);

      if (type !== 'loading' && duration > 0) {
        var self = this;
        setTimeout(function () {
          self.dismiss(toast);
        }, duration);
      }

      return toast;
    },

    dismiss: function (toast) {
      if (!toast) return;
      toast.classList.add('removing');
      toast.addEventListener('animationend', function () {
        if (toast.parentNode) toast.parentNode.removeChild(toast);
      });
    }
  };

  function escapeHtml(str) {
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
  }

  /* ==========================================================
     asyncSubmit — fetch with loading/success/failure UI
     ========================================================== */
  window.asyncSubmit = function (fetchPromise, options) {
    options = options || {};
    var submitBtn = options.submitBtn;
    var successMsg = options.successMsg || '操作成功';
    var errorMsg = options.errorMsg || '操作失败';
    var reload = options.reload !== false;
    var onSuccess = options.onSuccess;
    var onError = options.onError;
    var loadingText = options.loadingText || '处理中...';

    var btn = null;
    var originalHTML = '';
    var originalDisabled = false;

    if (submitBtn) {
      btn = typeof submitBtn === 'string' ? document.querySelector(submitBtn) : submitBtn;
    }
    if (btn) {
      originalHTML = btn.innerHTML;
      originalDisabled = btn.disabled;
      btn.classList.add('btn-loading');
      btn.setAttribute('aria-busy', 'true');
      btn.innerHTML = '<span class="spinner"></span>' + loadingText;
      btn.disabled = true;
    }

    var loadingToast = Toast.show(loadingText, 'loading', 0);

    return fetchPromise
      .then(function (res) {
        return res.text().then(function (text) {
          var data = null;
          try { data = JSON.parse(text); } catch (_) { data = null; }
          if (!res.ok) {
            throw new Error((data && data.message) || ('请求失败 (' + res.status + ')'));
          }
          return { data: data, res: res };
        });
      })
      .then(function (result) {
        Toast.dismiss(loadingToast);
        Toast.show((result.data && result.data.message) || successMsg, 'success');

        if (btn) {
          btn.classList.remove('btn-loading');
          btn.removeAttribute('aria-busy');
          btn.classList.add('btn-success-state');
          btn.innerHTML = '✓ 成功';
        }

        if (onSuccess) onSuccess(result.data);

        if (reload) {
          setTimeout(function () { window.location.reload(); }, 800);
        } else if (btn) {
          setTimeout(function () {
            btn.classList.remove('btn-success-state');
            btn.innerHTML = originalHTML;
            btn.disabled = originalDisabled;
          }, 1500);
        }
      })
      .catch(function (err) {
        Toast.dismiss(loadingToast);
        Toast.show(err.message || errorMsg, 'error');

        if (btn) {
          btn.classList.remove('btn-loading');
          btn.removeAttribute('aria-busy');
          btn.classList.add('btn-error-state');
          btn.innerHTML = '✕ 失败';
          setTimeout(function () {
            btn.classList.remove('btn-error-state');
            btn.innerHTML = originalHTML;
            btn.disabled = originalDisabled;
          }, 2000);
        }

        if (onError) onError(err);
      });
  };

  /* ==========================================================
     Confirm Dialog (accessible)
     ========================================================== */
  window.showConfirm = function (message, onConfirm) {
    if (!message || !onConfirm) return;
    if (window.confirm(message)) {
      onConfirm();
    }
  };

  /* ==========================================================
     Tab Switching with skeleton
     ========================================================== */
  function initTabs() {
    document.addEventListener('click', function (e) {
      var tabBtn = e.target.closest('[data-tab]');
      if (!tabBtn) return;

      var tabId = tabBtn.getAttribute('data-tab');
      var container = tabBtn.closest('.tabs-container');
      if (!container) return;

      container.querySelectorAll('[data-tab]').forEach(function (b) {
        b.classList.remove('active');
        b.setAttribute('aria-selected', 'false');
      });
      tabBtn.classList.add('active');
      tabBtn.setAttribute('aria-selected', 'true');

      var panel = document.getElementById(tabId) || document.querySelector('[data-tab-panel="' + tabId + '"]');
      if (!panel) return;

      var panelGroup = panel.parentElement;
      panelGroup.querySelectorAll('.tab-panel').forEach(function (p) {
        if (p !== panel) {
          p.style.display = 'none';
          p.classList.remove('active');
        }
      });

      if (panel._skeletonOriginal === undefined) {
        panel._skeletonOriginal = panel.innerHTML;
      }

      panel.classList.add('switching');
      panel.innerHTML = '<div class="skeleton skeleton-table-row"></div><div class="skeleton skeleton-table-row"></div><div class="skeleton skeleton-table-row"></div><div class="skeleton skeleton-table-row"></div>';

      setTimeout(function () {
        panel.innerHTML = panel._skeletonOriginal;
        panel.classList.remove('switching');
        panel.style.display = 'block';
        panel.classList.add('active');
        ScrollReveal.refresh();
      }, 250);
    });
  }

  /* ==========================================================
     Keyboard Navigation: close modals with Escape
     ========================================================== */
  function initKeyboard() {
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') {
        var visibleModals = document.querySelectorAll('.modal-overlay:not([style*="display:none"]):not(.modal-hidden)');
        if (visibleModals.length > 0) {
          var lastModal = visibleModals[visibleModals.length - 1];
          lastModal.classList.add('modal-hidden');
          setTimeout(function () {
            lastModal.style.display = 'none';
          }, 250);
        }
      }
    });
  }

  /* ==========================================================
     Initialize Everything
     ========================================================== */
  function init() {
    initRipple();
    ScrollReveal.init();
    initPageTransitions();
    initTabs();
    initKeyboard();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  window.SkeletonManager = SkeletonManager;
  window.Toast = Toast;
  window.ScrollReveal = ScrollReveal;
})();
