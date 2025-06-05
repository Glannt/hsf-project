package com.hsf.hsfproject.service.product;

import com.hsf.hsfproject.model.PC;
import org.springframework.data.domain.Page;

public interface IProductService {
    Page<PC> getPcList(int page, int limit);
}
