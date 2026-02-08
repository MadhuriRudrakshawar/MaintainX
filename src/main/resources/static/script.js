// if you want to use $.ajax, include jQuery in HTML (see note below)

$(function () {

    // button click
    $("#loginBtn").on("click", login);

    // press Enter inside password
    $("#password").on("keydown", function (e) {
        if (e.key === "Enter") login();
    });

});

function login() {
    const username = $("#username").val().trim();
    const password = $("#password").val();

    if (!username || !password) {
        alert("Enter username and password");
        return;
    }

    $.ajax({
        url: "/api/v1/auth/login",                 // change to "/api/auth/login" if that's your backend
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify({ username: username, password: password }),

        success: function (res) {
            alert(res.message || "Login OK");

            // very simple role routing
            if (res.role === "ADMIN") loadAdmin();
            else if (res.role === "APPROVER") loadApprover();
            else loadEngineer();
        },

        error: function (xhr) {
            alert("Login failed");
        }
    });
}

// dummy functions (replace with your real page load logic)
function loadAdmin() { window.location.href = "admin.html"; }
function loadApprover() { window.location.href = "approver.html"; }
function loadEngineer() { window.location.href = "engineer.html"; }
