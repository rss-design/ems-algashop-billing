package com.algaworks.algashop.billing.domain.model.invoice;

import com.algaworks.algashop.billing.domain.model.DomainException;
import com.algaworks.algashop.billing.domain.model.IdGenerator;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentSettings {

    @EqualsAndHashCode.Include
    private UUID id;
    private UUID creditCardId;
    private String gatewayCode;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @OneToOne(mappedBy = "paymentSettings")
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PACKAGE)
    private Invoice invoice;

    static PaymentSettings brandNew(PaymentMethod method, UUID creditCardId) {
        Objects.requireNonNull(method);
        if (method.equals(PaymentMethod.CREDIT_CARD)) {
            Objects.requireNonNull(creditCardId);
        }
        return new PaymentSettings(
            IdGenerator.generateTimeBasedUUID(),
            creditCardId,
            null,
            method,
            null
        );
    }

    void assignGatewayCode(String gatewayCode) {
        if (StringUtils.isBlank(gatewayCode)) {
            throw new IllegalArgumentException("Gateway code cannot be empty");
        }
        if (this.getGatewayCode() != null) {
            throw new DomainException("Gateway code already exists");
        }
        setGatewayCode(gatewayCode);
    }
}