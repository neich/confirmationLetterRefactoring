package com.example.confirmationletter.record.domain;

import com.example.confirmationletter.record.service.impl.Constants;

import java.math.BigDecimal;

public class TempRecord {
  private String sign;
  private Integer currencycode;
  private BigDecimal amount;

  public String getSign() {
    return sign;
  }

  public void setSign(String sign) {
    this.sign = sign;
  }

  public Integer getCurrencycode() {
    return currencycode;
  }

  public boolean isCurrencycode() {
    return true;
  }

  public void setCurrencycode(Integer currencycode) {
    this.currencycode = currencycode;
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
