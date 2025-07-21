package com.hsf.hsfproject.controller.admin;

import com.hsf.hsfproject.dtos.request.CreatePCRequest;
import com.hsf.hsfproject.model.Category;
import com.hsf.hsfproject.model.ComputerItem;
import com.hsf.hsfproject.model.Order;
import com.hsf.hsfproject.model.PC;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.order.IOrderService;
import com.hsf.hsfproject.service.product.IProductService;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.hsf.hsfproject.model.InstallmentPlan;
import com.hsf.hsfproject.model.InstalllmentType;
import com.hsf.hsfproject.repository.InstallmentPlanRepository;
import com.hsf.hsfproject.repository.InstallmentRepository;
import com.hsf.hsfproject.repository.InstalllmentTypeRepository;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IUserService userService;
    private final IProductService productService;
    private final IOrderService orderService;
    private final InstallmentPlanRepository installmentPlanRepository;
    private final InstallmentRepository installmentRepository;
    private final InstalllmentTypeRepository installmentTypeRepository;
    public void addUserToModel(Model model, Principal principal) {
        if (principal != null && !model.containsAttribute("user")) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            model.addAttribute("isLogin", true);
        } else {
            model.addAttribute("isLogin", false);
        }
    }

    @GetMapping
    public String adminDashboard(Model model, Principal principal){
        addUserToModel(model, principal);

        return "admin/index";
    }


    @GetMapping("/product")
    public String adminProduct(Model model,
                                 @RequestParam(name = "pcPage", defaultValue = "0") int pcPage,
                                 @RequestParam(name = "computerItemPage", defaultValue = "0") int computerItemPage,
                                 Principal principal) {
        addUserToModel(model,principal);
        List<Category> categories = productService.getAllCategories();
        Page<PC> pcList = productService.getPcList(pcPage, 2);
        Page<ComputerItem> computerItems = productService.getComputerItemList(computerItemPage, 2);
        model.addAttribute("pcs", pcList);
        model.addAttribute("computerItems", computerItems);
        model.addAttribute("categories", categories);
        model.addAttribute("computerItemsSelect", productService.getComputerItems());
        return "admin/product";
    }

//    Computer Item
    @PostMapping("/product/item/save")
    public String addComputerItem(@ModelAttribute ComputerItem item,Model model) {
        System.out.println("Adding item: " + item.getName());
        ComputerItem newItem = productService.addComputerItem(item);
        model.addAttribute("message", "Item added successfully: " + newItem.getName());
        return "redirect:/admin/product";
    }

    @PostMapping("/product/item/edit/{id}")
    public String editPcForm(@ModelAttribute ComputerItem item, Model model) {
        System.out.println("Editing item: " + item.getName());
        ComputerItem updatedItem = productService.updateComputerItem(item);
        return "redirect:/admin/product";
    }

    @PostMapping("/product/item/delete/{id}")
    public String deleteComputerItem(@PathVariable UUID id) {
        productService.deleteComputerItem(id); // Assuming this method deletes the item
        return "redirect:/admin/product";
    }


//    PC
    @PostMapping("/product/pc/save")
    public String addNewPC(@ModelAttribute PC item, Model model) {
        System.out.println("Adding item: " + item.getComputerItems());
       for (ComputerItem computerItem : item.getComputerItems()) {
            System.out.println("Computer Item: " + computerItem.getName());
        }
        // Assuming productService.addPc() returns the newly created PC
        PC newItem = productService.addPc(item);
        model.addAttribute("message", "Item added successfully: " + newItem.getName());
        return "redirect:/admin/product";
    }

    @PostMapping("/product/pc/delete/{id}")
    public String deletePC(@PathVariable UUID id) {
        productService.deletePc(id); // Assuming this method deletes the item
        return "redirect:/admin/product";
    }

    @GetMapping("/order")
    public String adminOrder(Model model,
                                 @RequestParam(name = "orderPage", defaultValue = "0") int orderPage,
                                 Principal principal) {
        addUserToModel(model,principal);
        Page<Order> orders = orderService.getOrderList(orderPage, 2);
        model.addAttribute("orders", orders);
        return "admin/order";
    }

    @PostMapping("/order/{orderId}")
    public String updateOrderStatus(
    @PathVariable UUID orderId,
    @RequestParam("status") String status
    ){
        orderService.updateOrderStatus(orderId ,status);
        return "redirect:/admin/order";
    }

    @GetMapping("/installments")
    public String viewInstallments(Model model, Principal principal){
        addUserToModel(model, principal);
        List<InstallmentPlan> plans = installmentPlanRepository.findAll();
        model.addAttribute("plans", plans);
        return "admin/installments";
    }

    @GetMapping("/installment-types")
    public String viewInstallmentTypes(Model model, Principal principal){
        addUserToModel(model, principal);
        List<InstalllmentType> types = installmentTypeRepository.findAll();
        model.addAttribute("types", types);
        return "admin/installment-type";
    }

    @PostMapping("/installment-type/update")
    public String updateInstallmentType(@RequestParam UUID id,
                                        @RequestParam int months,
                                        @RequestParam double rate){
        InstalllmentType type = installmentTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Type not found"));
        type.setMonths(months);
        type.setInterestRate(rate);
        installmentTypeRepository.save(type);
        return "redirect:/admin/installment-types";
    }
}
