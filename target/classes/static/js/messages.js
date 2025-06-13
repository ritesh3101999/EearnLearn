document.addEventListener('DOMContentLoaded', function () {
	// Select all alert messages
	const alerts = document.querySelectorAll('.alert');

	alerts.forEach(alert => {
		// Auto-close after 5 seconds
		setTimeout(() => {
			if (alert.classList.contains('show')) {
				alert.classList.remove('show');
				alert.classList.add('fade');
				setTimeout(() => alert.remove(), 500);
			}
		}, 5000);
	});
});
