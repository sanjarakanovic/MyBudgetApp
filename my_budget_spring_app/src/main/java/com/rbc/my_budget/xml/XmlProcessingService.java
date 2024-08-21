package com.rbc.my_budget.xml;

import com.rbc.my_budget.account.Account;
import com.rbc.my_budget.account.AccountRepository;
import com.rbc.my_budget.transaction.Transaction;
import com.rbc.my_budget.transaction.TransactionRepository;
import com.rbc.my_budget.transaction.Type;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
@AllArgsConstructor
public class XmlProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(XmlProcessingService.class);
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;

    @Transactional
    public void processXml() {
        XmlMapper xmlMapper = new XmlMapper();

        try (InputStream is = getClass().getResourceAsStream("/my_budget_data.xml")) {
            List<Account> accounts = xmlMapper.readValue(is, xmlMapper.getTypeFactory().constructCollectionType(List.class, Account.class));

            for (Account account : accounts) {
                if(account.getTransactions() != null){
                    accountRepository.save(account);
                    for (Transaction transaction : account.getTransactions()) {
                        if (transaction.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                            transaction.setType(Type.DEBIT);
                            transaction.setAmount(transaction.getAmount().abs());
                        }
                        else
                            transaction.setType(Type.CREDIT);

                    transaction.setAccount(account);
                    transactionRepository.save(transaction);
                    }
                } else
                    accountRepository.save(account);
            }
        } catch (IOException e) {
            logger.error("Error processing XML file", e);
        }
    }

}
