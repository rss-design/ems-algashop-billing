package com.algaworks.algashop.billing.infrastructure.creditcard.fastpay;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class FastpayTokenizedCreditCardModel {
  private String tokenizedCard;
  private OffsetDateTime expiresAt;
}
