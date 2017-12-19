package com.example.confirmationletter;

import com.example.confirmationletter.dao.CurrencyDao;
import com.example.confirmationletter.domain.*;
import com.example.confirmationletter.record.command.FileUploadCommand;
import com.example.confirmationletter.record.domain.FaultRecord;
import com.example.confirmationletter.record.domain.TempRecord;
import com.example.confirmationletter.record.parser.FileExtension;
import com.example.confirmationletter.record.service.impl.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.execution.RequestContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfirmationLetterGenerator {

  private static Log logger = LogFactory.getLog(ConfirmationLetterGenerator.class);

  private String crediting;
  private String debiting;
  private String debit;
  private String credit;
  private String type;
  private LetterSelector letterSelector;
  private CurrencyDao currencyDao;

  HashMap<String, BigDecimal> recordAmount = new HashMap<String, BigDecimal>() {{
    put(Constants.CURRENCY_FL, BigDecimal.ZERO);
    put(Constants.CURRENCY_USD, BigDecimal.ZERO);
    put(Constants.CURRENCY_EURO, BigDecimal.ZERO);
  }};

  CreditDebitHolder recordAmounts = new CreditDebitHolder();
  CreditDebitHolder sansAmounts = new CreditDebitHolder();
  CreditDebitHolder totalAmounts = new CreditDebitHolder();
  CreditDebitHolder faultyAccountRecordAmounts = new CreditDebitHolder();

  public CurrencyDao getCurrencyDao() {
    return currencyDao;
  }

  public void setCurrencyDao(CurrencyDao currencyDao) {
    this.currencyDao = currencyDao;
  }

  public OurOwnByteArrayOutputStream letter(RequestContext context,
                                            FileUploadCommand fileUploadCommand, Client client,
                                            HashBatchRecordsBalance hashBatchRecordsBalance, String branchName,
                                            List<AmountAndRecordsPerBank> bankMap,
                                            List<FaultRecord> faultyRecords,
                                            FileExtension extension, List<Record> records,
                                            List<TempRecord> faultyAccountNumberRecordList,
                                            List<TempRecord> sansDuplicateFaultRecordsList
  ) {

    ConfirmationLetter letter = createConfirmationLetter(fileUploadCommand, client, hashBatchRecordsBalance, branchName, bankMap, faultyRecords, extension, records, faultyAccountNumberRecordList, sansDuplicateFaultRecordsList);

    OurOwnByteArrayOutputStream arrayOutputStream = generateConfirmationLetterAsPDF(client, letter);

    context.getConversationScope().asMap().put("dsbByteArrayOutputStream",
        arrayOutputStream);

    return arrayOutputStream;
  }

  private OurOwnByteArrayOutputStream generateConfirmationLetterAsPDF(Client client, ConfirmationLetter letter) {
    return letterSelector
        .generateLetter(client.getCreditDebit(), letter);
  }

  private ConfirmationLetter createConfirmationLetter(FileUploadCommand fileUploadCommand, Client client, HashBatchRecordsBalance hashBatchRecordsBalance, String branchName, List<AmountAndRecordsPerBank> bankMap, List<FaultRecord> faultyRecords, FileExtension extension, List<Record> records, List<TempRecord> faultyAccountNumberRecordList, List<TempRecord> sansDuplicateFaultRecordsList) {

    ConfirmationLetter letter = new ConfirmationLetter();
    letter.setCurrency(records.get(0).getCurrency());
    letter.setExtension(extension);
    letter.setTotalRetrievedRecords(fileUploadCommand.getTotalRecords());
    letter.setCreditingErrors(faultyRecords);
    letter.setClient(client);
    letter.setBranchName(branchName);
    letter.setBanks(bankMap);

    letter.setHashTotalCredit(hashBatchRecordsBalance.getHashTotalCredit()
        .toString());
    letter.setHashTotalDebit(hashBatchRecordsBalance.getHashTotalDebit()
        .toString());

    letter.setBatchTotalDebit(hashBatchRecordsBalance.getBatchTotal(client.getAmountDivider(), Constants.DEBIT).toString());
    letter.setBatchTotalCredit(hashBatchRecordsBalance.getBatchTotal(client.getAmountDivider(), Constants.CREDIT).toString());

    letter.setTotalProcessedRecords(hashBatchRecordsBalance
        .getRecordsTotal().toString());

    letter.setTransactionCost(getTransactionCost(fileUploadCommand, hashBatchRecordsBalance));

    letter.setTransferType(hashBatchRecordsBalance.getCollectionType());

    // uncommented this line
    calculateRetrievedAmounts(records, faultyRecords,
        client, extension, faultyAccountNumberRecordList,
        sansDuplicateFaultRecordsList);

    letter.setRetrievedAmountEur(recordAmount.get(Constants.CURRENCY_EURO));
    letter.setRetrievedAmountFL(recordAmount.get(Constants.CURRENCY_FL));
    letter.setRetrievedAmountUsd(recordAmount.get(Constants.CURRENCY_USD));

    return letter;
  }

  private void calculateRetrievedAmounts(
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
      logger.debug("COUNTERTRANSFER [" + record.getIsCounterTransferRecord() + "] FEERECORD [" + record.getFeeRecord() + "]");
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

  private String getTransactionCost(FileUploadCommand fileUploadCommand, HashBatchRecordsBalance hashBatchRecordsBalance) {
    String transactionCost = "";
    if (fileUploadCommand.hasFee()) {
      transactionCost = hashBatchRecordsBalance.getTotalFee().toString();
    }
    return transactionCost;
  }

  private void setTempRecordCurrencyCodeToClientIfUnset(Client client, TempRecord faultyAccountNumberRecord) {
    if (faultyAccountNumberRecord.getCurrencyCode() == null) {
      String currencyId = currencyDao.retrieveCurrencyDefault(client
          .getProfile());
      Currency currency = currencyDao
          .retrieveCurrencyOnId(new Integer(currencyId));
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

  private List<AmountAndRecordsPerBank> amountAndRecords(
      List<Record> records, String transactionType) {
    List<AmountAndRecordsPerBank> list = new ArrayList<AmountAndRecordsPerBank>();
    String typeOfTransaction = transactionType.equalsIgnoreCase(crediting) ? crediting
        : debiting;
    type = typeOfTransaction.equalsIgnoreCase(crediting) ? credit : debit;
    if (transactionType.equalsIgnoreCase(typeOfTransaction)) {
      for (Record record : records) {
        getAmountAndRecords(record, list, transactionType);
      }
    }
    return list;
  }

  private List<AmountAndRecordsPerBank> getAmountAndRecords(Record record,
                                                            List<AmountAndRecordsPerBank> list, String transactionType) {
    Map<String, String> map = new HashMap<String, String>();
    if (record.getFeeRecord().compareTo(0) == 0
        && !map.containsKey(record.getBeneficiaryName())) {

      if (transactionType.equalsIgnoreCase(Constants.CREDITING)) {

        if (record.getBeneficiaryName() != null
            && !record.getBeneficiaryName().equalsIgnoreCase(
            Constants.RBTT_BANK_ALTERNATE)) {
          Boolean newList = true;
          if (list.size() == 0
              && record.getSign().equalsIgnoreCase(type)) {
            // logger.info("bank gegevens: "+record.getSign()+" : "+record.getBank().getName()+" : "+record.getBeneficiaryName());
            AmountAndRecordsPerBank aARPB = new AmountAndRecordsPerBank();
            aARPB.setBankName(record.getBank().getName());
            aARPB.setTotalRecord(1);
            aARPB.setAmount(record.getAmount());
            aARPB.setCurrencyType(record.getCurrency()
                .getCurrencyType());
            aARPB.setAccountNumber(record
                .getBeneficiaryAccountNumber());
            list.add(aARPB);
            newList = false;
          }
          if (newList && record.getSign().equalsIgnoreCase(type)) {
            // logger.info("bank gegevens: "+record.getSign()+" : "+record.getBank().getName()+" : "+record.getBeneficiaryName());
            Boolean newRecord = true;
            for (AmountAndRecordsPerBank object : list) {
              if (object.getBankName().equalsIgnoreCase(
                  record.getBank().getName())
                  && object.getCurrencyType()
                  .equalsIgnoreCase(
                      record.getCurrency()
                          .getCurrencyType())) {
                object.setAmount(object.getAmount().add(
                    record.getAmount()));
                object
                    .setTotalRecord(object.getTotalRecord() + 1);
                newRecord = false;
              }
            }
            if (newRecord) {
              AmountAndRecordsPerBank aARPB = new AmountAndRecordsPerBank();
              aARPB.setBankName(record.getBank().getName());
              aARPB.setTotalRecord(1);
              aARPB.setAmount(record.getAmount());
              aARPB.setCurrencyType(record.getCurrency()
                  .getCurrencyType());
              aARPB.setAccountNumber(record
                  .getBeneficiaryAccountNumber());
              list.add(aARPB);
            }
          }
        }
      }

      // del begin
      if (transactionType.equalsIgnoreCase(Constants.DEBITING)) {

        if (record.getBeneficiaryName() == null) {
          Boolean newList = true;
          if (list.size() == 0
              && record.getSign().equalsIgnoreCase(type)) {
            // logger.info("bank gegevens: "+record.getSign()+" : "+record.getBank().getName()+" : "+record.getBeneficiaryName());
            AmountAndRecordsPerBank aARPB = new AmountAndRecordsPerBank();
            aARPB.setBankName(record.getBank().getName());
            aARPB.setTotalRecord(1);
            aARPB.setAmount(record.getAmount());
            aARPB.setCurrencyType(record.getCurrency()
                .getCurrencyType());
            aARPB.setAccountNumber(record
                .getBeneficiaryAccountNumber());
            list.add(aARPB);
            newList = false;
          }
          if (newList && record.getSign().equalsIgnoreCase(type)) {
            // logger.info("bank gegevens: "+record.getSign()+" : "+record.getBank().getName()+" : "+record.getBeneficiaryName());
            Boolean newRecord = true;
            for (AmountAndRecordsPerBank object : list) {
              if (object.getBankName().equalsIgnoreCase(
                  record.getBank().getName())
                  && object.getCurrencyType()
                  .equalsIgnoreCase(
                      record.getCurrency()
                          .getCurrencyType())) {
                object.setAmount(object.getAmount().add(
                    record.getAmount()));
                object
                    .setTotalRecord(object.getTotalRecord() + 1);
                newRecord = false;
              }
            }
            if (newRecord) {
              AmountAndRecordsPerBank aARPB = new AmountAndRecordsPerBank();
              aARPB.setBankName(record.getBank().getName());
              aARPB.setTotalRecord(1);
              aARPB.setAmount(record.getAmount());
              aARPB.setCurrencyType(record.getCurrency()
                  .getCurrencyType());
              aARPB.setAccountNumber(record
                  .getBeneficiaryAccountNumber());
              list.add(aARPB);
            }
          }
        }
      }
      // del end
    }
    return list;
  }

  /*
   *
   * Getters and setters
   */

  public void setCrediting(String crediting) {
    this.crediting = crediting;
  }

  public void setDebiting(String debiting) {
    this.debiting = debiting;
  }

  public void setDebit(String debit) {
    this.debit = debit;
  }

  public void setCredit(String credit) {
    this.credit = credit;
  }

  public void setLetterSelector(LetterSelector letterSelector) {
    this.letterSelector = letterSelector;
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