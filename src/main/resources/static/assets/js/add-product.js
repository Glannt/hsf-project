const formTemplates = {
  pc: {
    action: "/admin/product/pc/save",
    title: "Thêm PC mới",
    html: `
      <div class="mb-3">
        <label class="form-label fw-bold">Tên sản phẩm</label>
        <input type="text" name="name" class="form-control form-control-lg rounded-3 fs-3" required>
      </div>
      <div class="mb-3">
        <label class="form-label fw-bold">Loại linh kiện</label>
        <select name="category.id" class="form-select form-select-lg rounded-3 fs-3" required id="create-category">
          <option value="" disabled selected>-- Chọn loại --</option>
        </select>
      </div>
      <div class="mb-3">
        <label for="pc-computer-items-list" class="form-label fw-bold mb-2">Chọn linh kiện</label>
        <div id="pc-computer-items-list" class="row g-2"></div>
      </div>
      <div class="mb-3">
        <label class="form-label fw-bold fs-3">Linh kiện đã chọn:</label>
        <ul id="pc-selected-list" class="list-group mb-2 fs-2"></ul>
        <div class="fs-1"><strong>Tổng giá:</strong> <span id="pc-total-price" class="text-primary">0</span> VND</div>
      </div>
      <div id="pc-selected-hidden-inputs"></div>

      <div class="mb-3">
        <label class="form-label fw-bold fs-3">Hình ảnh URL</label>
        <input type="text" name="imageUrl" class="form-control form-control-lg rounded-3 fs-3">
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
    const itemsListDiv = document.getElementById("pc-computer-items-list");
    const priceDisplay = document.getElementById("pc-total-price");
    const selectedList = document.getElementById("pc-selected-list");
    const allItemsMap = new Map();
    const selectedIds = new Set();

    catSelect.addEventListener("change", () => {
      const selectedCategoryId = catSelect.value;
      const category = window.categories.find(
        (c) => String(c.id) === selectedCategoryId
      );
      const newItems = category?.computerItems || [];
      allItemsMap.clear();
      newItems.forEach((ci) => allItemsMap.set(String(ci.id), ci));

      // Hiển thị danh sách checkbox
      itemsListDiv.innerHTML = newItems.length
        ? newItems
            .map(
              (ci, idx) => `
            <div class="col-12 col-md-6 col-lg-4">
              <div class="form-check">
                <input class="form-check-input" type="checkbox" id="ci-${
                  ci.id
                }" value="${ci.id}" data-price="${ci.price}">
                <label class="form-check-label" for="ci-${ci.id}">
                  ${ci.name} <span class="text-muted">(${Number(
                ci.price
              ).toLocaleString("vi-VN")} VND)</span>
                </label>
              </div>
            </div>
          `
            )
            .join("")
        : '<div class="text-muted">Không có linh kiện nào cho loại này.</div>';

      // Reset selected
      selectedIds.clear();
      updateSelectedDisplay(
        selectedIds,
        allItemsMap,
        priceDisplay,
        selectedList
      );

      // Gán sự kiện cho checkbox
      Array.from(
        itemsListDiv.querySelectorAll('input[type="checkbox"]')
      ).forEach((cb) => {
        cb.addEventListener("change", function () {
          if (this.checked) selectedIds.add(this.value);
          else selectedIds.delete(this.value);
          updateSelectedDisplay(
            selectedIds,
            allItemsMap,
            priceDisplay,
            selectedList
          );
        });
      });
    });
  }

  $("#modalCreateProduct").modal("show");
}

// ✅ Cập nhật danh sách và tổng giá
function updateSelectedDisplay(
  selectedIds,
  allItemsMap,
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

  priceDisplay.textContent = total.toLocaleString("vi-VN");
  selectedList.innerHTML = selectedHTML.join("");

  // Thêm hidden input để submit
  const hiddenContainer = document.getElementById("pc-selected-hidden-inputs");
  hiddenContainer.innerHTML = hiddenInputs.join("");
}

window.openCreateProductModal = openCreateProductModal;
