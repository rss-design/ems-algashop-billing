package com.algaworks.algashop.billing.infrastructure;

import com.algaworks.algashop.billing.domain.model.creditcard.LimitedCreditCard;
import com.algaworks.algashop.billing.infrastructure.creditcard.fastpay.CreditCardProviderServiceFastpayImpl;
import com.algaworks.algashop.billing.infrastructure.creditcard.fastpay.FastpayCreditCardTokenizationAPIClientConfig;
import com.algaworks.algashop.billing.infrastructure.creditcard.fastpay.FastpayCreditCardTokenizationApiClient;
import com.algaworks.algashop.billing.infrastructure.creditcard.fastpay.FastpayTokenizationInput;
import com.algaworks.algashop.billing.infrastructure.creditcard.fastpay.FastpayTokenizedCreditCardModel;
import java.time.Year;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(FastpayCreditCardTokenizationAPIClientConfig.class)
public abstract class AbstractFastpayIT {

  @Autowired
  protected CreditCardProviderServiceFastpayImpl creditCardProvider;

  @Autowired
  protected FastpayCreditCardTokenizationApiClient tokenizationAPIClient;

  protected static final UUID validCustomerId = UUID.randomUUID();
  protected static final String alwaysPaidCardNumber = "4622943127011022";

  protected LimitedCreditCard registerCard() {
    FastpayTokenizationInput input = FastpayTokenizationInput.builder()
      .number(alwaysPaidCardNumber)
      .cvv("222")
      .expMonth(1)
      .holderName("John Doe")
      .holderDocument("12345")
      .expYear(Year.now().getValue() + 5)
      .build();

    FastpayTokenizedCreditCardModel response = tokenizationAPIClient.tokenize(input);
    return creditCardProvider.register(validCustomerId, response.getTokenizedCard());
  }
}