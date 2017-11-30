package com.example.confirmationletter.record.command;

public class FileUploadCommand {
  private String fee;
  private Object totalRecords;

  public String getFee() {
    return fee;
  }

  public void setFee(String fee) {
    this.fee = fee;
  }

  public Object getTotalRecords() {
    return totalRecords;
  }

  public void setTotalRecords(Object totalRecords) {
    this.totalRecords = totalRecords;
  }
}
