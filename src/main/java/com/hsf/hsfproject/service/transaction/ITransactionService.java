package com.hsf.hsfproject.service.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hsf.hsfproject.model.Transaction;

public interface ITransactionService {
    //    Transaction createTransaction;
    Page<Transaction> getAllTransactions(Pageable pageable);

    Page<Transaction> getInstallmentTransactions(Pageable pageable);
}
