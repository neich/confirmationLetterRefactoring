package com.example.confirmationletter.record.domain;

public class TempRecord {
  private String sign;
  private Integer currencycode;
  private Integer amount;

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

  public Integer getAmount() {
    return amount;
  }

  public void setAmount(Integer amount) {
    this.amount = amount;
  }
}
