package com.example.confirmationletter.record.domain;

public class TempRecord {
  private String sign;
  private String currencycode;
  private Integer amount;

  public String getSign() {
    return sign;
  }

  public void setSign(String sign) {
    this.sign = sign;
  }

  public String getCurrencycode() {
    return currencycode;
  }

  public boolean isCurrencycode() {
    return true;
  }

  public void setCurrencycode(String currencycode) {
    this.currencycode = currencycode;
  }

  public Integer getAmount() {
    return amount;
  }

  public void setAmount(Integer amount) {
    this.amount = amount;
  }
}
