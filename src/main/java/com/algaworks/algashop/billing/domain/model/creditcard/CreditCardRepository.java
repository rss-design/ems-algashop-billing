package com.algaworks.algashop.billing.domain.model.creditcard;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {

    boolean existsByIdAndCustomerId(UUID creditCardId, UUID customerId);

}
