package com.example.confirmationletter.record.domain;

import com.example.confirmationletter.record.service.impl.Constants;

import java.math.BigDecimal;

public class TempRecord {
  private String sign;
  private Integer currencyCode;
  private BigDecimal amount;

  public String getSign() {
    return sign;
  }

  public void setSign(String sign) {
    this.sign = sign;
  }

  public Integer getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencycode(Integer currencyCode) {
    this.currencyCode = currencyCode;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public boolean isDebitRecord() {
    return this.getSign().equalsIgnoreCase(Constants.DEBIT);
  }
  public boolean isCreditRecord() {
    return this.getSign().equalsIgnoreCase(Constants.CREDIT);
  }
}
