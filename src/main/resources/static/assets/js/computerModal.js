// function viewProductDetail(button) {
//     const id = button.getAttribute("data-id");
//     const item = window.computerItems.find(p => p.id === id);
//     console.log("Viewing product details for ID:", id, item);
//     if (!item) {
//         alert("Không tìm thấy sản phẩm!");
//         return;
//     }
//
//     document.getElementById("view-id").textContent = item.id;
//     document.getElementById("view-name").textContent = item.name;
//     document.getElementById("view-category").textContent = item.category?.name || "(Không rõ)";
//     document.getElementById("view-price").textContent = Number(item.price).toLocaleString();
//     document.getElementById("view-description").textContent = item.description || "(Không có mô tả)";
//     document.getElementById("view-brand").textContent = item.brand || "(Không rõ)";
//     document.getElementById("view-model").textContent = item.model || "(Không rõ)";
//
//     const imgHtml = item.images.imageUrl
//         ? `<img src="${item.imageUrl}" alt="Ảnh" class="img-fluid" />`
//         : `<small class="text-muted">Không có hình ảnh</small>`;
//     document.getElementById("view-image").innerHTML = imgHtml;
// }