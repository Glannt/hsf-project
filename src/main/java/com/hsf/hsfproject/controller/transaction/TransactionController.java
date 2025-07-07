package com.hsf.hsfproject.controller.transaction;

import com.hsf.hsfproject.model.Transaction;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.repository.TransactionRepository;
import com.hsf.hsfproject.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @GetMapping("/transactions")
    public String viewTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        if (authentication != null && authentication.isAuthenticated()) {
            User currentUser = userService.findByUsername(authentication.getName());

            Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
            Page<Transaction> transactions = transactionRepository.findByOrderUserId(currentUser.getId(), pageable);

            model.addAttribute("transactions", transactions);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", transactions.getTotalPages());
            model.addAttribute("totalElements", transactions.getTotalElements());
            model.addAttribute("user", currentUser);
            model.addAttribute("isLogin", true);

            return "transaction/history";
        }

        model.addAttribute("isLogin", false);

        return "redirect:/login";
    }

    @GetMapping("/transaction/history")
    public String viewTransactionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        if (authentication != null && authentication.isAuthenticated()) {
            User currentUser = userService.findByUsername(authentication.getName());

            Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
            Page<Transaction> transactions = transactionRepository.findByOrderUserId(currentUser.getId(), pageable);

            model.addAttribute("transactions", transactions);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", transactions.getTotalPages());
            model.addAttribute("totalElements", transactions.getTotalElements());
            model.addAttribute("user", currentUser);
            model.addAttribute("isLogin", true);

            return "transaction/history";
        }

        model.addAttribute("isLogin", false);
        return "redirect:/login";
    }

    @GetMapping("/admin/transactions")
    public String viewAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<Transaction> transactions = transactionRepository.findAll(pageable);

        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalElements", transactions.getTotalElements());

        return "admin/transactions";
    }
}