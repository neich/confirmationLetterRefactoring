package com.example.confirmationletter;

import com.example.confirmationletter.domain.BatchTotal;
import com.example.confirmationletter.record.service.impl.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfirmationLetterGeneratorTest {

  ConfirmationLetterGenerator letterGenerator;

  @BeforeEach
  void setup() {
    letterGenerator = new ConfirmationLetterGenerator();
  }

  @Test
  public void testCreditBatchTotal_divider_one_mixed_credit_debit() {
    Collection<BatchTotal> batchTotals = new ArrayList<>();
    batchTotals.add(createBatchTotal(1, Constants.CREDIT));
    batchTotals.add(createBatchTotal(10, Constants.CREDIT));
    batchTotals.add(createBatchTotal(5, Constants.DEBIT));

    assertEquals(new BigDecimal(11), letterGenerator.calculateTotalOverBatches(batchTotals, 1, Constants.CREDIT));
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
