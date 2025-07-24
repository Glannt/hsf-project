package com.hsf.hsfproject.controller.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hsf.hsfproject.model.Transaction;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.transaction.ITransactionService;
import com.hsf.hsfproject.service.user.IUserService;

import lombok.RequiredArgsConstructor;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("transaction")
@RequiredArgsConstructor
public class TransactionController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final ITransactionService transactionService;
    private final IUserService userService;
    private void addUserInfo(Model model, Principal principal) {
        model.addAttribute("isLogin", principal != null);
        if (principal != null) {
            // Nếu bạn có userService, hãy inject và dùng như các controller khác
            User user = userService.findByUsername(principal.getName());
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("username", user.getUsername());
            }
        }
    }

    @GetMapping
    public String getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Principal principal
    ) {
        addUserInfo(model, principal);
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<Transaction> transactions = transactionService.getAllTransactions(pageable);

        logger.info("Fetched {} transactions, total elements: {}",
            transactions != null && transactions.getContent() != null ? transactions.getContent().size() : 0,
            transactions != null ? transactions.getTotalElements() : 0);
        if (transactions != null && transactions.getContent() != null) {
            for (Transaction t : transactions.getContent()) {
                if (t != null) {
                    logger.info("Transaction ID: {}, Status: {}, Order: {}", 
                        t.getId(), 
                        t.getStatus(), 
                        t.getOrder() != null ? t.getOrder().getId() : null);
                }
            }
        }

        // Calculate counts for each status
        int successCount = 0;
        int failedCount = 0;
        int pendingCount = 0;
        if (transactions != null && transactions.getContent() != null) {
            for (Transaction t : transactions.getContent()) {
                if (t != null && t.getStatus() != null) {
                    if ("SUCCESS".equalsIgnoreCase(t.getStatus())) successCount++;
                    else if ("FAILED".equalsIgnoreCase(t.getStatus())) failedCount++;
                    else if ("PENDING".equalsIgnoreCase(t.getStatus())) pendingCount++;
                }
            }
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("totalElements", transactions != null ? transactions.getTotalElements() : 0);
        model.addAttribute("totalPages", transactions != null ? transactions.getTotalPages() : 0);
        model.addAttribute("currentPage", page);
        model.addAttribute("successCount", successCount);
        model.addAttribute("failedCount", failedCount);
        model.addAttribute("pendingCount", pendingCount);

        return "transaction/index";
    }
    @GetMapping("/installments")
    public String getInstallmentTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Principal principal
    ) {
        addUserInfo(model, principal);
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<Transaction> transactions = transactionService.getInstallmentTransactions(pageable);

        model.addAttribute("transactions", transactions);
        model.addAttribute("totalElements", transactions.getTotalElements());
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("currentPage", page);

        return "transaction/index";
    }
    
}
