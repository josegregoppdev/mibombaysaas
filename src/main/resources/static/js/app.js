document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.alert-dismissible').forEach(function(alert) {
        setTimeout(function() {
            var bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });

    document.querySelectorAll('#sidebarOffcanvas .nav-link:not(.disabled)').forEach(function(link) {
        link.addEventListener('click', function() {
            var offcanvas = bootstrap.Offcanvas.getInstance(document.getElementById('sidebarOffcanvas'));
            if (offcanvas) offcanvas.hide();
        });
    });
});

function copyToClipboard(elementId) {
    var el = document.getElementById(elementId);
    if (!el) return;
    el.select();
    el.setSelectionRange(0, 99999);

    try {
        document.execCommand('copy');
        var btn = el.nextElementSibling;
        var orig = btn.innerHTML;
        btn.innerHTML = '<i class="bi bi-check"></i>';
        btn.classList.add('btn-success');
        btn.classList.remove('btn-outline-secondary');

        setTimeout(function() {
            btn.innerHTML = orig;
            btn.classList.remove('btn-success');
            btn.classList.add('btn-outline-secondary');
        }, 2000);
    } catch (e) {
        console.error('Copy failed:', e);
    }
}
