// ==== Common Utility ====
function formatVND(value) {
  return Number(value).toLocaleString("vi-VN");
}

// ==== Binding giá hiển thị dạng số có định dạng VND ====
function initPriceInputBinding() {
  const priceDisplay = document.getElementById("edit-price-display");
  const priceHidden = document.getElementById("edit-price");

  if (!priceDisplay || !priceHidden) return;

  priceDisplay.addEventListener("input", function () {
    const raw = this.value.replace(/[^\d]/g, "");
    priceHidden.value = raw;
    this.value = raw ? formatVND(raw) : "";
  });

  priceDisplay.addEventListener("keypress", function (e) {
    if (!/[0-9]/.test(String.fromCharCode(e.which))) {
      e.preventDefault();
    }
  });
}

// ==== Cập nhật giao diện chọn linh kiện PC ====
function updateSelectedDisplayEdit(
  selectedIds,
  allItemsMap,
  initialItemsMap = null
) {
  const priceInput = document.getElementById("edit-price");
  const priceDisplay = document.getElementById("edit-pc-total-price");
  const selectedList = document.getElementById("edit-pc-selected-list");
  const hiddenInputsContainer = document.getElementById(
    "edit-pc-selected-hidden-inputs"
  );
  console.log(initialItemsMap);
  let total = 0;
  const selectedHTML = [];
  const hiddenInputs = [];

  selectedIds.forEach((id) => {
    // Ưu tiên lấy từ initialItemsMap nếu có (khi mở form lần đầu)
    const item = initialItemsMap?.get(id) || allItemsMap.get(id);
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

  if (priceInput) priceInput.value = total;
  if (priceDisplay) priceDisplay.textContent = total.toLocaleString("vi-VN");
  if (selectedList) selectedList.innerHTML = selectedHTML.join("");
  if (hiddenInputsContainer)
    hiddenInputsContainer.innerHTML = hiddenInputs.join("");
}

// ==== Binding cho form chỉnh sửa PC ====
function bindEditPcForm(item) {
  const catSelect = document.getElementById("edit-category-select");
  const itemSelect = document.getElementById("edit-computer-items-select");

  const allItemsMap = new Map();
  const selectedIds = new Set(
    (item.computerItems || []).map((ci) => String(ci.id))
  );
  const pcComputerItems = new Map();

  // Đổ các linh kiện đang có trong PC
  item.computerItems?.forEach((ci) => {
    pcComputerItems.set(String(ci.id), ci);
  });

  // Lưu toàn bộ linh kiện từ các category
  if (Array.isArray(window.categories)) {
    window.categories.forEach((cat) => {
      (cat.computerItems || []).forEach((ci) => {
        allItemsMap.set(String(ci.id), ci);
      });
    });
  } else {
    console.warn('window.categories is not an array:', window.categories);
  }

  function updateComputerItemsByCategory(categoryId) {
    const category = window.categories.find(
      (c) => String(c.id) === String(categoryId)
    );
    const newItems = category?.computerItems || [];

    const newItemMap = new Map();
    newItems.forEach((ci) => newItemMap.set(String(ci.id), ci));

    // Cập nhật select options
    itemSelect.innerHTML = newItems
      .map((ci) => {
        const isSelected = selectedIds.has(String(ci.id)) ? "selected" : "";
        return `<option value="${ci.id}" data-price="${ci.price}" ${isSelected}>
                ${ci.name} (${Number(ci.price).toLocaleString("vi-VN")} VND)
            </option>`;
      })
      .join("");

    // Bỏ chọn những ID không còn thuộc category mới
    selectedIds.forEach((id) => {
      if (!newItemMap.has(id)) {
        selectedIds.delete(id);
      }
    });

    updateSelectedDisplayEdit(selectedIds, newItemMap);
  }

  // Gán option cho category
  if (catSelect && window.categories) {
    catSelect.innerHTML = `
            <option value="" disabled>-- Chọn loại --</option>
            ${window.categories
              .map(
                (c) =>
                  `<option value="${c.id}" ${
                    item.category?.id === c.id ? "selected" : ""
                  }>${c.name}</option>`
              )
              .join("")}
        `;
  }

  // Bind change category
  catSelect.addEventListener("change", function () {
    updateComputerItemsByCategory(this.value);
  });

  // Bind chọn linh kiện
  itemSelect.addEventListener("change", function () {
    Array.from(this.options).forEach((opt) => {
      if (opt.selected) selectedIds.add(opt.value);
      else selectedIds.delete(opt.value);
    });
    updateSelectedDisplayEdit(selectedIds, allItemsMap);
  });

  // Load lần đầu khi mở modal
  updateComputerItemsByCategory(item.category?.id);

  // Sau khi render xong options, đánh dấu các option đã được chọn từ đầu
  Array.from(itemSelect.options).forEach((opt) => {
    if (selectedIds.has(opt.value)) {
      opt.selected = true;
    }
  });

  // Gọi hàm hiển thị danh sách đã chọn lúc đầu
  updateSelectedDisplayEdit(selectedIds, allItemsMap, pcComputerItems);
}

// ==== Modal Template ====
const formEditTemplates = {
  editPc: {
    title: "Chỉnh sửa PC",
    html: `
        <input type="hidden" name="id" id="edit-id" />
        <div class="form-group">
            <label>Tên sản phẩm</label>
            <input type="text" name="name" class="form-control" id="edit-name" required>
        </div>
        <div class="form-group">
            <label>Loại linh kiện</label>
            <select name="category.id" class="form-control" required id="edit-category-select">
                <option value="" disabled selected>-- Chọn loại --</option>
            </select>
        </div>
        <div class="form-group">
            <label>Chọn linh kiện</label>
            <select multiple id="edit-computer-items-select" class="form-control" style="height: 200px;"></select>
            <small class="form-text text-muted">Giữ Ctrl hoặc Cmd để chọn nhiều.</small>
        </div>
        <div class="form-group">
            <label><strong>Linh kiện đã chọn:</strong></label>
            <ul id="edit-pc-selected-list" class="list-group small mb-2"></ul>
            <div><strong>Tổng giá:</strong> <span id="edit-pc-total-price" class="text-primary">0</span> VND</div>
        </div>
        <div id="edit-pc-selected-hidden-inputs"></div>
        <input type="hidden" name="price" id="edit-price" />
        <div class="form-group">
            <label>Hình ảnh URL</label>
            <input type="text" name="imageUrl" class="form-control" id="edit-imageUrl" />
        </div>
        `,
  },
  editItem: {
    title: "Chỉnh sửa linh kiện",
    html: `
        <input type="hidden" name="id" id="edit-id" />
        <div class="form-group">
            <label>Tên linh kiện</label>
            <input type="text" name="name" class="form-control" id="edit-name" required>
        </div>
        <div class="form-group">
            <label>Giá</label>
            <input type="text" class="form-control" id="edit-price-display" />
            <input type="hidden" name="price" id="edit-price" />
        </div>
        <div class="form-group">
            <label>Loại linh kiện</label>
            <select class="form-control" name="category.id" id="edit-category-select" required>
                <option value="" disabled>-- Chọn loại --</option>
            </select>
        </div>
        <div class="form-group">
            <label>Brand</label>
            <input type="text" class="form-control" name="brand" id="edit-brand" />
        </div>
        <div class="form-group">
            <label>Model</label>
            <input type="text" class="form-control" name="model" id="edit-model" />
        </div>
        <div class="form-group">
            <label>Mô tả</label>
            <textarea name="description" class="form-control" rows="3" id="edit-description"></textarea>
        </div>
        <div class="form-group">
            <label>Hình ảnh URL</label>
            <input type="text" name="imageUrl" class="form-control" id="edit-imageUrl" />
        </div>
        `,
  },
};

// ==== Mở Modal Edit ====
function openEditModalFromList(button, type = "computerItem") {
  const id = button.getAttribute("data-id");
  console.log("Opening edit modal for ID:", id, "Type:", type);
  const list = type === "pc" ? window.pcs : window.computerItems;
  const item = list.find((p) => String(p.id) === id);
  if (!item) return alert("Không tìm thấy sản phẩm.");

  const form = document.getElementById("edit-product-form");
  form.action =
    type === "pc"
      ? `/admin/product/pc/edit/${item.id}`
      : `/admin/product/item/edit/${item.id}`;

  const template = formEditTemplates[type === "pc" ? "editPc" : "editItem"];
  document.getElementById("edit-modal-title").textContent = template.title;
  document.getElementById("modalEditProductBody").innerHTML = template.html;

  // Bind dữ liệu chung
  document.getElementById("edit-id").value = item.id || "";
  document.getElementById("edit-name").value = item.name || "";
  document.getElementById("edit-imageUrl").value = item.imageUrl || "";
  const price = item.price || 0;
  const priceInput = document.getElementById("edit-price");
  const priceDisplay = document.getElementById("edit-price-display");
  if (priceInput) priceInput.value = price;
  if (priceDisplay) priceDisplay.value = formatVND(price);

  if (type === "pc") {
    bindEditPcForm(item);
  } else {
    document.getElementById("edit-description").value = item.description || "";
    document.getElementById("edit-brand").value = item.brand || "";
    document.getElementById("edit-model").value = item.model || "";
    // Bind select category
    const catSelect = document.getElementById("edit-category-select");
    if (catSelect && window.categories) {
      catSelect.innerHTML = `
                <option value="" disabled>-- Chọn loại --</option>
                ${window.categories
                  .map(
                    (c) =>
                      `<option value="${c.id}" ${
                        item.category?.id === c.id ? "selected" : ""
                      }>${c.name}</option>`
                  )
                  .join("")}
            `;
    }
  }

  $("#modalEditProduct").modal("show");
}

// ==== On Load ====
document.addEventListener("DOMContentLoaded", initPriceInputBinding);
