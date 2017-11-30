package com.example.confirmationletter.domain;

import com.example.confirmationletter.record.domain.FaultRecord;
import com.example.confirmationletter.record.parser.FileExtension;

import java.math.BigDecimal;
import java.util.List;

public class ConfirmationLetter {
  private Object currency;
  private FileExtension extension;
  private String hashTotalCredit;
  private String hashTotalDebit;
  private String batchTotalDebit;
  private String batchTotalCredit;
  private String totalProcessedRecords;
  private String transactionCost;
  private Object transferType;
  private List<AmountAndRecordsPerBank> banks;
  private List<FaultRecord> creditingErrors;
  private Client client;
  private String branchName;
  private BigDecimal retrievedAmountEur;
  private BigDecimal retrievedAmountFL;
  private BigDecimal retrievedAmountUsd;
  private Object totalRetrievedRecords;

  public void setCurrency(Object currency) {
    this.currency = currency;
  }

  public Object getCurrency() {
    return currency;
  }

  public void setExtension(FileExtension extension) {
    this.extension = extension;
  }

  public FileExtension getExtension() {
    return extension;
  }

  public void setHashTotalCredit(String hashTotalCredit) {
    this.hashTotalCredit = hashTotalCredit;
  }

  public void setHashTotalDebit(String hashTotalDebit) {
    this.hashTotalDebit = hashTotalDebit;
  }

  public void setBatchTotalDebit(String batchTotalDebit) {
    this.batchTotalDebit = batchTotalDebit;
  }

  public String getBatchTotalDebit() {
    return batchTotalDebit;
  }

  public void setBatchTotalCredit(String batchTotalCredit) {
    this.batchTotalCredit = batchTotalCredit;
  }

  public String getBatchTotalCredit() {
    return batchTotalCredit;
  }

  public void setTotalProcessedRecords(String totalProcessedRecords) {
    this.totalProcessedRecords = totalProcessedRecords;
  }

  public String getTotalProcessedRecords() {
    return totalProcessedRecords;
  }

  public void setTransactionCost(String transactionCost) {
    this.transactionCost = transactionCost;
  }

  public String getTransactionCost() {
    return transactionCost;
  }

  public void setTransferType(Object transferType) {
    this.transferType = transferType;
  }

  public Object getTransferType() {
    return transferType;
  }

  public void setBanks(List<AmountAndRecordsPerBank> banks) {
    this.banks = banks;
  }

  public List<AmountAndRecordsPerBank> getBanks() {
    return banks;
  }

  public void setCreditingErrors(List<FaultRecord> creditingErrors) {
    this.creditingErrors = creditingErrors;
  }

  public List<FaultRecord> getCreditingErrors() {
    return creditingErrors;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public Client getClient() {
    return client;
  }

  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }

  public String getBranchName() {
    return branchName;
  }

  public void setRetrievedAmountEur(BigDecimal retrievedAmountEur) {
    this.retrievedAmountEur = retrievedAmountEur;
  }

  public BigDecimal getRetrievedAmountEur() {
    return retrievedAmountEur;
  }

  public void setRetrievedAmountFL(BigDecimal retrievedAmountFL) {
    this.retrievedAmountFL = retrievedAmountFL;
  }

  public BigDecimal getRetrievedAmountFL() {
    return retrievedAmountFL;
  }

  public void setRetrievedAmountUsd(BigDecimal retrievedAmountUsd) {
    this.retrievedAmountUsd = retrievedAmountUsd;
  }

  public BigDecimal getRetrievedAmountUsd() {
    return retrievedAmountUsd;
  }

  public void setTotalRetrievedRecords(Object totalRetrievedRecords) {
    this.totalRetrievedRecords = totalRetrievedRecords;
  }

  public Object getTotalRetrievedRecords() {
    return totalRetrievedRecords;
  }
}
