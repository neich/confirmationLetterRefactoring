package com.example.confirmationletter;

import com.example.confirmationletter.domain.Client;
import com.example.confirmationletter.record.service.impl.Constants;
import mockit.Tested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientTest {

  @Tested Client client;

  @Test
  void test_is_balanced() {
    client.setCounterTransfer(Constants.TRUE);
    assertTrue(client.isBalanced());
  }

  @Test
  void test_is_not_balanced() {
    client.setCounterTransfer(Constants.FALSE);
    assertFalse(client.isBalanced());
  }

  @Test
  void test_other() {
    client.setCounterTransfer("WTF!");
    assertFalse(client.isBalanced());
  }


}
