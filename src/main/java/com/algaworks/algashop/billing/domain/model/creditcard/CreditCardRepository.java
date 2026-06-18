package com.algaworks.algashop.billing.domain.model.creditcard;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {
  Optional<CreditCard> findByCustomerIdAndId(UUID customerId, UUID creditCardId);
  List<CreditCard> findAllByCustomerId(UUID customerId);
}
