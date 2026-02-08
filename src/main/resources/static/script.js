$(function () {

    if (sessionStorage.getItem("role")) {
        showHome();
    } else {
        showLogin();
    }

    $("#loginBtn").on("click", login);

    $("#password").on("keydown", function (e) {
        if (e.key === "Enter") login();
    });

    $("#logoutBtn").on("click", logout);

});

function showHome() {
    $("#loginView").addClass("d-none");
    $("#homeView").removeClass("d-none");
}

function showLogin() {
    $("#homeView").addClass("d-none");
    $("#loginView").removeClass("d-none");
}

function login() {
    const username = $("#username").val().trim();
    const password = $("#password").val();

    if (!username || !password) return;

    $.ajax({
        url: "/api/v1/auth/login",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify({ username, password }),

        success: function (res) {
            sessionStorage.setItem("role", res.role || "");
            sessionStorage.setItem("username", res.username || username);
            showHome();
        },

        error: function () {
            // stay on login view (no alerts)
        }
    });
}

function logout() {
    sessionStorage.clear();

    // clear form
    $("#username").val("");
    $("#password").val("");


    showLogin();
}
