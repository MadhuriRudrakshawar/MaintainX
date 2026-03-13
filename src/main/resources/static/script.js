$(function () {
    const API = "/api/v1/network-elements";
    const MW_API = "/api/v1/maintenance-windows";
    const ANALYTICS_API = "/api/v1/analytics/dashboard";
    const TOKEN_KEY = "accessToken";
    const mwCache = new Map();

    $.ajaxSetup({
        beforeSend: function (xhr) {
            const token = sessionStorage.getItem(TOKEN_KEY);
            if (token) {
                xhr.setRequestHeader("Authorization", "Bearer " + token);
            }
        }
    });

    $(document).ajaxError(function (_event, xhr, settings) {
        const url = settings && settings.url ? String(settings.url) : "";
        const authEndpoint = url.includes("/api/v1/auth/login") || url.includes("/api/v1/auth/logout");

        if (xhr && xhr.status === 401 && !authEndpoint) {
            clearSessionData();
            hideAllRolePages();
            showLogin();
            alert("Session expired. Please login again.");
            return;
        }

        if (xhr && xhr.status === 403 && !authEndpoint) {
            alert(errMsg(xhr) || "Action not allowed");
        }
    });

    // ===================== Cache jQuery selectors =====================
    const $loginView = $("#loginView");
    const $homeView = $("#homeView");

    const $username = $("#username");
    const $password = $("#password");
    const $loginBtn = $("#loginBtn");
    const $logoutBtn = $("#logoutBtn");

    const $adminPage = $("#adminPage");
    const $approverPage = $("#approverPage");
    const $engineerPage = $("#engineerPage");
    const $rolePages = $adminPage.add($approverPage).add($engineerPage);
    const $analyticsView = $("#analyticsView");
    const $viewAnalyticsBtns = $(".js-view-analytics");
    const $backToDashboardBtn = $("#backToDashboardBtn");
    const $approvalTrendNote = $("#approvalTrendNote");

    const charts = {};
    let activeRole = "";

    const $addElementPanel = $("#addElementPanel");
    const $showAddElementBtn = $("#showAddElementBtn");
    const $cancelAddElementBtn = $("#cancelAddElementBtn");
    const $backFromElementFormBtn = $("#backFromElementFormBtn");
    const $saveElementBtn = $("#saveElementBtn");
    const $neTableSection = $("#neTableSection");

    const $adminAuditBtn = $("#adminAuditBtn");
    const $approverAuditBtn = $("#approverAuditBtn");

    const $auditTableEl = $("#auditTable");


    const $neCode = $("#neCode");
    const $neName = $("#neName");
    const $neType = $("#neType");
    const $neRegion = $("#neRegion");
    const $neForm = $("#neForm");
    const $statusActive = $("#statusActive");
    const $statusDeactive = $("#statusDeactive");

    const $neTableEl = $("#neTable");
    const $mwTableEl = $("#mwTable");
    const $mwTableSection = $("#mwTableSection");

    const $addWindowPanel = $("#addWindowPanel");
    const $showAddWindowBtn = $("#showAddWindowBtn");
    const $cancelAddWindowBtn = $("#cancelAddWindowBtn");
    const $backFromWindowFormBtn = $("#backFromWindowFormBtn");
    const $saveWindowBtn = $("#saveWindowBtn");

    const $mwId = $("#mwId");
    const $mwTitle = $("#mwTitle");
    const $mwStart = $("#mwStart");
    const $mwEnd = $("#mwEnd");
    const $mwElements = $("#mwElements");
    const $mwForm = $("#mwForm");

    // ===================== Approver Reject Modal (optional) =====================

    const $rejectMwId = $("#rejectMwId");
    const $rejectReason = $("#rejectReason");
    const $confirmRejectBtn = $("#confirmRejectBtn");

    const rejectModal = (function () {
        const el = document.getElementById("rejectModal");
        if (el && window.bootstrap && window.bootstrap.Modal) {
            return new bootstrap.Modal(el);
        }
        return null;
    })();


    const auditModal = (function () {
        const el = document.getElementById("auditModal");
        if (el && window.bootstrap && window.bootstrap.Modal) {
            return new bootstrap.Modal(el);
        }
        return null;
    })();

    // ===================== DataTables =====================
    const table = $neTableEl.DataTable({
        dom: "frt<'dt-bottom d-flex align-items-center justify-content-between' i l p>",
        pageLength: 5,
        lengthChange: true,
        lengthMenu: [[5, 10, 20], [5, 10, 20]],
        scrollY: "320px",
        scrollCollapse: true,
        autoWidth: false,
        rowId: "id",
        columnDefs: [{orderable: false, targets: 5}],
        columns: [
            {data: "elementCode", width: "14%"},
            {data: "name", width: "24%"},
            {data: "elementType", width: "16%"},
            {data: "region", width: "14%"},
            {data: "status", width: "12%", render: (v) => makeStatusBadge(v)},
            {data: null, width: "20%", render: () => actionsHtml()}
        ]
    });

    const mwTable = $mwTableEl.DataTable({
        dom: "frt<'dt-bottom d-flex align-items-center justify-content-between' i l p>",
        pageLength: 5,
        lengthChange: true,
        lengthMenu: [[5, 10, 20], [5, 10, 20]],
        scrollY: "320px",
        scrollCollapse: true,
        autoWidth: false,
        rowId: "id",
        columnDefs: [{orderable: false, targets: 7}],
        columns: [
            {data: "id", width: "8%", render: (v) => escapeHtml(formatMwNumber(v))},
            {data: "title", width: "18%", render: (v) => escapeHtml(v || "")},
            {
                data: "networkElementNames",
                width: "22%",
                render: (v) => renderNetworkElements(v)
            },
            {data: "startTime", width: "12%", render: (v) => escapeHtml(formatDateTime(v))},
            {data: "endTime", width: "12%", render: (v) => escapeHtml(formatDateTime(v))},
            {
                data: "windowStatus",
                width: "12%",
                render: (v, _t, row) => {
                    const st = String(v || "");
                    if (st.toUpperCase() === "REJECTED" && row && row.rejectionReason) {
                        return escapeHtml(st + " (" + row.rejectionReason + ")");
                    }
                    return escapeHtml(st);
                }
            },
            {
                data: null,
                width: "12%",
                render: (_v, _t, row) => renderExecutionStatus(row)
            },
            {
                data: "id",
                width: "18%",
                render: (id, _t, row) => mwActionsHtml(id, row)
            }
        ]
    });

    const $pendingMwTableEl = $("#pendingMwTable");
    let pendingMwTable = null;

    if ($pendingMwTableEl.length) {
        pendingMwTable = $pendingMwTableEl.DataTable({
            dom: "frt<'dt-bottom d-flex align-items-center justify-content-between' i l p>",
            pageLength: 5,
            lengthChange: true,
            lengthMenu: [[5, 10, 20], [5, 10, 20]],
            scrollY: "320px",
            scrollCollapse: true,
            autoWidth: false,
            rowId: "id",
            columnDefs: [{orderable: false, targets: 6}],
            columns: [
                {data: "title", width: "20%", render: (v) => escapeHtml(v || "")},
                {data: "requestedByUsername", width: "12%", render: (v) => escapeHtml(v || "")},
                {data: "networkElementNames", width: "24%", render: (v) => renderNetworkElements(v)},
                {data: "startTime", width: "13%", render: (v) => escapeHtml(formatDateTime(v))},
                {data: "endTime", width: "13%", render: (v) => escapeHtml(formatDateTime(v))},
                {data: "windowStatus", width: "12%", render: (v) => escapeHtml(v || "")},
                {
                    data: "id", width: "6%", render: (id) => `
      <button class="btn btn-success btn-sm js-approve" data-id="${id}">Approve</button>
      <button class="btn btn-danger btn-sm js-reject" data-id="${id}">Reject</button>
    `
                }
            ]
        });
    }


    // ===================== Audit Log DataTable =====================
    let auditTable = null;

    if ($auditTableEl.length) {
        auditTable = $auditTableEl.DataTable({
            dom: "frt<'dt-bottom d-flex align-items-center justify-content-between' i l p>",
            pageLength: 5,
            lengthChange: true,
            lengthMenu: [[5, 10, 20], [5, 10, 20]],
            scrollY: "320px",
            scrollCollapse: true,
            ordering: true,
            order: [[0, "desc"]],
            columns: [
                {data: "createdAt", render: (v) => escapeHtml(formatDateTime(v))},
                {data: "entityType", render: (v) => escapeHtml(v || "")},
                {
                    data: "entityId",
                    render: (v, _t, row) => escapeHtml(formatAuditEntityId(row && row.entityType, v))
                },
                {data: "action", render: (v) => escapeHtml(v || "")},
                {data: "username", render: (v) => escapeHtml(v || "")},
                {data: "roleName", render: (v) => escapeHtml(v || "")},
                {data: "details", render: (v) => escapeHtml(v || "")}
            ]
        });
    }

    // ===================== Initial Routing =====================
    const savedToken = sessionStorage.getItem(TOKEN_KEY);
    const savedRole = sessionStorage.getItem("role");
    const savedUserId = sessionStorage.getItem("userId");

    if (savedToken && savedRole && savedUserId) {
        showHome();
        routeByRole(savedRole);
    } else {
        clearSessionData();
        showLogin();
    }

    // ===================== Auth Events =====================
    $loginBtn.on("click", login);

    $password.on("keydown", function (e) {
        if (e.key === "Enter") login();
    });

    $logoutBtn.on("click", logout);
    $viewAnalyticsBtns.on("click", openAnalyticsView);
    $backToDashboardBtn.on("click", closeAnalyticsView);


    // ===================== Audit Log (Admin + Approver) =====================
    $adminAuditBtn.on("click", openAuditLog);
    $approverAuditBtn.on("click", openAuditLog);


    // ===================== Maintenance Window Events =====================
    $saveWindowBtn.on("click", createMaintenanceWindow);
    initPanelToggle($showAddWindowBtn, $cancelAddWindowBtn, $addWindowPanel, $mwTableSection, clearMwForm, $backFromWindowFormBtn);

    $mwStart.add($mwEnd).on("focus click input change", applyMwDateConstraints);

    $mwTableEl.on("click", ".js-mw-delete", function () {
        const id = $(this).data("id");
        deleteMaintenanceWindow(id);
    });

    $mwTableEl.on("click", ".js-mw-edit", function () {
        const id = Number($(this).data("id"));
        const row = mwCache.get(id);
        if (!row) {
            alert("Unable to load maintenance window for editing");
            return;
        }
        fillMwForm(row);
        showPanel($showAddWindowBtn, $addWindowPanel, $mwTableSection, $backFromWindowFormBtn);
    });

// ===================== Approver Actions (Approve/Reject) =====================
    $pendingMwTableEl.on("click", ".js-approve", function () {
        const id = Number($(this).data("id"));
        if (!id) return;

        if (!confirm("Approve this maintenance request?")) return;

        approveMaintenanceWindow(id);
    });

    $pendingMwTableEl.on("click", ".js-reject", function () {
        const id = Number($(this).data("id"));
        if (!id) return;

        if (rejectModal && $rejectMwId.length && $rejectReason.length && $confirmRejectBtn.length) {
            $rejectMwId.val(String(id));
            $rejectReason.val("");
            rejectModal.show();
        } else {
            const reason = prompt("Enter rejection reason:");
            if (!reason) {
                alert("Rejection reason is required");
                return;
            }
            rejectMaintenanceWindow(id, reason.trim());
        }
    });

    $confirmRejectBtn.on("click", function () {
        const id = Number($rejectMwId.val());
        const reason = ($rejectReason.val() || "").trim();

        if (!id) {
            alert("Invalid request id");
            return;
        }

        if (!reason) {
            alert("Rejection reason is required");
            return;
        }

        rejectMaintenanceWindow(id, reason);
    });

    // ===================== Network Element Events =====================
    initPanelToggle($showAddElementBtn, $cancelAddElementBtn, $addElementPanel, $neTableSection, function () {
        clearForm();
        $saveElementBtn.removeData("editId");
    }, $backFromElementFormBtn);

    $saveElementBtn.on("click", function () {
        const payload = readForm();
        if (!payload) return;

        const editId = $(this).data("editId");

        if (editId) {
            $.ajax({
                url: `${API}/${editId}`,
                method: "PUT",
                contentType: "application/json",
                data: JSON.stringify(payload)
            })
                .done((updated) => {
                    table.row("#" + $.escapeSelector(String(updated.id))).data(updated).draw(false);
                    hidePanel($showAddElementBtn, $addElementPanel, $neTableSection, function () {
                        clearForm();
                        $saveElementBtn.removeData("editId");
                    }, $backFromElementFormBtn);
                })
                .fail((xhr) => {
                    alert(errMsg(xhr) || "Update failed");
                    console.log(xhr.responseText);
                });
        } else {
            $.ajax({
                url: API,
                method: "POST",
                contentType: "application/json",
                data: JSON.stringify(payload)
            })
                .done((created) => {
                    loadAll();
                    hidePanel($showAddElementBtn, $addElementPanel, $neTableSection, function () {
                        clearForm();
                        $saveElementBtn.removeData("editId");
                    }, $backFromElementFormBtn);
                })
                .fail((xhr) => {
                    alert(errMsg(xhr) || "Create failed");
                    console.log(xhr.responseText);
                });
        }
    });

    $neTableEl.on("click", ".js-edit", function () {
        const row = table.row($(this).closest("tr"));
        const data = row.data();

        fillForm(data);
        $saveElementBtn.data("editId", data.id);
        showPanel($showAddElementBtn, $addElementPanel, $neTableSection, $backFromElementFormBtn);
    });

    $neTableEl.on("click", ".js-toggle", function () {
        const row = table.row($(this).closest("tr"));
        const data = row.data();

        const isActive = String(data.status).toUpperCase() === "ACTIVE";
        const endpoint = isActive ? "deactivate" : "activate";
        const actionLabel = isActive ? "deactivate" : "activate";
        const confirmMessage = `Do you want to ${actionLabel} ${data.elementCode}?`;

        if (isActive) {
            canDeactivateNetworkElement(data.id)
                .done((result) => {
                    if (!result.allowed) {
                        const mwName = result.window && result.window.title ? result.window.title : "scheduled window";
                        alert(`Network element is already in use in "${mwName}".`);
                        return;
                    }
                    if (!confirm(confirmMessage)) return;
                    patchElementStatus(data.id, endpoint);
                })
                .fail((xhr) => {
                    alert(errMsg(xhr) || "Unable to validate schedule usage");
                    console.log(xhr.responseText);
                });
            return;
        }

        if (!confirm(confirmMessage)) return;
        patchElementStatus(data.id, endpoint);
    });

    $neTableEl.on("click", ".js-delete", function () {
        const row = table.row($(this).closest("tr"));
        const data = row.data();

        canDeactivateNetworkElement(data.id)
            .done((result) => {
                if (!result.allowed) {
                    const mwName = result.window && result.window.title ? result.window.title : "scheduled window";
                    alert(`Network element is already in use in "${mwName}".`);
                    return;
                }

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
            })
            .fail((xhr) => {
                alert(errMsg(xhr) || "Unable to validate schedule usage");
                console.log(xhr.responseText);
            });
    });

    // ===================== Functions =====================
    function login() {
        const username = $username.val().trim();
        const password = $password.val();

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
                if (!res || !res.token) {
                    alert("Login response missing token");
                    clearSessionData();
                    showLogin();
                    return;
                }

                sessionStorage.setItem(TOKEN_KEY, String(res.token));
                sessionStorage.setItem("role", res.role || "");
                sessionStorage.setItem("username", res.username || username);

                const loginUserId = res && (res.id ?? res.userId ?? res.userID ?? res.user_id);
                if (loginUserId !== null && loginUserId !== undefined && String(loginUserId).trim() !== "") {
                    sessionStorage.setItem("userId", String(loginUserId));
                } else {
                    sessionStorage.removeItem("userId");
                }

                showHome();
                routeByRole(res.role);

                // clear login inputs
                $username.val("");
                $password.val("");
            },
            error: function (xhr) {
                clearSessionData();
                alert("Invalid username or password.");
                alert(errMsg(xhr) || "Login failed");
                console.log(xhr.status, xhr.responseText);
            }
        });
    }

    function loadAll() {
        $.get(API)
            .done((rows) => {
                const sorted = (rows || []).slice().sort((a, b) => Number(b.id || 0) - Number(a.id || 0));
                table.clear().rows.add(sorted).draw();
            })
            .fail((xhr) => {
                alert("Failed to load network elements");
                console.log(xhr.responseText);
            });
    }

    function logout() {
        $.ajax({
            url: "/api/v1/auth/logout",
            method: "POST"
        }).always(function () {
            $("body").removeClass("analytics-mode");
            clearSessionData();
            $username.val("");
            $password.val("");
            $analyticsView.addClass("d-none");
            hideAllRolePages();
            showLogin();
        });
    }

    function clearSessionData() {
        sessionStorage.removeItem(TOKEN_KEY);
        sessionStorage.removeItem("role");
        sessionStorage.removeItem("username");
        sessionStorage.removeItem("userId");
    }

    function showHome() {
        $loginView.addClass("d-none");
        $homeView.removeClass("d-none");
    }

    function showLogin() {
        $homeView.addClass("d-none");
        $loginView.removeClass("d-none");
    }

    function hideAllRolePages() {
        $rolePages.addClass("d-none");
    }

    function routeByRole(role) {
        const r = String(role || "").toUpperCase();
        activeRole = r;
        $analyticsView.addClass("d-none");
        hideAllRolePages();

        if (r === "ADMIN") {
            $adminPage.removeClass("d-none");
            loadAll();
        } else if (r === "APPROVER") {
            $approverPage.removeClass("d-none");
            loadPendingMaintenanceWindows();
        } else if (r === "ENGINEER") {
            $engineerPage.removeClass("d-none");
            applyMwDateConstraints();
            loadNetworkElementsForMw();
            loadMaintenanceWindows();
        } else {
            logout();
        }
    }

    function openAnalyticsView() {
        if (!window.Chart) {
            alert("Chart.js is not available.");
            return;
        }

        hideAllRolePages();
        $analyticsView.removeClass("d-none");
        $("body").addClass("analytics-mode");
        renderAnalyticsDashboard();
    }

    function closeAnalyticsView() {
        $analyticsView.addClass("d-none");
        $("body").removeClass("analytics-mode");
        routeByRole(activeRole || sessionStorage.getItem("role"));
    }

    function renderAnalyticsDashboard() {
        $.get(ANALYTICS_API)
            .done((dashboard) => {
                drawMaintenanceStatusChart(dashboard.maintenanceStatusCounts || {});
                drawApprovedWindowScheduleChart(dashboard.approvedWindowTimeline || []);
                drawElementsByTypeChart(dashboard.elementsByType || {});
                drawElementHealthChart(dashboard.elementsByStatus || {});
                drawApprovalTrendChart(dashboard.approvalRejectionTrend || []);
                drawTopImpactedElementsChart(dashboard.topImpactedElements || {});
            })
            .fail(() => {
                alert("Failed to load analytics data.");
            });
    }

    function mapToEntries(countMap) {
        return Object.entries(countMap || {});
    }

    function createOrUpdateChart(canvasId, config) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;

        if (charts[canvasId]) {
            charts[canvasId].destroy();
        }
        charts[canvasId] = new Chart(canvas, config);
    }

    function drawMaintenanceStatusChart(statusCounts) {
        const entries = mapToEntries(statusCounts);
        createOrUpdateChart("chartMaintenanceStatus", {
            type: "doughnut",
            data: {
                labels: entries.map(e => e[0]),
                datasets: [{
                    label: "Windows",
                    data: entries.map(e => e[1]),
                    backgroundColor: ["#0d6efd", "#198754", "#dc3545", "#fd7e14", "#6c757d"]
                }]
            },
            options: {responsive: true, maintainAspectRatio: false}
        });
    }

    function formatHm(dateObj) {
        const hh = String(dateObj.getHours()).padStart(2, "0");
        const mm = String(dateObj.getMinutes()).padStart(2, "0");
        return `${hh}:${mm}`;
    }

    function drawElementsByTypeChart(typeCounts) {
        const entries = mapToEntries(typeCounts);
        createOrUpdateChart("chartElementsByType", {
            type: "bar",
            data: {
                labels: entries.map(e => e[0]),
                datasets: [{
                    label: "Elements",
                    data: entries.map(e => e[1]),
                    backgroundColor: "#6f42c1"
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {y: {beginAtZero: true, ticks: {precision: 0}}}
            }
        });
    }

    function drawElementHealthChart(healthCounts) {
        const normalizedCounts = {
            ACTIVE: Number(healthCounts.ACTIVE || 0),
            DEACTIVE: Number(healthCounts.DEACTIVE || 0)
        };
        const entries = mapToEntries(normalizedCounts);
        createOrUpdateChart("chartElementHealth", {
            type: "pie",
            data: {
                labels: entries.map((e) => e[0] === "ACTIVE" ? "Active" : "Deactive"),
                datasets: [{
                    label: "Elements",
                    data: entries.map(e => e[1]),
                    backgroundColor: ["#198754", "#dc3545"]
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: "bottom"
                    }
                }
            }
        });
    }

    function drawApprovalTrendChart(trend) {
        const labels = (trend || []).map((p) => p.date);
        const approved = (trend || []).map((p) => Number(p.approved || 0));
        const rejected = (trend || []).map((p) => Number(p.rejected || 0));

        createOrUpdateChart("chartApprovalTrend", {
            type: "bar",
            data: {
                labels,
                datasets: [
                    {label: "Approved", data: approved, backgroundColor: "#198754"},
                    {label: "Rejected", data: rejected, backgroundColor: "#dc3545"}
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {y: {beginAtZero: true, ticks: {precision: 0}}}
            }
        });

        $approvalTrendNote.text("Trend is aggregated by backend from maintenance window status by date.");
    }

    function drawTopImpactedElementsChart(topImpactedElements) {
        const entries = mapToEntries(topImpactedElements);
        createOrUpdateChart("chartTopImpactedElements", {
            type: "bar",
            data: {
                labels: entries.map(e => e[0]),
                datasets: [{
                    label: "Maintenance impact count",
                    data: entries.map(e => e[1]),
                    backgroundColor: "#fd7e14"
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                indexAxis: "y",
                scales: {x: {beginAtZero: true, ticks: {precision: 0}}}
            }
        });
    }

    // ===== Network Element Helpers =====
    function readForm() {
        if (!validateForm($neForm)) return null;

        const elementCode = $neCode.val().trim();
        const name = $neName.val().trim();
        const elementType = $neType.val();
        const region = $neRegion.val();
        const status = $('input[name="neStatus"]:checked').val();

        return {elementCode, name, elementType, region, status};
    }

    function fillForm(e) {
        $neCode.val(e.elementCode);
        $neName.val(e.name);
        $neType.val(e.elementType);
        $neRegion.val(e.region);

        const s = String(e.status).toUpperCase();
        if (s === "DEACTIVE") $statusDeactive.prop("checked", true);
        else $statusActive.prop("checked", true);
    }

    function clearForm() {
        resetFormValidation($neForm);
        $neCode.val("");
        $neName.val("");
        $neType.val("");
        $neRegion.val("");
        $statusActive.prop("checked", true);
    }

    function initPanelToggle($showBtn, $cancelBtn, $panel, $tableSection, onHide, $backBtn) {
        $showBtn.on("click", function () {
            showPanel($showBtn, $panel, $tableSection, $backBtn);
        });

        $cancelBtn.on("click", function () {
            hidePanel($showBtn, $panel, $tableSection, onHide, $backBtn);
        });

        if ($backBtn && $backBtn.length) {
            $backBtn.on("click", function () {
                hidePanel($showBtn, $panel, $tableSection, onHide, $backBtn);
            });
        }
    }

    function showPanel($showBtn, $panel, $tableSection, $backBtn) {
        $panel.removeClass("d-none");
        $tableSection.addClass("d-none");
        if ($backBtn && $backBtn.length) $backBtn.removeClass("d-none");
        $showBtn.attr("aria-expanded", "true");
    }

    function hidePanel($showBtn, $panel, $tableSection, onHide, $backBtn) {
        $panel.addClass("d-none");
        $tableSection.removeClass("d-none");
        if ($backBtn && $backBtn.length) $backBtn.addClass("d-none");
        $showBtn.attr("aria-expanded", "false");
        if (typeof onHide === "function") onHide();
    }

    function makeStatusBadge(status) {
        const s = String(status || "").toUpperCase();
        return s === "ACTIVE"
            ? '<span class="badge text-bg-success">ACTIVE</span>'
            : '<span class="badge text-bg-danger">DEACTIVE</span>';
    }

    function patchElementStatus(id, endpoint) {
        $.ajax({
            url: `${API}/${id}/${endpoint}`,
            method: "PATCH"
        })
            .done((updated) => {
                table.row("#" + $.escapeSelector(String(updated.id))).data(updated).draw(false);
            })
            .fail((xhr) => {
                alert(errMsg(xhr) || "Status update failed");
                console.log(xhr.responseText);
            });
    }

    function canDeactivateNetworkElement(elementId) {
        return $.get(MW_API).then((rows) => {
            const now = Date.now();
            const blockingWindow = (rows || []).find((mw) => {
                const status = String((mw && mw.windowStatus) || "").trim().toUpperCase();
                if (status !== "PENDING" && status !== "APPROVED") return false;
                const startMs = Date.parse(String((mw && mw.startTime) || ""));
                const endMs = Date.parse(String((mw && mw.endTime) || ""));
                if (isNaN(startMs) || isNaN(endMs)) return false;
                if (endMs < now) return false;
                const ids = (mw && mw.networkElementIds) || [];
                return Array.isArray(ids) && ids.includes(Number(elementId));
            });
            return {
                allowed: !blockingWindow,
                window: blockingWindow || null
            };
        });
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


    function makeExecutionBadge(status) {
        const s = String(status || "").toUpperCase();
        if (s === "COMPLETED") return '<span class="badge text-bg-success">COMPLETED</span>';
        if (s === "IN_PROGRESS") return '<span class="badge text-bg-warning">IN_PROGRESS</span>';
        if (s === "PLANNED") return '<span class="badge text-bg-secondary">PLANNED</span>';
        return "";
    }

    function renderExecutionStatus(row) {
        const effective = deriveExecutionStatus(row);
        return effective ? makeExecutionBadge(effective) : '<span class="text-muted">-</span>';
    }

    function deriveExecutionStatus(row) {
        const approval = String((row && row.windowStatus) || "").trim().toUpperCase();
        if (approval !== "APPROVED") return "";

        const now = Date.now();
        const s = Date.parse(String((row && row.startTime) || ""));
        const e = Date.parse(String((row && row.endTime) || ""));

        if (isNaN(s) || isNaN(e)) return "";
        if (now > e) return "COMPLETED";
        if (now >= s && now <= e) return "IN_PROGRESS";
        return "PLANNED";
    }

    function mwActionsHtml(id, row) {
        const approval = String((row && row.windowStatus) || "").toUpperCase();
        const canEditDelete = approval === "PENDING";

        const btns = [];
        if (canEditDelete) btns.push(`<button class="btn btn-outline-primary js-mw-edit" data-id="${id}">Edit</button>`);
        if (canEditDelete) btns.push(`<button class="btn btn-outline-danger js-mw-delete" data-id="${id}">Delete</button>`);

        if (!btns.length) return `<span class="text-muted">—</span>`;

        return `<div class="btn-group btn-group-sm" role="group">${btns.join("")}</div>`;
    }

    function renderNetworkElements(values) {
        const list = Array.isArray(values) ? values : [];
        if (!list.length) return "";
        const items = list
            .map((name) => `<span class="text-break">${escapeHtml(name)}</span>`)
            .join("");
        return `<div class="d-flex flex-column gap-1">${items}</div>`;
    }

// ===== MW Helpers =====
    function loadNetworkElementsForMw() {
        $.get(API)
            .done((rows) => {
                $mwElements.empty();
                (rows || [])
                    .filter((ne) => String((ne && ne.status) || "").trim().toUpperCase() === "ACTIVE")
                    .forEach((ne) => {
                        const code = escapeHtml(ne.elementCode || "");
                        const name = escapeHtml(ne.name || "");
                        $mwElements.append(`
                            <label class="mw-element-option">
                                <input class="form-check-input" type="checkbox" value="${ne.id}">
                                <span>${code} - ${name}</span>
                            </label>
                        `);
                    });
            })
            .fail((xhr) => {
                alert("Failed to load network elements");
                console.log(xhr.responseText);
            });
    }

    function loadMaintenanceWindows() {
        $.get(MW_API)
            .done((rows) => {
                const sorted = (rows || []).slice().sort((a, b) => Number(b.id || 0) - Number(a.id || 0));
                mwCache.clear();
                sorted.forEach((w) => mwCache.set(Number(w.id), w));
                mwTable.clear().rows.add(sorted).draw();
            })
            .fail((xhr) => {
                alert("Failed to load maintenance windows");
                console.log(xhr.responseText);
            });
    }

    function createMaintenanceWindow() {
        const payload = readMwForm();
        if (!payload) return;

        const editId = $mwId.val().trim();

        if (editId) {
            $.ajax({
                url: `${MW_API}/${editId}`,
                method: "PUT",
                contentType: "application/json",
                data: JSON.stringify(payload)
            })
                .done(() => {
                    hidePanel($showAddWindowBtn, $addWindowPanel, $mwTableSection, clearMwForm, $backFromWindowFormBtn);
                    loadMaintenanceWindows();
                })
                .fail((xhr) => {
                    alert(errMsg(xhr) || "Update failed");
                    console.log(xhr.responseText);
                });
        } else {
            $.ajax({
                url: MW_API,
                method: "POST",
                contentType: "application/json",
                data: JSON.stringify(payload)
            })
                .done(() => {
                    hidePanel($showAddWindowBtn, $addWindowPanel, $mwTableSection, clearMwForm, $backFromWindowFormBtn);
                    loadMaintenanceWindows();
                })
                .fail((xhr) => {
                    alert(errMsg(xhr) || "Create failed");
                    console.log(xhr.responseText);
                });
        }
    }

    function loadPendingMaintenanceWindows() {
        if (!pendingMwTable) return;

        $.get(MW_API)
            .done((rows) => {
                const pending = (rows || [])
                    .filter(r => String(r.windowStatus || "").toUpperCase() === "PENDING")
                    .sort((a, b) => Number(b.id || 0) - Number(a.id || 0));
                pendingMwTable.clear().rows.add(pending).draw();
            })
            .fail((xhr) => {
                alert("Failed to load pending maintenance windows");
                console.log(xhr.responseText);
            });
    }


    // ===================== Audit Log functions =====================
    function openAuditLog() {
        if (!auditModal) {
            alert("Audit modal not available");
            return;
        }

        if (auditTable) auditTable.clear().draw();
        loadAuditLogs();
        auditModal.show();
    }

    function loadAuditLogs() {
        if (!auditTable) return;

        $.ajax({
            url: "/api/v1/audit-logs",
            method: "GET"
        })
            .done((rows) => {
                const sorted = (rows || []).slice().sort((a, b) => String(b.createdAt || "").localeCompare(String(a.createdAt || "")));
                auditTable.clear().rows.add(sorted).draw();
            })
            .fail((xhr) => {
                alert(errMsg(xhr) || "Failed to load audit logs");
                console.log(xhr && xhr.responseText ? xhr.responseText : xhr);
            });
    }


    function approveMaintenanceWindow(id) {
        $.ajax({
            url: `${MW_API}/${id}/approve`,
            method: "PATCH"
        })
            .done(() => {
                loadPendingMaintenanceWindows();
                // optional: refresh engineer list if they have it open later
            })
            .fail((xhr) => {
                alert(errMsg(xhr) || "Approve failed");
                console.log(xhr.responseText);
            });
    }

    function rejectMaintenanceWindow(id, reason) {
        $.ajax({
            url: `${MW_API}/${id}/reject`,
            method: "PATCH",
            contentType: "application/json",
            data: JSON.stringify({reason: reason})
        })
            .done(() => {
                if (rejectModal) rejectModal.hide();
                loadPendingMaintenanceWindows();
            })
            .fail((xhr) => {
                alert(errMsg(xhr) || "Reject failed");
                console.log(xhr.responseText);
            });
    }

    function deleteMaintenanceWindow(id) {
        if (!confirm("Delete this maintenance window?")) return;

        $.ajax({
            url: `${MW_API}/${id}`,
            method: "DELETE"
        })
            .done(() => loadMaintenanceWindows())
            .fail((xhr) => {
                alert(errMsg(xhr) || "Delete failed");
                console.log(xhr.responseText);
            });
    }

    function readMwForm() {
        applyMwDateConstraints();
        if (!validateForm($mwForm)) return null;

        const requestedById = Number(sessionStorage.getItem("userId"));

        const title = $mwTitle.val().trim();
        const startTime = $mwStart.val();
        const endTime = $mwEnd.val();

        const startNum = document.getElementById("mwStart").valueAsNumber;
        const endNum = document.getElementById("mwEnd").valueAsNumber;
        const nowNum = Date.now();

        const selected = $mwElements.find('input:checked')
            .map((_, el) => Number(el.value))
            .get();

        if (!requestedById || isNaN(requestedById)) {
            alert("Session expired. Please login again.");
            return null;
        }

        if (!selected.length) {
            alert("Please select at least one Network Element");
            return null;
        }

        if (startNum < nowNum || endNum < nowNum) {
            alert("Past date/time is not allowed");
            return null;
        }

        if (endNum <= startNum) {
            alert("End Time must be after Start Time");
            return null;
        }

        if (!requestedById || isNaN(requestedById)) {
            alert("Session expired. Please login again.");
            return null;
        }

        return {
            title: title,
            description: "",
            startTime: toSeconds(startTime),
            endTime: toSeconds(endTime),
            networkElementIds: selected,
        };
    }

    function clearMwForm() {
        resetFormValidation($mwForm);
        $mwId.val("");
        $mwTitle.val("");
        $mwStart.val("");
        $mwEnd.val("");
        $mwElements.find("input").prop("checked", false);
        applyMwDateConstraints();
    }

    function fillMwForm(w) {
        const selectedIds = new Set((w.networkElementIds || []).map(String));
        $mwId.val(w.id);
        $mwTitle.val(w.title || "");
        $mwStart.val(toDateTimeLocalValueFromServer(w.startTime));
        $mwEnd.val(toDateTimeLocalValueFromServer(w.endTime));
        $mwElements.find("input").each((_, el) => {
            $(el).prop("checked", selectedIds.has(String(el.value)));
        });
        applyMwDateConstraints();
    }

    function applyMwDateConstraints() {
        const nowValue = toDateTimeLocalValue(nowLocalMinute());

        $mwStart.attr("min", nowValue);

        const startValue = $mwStart.val();
        const endMin = (startValue && startValue > nowValue) ? startValue : nowValue;
        $mwEnd.attr("min", endMin);
    }

    function nowLocalMinute() {
        const d = new Date();
        d.setSeconds(0, 0);
        return d;
    }

    function toDateTimeLocalValue(d) {
        const pad = (n) => String(n).padStart(2, "0");
        return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    }

    function validateForm($form) {
        const form = $form.get(0);
        if (!form.checkValidity()) {
            $form.addClass("was-validated");
            form.reportValidity();
            return false;
        }
        return true;
    }

    function resetFormValidation($form) {
        $form.removeClass("was-validated");
    }

    function toSeconds(dtLocal) {
        if (!dtLocal) return dtLocal;
        return dtLocal.length === 16 ? (dtLocal + ":00") : dtLocal;
    }

    function formatMwNumber(id) {
        const n = Number(id);
        if (!Number.isFinite(n) || n <= 0) return "";
        return "MW-" + String(n).padStart(2, "0");
    }

    function formatAuditEntityId(entityType, id) {
        const n = Number(id);
        if (!Number.isFinite(n) || n <= 0) return id == null ? "" : String(id);

        const type = String(entityType || "").toUpperCase();
        if (type === "MAINTENANCE_WINDOW") return "MW-" + String(n).padStart(2, "0");
        if (type === "NETWORK_ELEMENT") return "NE-" + String(n).padStart(3, "0");
        return String(n);
    }

    function toDateTimeLocalValueFromServer(val) {
        if (!val) return "";
        return String(val).substring(0, 16);
    }

    function formatDateTime(val) {
        if (!val) return "";
        return String(val).replace("T", " ").substring(0, 16);
    }

    function escapeHtml(str) {
        if (str === null || str === undefined) return "";
        return String(str)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function errMsg(xhr) {
        try {
            const j = JSON.parse(xhr.responseText);
            return j.message;
        } catch (e) {
            return null;
        }
    }


    function drawApprovedWindowScheduleChart(approvedWindows) {
        const $container = $("#approvedWindowScheduleChartWrap");
        if (!$container.length) return;

        if (charts["chartApprovedWindowSchedule"]) {
            charts["chartApprovedWindowSchedule"].destroy();
            delete charts["chartApprovedWindowSchedule"];
        }

        if (!approvedWindows || approvedWindows.length === 0) {
            $container.html('<div class="text-muted small">No approved bookings found.</div>');
            return;
        }

        const dataPoints = approvedWindows.map(w => {
            const start = new Date(String(w.startTime).replace(" ", "T")).getTime();
            const end = new Date(String(w.endTime).replace(" ", "T")).getTime();
            const title = String(w.title || "UNTITLED");

            return {
                x: [start, end],
                y: title,
                title: title,
                barLabel: `${formatHm(new Date(start))} - ${formatHm(new Date(end))}`,
                startLabel: formatDateTime(w.startTime),
                endLabel: formatDateTime(w.endTime)
            };
        });

        const chartHeight = Math.max(260, dataPoints.length * 38 + 60);
        const containerWidth = $container.innerWidth() || 0;
        const chartWidth = Math.max(containerWidth, 1200, dataPoints.length * 140);

        $container.html(`
        <div class="approved-window-chart-inner" style="height: ${chartHeight}px; width: ${chartWidth}px;">
            <canvas id="chartApprovedWindowSchedule"></canvas>
        </div>
    `);

        const minStart = Math.min(...dataPoints.map(point => point.x[0]));
        const maxEnd = Math.max(...dataPoints.map(point => point.x[1]));
        const totalRange = Math.max(maxEnd - minStart, 1);
        const axisPadding = Math.max(totalRange * 0.04, 60 * 60 * 1000);

        const ctx = document.getElementById("chartApprovedWindowSchedule");
        if (!ctx) return;

        charts["chartApprovedWindowSchedule"] = new Chart(ctx, {
            type: "bar",
            data: {
                datasets: [{
                    label: "Approved Window Schedule",
                    data: dataPoints,
                    backgroundColor: "#2563eb",
                    borderColor: "#1d4ed8",
                    borderWidth: 1,
                    borderRadius: 4,
                    borderSkipped: false,
                    barThickness: 34,
                    minBarLength: 100
                }]
            },
            options: {
                indexAxis: "y",
                responsive: true,
                maintainAspectRatio: false,
                parsing: {
                    xAxisKey: "x",
                    yAxisKey: "y"
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            title: function (items) {
                                return items[0].raw.title;
                            },
                            label: function (context) {
                                const raw = context.raw;
                                return `${raw.startLabel} → ${raw.endLabel}`;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        type: "linear",
                        position: "top",
                        min: minStart - axisPadding,
                        max: maxEnd + axisPadding,
                        grid: {
                            display: true,
                            color: "rgba(15, 23, 42, 0.28)",
                            lineWidth: 1
                        },
                        ticks: {
                            maxTicksLimit: 8,
                            callback: function (value) {
                                return formatApprovedWindowAxis(value);
                            }
                        },
                        title: {
                            display: true,
                            text: "Date / Time"
                        }
                    },
                    y: {
                        grid: {
                            display: true,
                            color: "rgba(15, 23, 42, 0.22)",
                            lineWidth: 1
                        },
                        title: {
                            display: true,
                            text: "Maintenance Window"
                        }
                    }
                }
            },
            plugins: [{
                id: "approvedWindowBarLabels",
                afterDatasetsDraw(chart) {
                    const {ctx} = chart;
                    const meta = chart.getDatasetMeta(0);
                    const dataset = chart.data.datasets[0];

                    ctx.save();
                    ctx.font = "12px sans-serif";
                    ctx.fillStyle = "#ffffff";
                    ctx.textAlign = "center";
                    ctx.textBaseline = "middle";

                    meta.data.forEach((bar, index) => {
                        const raw = dataset.data[index];
                        const label = String(raw.barLabel || "");
                        const x = (bar.x + bar.base) / 2;
                        const y = bar.y;

                        ctx.fillText(label, x, y);
                    });

                    ctx.restore();
                }
            }]
        });
    }

    function formatApprovedWindowAxis(value) {
        const d = new Date(Number(value));
        if (isNaN(d.getTime())) return "";

        const yyyy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, "0");
        const dd = String(d.getDate()).padStart(2, "0");
        const hh = String(d.getHours()).padStart(2, "0");
        const mi = String(d.getMinutes()).padStart(2, "0");

        return `${yyyy}-${mm}-${dd} ${hh}:${mi}`;
    }

});
