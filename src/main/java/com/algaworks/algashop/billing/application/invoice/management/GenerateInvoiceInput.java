package com.algaworks.algashop.billing.application.invoice.management;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GenerateInvoiceInput{
  private String orderId;

  @NotNull
  private UUID customerId;

  @NotNull
  private PaymentSettingsInput paymentSettings;

  @NotNull
  @Valid
  private PayerData payer;

  @NotEmpty
  private List<LineItemInput> items;
}