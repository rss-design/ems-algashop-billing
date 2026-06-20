package com.algaworks.algashop.billing.infrastructure.payment.fastpay.webhook;

import com.algaworks.algashop.billing.application.invoice.management.InvoiceManagementApplicationService;
import com.algaworks.algashop.billing.infrastructure.payment.fastpay.FastpayEnumConverter;
import com.algaworks.algashop.billing.infrastructure.payment.fastpay.FastpayPaymentStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastpayWebhookHandler {

  private final InvoiceManagementApplicationService invoiceManagementApplicationService;

  public void process(FastpayPaymentWebhookEvent event) {
    log.info("Processing Fastpay Webhook Event: {}", event);

    invoiceManagementApplicationService.updatePaymentStatus(
      UUID.fromString(event.getReferenceCode()),
      FastpayEnumConverter.convert(FastpayPaymentStatus.valueOf(event.getStatus()))
    );
  }

}
