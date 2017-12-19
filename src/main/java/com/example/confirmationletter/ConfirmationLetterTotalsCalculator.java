package com.example.confirmationletter;

import com.example.confirmationletter.domain.Client;
import com.example.confirmationletter.domain.Currency;
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

  public void calculateRetrievedAmounts(
      List<Record> records,
      List<FaultRecord> faultyRecords,
      Client client, FileExtension extension,
      List<TempRecord> faultyAccountNumberRecordList,
      List<TempRecord> sansDuplicateFaultRecordsList) {

    if (client.isBalanced()) {
      calculateTotalsForBalancedRecords(records);
    } else {

      calculateTotalsForCounterBalancedRecords(records);
      calculateTotalOverTempRecords(sansDuplicateFaultRecordsList, sansAmounts,client);
      calculateTotalOverTempRecords(faultyAccountNumberRecordList, faultyAccountRecordAmounts, client);
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

  private void calculateTotalsForCounterBalancedRecords(List<Record> records) {
    for (Record record : records) {
      if (!record.isCounterTransferRecord() && !record.hasFee()) {
        addAmountToSignedTotal(record, recordAmounts);
      }

    }
  }

  private void calculateTotalOverTempRecords(List<TempRecord> faultyAccountNumberRecordList, CreditDebitHolder amountsHolder, Client client) {

    for (TempRecord faultyAccountNumberRecord : faultyAccountNumberRecordList) {
      setTempRecordSignToClientSignIfUnset(client, faultyAccountNumberRecord);
      setTempRecordCurrencyCodeToClientIfUnset(client, faultyAccountNumberRecord);

      addAmountToSignedTotal(faultyAccountNumberRecord, amountsHolder);
    }
  }

  private void calculateOverallTotalsForAllCurrencies() {
    for (String currency: recordAmount.keySet()) {
      calculateTotal(currency);
    }
  }

  private void setTempRecordCurrencyCodeToClientIfUnset(Client client, TempRecord faultyAccountNumberRecord) {
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

  private void setTempRecordSignToClientSignIfUnset(Client client, TempRecord sansDupRec) {
    if (sansDupRec.getSign() == null) {
      String sign = client.getCreditDebit();
      sansDupRec.setSign(sign);
    }
  }

  private void addAmountToTotal(Record record) {
    String currencyIsoCode = getCurrencyByCode(record.getCurrency().getCode());
    BigDecimal previousValue = recordAmount.get(currencyIsoCode);
    if (previousValue == null) {
      previousValue = BigDecimal.ZERO;
    }
    BigDecimal newValue = previousValue.add(record.getAmount());
    recordAmount.put(currencyIsoCode, newValue);
  }

  protected String getCurrencyByCode(Integer code) {

    if (Constants.EUR_CURRENCY_CODE.equals(code)) {
      return Constants.CURRENCY_EURO;
    } else if (Constants.FL_CURRENCY_CODE.equals(code)
        || Constants.FL_CURRENCY_CODE_FOR_WEIRD_BANK.equals(code)) {
      return Constants.CURRENCY_FL;
    } else if (Constants.USD_CURRENCY_CODE.equals(code)) {
      return Constants.CURRENCY_USD;
    } else {
      throw new IllegalArgumentException("Unknown currency code encountered");
    }
  }

  private void addAmountToSignedTotal(Record record, CreditDebitHolder amounts) {
    amounts.addValue(record.getCurrency().getCurrencyType(), record.getSign(), record.getAmount());
  }

  private void addAmountToSignedTotal(TempRecord record, CreditDebitHolder amounts) {
    amounts.addValue(getCurrencyByCode(record.getCurrencyCode()), record.getSign(), record.getAmount());
  }

  public BigDecimal getRecordAmount(String currency) {
    return recordAmount.get(currency);
  }

  class CreditDebitHolder {

    HashMap<String, BigDecimal> creditValues = new HashMap<String, BigDecimal>() {{
      put(Constants.CURRENCY_FL, BigDecimal.ZERO);
      put(Constants.CURRENCY_USD, BigDecimal.ZERO);
      put(Constants.CURRENCY_EURO, BigDecimal.ZERO);
    }};
    HashMap<String, BigDecimal> debitValues = new HashMap<String, BigDecimal>() {{
      put(Constants.CURRENCY_FL, BigDecimal.ZERO);
      put(Constants.CURRENCY_USD, BigDecimal.ZERO);
      put(Constants.CURRENCY_EURO, BigDecimal.ZERO);
    }};

    public BigDecimal getValue(String sign, String currency) {
      BigDecimal value = creditValues.get(currency);
      if (Constants.DEBIT.equals(sign)) {
        value = debitValues.get(currency);
      }
      if (value == null) {
        value = BigDecimal.ZERO;
      }
      return value;
    }

    public void setValue(String currency, String sign, BigDecimal value) {
      if (Constants.DEBIT.equals(sign)) {
        debitValues.put(currency, value);
      } else {
        creditValues.put(currency, value);
      }
    }

    public void addValue(String currency, String sign, BigDecimal value) {
      if (Constants.DEBIT.equals(sign)) {
        debitValues.put(currency, debitValues.get(currency).add(value));
      } else {
        creditValues.put(currency, creditValues.get(currency).add(value));
      }
    }
  }

}
