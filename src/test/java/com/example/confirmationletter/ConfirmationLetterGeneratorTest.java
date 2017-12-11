package com.example.confirmationletter;

import com.example.confirmationletter.domain.BatchTotal;
import com.example.confirmationletter.domain.HashBatchRecordsBalance;
import com.example.confirmationletter.record.service.impl.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfirmationLetterGeneratorTest {

  HashBatchRecordsBalance recordsBalance;

  @BeforeEach
  void setup() {
    recordsBalance = new HashBatchRecordsBalance();
  }

  @Test
  public void testCreditBatchTotal_divider_one_mixed_credit_debit() {
    Map<Integer, BatchTotal> batchTotals = new HashMap<>();
    batchTotals.put(1, createBatchTotal(1, Constants.CREDIT));
    batchTotals.put(2, createBatchTotal(10, Constants.CREDIT));
    batchTotals.put(3, createBatchTotal(5, Constants.DEBIT));
    recordsBalance.setBatchTotals(batchTotals);

    assertEquals(new BigDecimal(11), recordsBalance.getBatchTotal(1, Constants.CREDIT));
  }

  private BatchTotal createBatchTotal(int number, String sign) {
    BatchTotal bt = new BatchTotal();
    bt.setTransactionSign(sign);
    if (Constants.CREDIT.equals(sign)) {
      bt.setCreditValue(new BigDecimal(number));
    } else if (Constants.DEBIT.equals(sign)) {
      bt.setDebitValue(new BigDecimal(number));
    }
    return bt;
  }
}
