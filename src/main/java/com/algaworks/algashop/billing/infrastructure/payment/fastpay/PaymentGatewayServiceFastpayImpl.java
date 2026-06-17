package com.algaworks.algashop.billing.infrastructure.payment.fastpay;

import com.algaworks.algashop.billing.domain.model.creditcard.CreditCard;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardNotFoundException;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardRepository;
import com.algaworks.algashop.billing.domain.model.invoice.Address;
import com.algaworks.algashop.billing.domain.model.invoice.Payer;
import com.algaworks.algashop.billing.domain.model.invoice.payment.Payment;
import com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentGatewayService;
import com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algashop.integrations.payment.provider", havingValue = "FASTPAY")
public class PaymentGatewayServiceFastpayImpl implements PaymentGatewayService {

  private final FastpayPaymentApiClient fastpayPaymentApiClient;
  private final CreditCardRepository creditCardRepository;

  @Override
  public Payment capture(PaymentRequest request) {
    FastpayPaymentInput input = convertToInput(request);
    FastpayPaymentModel response = fastpayPaymentApiClient.capture(input);
    return convertToPayment(response);
  }

  @Override
  public Payment findByCode(String gatewayCode) {
    FastpayPaymentModel response = fastpayPaymentApiClient.findById(gatewayCode);
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
      .replyToUrl("http://example.com/webhook");

    switch (request.getMethod()) {
      case CREDIT_CARD -> {
        builder.method(FastpayPaymentMethod.CREDIT.name());

        CreditCard creditCard = creditCardRepository.findById(request.getCreditCardId())
          .orElseThrow(CreditCardNotFoundException::new);

        builder.creditCardId(creditCard.getGatewayCode());
      }
      case GATEWAY_BALANCE -> {
        builder.method(FastpayPaymentMethod.GATEWAY_BALANCE.name());
      }
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