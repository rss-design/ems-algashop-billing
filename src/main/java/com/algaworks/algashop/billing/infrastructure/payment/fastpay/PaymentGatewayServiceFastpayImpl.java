package com.algaworks.algashop.billing.infrastructure.payment.fastpay;

import com.algaworks.algashop.billing.domain.model.creditcard.CreditCard;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardNotFoundException;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardRepository;
import com.algaworks.algashop.billing.domain.model.invoice.Address;
import com.algaworks.algashop.billing.domain.model.invoice.Payer;
import com.algaworks.algashop.billing.domain.model.invoice.payment.Payment;
import com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentGatewayService;
import com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentRequest;
import com.algaworks.algashop.billing.infrastructure.payment.AlgaShopPaymentProperties;
import com.algaworks.algashop.billing.presentation.BadGatewayException;
import com.algaworks.algashop.billing.presentation.GatewayTimeoutException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algashop.integrations.payment.provider", havingValue = "FASTPAY")
public class PaymentGatewayServiceFastpayImpl implements PaymentGatewayService {

  private final FastpayPaymentApiClient fastpayPaymentApiClient;
  private final CreditCardRepository creditCardRepository;
  private final AlgaShopPaymentProperties algaShopPaymentProperties;

  @Override
  public Payment capture(PaymentRequest request) {
    log.info("[PaymentGatewayServiceFastpayImpl] Payment capture for invoiceId: {} and method: {}",
      request.getInvoiceId(), request.getMethod());

    FastpayPaymentInput input = convertToInput(request);
    FastpayPaymentModel response;

    try {
      response = fastpayPaymentApiClient.capture(input);
    } catch (ResourceAccessException e) {
      throw new GatewayTimeoutException("Fastpay API Timeout", e);
    } catch (HttpClientErrorException e) {
      throw new BadGatewayException("Fastpay API Bad Gateway", e);
    }

    return convertToPayment(response);
  }

  @Override
  public Payment findByCode(String gatewayCode) {
    log.info("[PaymentGatewayServiceFastpayImpl] Find by gateway code {}", gatewayCode);

    FastpayPaymentModel response;

    try {
      response = fastpayPaymentApiClient.findById(gatewayCode);
    } catch (ResourceAccessException e) {
      throw new GatewayTimeoutException("Fastpay API Timeout", e);
    } catch (HttpClientErrorException e) {
      throw new BadGatewayException("Fastpay API Bad Gateway", e);
    }

    return convertToPayment(response);
  }

  private FastpayPaymentInput convertToInput(PaymentRequest request) {
    Payer payer = request.getPayer();
    Address address = payer.getAddress();

    var builder = FastpayPaymentInput.builder()
      .totalAmount(request.getAmount())
      .referenceCode(request.getInvoiceId().toString())
      .fullName(payer.getFullName())
      .document(payer.getDocument())
      .phone(payer.getPhone())
      .zipCode(address.getZipCode())
      .addressLine1(address.getStreet() + ", " + address.getNumber())
      .addressLine2(address.getComplement())
      .replyToUrl(algaShopPaymentProperties.getFastpay().getWebhookUrl());

    switch (request.getMethod()) {
      case CREDIT_CARD -> {
        builder.method(FastpayPaymentMethod.CREDIT.name());

        CreditCard creditCard = creditCardRepository.findById(request.getCreditCardId())
          .orElseThrow(CreditCardNotFoundException::new);

        builder.creditCardId(creditCard.getGatewayCode());
      }
      case GATEWAY_BALANCE -> builder.method(FastpayPaymentMethod.GATEWAY_BALANCE.name());
    }

    return builder.build();
  }

  private Payment convertToPayment(FastpayPaymentModel response) {
    var builder = Payment.builder()
      .gatewayCode(response.getId())
      .invoiceId(UUID.fromString(response.getReferenceCode()));

    FastpayPaymentMethod fastpayPaymentMethod;
    try {
      fastpayPaymentMethod = FastpayPaymentMethod.valueOf(response.getMethod());
    } catch (Exception e) {
      throw new IllegalArgumentException("Unknow payment method: " + response.getMethod());
    }

    FastpayPaymentStatus fastpayPaymentStatus;
    try {
      fastpayPaymentStatus = FastpayPaymentStatus.valueOf(response.getStatus());
    } catch (Exception e) {
      throw new IllegalArgumentException("Unknow payment status: " + response.getStatus());
    }

    builder.method(FastpayEnumConverter.convert(fastpayPaymentMethod));
    builder.status(FastpayEnumConverter.convert(fastpayPaymentStatus));

    return builder.build();
  }

}