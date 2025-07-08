const formTemplates = {
  pc: {
    action: "/admin/product/pc/save",
    title: "Thêm PC mới",
    html: `
      <div class="mb-3">
        <label class="form-label fw-bold">Tên sản phẩm</label>
        <input type="text" name="name" class="form-control form-control-lg rounded-3 fs-4" required>
      </div>
      <div class="mb-3">
        <label class="form-label fw-bold">Loại linh kiện</label>
        <select name="category.id" class="form-select form-select-lg rounded-3 fs-4" required id="create-category">
          <option value="" disabled selected>-- Chọn loại --</option>
        </select>
      </div>
      <div class="mb-3">
        <label for="pc-computer-items" class="form-label fw-bold mb-2">Chọn linh kiện</label>
        <select name="pc-computer-items" id="pc-computer-items" class="form-select rounded-3" multiple required size="8" style="min-height: 180px;"></select>
        <small class="form-text text-muted mt-2">
          Giữ <kbd>Ctrl</kbd> (hoặc <kbd>Cmd</kbd>) để chọn nhiều linh kiện.
        </small>
      </div>
      <div class="mb-3">
        <label class="form-label fw-bold">Linh kiện đã chọn:</label>
        <ul id="pc-selected-list" class="list-group small mb-2"></ul>
        <div><strong>Tổng giá:</strong> <span id="pc-total-price" class="text-primary">0</span> VND</div>
      </div>
      <div id="pc-selected-hidden-inputs"></div>
      <div class="mb-3">
        <label class="form-label fw-bold">Giá</label>
        <input type="number" name="price" id="pc-price" class="form-control form-control-lg rounded-3 fs-4" readonly>
      </div>
      <div class="mb-3">
        <label class="form-label fw-bold">Hình ảnh URL</label>
        <input type="text" name="imageUrl" class="form-control form-control-lg rounded-3 fs-4">
      </div>
    `,
  },
  computerItem: {
    action: "/admin/product/item/save",
    title: "Thêm linh kiện mới",
    html: `
      <div class="mb-3">
        <label class="form-label fw-bold">Tên linh kiện</label>
        <input type="text" name="name" class="form-control form-control-lg rounded-3 fs-4" required>
      </div>
      <div class="mb-3">
        <label class="form-label fw-bold">Giá</label>
        <input type="number" name="price" class="form-control form-control-lg rounded-3 fs-4" required>
      </div>
      <div class="mb-3">
        <label class="form-label fw-bold">Loại linh kiện</label>
        <select name="category.id" class="form-select form-select-lg rounded-3 fs-4" required id="create-category">
          <option value="" disabled selected>-- Chọn loại --</option>
        </select>
      </div>
      <div class="mb-3">
        <label class="form-label fw-bold">Brand</label>
        <input type="text" name="brand" class="form-control form-control-lg rounded-3 fs-4">
      </div>
      <div class="mb-3">
        <label class="form-label fw-bold">Model</label>
        <input type="text" name="model" class="form-control form-control-lg rounded-3 fs-4">
      </div>
      <div class="mb-3">
        <label class="form-label fw-bold">Mô tả</label>
        <textarea name="description" class="form-control form-control-lg rounded-3 fs-4" rows="3"></textarea>
      </div>
      <div class="mb-3">
        <label class="form-label fw-bold">Hình ảnh URL</label>
        <input type="text" name="imageUrl" class="form-control form-control-lg rounded-3 fs-4">
      </div>
    `,
  },
};

function openCreateProductModal(type) {
  const template = formTemplates[type];
  if (!template) return;

  const form = document.getElementById("create-product-form");
  form.setAttribute("action", template.action);
  document.getElementById("modalCreateProductTitle").textContent =
    template.title;
  document.getElementById("modalCreateProductBody").innerHTML = template.html;

  const catSelect = document.getElementById("create-category");

  if (catSelect && window.categories) {
    catSelect.innerHTML = `
      <option value="" disabled selected>-- Chọn loại --</option>
      ${window.categories
        .map((c) => `<option value="${c.id}">${c.name}</option>`)
        .join("")}
    `;
  }

  if (type === "pc") {
    const itemSelect = document.getElementById("pc-computer-items");
    const priceInput = document.getElementById("pc-price");
    const priceDisplay = document.getElementById("pc-total-price");
    const selectedList = document.getElementById("pc-selected-list");

    const allItemsMap = new Map(); // Mọi item từng load
    const selectedIds = new Set(); // Các ID được chọn

    // Khi đổi category
    catSelect.addEventListener("change", () => {
      const selectedCategoryId = catSelect.value;
      const category = window.categories.find(
        (c) => String(c.id) === selectedCategoryId
      );
      const newItems = category?.computerItems || [];

      // Lưu vào map
      newItems.forEach((ci) => allItemsMap.set(String(ci.id), ci));

      // Hiển thị lại select với chỉ item thuộc category
      itemSelect.innerHTML = newItems
        .map((ci) => {
          const selected = selectedIds.has(String(ci.id)) ? "selected" : "";
          return `<option value="${ci.id}" data-price="${ci.price}" ${selected}>
                  ${ci.name} (${Number(ci.price).toLocaleString("vi-VN")} VND)
                </option>`;
        })
        .join("");

      // Cập nhật UI
      updateSelectedDisplay(
        selectedIds,
        allItemsMap,
        priceInput,
        priceDisplay,
        selectedList
      );
    });

    // Khi chọn item từ select
    itemSelect.addEventListener("change", () => {
      Array.from(itemSelect.options).forEach((opt) => {
        if (opt.selected) selectedIds.add(opt.value);
        else selectedIds.delete(opt.value);
      });
      updateSelectedDisplay(
        selectedIds,
        allItemsMap,
        priceInput,
        priceDisplay,
        selectedList
      );
    });
  }

  $("#modalCreateProduct").modal("show");
}

// ✅ Cập nhật danh sách và tổng giá
function updateSelectedDisplay(
  selectedIds,
  allItemsMap,
  priceInput,
  priceDisplay,
  selectedList
) {
  let total = 0;
  const selectedHTML = [];
  const hiddenInputs = [];

  selectedIds.forEach((id) => {
    const item = allItemsMap.get(id);
    if (item) {
      total += parseFloat(item.price || 0);
      selectedHTML.push(
        `<li class="list-group-item py-1">${item.name} (${Number(
          item.price
        ).toLocaleString("vi-VN")} VND)</li>`
      );
      hiddenInputs.push(
        `<input type="hidden" name="computerItems" value="${id}" />`
      );
    }
  });

  priceInput.value = total;
  priceDisplay.textContent = total.toLocaleString("vi-VN");
  selectedList.innerHTML = selectedHTML.join("");

  // Thêm hidden input để submit
  const hiddenContainer = document.getElementById("pc-selected-hidden-inputs");
  hiddenContainer.innerHTML = hiddenInputs.join("");
}

window.openCreateProductModal = openCreateProductModal;
