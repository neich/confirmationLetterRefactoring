package com.example.confirmationletter;

import com.example.confirmationletter.domain.Client;
import com.example.confirmationletter.domain.Currency;
import com.example.confirmationletter.domain.GenericRecord;
import com.example.confirmationletter.domain.Record;
import com.example.confirmationletter.record.domain.FaultRecord;
import com.example.confirmationletter.record.domain.TempRecord;
import com.example.confirmationletter.record.parser.FileExtension;
import com.example.confirmationletter.record.service.impl.Constants;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class ConfirmationLetterTotalsCalculator {
  HashMap<String, BigDecimal> recordAmount = new HashMap<String, BigDecimal>() {{
    put(Constants.CURRENCY_FL, BigDecimal.ZERO);
    put(Constants.CURRENCY_USD, BigDecimal.ZERO);
    put(Constants.CURRENCY_EURO, BigDecimal.ZERO);
  }};

  CreditDebitHolder recordAmounts = new CreditDebitHolder();
  CreditDebitHolder sansAmounts = new CreditDebitHolder();
  CreditDebitHolder totalAmounts = new CreditDebitHolder();
  CreditDebitHolder faultyAccountRecordAmounts = new CreditDebitHolder();

  Client client;

  class RecordFilterStrategy {
    boolean filter(GenericRecord record) {
      return true;
    }
  }

  RecordFilterStrategy sansAmountsFilter = new RecordFilterStrategy();
  RecordFilterStrategy faultyAmountsFilter = new RecordFilterStrategy();
  RecordFilterStrategy recordAmountsFilter = new RecordFilterStrategy() {
    boolean filter(GenericRecord record) {
      return record.isCounterTransferRecord() && !record.hasFee();
    }
  };
  RecordFilterStrategy balancedFilter = new RecordFilterStrategy() {
    boolean filter(GenericRecord record) {
      return !record.hasFee() && record.isDebitRecord();
    }
  };

  public ConfirmationLetterTotalsCalculator(Client client) {
    this.client = client;
  }

  public void calculateRetrievedAmounts(
      List<Record> records,
      List<FaultRecord> faultyRecords,
      FileExtension extension,
      List<TempRecord> faultyAccountNumberRecordList,
      List<TempRecord> sansDuplicateFaultRecordsList) {

    if (client.isBalanced()) {
      calculateTotalsForBalancedRecords(records);
    } else {

      calculateTotalOverRecords(records, recordAmounts, recordAmountsFilter);
      calculateTotalOverRecords(sansDuplicateFaultRecordsList, sansAmounts, sansAmountsFilter);
      calculateTotalOverRecords(faultyAccountNumberRecordList, faultyAccountRecordAmounts, faultyAmountsFilter);
      calculateOverallTotalsForAllCurrencies();
    }
  }

  private void calculateTotalsForBalancedRecords(List<Record> records) {
    for (Record record : records) {
      if (record.isCounterTransferRecord() && record.isDebitRecord()) {
        addAmountToTotal(record);
      }
    }
  }

  private void calculateTotalOverRecords(List<? extends GenericRecord> records, CreditDebitHolder amountsHolder, RecordFilterStrategy filter) {

    for (GenericRecord record : records) {
      if (filter.filter(record)) {
        addAmountToSignedTotal(record, amountsHolder);
      }
    }
  }

  private void calculateOverallTotalsForAllCurrencies() {
    for (String currency: recordAmount.keySet()) {
      calculateTotal(currency);
    }
  }

  private void setTempRecordCurrencyCodeToClientIfUnset(TempRecord faultyAccountNumberRecord) {
    if (faultyAccountNumberRecord.getCurrencyCode() == null) {
      Currency currency = Util.getInstance().getDefaultCurrencyForClient(client);
      faultyAccountNumberRecord.setCurrencycode(currency.getCode());
    }
  }

  private void calculateTotal(String currency) {
    totalAmounts.setValue(currency, Constants.CREDIT, recordAmounts.getValue(currency, Constants.CREDIT).add(sansAmounts.getValue(currency, Constants.CREDIT)).subtract(faultyAccountRecordAmounts.getValue(currency, Constants.CREDIT)));
    totalAmounts.setValue(currency, Constants.DEBIT, recordAmounts.getValue(currency, Constants.DEBIT).add(sansAmounts.getValue(currency, Constants.DEBIT)).subtract(faultyAccountRecordAmounts.getValue(currency, Constants.DEBIT)));

    recordAmount.put(currency, totalAmounts.getValue(currency, Constants.CREDIT).subtract(totalAmounts.getValue(currency, Constants.DEBIT)));
  }

  private void setTempRecordSignToClientSignIfUnset(TempRecord sansDupRec) {
    if (sansDupRec.getSign() == null) {
      String sign = client.getCreditDebit();
      sansDupRec.setSign(sign);
    }
  }

  private void addAmountToTotal(Record record) {
    String currencyIsoCode = Currency.getCurrencyByCode(record.getCurrency().getCode());
    BigDecimal previousValue = recordAmount.get(currencyIsoCode);
    if (previousValue == null) {
      previousValue = BigDecimal.ZERO;
    }
    BigDecimal newValue = previousValue.add(record.getAmount());
    recordAmount.put(currencyIsoCode, newValue);
  }

  private void addAmountToSignedTotal(GenericRecord record, CreditDebitHolder amounts) {
    amounts.addValue(Currency.getCurrencyByCode(record.getCurrencyNumericCode()), record.getSign(), record.getAmountAsBigDecimal());
  }

  public BigDecimal getRecordAmount(String currency) {
    return recordAmount.get(currency);
  }

  class CreditDebitHolder {

    HashMap<String, HashMap<String, BigDecimal>> values = new HashMap<>();

    public CreditDebitHolder() {
      values.put(Constants.DEBIT, new HashMap<String, BigDecimal>());
      values.put(Constants.CREDIT, new HashMap<String, BigDecimal>());
    }

    public BigDecimal getValue(String sign, String currency) {
      BigDecimal value = values.get(sign).get(currency);
      if (value == null) {
        value = BigDecimal.ZERO;
      }
      return value;
    }

    public void setValue(String currency, String sign, BigDecimal value) {
      values.get(sign).put(currency, value);
    }

    public void addValue(String currency, String sign, BigDecimal value) {
        values.get(sign).put(currency, getValue(currency, sign).add(value));
    }
  }

}
