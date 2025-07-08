package com.hsf.hsfproject.service.product;

import com.hsf.hsfproject.model.Category;
import com.hsf.hsfproject.model.ComputerItem;
import com.hsf.hsfproject.model.PC;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface IProductService {
    Page<PC> getPcList(int page, int limit);
    Page<ComputerItem> getComputerItemList(int page, int limit);
    PC addPc(PC pc);
    ComputerItem addComputerItem(ComputerItem computerItem);
    Object getProduct(UUID id);
    PC findPcById(UUID id);
    ComputerItem findItemById(UUID id);
//    List<Object> getProductsByCategorySuggest(String category);
    List<ComputerItem> getComputerItems();
    ComputerItem updateComputerItem(ComputerItem computerItem);
    ComputerItem deleteComputerItem(UUID id);
    List<Category> getAllCategories();
    PC deletePc(UUID id);
}
