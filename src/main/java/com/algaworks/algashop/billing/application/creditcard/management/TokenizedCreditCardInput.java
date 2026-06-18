package com.algaworks.algashop.billing.application.creditcard.management;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Data;

@Data
public class TokenizedCreditCardInput {
  private UUID customerId;
  @NotBlank
  private String tokenizedCard;
}
