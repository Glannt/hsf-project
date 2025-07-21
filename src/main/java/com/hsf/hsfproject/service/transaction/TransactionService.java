package com.hsf.hsfproject.service.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.hsf.hsfproject.model.Transaction;
import com.hsf.hsfproject.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {
    private final TransactionRepository transactionRepository;

    @Override
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    @Override
    public Page<Transaction> getInstallmentTransactions(Pageable pageable) {
        return transactionRepository.findByInstallmentIsNotNull(pageable);
    }
}
