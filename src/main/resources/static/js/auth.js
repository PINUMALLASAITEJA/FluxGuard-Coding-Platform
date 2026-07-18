document.addEventListener("DOMContentLoaded", function () {

    const loginForm = document.getElementById("loginForm");

    if (loginForm) {

        loginForm.addEventListener("submit", function (event) {

            const email = document.querySelector("input[name='email']").value.trim();
            const password = document.querySelector("input[name='password']").value.trim();

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

        });

    }

});