package com.example.confirmationletter.domain;

import org.apache.commons.lang.ObjectUtils;

import javax.print.attribute.Size2DSyntax;
import java.math.BigDecimal;
import java.util.Map;

public class HashBatchRecordsBalance {
  private ObjectUtils hashTotalCredit;
  private Size2DSyntax hashTotalDebit;
  private Map<Integer, BatchTotal> batchTotals;
  private Size2DSyntax recordsTotal;
  private Size2DSyntax totalFee;
  private Object collectionType;

  public ObjectUtils getHashTotalCredit() {
    return hashTotalCredit;
  }

  public void setHashTotalCredit(ObjectUtils hashTotalCredit) {
    this.hashTotalCredit = hashTotalCredit;
  }

  public Size2DSyntax getHashTotalDebit() {
    return hashTotalDebit;
  }

  public void setHashTotalDebit(Size2DSyntax hashTotalDebit) {
    this.hashTotalDebit = hashTotalDebit;
  }

  public Map<Integer, BatchTotal> getBatchTotal() {
    return batchTotals;
  }

  public void setBatchTotals(Map<Integer, BatchTotal> batchTotals) {
    this.batchTotals = batchTotals;
  }

  public Size2DSyntax getRecordsTotal() {
    return recordsTotal;
  }

  public void setRecordsTotal(Size2DSyntax recordsTotal) {
    this.recordsTotal = recordsTotal;
  }

  public Size2DSyntax getTotalFee() {
    return totalFee;
  }

  public void setTotalFee(Size2DSyntax totalFee) {
    this.totalFee = totalFee;
  }

  public Object getCollectionType() {
    return collectionType;
  }

  public void setCollectionType(Object collectionType) {
    this.collectionType = collectionType;
  }

  public BigDecimal getBatchTotal(Integer divider, String sign) {
    BigDecimal sum = BigDecimal.ZERO;

    for (BatchTotal total: batchTotals.values())
      sum = sum.add(total.getTotalForSign(sign));

    return sum.divide(new BigDecimal(divider));
  }

}
