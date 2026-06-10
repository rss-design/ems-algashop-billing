package com.algaworks.algashop.billing.infrastructure.creditcard.fastpay;

import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardProviderService;
import com.algaworks.algashop.billing.domain.model.creditcard.LimitedCreditCard;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algashop.integrations.payment.provider", havingValue = "FASTPAY")
public class CreditCardProviderServiceFastpayImpl implements CreditCardProviderService {

  private final FastpayCreditCardApiClient fastpayCreditCardApiClient;

  @Override
  public LimitedCreditCard register(UUID customerId, String tokenizedCard) {

    FastpayCreditCardInput input = FastpayCreditCardInput.builder()
      .tokenizedCard(tokenizedCard)
      .customerCode(customerId.toString())
      .build();

    FastpayCreditCardResponse response = fastpayCreditCardApiClient.create(input);

    return toLimitedCreditCard(response);
  }

  @Override
  public Optional<LimitedCreditCard> findById(String gatewayCode) {
    FastpayCreditCardResponse response = fastpayCreditCardApiClient.findById(gatewayCode);
    return Optional.ofNullable(toLimitedCreditCard(response));
  }

  @Override
  public void delete(String gatewayCode) {
    fastpayCreditCardApiClient.delete(gatewayCode);
  }

  private LimitedCreditCard toLimitedCreditCard(FastpayCreditCardResponse response) {
    return LimitedCreditCard.builder()
      .gatewayCode(response.getId())
      .lastNumbers(response.getLastName())
      .brand(response.getBrand())
      .expMonth(response.getExpMonth())
      .expYear(response.getExpYear())
      .build();
  }
}
