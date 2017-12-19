package com.example.confirmationletter.domain;

import java.math.BigDecimal;

public interface GenericRecord {

  boolean isCreditRecord();
  boolean isDebitRecord();
  public boolean isCounterTransferRecord();
  public boolean hasFee();
  public BigDecimal getAmountAsBigDecimal();
  public Integer getCurrencyNumericCode();
  public void setCurrencyNumericCode(Integer code);
  public String getSign();
  public void setSign(String sign);
}