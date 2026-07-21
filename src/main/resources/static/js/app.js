document.addEventListener('DOMContentLoaded', function() {
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            bsAlert.close();
        }, 5000);
    });
});

function copyToClipboard(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.select();
        element.setSelectionRange(0, 99999);
        
        try {
            document.execCommand('copy');
            
            const button = element.nextElementSibling;
            const originalHTML = button.innerHTML;
            button.innerHTML = '<i class="bi bi-check"></i>';
            button.classList.add('btn-success');
            button.classList.remove('btn-outline-secondary');
            
            setTimeout(function() {
                button.innerHTML = originalHTML;
                button.classList.remove('btn-success');
                button.classList.add('btn-outline-secondary');
            }, 2000);
        } catch (err) {
            console.error('Error al copiar:', err);
        }
    }
}
