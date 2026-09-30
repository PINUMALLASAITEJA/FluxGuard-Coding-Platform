document.addEventListener("DOMContentLoaded", function () {

    const loginForm = document.getElementById("loginForm");

    if (loginForm) {

        loginForm.addEventListener("submit", async function (event) {

            const usernameField = document.querySelector("input[name='username']");
            const passwordField = document.querySelector("input[name='password']");
            const email = usernameField.value.trim();
            const password = passwordField.value.trim();

            if (email === "") {
                alert("Please enter your email.");
                event.preventDefault();
                return;
            }

            if (password === "") {
                alert("Please enter your password.");
                event.preventDefault();
                return;
            }

            if (password.length < 6) {
                alert("Password must contain at least 6 characters.");
                event.preventDefault();
                return;
            }

            event.preventDefault();

            try {
                const response = await fetch(loginForm.action || window.location.pathname, {
                    method: "POST",
                    body: new FormData(loginForm),
                    credentials: "same-origin",
                    headers: {
                        "X-Requested-With": "XMLHttpRequest"
                    }
                });

                if (response.status === 403) {
                    alert("Action Denied!");
                    return;
                }

                const finalUrl = response.url || window.location.href;
                if (finalUrl && finalUrl !== window.location.href) {
                    window.location.assign(finalUrl);
                    return;
                }

                if (response.redirected) {
                    window.location.assign(window.location.href);
                    return;
                }

                if (response.ok) {
                    window.location.assign("/dashboard");
                }
            } catch (error) {
                console.error("Login submission failed:", error);
                loginForm.submit();
            }

        });

    }

});