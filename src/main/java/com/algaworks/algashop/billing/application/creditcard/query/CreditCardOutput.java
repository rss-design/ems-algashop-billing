package com.algaworks.algashop.billing.application.creditcard.query;

import java.util.UUID;
import lombok.Data;

@Data
public class CreditCardOutput {
  private UUID id;
  private String lastNumbers;
  private Integer expMonth;
  private Integer expYear;
  private String brand;
}
