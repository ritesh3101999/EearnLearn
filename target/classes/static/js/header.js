// header.js
document.addEventListener('DOMContentLoaded', function() {
    const dropdownToggle = document.getElementById('moreDropdown');
    const dropdownMenu = dropdownToggle ? dropdownToggle.nextElementSibling : null;

    if (dropdownToggle && dropdownMenu) {
        dropdownToggle.addEventListener('click', function(event) {
            event.preventDefault(); // Prevent default link behavior
            dropdownMenu.classList.toggle('show'); // Toggle the 'show' class to display/hide
        });

        // Close the dropdown if the user clicks outside of it
        window.addEventListener('click', function(event) {
            if (!dropdownToggle.contains(event.target) && !dropdownMenu.contains(event.target)) {
                if (dropdownMenu.classList.contains('show')) {
                    dropdownMenu.classList.remove('show');
                }
            }
        });
    }
});
