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
    Map<String, RetrievedAmountsHolder> retrievedAmounts = calculateRetrievedAmounts(records, faultyRecords,
        client, extension, faultyAccountNumberRecordList,
        sansDuplicateFaultRecordsList);

    letter.setRetrievedAmountEur(retrievedAmounts
        .get(Constants.CURRENCY_EURO).recordAmount);
    letter.setRetrievedAmountFL(retrievedAmounts
        .get(Constants.CURRENCY_FL).recordAmount);
    letter.setRetrievedAmountUsd(retrievedAmounts
        .get(Constants.CURRENCY_FL).recordAmount);
    return letter;
  }

  private Map<String, RetrievedAmountsHolder> calculateRetrievedAmounts(
      List<Record> records,
      List<FaultRecord> faultyRecords,
      Client client, FileExtension extension,
      List<TempRecord> faultyAccountNumberRecordList,
      List<TempRecord> sansDuplicateFaultRecordsList) {

    Map<String, RetrievedAmountsHolder> retrievedAmounts = new HashMap<String, RetrievedAmountsHolder>() {{
      this.put(Constants.CURRENCY_FL, new RetrievedAmountsHolder());
      this.put(Constants.CURRENCY_EURO, new RetrievedAmountsHolder());
      this.put(Constants.CURRENCY_USD, new RetrievedAmountsHolder());
    }};

    if (client.isBalanced()) {
      calculateTotalsForBalancedRecords(records, retrievedAmounts);
    } else {

      calculateTotalsForCounterBalancedRecords(records, retrievedAmounts);
      calculateTotalsForSansDuplicateFaultRecords(client, sansDuplicateFaultRecordsList, retrievedAmounts);
      calculateAmountsFaultyAccountNumber(faultyAccountNumberRecordList, retrievedAmounts, client);
      calculateOverallTotalsForAllCurrencies(retrievedAmounts);
    }

    return retrievedAmounts;
  }

  private void calculateTotalsForBalancedRecords(List<Record> records, Map<String, RetrievedAmountsHolder> retrievedAmounts) {
    for (Record record : records) {
      if (record.isCounterTransferRecord() && record.isDebitRecord()) {
        addAmountToTotal(retrievedAmounts, record);
      }
    }
  }

  private void calculateTotalsForCounterBalancedRecords(List<Record> records, Map<String, RetrievedAmountsHolder> holders) {
    for (Record record : records) {
      logger.debug("COUNTERTRANSFER [" + record.getIsCounterTransferRecord() + "] FEERECORD [" + record.getFeeRecord() + "]");
      if (!record.isCounterTransferRecord() && !record.hasFee()) {
        RetrievedAmountsHolder holder = getHolderForRecord(record, holders);
        addAmountToSignedTotal(record, holder.recordAmounts);
      }

    }
  }

  private RetrievedAmountsHolder getHolderForRecord(Record record, Map<String, RetrievedAmountsHolder> holders) {
    return holders.get(getCurrencyByCode(record.getCurrency().getCode()));
  }

  private void calculateTotalsForSansDuplicateFaultRecords(Client client, List<TempRecord> sansDuplicateFaultRecordsList, Map<String, RetrievedAmountsHolder> retrievedAmounts) {
    for (TempRecord sansDupRec : sansDuplicateFaultRecordsList) {
      setTempRecordSignToClientSignIfUnset(client, sansDupRec);
      setTempRecordCurrencyCodeToClientIfUnset(client, sansDupRec);

      RetrievedAmountsHolder holder = retrievedAmounts.get(getCurrencyByCode(sansDupRec.getCurrencycode()));

      if (sansDupRec.isDebitRecord())
        holder.amountSansDebit.add(BigDecimal.valueOf(sansDupRec.getAmount()));

      if (sansDupRec.isCreditRecord())
        holder.amountSansCredit.add(BigDecimal.valueOf(sansDupRec.getAmount()));
    }
  }

  private void calculateAmountsFaultyAccountNumber(
      List<TempRecord> faultyAccountNumberRecordList, Map<String, RetrievedAmountsHolder> retrievedAmounts, Client client) {

    for (TempRecord faultyAccountNumberRecord : faultyAccountNumberRecordList) {
      setTempRecordSignToClientSignIfUnset(client, faultyAccountNumberRecord);
      setTempRecordCurrencyCodeToClientIfUnset(client, faultyAccountNumberRecord);

      RetrievedAmountsHolder holder = retrievedAmounts.get(
          getCurrencyByCode(faultyAccountNumberRecord.getCurrencycode()));

      if (faultyAccountNumberRecord.isDebitRecord()) {
        holder.faultyAccRecordAmountDebit = holder.faultyAccRecordAmountDebit.add(
            new BigDecimal(faultyAccountNumberRecord.getAmount()));
      }
      if (faultyAccountNumberRecord.isCreditRecord()) {
        holder.faultyAccRecordAmountCredit = holder.faultyAccRecordAmountCredit.add(
            new BigDecimal(faultyAccountNumberRecord.getAmount()));
      }

    }
  }

  private void calculateOverallTotalsForAllCurrencies(Map<String, RetrievedAmountsHolder> retrievedAmounts) {
    for (RetrievedAmountsHolder holder : retrievedAmounts.values()) {
      calculateTotal(holder);
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
    if (faultyAccountNumberRecord.getCurrencycode() == null) {
      String currencyId = currencyDao.retrieveCurrencyDefault(client
          .getProfile());
      Currency currency = currencyDao
          .retrieveCurrencyOnId(new Integer(currencyId));
      faultyAccountNumberRecord.setCurrencycode(currency.getCode());
    }
  }

  private void calculateTotal(RetrievedAmountsHolder holder) {
    holder.totalDebit = holder.recordAmounts.get(Constants.CREDIT).add(holder.amountSansDebit)
        .subtract(holder.faultyAccRecordAmountDebit);
    holder.totalCredit = holder.recordAmounts.get(Constants.DEBIT).add(holder.amountSansCredit)
        .subtract(holder.faultyAccRecordAmountCredit);

    holder.recordAmount = holder.totalCredit.subtract(holder.totalDebit).abs();
  }

  private void setTempRecordSignToClientSignIfUnset(Client client, TempRecord sansDupRec) {
    if (sansDupRec.getSign() == null) {
      String sign = client.getCreditDebit();
      sansDupRec.setSign(sign);
    }
  }

  private void addAmountToTotal(Map<String, RetrievedAmountsHolder> retrievedAmounts, Record record) {
    String currencyIsoCode = getCurrencyByCode(record.getCurrency().getCode());
    RetrievedAmountsHolder holder = retrievedAmounts.get(currencyIsoCode);
    BigDecimal previousValue = holder.recordAmount;
    if (previousValue == null) {
      previousValue = BigDecimal.ZERO;
    }
    BigDecimal newValue = previousValue.add(record.getAmount());
    holder.recordAmount = newValue;
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

  private void addAmountToSignedTotal(Record record, Map<String, BigDecimal> amounts) {
    amounts.put(record.getSign(),
        amounts.get(record.getSign()).add(record.getAmount()));
  }

  class RetrievedAmountsHolder {
    Map<String, BigDecimal> recordAmounts = new HashMap<String, BigDecimal>() {{
      put(Constants.CREDIT, BigDecimal.ZERO);
      put(Constants.DEBIT, BigDecimal.ZERO);
    }};
    BigDecimal amountSansDebit = BigDecimal.ZERO;
    BigDecimal amountSansCredit = BigDecimal.ZERO;
    BigDecimal totalDebit = BigDecimal.ZERO;
    BigDecimal totalCredit = BigDecimal.ZERO;
    BigDecimal faultyAccRecordAmountDebit = BigDecimal.ZERO;
    BigDecimal faultyAccRecordAmountCredit = BigDecimal.ZERO;
    BigDecimal recordAmount = BigDecimal.ZERO;
  }
}