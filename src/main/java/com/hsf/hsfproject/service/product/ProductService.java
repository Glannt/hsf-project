package com.hsf.hsfproject.service.product;

import com.hsf.hsfproject.dtos.request.CreatePCRequest;
import com.hsf.hsfproject.model.Category;
import com.hsf.hsfproject.model.ComputerItem;
import com.hsf.hsfproject.model.PC;
import com.hsf.hsfproject.repository.CategoryRepository;
import com.hsf.hsfproject.repository.ComputerItemRepository;
import com.hsf.hsfproject.repository.ImageRepository;
import com.hsf.hsfproject.repository.OrderDetailRepository;
import com.hsf.hsfproject.repository.OrderRepository;
import com.hsf.hsfproject.repository.PCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService implements IProductService {

    private final PCRepository pcRepository;
    private final ComputerItemRepository computerItemRepository;
    private final ImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    // This method should be implemented to return a paginated list of PCs
    @Override
    public Page<PC> getPcList(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<PC> result = pcRepository.findAll(pageable);
        return result;
    }

    @Override
    public Page<ComputerItem> getComputerItemList(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<ComputerItem> result = computerItemRepository.findAll(pageable);
        return result;
    }

    @Override
    public PC addPc(PC request) {
        PC existingPc = pcRepository.findByName(request.getName());
        if (existingPc != null) {
            throw new IllegalArgumentException("PC with name " + request.getName() + " already exists");
        }
        PC newPc = PC.builder()
                .name(request.getName())
                .description(request.getDescription())
                .computerItems(request.getComputerItems())
                .price(request.getPrice())
                .build();
        pcRepository.save(newPc);
        return newPc;
    }

    @Override
    public PC updatePc(PC request) {
        PC existingPc = pcRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("PC not found"));
        
        // Kiểm tra tên PC đã tồn tại (trừ PC hiện tại)
        PC pcWithSameName = pcRepository.findByName(request.getName());
        if (pcWithSameName != null && !pcWithSameName.getId().equals(request.getId())) {
            throw new IllegalArgumentException("PC with name " + request.getName() + " already exists");
        }
        
        // Cập nhật thông tin
        existingPc.setName(request.getName());
        if (request.getDescription() != null) {
            existingPc.setDescription(request.getDescription());
        }
        if (request.getComputerItems() != null && !request.getComputerItems().isEmpty()) {
            existingPc.setComputerItems(request.getComputerItems());
        }
        // Cho phép cập nhật giá trực tiếp
        if (request.getPrice() != null) {
            existingPc.setPrice(request.getPrice());
        }
        
        pcRepository.save(existingPc);
        return existingPc;
    }

    @Override
    public ComputerItem addComputerItem(ComputerItem computerItem) {
        ComputerItem existingItem = computerItemRepository.findByName(computerItem.getName());
        if (existingItem != null) {
            throw new IllegalArgumentException("Computer item with name " + computerItem.getName() + " already exists");
        }
        if (computerItem.getCategory() != null) {
            Category category = categoryRepository.findById(computerItem.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            computerItem.setCategory(category);
        }
        computerItemRepository.save(computerItem);
        return computerItem;
    }

    @Override
    public Object getProduct(UUID id) {
        PC pc = pcRepository.findById(id).orElse(null);
        if (pc != null) {
            return pc;
        }

        ComputerItem computerItem = computerItemRepository.findById(id).orElse(null);
        if (computerItem != null) {
            return computerItem;
        }
        return null;
    }

    @Override
    public PC findPcById(UUID id) {
        return pcRepository.findById(id).orElseThrow();
    }

    @Override
    public ComputerItem findItemById(UUID id) {

        return computerItemRepository.findById(id).orElseThrow();
    }

    @Override
    public List<ComputerItem> getComputerItems() {

        return computerItemRepository.findAll();
    }

    @Override
    public ComputerItem updateComputerItem(ComputerItem computerItem) {
        ComputerItem existingItem = computerItemRepository.findById(computerItem.getId())
                .orElseThrow(() -> new IllegalArgumentException("Computer item not found"));
        
        // Kiểm tra tên sản phẩm đã tồn tại (trừ sản phẩm hiện tại)
        ComputerItem itemWithSameName = computerItemRepository.findByName(computerItem.getName());
        if (itemWithSameName != null && !itemWithSameName.getId().equals(computerItem.getId())) {
            throw new IllegalArgumentException("Computer item with name " + computerItem.getName() + " already exists");
        }
        
        // Cập nhật thông tin
        existingItem.setName(computerItem.getName());
        if (computerItem.getPrice() != null) {
            existingItem.setPrice(computerItem.getPrice());
        }
        if (computerItem.getDescription() != null) {
            existingItem.setDescription(computerItem.getDescription());
        }
        if (computerItem.getBrand() != null) {
            existingItem.setBrand(computerItem.getBrand());
        }
        if (computerItem.getModel() != null) {
            existingItem.setModel(computerItem.getModel());
        }
        if (computerItem.getCategory() != null) {
            Category category = categoryRepository.findById(computerItem.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            existingItem.setCategory(category);
        }
        
        computerItemRepository.save(existingItem);
        return existingItem;
    }

    @Override
    public ComputerItem deleteComputerItem(UUID id) {
        log.info("=== Service: Starting Computer Item deletion ===");
        log.info("Service: Deleting computer item with ID: {}", id);
        
        ComputerItem existingItem = computerItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Computer item not found"));
        
        log.info("Service: Found item to delete: {}", existingItem.getName());
        log.info("Service: Item details - ID: {}, Name: {}, Price: {}", 
                existingItem.getId(), existingItem.getName(), existingItem.getPrice());
        
        computerItemRepository.delete(existingItem);
        log.info("Service: Successfully deleted computer item from repository");
        
        return null;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public PC deletePc(UUID id) {
        log.info("=== Service: Starting PC deletion ===");
        log.info("Service: Deleting PC with ID: {}", id);
        // Xóa order_detail liên quan trước
        orderDetailRepository.deleteByPcId(id);
        PC existingPc = pcRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PC not found"));
        log.info("Service: Found PC to delete: {}", existingPc.getName());
        log.info("Service: PC details - ID: {}, Name: {}, Price: {}", 
                existingPc.getId(), existingPc.getName(), existingPc.getPrice());
        pcRepository.delete(existingPc);
        log.info("Service: Successfully deleted PC from repository");
        return null;
    }

    // Dashboard methods
    @Override
    public long countOrders() {
        // Đếm tổng số đơn hàng
        return orderRepository.count();
    }

    @Override
    public Double sumOrderRevenue() {
        // Tổng doanh thu
        try {
            Double sum = orderRepository.sumTotalRevenue();
            return sum != null ? sum : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public long countAllProducts() {
        // Tổng số sản phẩm = tổng PC + tổng linh kiện
        long pcCount = pcRepository.count();
        long itemCount = computerItemRepository.count();
        return pcCount + itemCount;
    }

//    @Override
//    public List<Object> getProductsByCategorySuggest(String category) {
//        List<ComputerItem> computerItems = computerItemRepository.findByCategory(category);
//        if (!computerItems.isEmpty()) {
//            return List.copyOf(computerItems);
//        }
//        return List.of();
//    }
//
//    public Category getCategoryByName(String categoryName) {
//        Category existingCategory = categoryRepository.findByName(categoryName);
//        if( existingCategory == null) {
//            throw new IllegalArgumentException("Category with name " + categoryName + " does not exist");
//        }
//        return existingCategory;
//
//    }
}
