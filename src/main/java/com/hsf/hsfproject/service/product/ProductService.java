package com.hsf.hsfproject.service.product;

import com.hsf.hsfproject.dtos.request.CreatePCRequest;
import com.hsf.hsfproject.model.Category;
import com.hsf.hsfproject.model.ComputerItem;
import com.hsf.hsfproject.model.PC;
import com.hsf.hsfproject.repository.CategoryRepository;
import com.hsf.hsfproject.repository.ComputerItemRepository;
import com.hsf.hsfproject.repository.ImageRepository;
import com.hsf.hsfproject.repository.PCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final PCRepository pcRepository;
    private final ComputerItemRepository computerItemRepository;
    private final ImageRepository imageRepository;
    private final CategoryRepository categoryRepository;

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
        Double price = request.getComputerItems().stream()
                .map(ComputerItem::getPrice)
                .reduce(0.0, Double::sum);
        PC newPc = PC.builder()
                .name(request.getName())
                .description(request.getDescription())
                .computerItems(request.getComputerItems())
                .price(price)
                .build();
        pcRepository.save(newPc);
        return newPc;
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
        if( existingItem.getName() != null && !existingItem.getName().equals(computerItem.getName())) {
            throw new IllegalArgumentException("Computer item with name " + computerItem.getName() + " already exists");
        }
        existingItem.setName(computerItem.getName());
        if( computerItem.getPrice() != null) {
            existingItem.setPrice(computerItem.getPrice());
        }
        if( computerItem.getDescription() != null) {
            existingItem.setDescription(computerItem.getDescription());
        }
        if( computerItem.getBrand() != null) {
            existingItem.setBrand(computerItem.getBrand());
        }
        if( computerItem.getModel() != null) {
            existingItem.setModel(computerItem.getModel());
        }
        if( computerItem.getCategory() != null) {
            Category category = categoryRepository.findById(computerItem.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            existingItem.setCategory(category);
        }
        computerItemRepository.save(existingItem);
        return computerItem;
    }

    @Override
    public ComputerItem deleteComputerItem(UUID id) {
        ComputerItem existingItem = computerItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Computer item not found"));
        computerItemRepository.delete(existingItem);
        return existingItem; // Return the deleted item for confirmation
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public PC deletePc(UUID id) {
        PC existingPc = pcRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PC not found"));
        pcRepository.delete(existingPc);
        return existingPc; // Return the deleted item for confirmation
    }

    // TODO: Future implementation for category-based product suggestions
    // public List<Object> getProductsByCategorySuggest(String category)
    
    // TODO: Future implementation for category lookup by name
    // public Category getCategoryByName(String categoryName)
}
