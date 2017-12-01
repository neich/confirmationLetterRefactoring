package com.example.confirmationletter.domain;

import java.math.BigDecimal;

public class BatchTotal {
  private BigDecimal creditValue = BigDecimal.ZERO;
  private BigDecimal creditCounterValueForDebit = BigDecimal.ZERO;
  private BigDecimal debitValue = BigDecimal.ZERO;
  private String transactionSign;

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

  public void setDebitValue(BigDecimal debitValue) {
    this.debitValue = debitValue;
  }

  public BigDecimal getDebitValue() {
    return debitValue;
  }

  public void setTransactionSign(String transactionSign) {
    this.transactionSign = transactionSign;
  }

  public String getTransactionSign() {
    return transactionSign;
  }
}
