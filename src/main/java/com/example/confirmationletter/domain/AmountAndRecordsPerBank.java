package com.example.confirmationletter.domain;

import java.math.BigDecimal;

public class AmountAndRecordsPerBank {
  private String bankName;
  private int totalRecord;
  private BigDecimal amount;
  private String currencyType;
  private String accountNumber;

  public void setBankName(String bankName) {
    this.bankName = bankName;
  }

  public String getBankName() {
    return bankName;
  }

  public void setTotalRecord(int totalRecord) {
    this.totalRecord = totalRecord;
  }

  public int getTotalRecord() {
    return totalRecord;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setCurrencyType(String currencyType) {
    this.currencyType = currencyType;
  }

  public String getCurrencyType() {
    return currencyType;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public String getAccountNumber() {
    return accountNumber;
  }
}
