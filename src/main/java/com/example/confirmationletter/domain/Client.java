package com.example.confirmationletter.domain;

public class Client {
  private String creditDebit;
  private Object profile;
  private String counterTransfer;
  private Integer amountDivider;

  public String getCreditDebit() {
    return creditDebit;
  }

  public void setCreditDebit(String creditDebit) {
    this.creditDebit = creditDebit;
  }

  public Object getProfile() {
    return profile;
  }

  public void setProfile(Object profile) {
    this.profile = profile;
  }

  public String getCounterTransfer() {
    return counterTransfer;
  }

  public void setCounterTransfer(String counterTransfer) {
    this.counterTransfer = counterTransfer;
  }

  public Integer getAmountDivider() {
    return amountDivider;
  }

  public void setAmountDivider(Integer amountDivider) {
    this.amountDivider = amountDivider;
  }
}
