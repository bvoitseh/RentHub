/* Sets the colour theme before the app starts, so there is no flash of the wrong theme (D-50). External file,
   because the production Content-Security-Policy does not allow inline scripts. */
(function () {
  var theme = null;
  try {
    theme = window.localStorage.getItem('renthub-theme');
  } catch (e) {
    /* storage unavailable: follow the system */
  }
  if (theme !== 'light' && theme !== 'dark') {
    theme = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
  document.documentElement.dataset.theme = theme;
})();
