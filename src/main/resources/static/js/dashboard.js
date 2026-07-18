"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const navLinks = document.querySelectorAll(".nav-links a");

    navLinks.forEach((link) => {
        if (link.getAttribute("href") === window.location.pathname) {
            link.classList.add("active");
        }
    });

    // Future feature: Live notifications
    // Future feature: Contest countdown
    // Future feature: Dashboard auto refresh
    // Future feature: FluxGuard live monitoring
});
