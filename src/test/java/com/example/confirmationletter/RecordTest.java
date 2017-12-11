package com.example.confirmationletter;

import com.example.confirmationletter.domain.Record;
import mockit.Tested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecordTest {

  @Tested Record record;

  @Test
  public void testIsCounterTranferRecordTrue() {
    Record record = new Record();
    record.setIsCounterTransferRecord(Integer.valueOf(1));
    assertTrue(record.isCounterTransferRecord());
  }

  @Test
  public void testIsCounterTranferRecordNotTrue() {
    Record record = new Record();
    record.setIsCounterTransferRecord(Integer.valueOf(0));
    assertFalse(record.isCounterTransferRecord());
  }

  @Test
  public void testhasFeeTrue() {
    Record record = new Record();
    record.setFeeRecord(Integer.valueOf(1));
    assertTrue(record.hasFee());
  }

  @Test
  public void testhasFeeFalse() {
    Record record = new Record();
    record.setFeeRecord(Integer.valueOf(0));
    assertFalse(record.hasFee());
  }

}
