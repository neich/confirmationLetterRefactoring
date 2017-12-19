package com.example.confirmationletter;

import com.example.confirmationletter.dao.CurrencyDao;
import com.example.confirmationletter.domain.Client;
import com.example.confirmationletter.domain.Currency;

public class Util {
  private static Util utilInstance;
  private CurrencyDao currencyDao;

  public static Util getInstance() {
    // We do not have knowledge on how the instance is created from the original example
    return utilInstance;
  }

  public Currency getDefaultCurrencyForClient(Client client) {
    String currencyId = currencyDao.retrieveCurrencyDefault(client
        .getProfile());
    return currencyDao
        .retrieveCurrencyOnId(new Integer(currencyId));
  }

}
