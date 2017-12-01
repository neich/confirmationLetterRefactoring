package com.example.confirmationletter;

import com.example.confirmationletter.domain.BatchTotal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfirmationLetterGeneratorTest {

  @Test
  public void testCreditBatchTotal_divider_one_all_credit() {
    BatchTotal one = new BatchTotal();
    one.setCreditValue(BigDecimal.ONE);
    BatchTotal ten = new BatchTotal();
    ten.setCreditValue(BigDecimal.TEN);

    HashMap<Integer, BatchTotal> batchTotals = new HashMap<Integer, BatchTotal>();
    batchTotals.put(99, one);
    batchTotals.put(98, ten);

    ConfirmationLetterGenerator letterGenerator = new ConfirmationLetterGenerator();
    BigDecimal result = letterGenerator.creditBatchTotal(batchTotals, 1);
    assertEquals(new BigDecimal(11), result);
  }
}
