function viewProductDetailFromList(button, type = 'computerItem') {
    const list = type === 'pc' ? window.pcs : window.computerItems;
    const id = button.getAttribute('data-id');
    const obj = list.find(i => String(i.id) === id);
    if (!obj) {
        alert("Không tìm thấy sản phẩm!");
        return;
    }
    viewProductDetailByObject(obj, type);
}

function viewProductDetailByObject(item, type = 'computerItem') {
    console.log("Viewing product details:", item, "Type:", type);

    // Set common fields
    document.getElementById("view-id").textContent = item.id;
    document.getElementById("view-name").textContent = item.name;
    document.getElementById("view-price").textContent = Number(item.price || 0).toLocaleString("vi-VN");

    // Modal title
    document.getElementById("modalViewTitle").textContent = type === 'pc' ? "Chi tiết PC" : "Chi tiết linh kiện";

    // Brand & Model
    if (item.brand || item.model) {
        document.getElementById("row-brand-model").style.display = "flex";
        document.getElementById("view-brand").textContent = item.brand || "";
        document.getElementById("view-model").textContent = item.model || "";
    } else {
        document.getElementById("row-brand-model").style.display = "none";
    }

    // Description
    if (item.description) {
        document.getElementById("row-description").style.display = "block";
        document.getElementById("view-description").textContent = item.description;
    } else {
        document.getElementById("row-description").style.display = "none";
    }

    // Category vs ComputerItems
    if (type === 'computerItem') {
        document.getElementById("view-category-group").style.display = "block";
        document.getElementById("view-computer-items-group").style.display = "none";
        document.getElementById("view-category").textContent = item.category?.name || "(Không rõ)";
    } else if (type === 'pc') {
        document.getElementById("view-category-group").style.display = "none";
        document.getElementById("view-computer-items-group").style.display = "block";
        const components = item.computerItems?.map(ci => ci.name).join(", ") || "(Không có linh kiện)";
        document.getElementById("view-computer-items").textContent = components;
    }

    // Image
    document.getElementById("view-image").innerHTML = item.imageUrl
        ? `<img src="${item.imageUrl}" class="img-fluid rounded shadow border" style="max-height: 250px;" />`
        : `<small class="text-muted">Không có hình ảnh</small>`;

    // Show modal
    $('#modalViewProduct').modal('show');
}
