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
    Map<String, BigDecimal> retrievedAmounts = new HashMap<String, BigDecimal>();
    retrievedAmounts = calculateRetrieveAmounts(records, faultyRecords,
        client, extension, faultyAccountNumberRecordList,
        sansDuplicateFaultRecordsList);
    letter.setRetrievedAmountEur(retrievedAmounts
        .get(Constants.CURRENCY_EURO));
    letter.setRetrievedAmountFL(retrievedAmounts
        .get(Constants.CURRENCY_FL));
    letter.setRetrievedAmountUsd(retrievedAmounts
        .get(Constants.CURRENCY_FL));
    return letter;
  }

  private String getTransactionCost(FileUploadCommand fileUploadCommand, HashBatchRecordsBalance hashBatchRecordsBalance) {
    String transactionCost = "";
    if (fileUploadCommand.hasFee()) {
      transactionCost = hashBatchRecordsBalance.getTotalFee().toString();
    }
    return transactionCost;
  }

  // Calculate sum amount from faultyAccountnumber list

  private Map<String, BigDecimal> calculateAmountsFaultyAccountNumber(
      List<TempRecord> faultyAccountNumberRecordList, Client client) {
    Map<String, BigDecimal> retrievedAmountsFaultyAccountNumber = new HashMap<String, BigDecimal>();

    BigDecimal faultyAccRecordAmountCreditFL = BigDecimal.ZERO;
    BigDecimal faultyAccRecordAmountCreditUSD = BigDecimal.ZERO;
    BigDecimal faultyAccRecordAmountCreditEUR = BigDecimal.ZERO;

    BigDecimal faultyAccRecordAmountDebitFL = BigDecimal.ZERO;
    BigDecimal faultyAccRecordAmountDebitUSD = BigDecimal.ZERO;
    BigDecimal faultyAccRecordAmountDebitEUR = BigDecimal.ZERO;

    for (TempRecord faultyAccountNumberRecord : faultyAccountNumberRecordList) {
      // // logger.debug("faultyAccountNumberRecord: "+
      // faultyAccountNumberRecord);
      // FL
      setTempRecordSignToClientSignIfUnset(client, faultyAccountNumberRecord);
      setTempRecordCurrencyCodeToClientIfUnset(client, faultyAccountNumberRecord);

      if (faultyAccountNumberRecord.getCurrencycode().equals(
          Constants.FL_CURRENCY_CODE)
          || faultyAccountNumberRecord.getCurrencycode().equals(
          Constants.FL_CURRENCY_CODE_FOR_WEIRD_BANK)) {

        if (faultyAccountNumberRecord.getSign().equalsIgnoreCase(
            Constants.DEBIT)) {
          faultyAccRecordAmountDebitFL = new BigDecimal(
              faultyAccountNumberRecord.getAmount())
              .add(faultyAccRecordAmountDebitFL);
        } else {
          faultyAccRecordAmountCreditFL = new BigDecimal(
              faultyAccountNumberRecord.getAmount())
              .add(faultyAccRecordAmountCreditFL);
        }
      }
      if (faultyAccountNumberRecord.getCurrencycode().equals(
          Constants.USD_CURRENCY_CODE)) {
        if (faultyAccountNumberRecord.getSign().equalsIgnoreCase(
            Constants.DEBIT)) {
          faultyAccRecordAmountDebitUSD = new BigDecimal(
              faultyAccountNumberRecord.getAmount())
              .add(faultyAccRecordAmountDebitUSD);
        } else {
          faultyAccRecordAmountCreditUSD = new BigDecimal(
              faultyAccountNumberRecord.getAmount())
              .add(faultyAccRecordAmountCreditUSD);
        }
      }
      if (faultyAccountNumberRecord.getCurrencycode().equals(
          Constants.EUR_CURRENCY_CODE)) {
        if (faultyAccountNumberRecord.getSign().equalsIgnoreCase(
            Constants.DEBIT)) {
          faultyAccRecordAmountDebitEUR = new BigDecimal(
              faultyAccountNumberRecord.getAmount())
              .add(faultyAccRecordAmountDebitEUR);
        } else {
          faultyAccRecordAmountCreditEUR = new BigDecimal(
              faultyAccountNumberRecord.getAmount())
              .add(faultyAccRecordAmountCreditEUR);
        }
      }

      retrievedAmountsFaultyAccountNumber.put("FaultyAccDebitFL",
          faultyAccRecordAmountDebitFL);
      retrievedAmountsFaultyAccountNumber.put("FaultyAccDebitUSD",
          faultyAccRecordAmountDebitUSD);
      retrievedAmountsFaultyAccountNumber.put("FaultyAccDebitEUR",
          faultyAccRecordAmountDebitEUR);

      retrievedAmountsFaultyAccountNumber.put("FaultyAccCreditFL",
          faultyAccRecordAmountCreditFL);
      retrievedAmountsFaultyAccountNumber.put("FaultyAccCreditUSD",
          faultyAccRecordAmountCreditUSD);
      retrievedAmountsFaultyAccountNumber.put("FaultyAccCreditEUR",
          faultyAccRecordAmountCreditEUR);

    }
    return retrievedAmountsFaultyAccountNumber;
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

  private Map<String, BigDecimal> calculateRetrieveAmounts(
      List<Record> records,
      List<FaultRecord> faultyRecords,
      Client client, FileExtension extension,
      List<TempRecord> faultyAccountNumberRecordList,
      List<TempRecord> sansDuplicateFaultRecordsList) {

    Map<String, BigDecimal> retrievedAmounts = new HashMap<String, BigDecimal>();

    BigDecimal recordAmountDebitFL = BigDecimal.ZERO;
    BigDecimal recordAmountDebitEUR = BigDecimal.ZERO;
    BigDecimal recordAmountDebitUSD = BigDecimal.ZERO;

    BigDecimal recordAmountCreditFL = BigDecimal.ZERO;
    BigDecimal recordAmountCreditEUR = BigDecimal.ZERO;
    BigDecimal recordAmountCreditUSD = BigDecimal.ZERO;

    BigDecimal amountSansDebitFL = BigDecimal.ZERO;
    BigDecimal amountSansDebitUSD = BigDecimal.ZERO;
    BigDecimal amountSansDebitEUR = BigDecimal.ZERO;

    BigDecimal amountSansCreditFL = BigDecimal.ZERO;
    BigDecimal amountSansCreditUSD = BigDecimal.ZERO;
    BigDecimal amountSansCreditEUR = BigDecimal.ZERO;

    BigDecimal totalDebitFL = BigDecimal.ZERO;
    BigDecimal totalDebitUSD = BigDecimal.ZERO;
    BigDecimal totalDebitEUR = BigDecimal.ZERO;

    BigDecimal totalCreditFL = BigDecimal.ZERO;
    BigDecimal totalCreditUSD = BigDecimal.ZERO;
    BigDecimal totalCreditEUR = BigDecimal.ZERO;

    if (client.isBalanced()) {
      calculateTotalsForBalancedRecords(records, retrievedAmounts);
    } else {
      Map<String, RetrievedAmountsHolder> holders = new HashMap<String, RetrievedAmountsHolder>() {{
        this.put(Constants.CURRENCY_FL, new RetrievedAmountsHolder());
        this.put(Constants.CURRENCY_EURO, new RetrievedAmountsHolder());
        this.put(Constants.CURRENCY_USD, new RetrievedAmountsHolder());
      }};

      calculateTotalsForCounterBalancedRecords(records, holders);

      // Sansduplicate
      calculateTotalsForSansDuplicateFaultRecords(client, sansDuplicateFaultRecordsList, holders);

      Map<String, BigDecimal> retrievedAccountNumberAmounts = calculateAmountsFaultyAccountNumber(
          faultyAccountNumberRecordList, client);

      BigDecimal recordAmountFL = calculateTotals(holders.get(Constants.CURRENCY_FL),
          retrievedAccountNumberAmounts.get("FaultyAccDebitFL"),
          retrievedAccountNumberAmounts.get("FaultyAccCreditFL"));
      BigDecimal recordAmountUSD = calculateTotals(holders.get(Constants.CURRENCY_USD),
          retrievedAccountNumberAmounts.get("FaultyAccDebitUSD"),
          retrievedAccountNumberAmounts.get("FaultyAccCreditUSD"));
      BigDecimal recordAmountEUR = calculateTotals(holders.get(Constants.CURRENCY_EURO),
          retrievedAccountNumberAmounts.get("FaultyAccDebitEUR"),
          retrievedAccountNumberAmounts.get("FaultyAccCreditEUR"));

      retrievedAmounts.put(Constants.CURRENCY_EURO, recordAmountEUR);
      retrievedAmounts.put(Constants.CURRENCY_USD, recordAmountUSD);
      retrievedAmounts.put(Constants.CURRENCY_FL, recordAmountFL);
    }

    return retrievedAmounts;
  }

  private BigDecimal calculateTotals(RetrievedAmountsHolder holder,
                               BigDecimal faultyAccountDebitAmount,
                               BigDecimal faultyAccountCreditAmount) {
    holder.totalDebit = holder.recordAmountDebit.add(holder.amountSansDebit)
        .subtract(faultyAccountDebitAmount);
    holder.totalCredit = holder.recordAmountCredit.add(holder.amountSansCredit)
        .subtract(faultyAccountCreditAmount);
    return holder.totalCredit.subtract(holder.totalDebit).abs();
  }

  private void calculateTotalsForSansDuplicateFaultRecords(Client client, List<TempRecord> sansDuplicateFaultRecordsList, Map<String, RetrievedAmountsHolder> holders) {
    for (TempRecord sansDupRec : sansDuplicateFaultRecordsList) {
      setTempRecordSignToClientSignIfUnset(client, sansDupRec);

      Integer currencyCode = sansDupRec.getCurrencycode();

      if (currencyCode == null) {
        String currencyId = currencyDao
            .retrieveCurrencyDefault(client.getProfile());
        Currency currency = currencyDao
            .retrieveCurrencyOnId(new Integer(currencyId));
        sansDupRec.setCurrencycode(currency.getCode());
      }

      String currencyISOCode = getCurrencyByCode(currencyCode);
      RetrievedAmountsHolder holder = holders.get(currencyISOCode);

      if (sansDupRec.isDebitRecord())
        holder.amountSansDebit.add(BigDecimal.valueOf(sansDupRec.getAmount()));

      if (sansDupRec.isCreditRecord())
        holder.amountSansCredit.add(BigDecimal.valueOf(sansDupRec.getAmount()));
    }
  }

  private void setTempRecordSignToClientSignIfUnset(Client client, TempRecord sansDupRec) {
    if (sansDupRec.getSign() == null) {
      String sign = client.getCreditDebit();
      sansDupRec.setSign(sign);
    }
  }

  private void calculateTotalsForCounterBalancedRecords(List<Record> records, Map<String, RetrievedAmountsHolder> holders) {
    for (Record record : records) {
      logger.debug("COUNTERTRANSFER [" + record.getIsCounterTransferRecord() + "] FEERECORD [" + record.getFeeRecord() + "]");
      if (!record.isCounterTransferRecord() && !record.hasFee()) {
        String currencyISOCode = getCurrencyByCode(record.getCurrency().getCode());
        RetrievedAmountsHolder holder = holders.get(currencyISOCode);

        if (record.isDebitRecord())
          holder.recordAmountDebit.add(record.getAmount());

        if (record.isCreditRecord())
          holder.recordAmountCredit.add(record.getAmount());
      }

    }
  }

  private void calculateTotalsForBalancedRecords(List<Record> records, Map<String, BigDecimal> retrievedAmounts) {
    for (Record record : records) {
      if (record.isCounterTransferRecord() && record.isDebitRecord()) {
        addAmountToTotal(retrievedAmounts, record);
      }
    }
  }

  private void addAmountToTotal(Map<String, BigDecimal> retrievedAmounts, Record record) {
    String currencyIsoCode = getCurrencyByCode(record.getCurrency().getCode());
    BigDecimal previousValue = retrievedAmounts.get(currencyIsoCode);
    if (previousValue == null) {
      previousValue = BigDecimal.ZERO;
    }
    BigDecimal newValue = previousValue.add(record.getAmount());
    retrievedAmounts.put(currencyIsoCode, newValue);
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

  class RetrievedAmountsHolder {
    BigDecimal recordAmountDebit = BigDecimal.ZERO;
    BigDecimal recordAmountCredit = BigDecimal.ZERO;
    BigDecimal amountSansDebit = BigDecimal.ZERO;
    BigDecimal amountSansCredit = BigDecimal.ZERO;
    BigDecimal totalDebit = BigDecimal.ZERO;
    BigDecimal totalCredit = BigDecimal.ZERO;
  }
}