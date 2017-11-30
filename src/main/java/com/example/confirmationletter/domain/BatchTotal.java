package com.example.confirmationletter.domain;

import java.math.BigDecimal;

public class BatchTotal {
  private BigDecimal creditValue;
  private BigDecimal creditCounterValueForDebit;

  public BigDecimal getCreditValue() {
    return creditValue;
  }

  public void setCreditValue(BigDecimal creditValue) {
    this.creditValue = creditValue;
  }

  public BigDecimal getCreditCounterValueForDebit() {
    return creditCounterValueForDebit;
  }

  public void setCreditCounterValueForDebit(BigDecimal creditCounterValueForDebit) {
    this.creditCounterValueForDebit = creditCounterValueForDebit;
  }
}
