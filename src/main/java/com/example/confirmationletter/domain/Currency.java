package com.example.confirmationletter.domain;

public class Currency {
  private Object code;
  private String currencyType;

  public Object getCode() {
    return code;
  }

  public void setCode(Object code) {
    this.code = code;
  }

  public String getCurrencyType() {
    return currencyType;
  }

  public void setCurrencyType(String currencyType) {
    this.currencyType = currencyType;
  }
}
