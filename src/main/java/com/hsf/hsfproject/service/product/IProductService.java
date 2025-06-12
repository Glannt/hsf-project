package com.hsf.hsfproject.service.product;

import com.hsf.hsfproject.dtos.request.CreatePCRequest;
import com.hsf.hsfproject.model.ComputerItem;
import com.hsf.hsfproject.model.PC;
import org.springframework.data.domain.Page;

public interface IProductService {
    Page<PC> getPcList(int page, int limit);
    Page<ComputerItem> getComputerItemList(int page, int limit);
    PC addPc(CreatePCRequest pc);
    ComputerItem addComputerItem(ComputerItem computerItem);
}
