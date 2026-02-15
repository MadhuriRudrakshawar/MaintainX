$(function () {
    const API = "/api/v1/network-elements";

    $.ajaxSetup({xhrFields: {withCredentials: true}});


    const table = $("#neTable").DataTable({
        pageLength: 5,
        lengthChange: false,
        rowId: "id",
        columnDefs: [{orderable: false, targets: 5}],
        columns: [
            {data: "elementCode"},
            {data: "name"},
            {data: "elementType"},
            {data: "region"},
            {data: "status", render: (v) => makeStatusBadge(v)},
            {data: null, render: () => actionsHtml()}
        ]
    });

    if (sessionStorage.getItem("role")) {
        showHome();
        routeByRole(sessionStorage.getItem("role"));
    } else {
        showLogin();
    }

    $("#loginBtn").on("click", login);

    $("#password").on("keydown", function (e) {
        if (e.key === "Enter") login();
    });

    $("#logoutBtn").on("click", logout);

    // Save button (Add or Update)
    $("#saveElementBtn").on("click", function () {
        const payload = readForm();
        if (!payload) return;

        const editId = $(this).data("editId");

        if (editId) {
            // UPDATE
            $.ajax({
                url: `${API}/${editId}`,
                method: "PUT",
                contentType: "application/json",
                data: JSON.stringify(payload)
            })
                .done((updated) => {
                    table.row("#" + updated.id).data(updated).draw(false);
                    clearForm();
                    $("#saveElementBtn").removeData("editId");
                    $("#addElementPanel").collapse("hide");
                })
                .fail((xhr) => {
                    alert(errMsg(xhr) || "Update failed");
                    console.log(xhr.responseText);
                });
        } else {
            // CREATE
            $.ajax({
                url: API,
                method: "POST",
                contentType: "application/json",
                data: JSON.stringify(payload)
            })
                .done((created) => {
                    table.row.add(created).draw(false);
                    clearForm();
                    $("#addElementPanel").collapse("hide");
                })
                .fail((xhr) => {
                    alert(errMsg(xhr) || "Create failed");
                    console.log(xhr.responseText);
                });
        }
    });

    // Edit button
    $("#neTable").on("click", ".js-edit", function () {
        const row = table.row($(this).closest("tr"));
        const data = row.data();

        fillForm(data);
        $("#saveElementBtn").data("editId", data.id);
        $("#addElementPanel").collapse("show");
    });

    // Deactivate / Activate toggle
    $("#neTable").on("click", ".js-toggle", function () {
        const row = table.row($(this).closest("tr"));
        const data = row.data();

        const isActive = String(data.status).toUpperCase() === "ACTIVE";
        const endpoint = isActive ? "deactivate" : "activate";

        $.ajax({
            url: `${API}/${data.id}/${endpoint}`,
            method: "PATCH"
        })
            .done((updated) => {
                table.row("#" + updated.id).data(updated).draw(false);
            })
            .fail((xhr) => {
                alert(errMsg(xhr) || "Status update failed");
                console.log(xhr.responseText);
            });
    });

    // Delete button
    $("#neTable").on("click", ".js-delete", function () {
        const row = table.row($(this).closest("tr"));
        const data = row.data();

        if (!confirm(`Delete ${data.elementCode}?`)) return;

        $.ajax({
            url: `${API}/${data.id}`,
            method: "DELETE"
        })
            .done(() => {
                row.remove().draw(false);
            })
            .fail((xhr) => {
                alert(errMsg(xhr) || "Delete failed");
                console.log(xhr.responseText);
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
            url: "/api/v1/auth/login",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify({username, password}),
            success: function (res) {
                sessionStorage.setItem("role", res.role || "");
                sessionStorage.setItem("username", res.username || username);
                showHome();
                routeByRole(res.role);
                // clear login inputs after successful login
                $("#username").val("");
                $("#password").val("");
            },
            error: function (xhr) {
                alert("Login failed: " + (xhr.responseText || xhr.status));
                console.log(xhr.status, xhr.responseText);
            }
        });
    }

    function loadAll() {
        $.get(API)
            .done((rows) => {
                table.clear().rows.add(rows).draw();
            })
            .fail((xhr) => {
                alert("Failed to load network elements");
                console.log(xhr.responseText);
            });
    }

    function logout() {
        sessionStorage.clear();

        // clear login form
        $("#username").val("");
        $("#password").val("");

        hideAllRolePages();
        showLogin();
    }

    function showHome() {
        $("#loginView").addClass("d-none");
        $("#homeView").removeClass("d-none");
    }

    function showLogin() {
        $("#homeView").addClass("d-none");
        $("#loginView").removeClass("d-none");
    }


    function hideAllRolePages() {
        $("#adminPage, #approverPage, #engineerPage").addClass("d-none");
    }

    function routeByRole(role) {
        const r = String(role || "").toUpperCase();

        hideAllRolePages();

        if (r === "ADMIN") {
            $("#adminPage").removeClass("d-none");
            loadAll();
            
        } else if (r === "APPROVER") {
            $("#approverPage").removeClass("d-none");
        } else if (r === "ENGINEER") {
            $("#engineerPage").removeClass("d-none");
        } else {
            // unknown role -> force logout
            logout();
        }
    }

    // Helpers
    function readForm() {
        const elementCode = $("#neCode").val().trim();
        const name = $("#neName").val().trim();
        const elementType = $("#neType").val();
        const region = $("#neRegion").val();
        const status = $('input[name="neStatus"]:checked').val(); // ACTIVE / DEACTIVE

        if (!elementCode || !name || !elementType || !region || !status) {
            alert("Please fill all fields");
            return null;
        }

        return {elementCode, name, elementType, region, status};
    }

    function fillForm(e) {
        $("#neCode").val(e.elementCode);
        $("#neName").val(e.name);
        $("#neType").val(e.elementType);
        $("#neRegion").val(e.region);

        const s = String(e.status).toUpperCase();
        if (s === "DEACTIVE") $("#statusDeactive").prop("checked", true);
        else $("#statusActive").prop("checked", true);
    }

    function clearForm() {
        $("#neCode").val("");
        $("#neName").val("");
        $("#neType").val("");
        $("#neRegion").val("");
        $("#statusActive").prop("checked", true);
    }

    function makeStatusBadge(status) {
        const s = String(status || "").toUpperCase();
        return s === "ACTIVE"
            ? '<span class="badge text-bg-success">ACTIVE</span>'
            : '<span class="badge text-bg-danger">DEACTIVE</span>';
    }

    function actionsHtml() {
        return `
      <div class="btn-group btn-group-sm" role="group">
        <button type="button" class="btn btn-outline-primary js-edit">Edit</button>
        <button type="button" class="btn btn-outline-warning js-toggle">Active/Deactive</button>
        <button type="button" class="btn btn-outline-danger js-delete">Delete</button>
      </div>
    `;
    }

    function errMsg(xhr) {
        try {
            const j = JSON.parse(xhr.responseText);
            return j.message;
        } catch (e) {
            return null;
        }
    }
});
