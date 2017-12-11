package com.example.confirmationletter.domain;

import com.example.confirmationletter.record.service.impl.Constants;

import java.math.BigDecimal;

public class Record {
  private Currency currency;
  private Integer feeRecord;
  private String sign;
  private BigDecimal amount;
  private Integer isCounterTransferRecord;
  private String beneficiaryName;
  private Bank bank;
  private String beneficiaryAccountNumber;

  public Currency getCurrency() {
    return currency;
  }

  public Integer getFeeRecord() {
    return feeRecord;
  }

  public void setFeeRecord(Integer feeRecord) {
    this.feeRecord = feeRecord;
  }

  public String getSign() {
    return sign;
  }

  public void setSign(String sign) {
    this.sign = sign;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public Integer getIsCounterTransferRecord() {
    return isCounterTransferRecord;
  }

  public void setIsCounterTransferRecord(Integer isCounterTransferRecord) {
    this.isCounterTransferRecord = isCounterTransferRecord;
  }

  public String getBeneficiaryName() {
    return beneficiaryName;
  }

  public void setBeneficiaryName(String beneficiaryName) {
    this.beneficiaryName = beneficiaryName;
  }

  public Bank getBank() {
    return bank;
  }

  public void setBank(Bank bank) {
    this.bank = bank;
  }

  public String getBeneficiaryAccountNumber() {
    return beneficiaryAccountNumber;
  }

  public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) {
    this.beneficiaryAccountNumber = beneficiaryAccountNumber;
  }

  public boolean hasFlCurrency() {
    return getCurrency().getCode().equals(
        Constants.FL_CURRENCY_CODE) ||
        getCurrency().getCode().equals(
            Constants.FL_CURRENCY_CODE_FOR_WEIRD_BANK);
  }

  public boolean hasEurCurrency() {
    return getCurrency().getCode().equals(
        Constants.EUR_CURRENCY_CODE);
  }
}
