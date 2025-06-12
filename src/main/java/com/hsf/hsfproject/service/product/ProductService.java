package com.hsf.hsfproject.service.product;

import com.hsf.hsfproject.dtos.request.CreatePCRequest;
import com.hsf.hsfproject.model.ComputerItem;
import com.hsf.hsfproject.model.PC;
import com.hsf.hsfproject.repository.ComputerItemRepository;
import com.hsf.hsfproject.repository.PCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final PCRepository pcRepository;
    private final ComputerItemRepository computerItemRepository;
    // This method should be implemented to return a paginated list of PCs
    @Override
    public Page<PC> getPcList(int page, int limit) {
        System.out.println("Fetching PC list: page=" + page + ", limit=" + limit);
        Pageable pageable = PageRequest.of(page, limit);
        Page<PC> result = pcRepository.findAll(pageable);
        System.out.println("Fetched " + result.getNumberOfElements() + " PCs");
        return result;
    }

    @Override
    public Page<ComputerItem> getComputerItemList(int page, int limit) {
        System.out.println("Fetching computer item list: page=" + page + ", limit=" + limit);
        Pageable pageable = PageRequest.of(page, limit);
        Page<ComputerItem> result = computerItemRepository.findAll(pageable);
        System.out.println("Fetched " + result.getNumberOfElements() + " computer items");
        System.out.println("Total pages: " + result.getTotalPages());
        return result;
    }

    @Override
    public PC addPc(CreatePCRequest request) {
       PC existingPc = pcRepository.findByName(request.getName());
       if(existingPc != null) {
          throw new IllegalArgumentException("PC with name " + request.getName() + " already exists");
       }
         PC newPc = PC.builder()
                .name(request.getName())
                 .description(request.getDescription())
                .price(request.getTotalPrice())
                .build();
        return null;
    }

    @Override
    public ComputerItem addComputerItem(ComputerItem computerItem) {
        return null;
    }
}
