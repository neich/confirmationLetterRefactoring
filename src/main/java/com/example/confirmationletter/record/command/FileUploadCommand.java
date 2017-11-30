package com.example.confirmationletter.record.command;

import com.example.confirmationletter.record.service.impl.Constants;

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

  public boolean hasFee() {
    return fee.equalsIgnoreCase(Constants.YES);
  }
}
