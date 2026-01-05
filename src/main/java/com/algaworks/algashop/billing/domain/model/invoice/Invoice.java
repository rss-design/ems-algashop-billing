package com.algaworks.algashop.billing.domain.model.invoice;

import com.algaworks.algashop.billing.domain.model.DomainException;
import com.algaworks.algashop.billing.domain.model.IdGenerator;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.util.Collections;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Invoice {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    private String orderId;
    private UUID customerId;

    private OffsetDateTime issuedAt;
    private OffsetDateTime paidAt;
    private OffsetDateTime canceledAt;
    private OffsetDateTime expiresAt;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private PaymentSettings paymentSettings;

    private Set<LineItem> items = new HashSet<>();

    private Payer payer;

    private String cancelReason;

    public static Invoice issue(String orderId,
                                UUID customerId,
                                Payer payer,
                                Set<LineItem> items) {
        Objects.requireNonNull(customerId);
        Objects.requireNonNull(payer);
        Objects.requireNonNull(items);

        if (StringUtils.isBlank(orderId)) {
            throw new IllegalArgumentException("Order ID cannot be blank");
        }

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Items cannot be empty");
        }

        BigDecimal totalAmount = items.stream()
            .map(LineItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Invoice(
            IdGenerator.generateTimeBasedUUID(),
            orderId,
            customerId,
            OffsetDateTime.now(),
            null,
            null,
            OffsetDateTime.now().plusDays(3),
            totalAmount,
            InvoiceStatus.UNPAID,
            null,
            items,
            payer,
            null
        );
    }

    public Set<LineItem> getItems() {
        return Collections.unmodifiableSet(items);
    }

    public boolean isCanceled() {
        return InvoiceStatus.CANCELED.equals(this.getStatus());
    }

    public boolean isUnPaid() {
        return InvoiceStatus.UNPAID.equals(this.getStatus());
    }

    public boolean isPaid() {
        return InvoiceStatus.PAID.equals(this.getStatus());
    }

    public void markAsPaid() {
        if (!isUnPaid()) {
            throw new DomainException(
                String.format("Invoice %s with status %s cannot be marked as paid",
                    this.getId(), this.getStatus().toString().toLowerCase()));
        }
        setPaidAt(OffsetDateTime.now());
        setStatus(InvoiceStatus.PAID);
    }

    public void cancel(String cancelReason) {
        if (isCanceled()) {
            throw new DomainException(
                String.format("Invoice %s is already canceled", this.getId()));
        }
        setCancelReason(cancelReason);
        setCanceledAt(OffsetDateTime.now());
        setStatus(InvoiceStatus.CANCELED);
    }

    public void assignPaymentGatewayCode(String code) {
        if (!isUnPaid()) {
            throw new DomainException(
                String.format("Invoice %s with status %s cannot be edited",
                    this.getId(), this.getStatus().toString().toLowerCase()));
        }
        if (this.getPaymentSettings() == null) {
            throw new DomainException("Invoice has no payment settings");
        }
        this.getPaymentSettings().assignGatewayCode(code);
    }

    public void changePaymentSettings(PaymentMethod method, UUID creditCardId) {
        if (!isUnPaid()) {
            throw new DomainException(
                String.format("Invoice %s with status %s cannot be edited",
                    this.getId(), this.getStatus().toString().toLowerCase()));
        }
        PaymentSettings paymentSettings = PaymentSettings.brandNew(method, creditCardId);
        paymentSettings.setInvoice(this);
        this.setPaymentSettings(paymentSettings);
    }

}