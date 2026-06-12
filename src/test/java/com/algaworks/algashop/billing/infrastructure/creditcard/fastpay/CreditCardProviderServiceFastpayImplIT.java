package com.algaworks.algashop.billing.infrastructure.creditcard.fastpay;

import com.algaworks.algashop.billing.domain.model.creditcard.LimitedCreditCard;
import java.time.Year;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(FastpayCreditCardTokenizationAPIClientConfig.class)
class CreditCardProviderServiceFastpayImplIT {

  @Autowired
  private CreditCardProviderServiceFastpayImpl creditCardProvider;

  @Autowired
  private FastpayCreditCardTokenizationApiClient tokenizationApiClient;

  private static final UUID validCustomerId = UUID.randomUUID();
  private static final String alwaysPaidCardNumber = "4622943127011022";

  @Test
  void shouldRegisterCreditCard() {
    LimitedCreditCard limitedCreditCard = registerCard();
    Assertions.assertThat(limitedCreditCard.getGatewayCode()).isNotBlank();
  }

  @Test
  void shouldFindRegisteredCreditCard() {
    LimitedCreditCard limitedCreditCard = registerCard();

    LimitedCreditCard limitedCreditCardFound = creditCardProvider.findById(limitedCreditCard.getGatewayCode()).orElseThrow();

    Assertions.assertThat(limitedCreditCardFound.getGatewayCode()).isEqualTo(limitedCreditCard.getGatewayCode());
  }

  @Test
  void shouldDeleteRegisteredCreditCard() {
    LimitedCreditCard limitedCreditCard = registerCard();

    creditCardProvider.delete(limitedCreditCard.getGatewayCode());

    Optional<LimitedCreditCard> possibleCreditCard = creditCardProvider.findById(limitedCreditCard.getGatewayCode());

    Assertions.assertThat(possibleCreditCard).isEmpty();
  }

  private LimitedCreditCard registerCard() {
    FastpayTokenizationInput input = FastpayTokenizationInput.builder()
      .number(alwaysPaidCardNumber)
      .cvv("222")
      .expMonth(1)
      .holderName("John Doe")
      .holderDocument("12345")
      .expYear(Year.now().getValue() + 5)
      .build();

    FastpayTokenizedCreditCardModel response = tokenizationApiClient.tokenize(input);
    return creditCardProvider.register(validCustomerId, response.getTokenizedCard());
  }

}