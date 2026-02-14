$(function () {

    const table = $("#neTable").DataTable({
        pageLength: 5,
        lengthChange: false,
        columnDefs: [
            { orderable: false, targets: 5 }
        ]
    });

    // Save -> add row
    $("#saveElementBtn").on("click", function () {

        const code = $("#neCode").val().trim();
        const name = $("#neName").val().trim();

        const typeVal = $("#neType").val();
        const typeText = $("#neType option:selected").text();

        const regionVal = $("#neRegion").val();
        const regionText = $("#neRegion option:selected").text();

        const statusVal = $('input[name="neStatus"]:checked').val();

        if (!code || !name || !typeVal || !regionVal || !statusVal) {
            alert("Please fill all fields");
            return;
        }

        const statusBadge = makeStatusBadge(statusVal);

        const actionsHtml = `
      <div class="btn-group btn-group-sm" role="group">
        <button type="button" class="btn btn-outline-primary js-edit">Edit</button>
        <button type="button" class="btn btn-outline-danger js-deactivate">Deactivate</button>
      </div>
    `;

        table.row.add([
            code,           /
            name,
            typeText,
            regionText,
            statusBadge,
            actionsHtml
        ]).draw(false);

        // clear
        $("#neCode").val("");
        $("#neName").val("");
        $("#neType").val("");
        $("#neRegion").val("");
        $("#statusActive").prop("checked", true);
    });

    // Deactivate button
    $("#neTable").on("click", ".js-deactivate", function () {
        const row = table.row($(this).closest("tr"));
        const data = row.data(); /

        // If already deactive, do nothing
        const statusText = stripHtml(data[4]).toUpperCase();
        if (statusText.includes("DEACTIVE")) return;

        data[4] = makeStatusBadge("DEACTIVE");
        row.data(data).draw(false);
    });

    // Edit button -> put row values back into form
    $("#neTable").on("click", ".js-edit", function () {
        const row = table.row($(this).closest("tr"));
        const data = row.data();

        // fill form
        $("#neCode").val(data[0]);
        $("#neName").val(data[1]);

        // match select by visible text
        $("#neType").val(valueByText("#neType", stripHtml(data[2])));
        $("#neRegion").val(valueByText("#neRegion", stripHtml(data[3])));

        const statusText = stripHtml(data[4]).toUpperCase();
        if (statusText.includes("DEACTIVE")) $("#statusDeactive").prop("checked", true);
        else $("#statusActive").prop("checked", true);

        // store row index to update on save
        $("#saveElementBtn").data("editRow", row.index());

        // open the collapse panel so user sees the form
        $("#addElementPanel").collapse("show");
    });

    // Save should update existing row if we are editing
    $("#saveElementBtn").on("click", function () {
        const editIndex = $(this).data("editRow");
        if (editIndex === undefined) return;

        const code = $("#neCode").val().trim();
        const name = $("#neName").val().trim();

        const typeVal = $("#neType").val();
        const typeText = $("#neType option:selected").text();

        const regionVal = $("#neRegion").val();
        const regionText = $("#neRegion option:selected").text();

        const statusVal = $('input[name="neStatus"]:checked').val();

        if (!code || !name || !typeVal || !regionVal || !statusVal) {
            alert("Please fill all fields");
            return;
        }

        const statusBadge = makeStatusBadge(statusVal);

        const row = table.row(editIndex);
        const old = row.data();

        old[0] = code;
        old[1] = name;
        old[2] = typeText;
        old[3] = regionText;
        old[4] = statusBadge;

        row.data(old).draw(false);

        // clear edit mode + form
        $("#saveElementBtn").removeData("editRow");
        $("#neCode").val("");
        $("#neName").val("");
        $("#neType").val("");
        $("#neRegion").val("");
        $("#statusActive").prop("checked", true);
    });

    function makeStatusBadge(status) {
        return status === "ACTIVE"
            ? '<span class="badge text-bg-success">ACTIVE</span>'
            : '<span class="badge text-bg-danger">DEACTIVE</span>';
    }

    function stripHtml(html) {
        return $("<div>").html(html).text();
    }

    function valueByText(selectId, text) {
        const $opt = $(selectId + " option").filter(function () {
            return $(this).text().trim().toLowerCase() === String(text).trim().toLowerCase();
        }).first();
        return $opt.val() || "";
    }

});
