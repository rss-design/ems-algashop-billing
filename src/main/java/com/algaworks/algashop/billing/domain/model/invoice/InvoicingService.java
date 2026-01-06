package com.algaworks.algashop.billing.domain.model.invoice;

import com.algaworks.algashop.billing.domain.model.DomainException;
import com.algaworks.algashop.billing.domain.model.invoice.payment.Payment;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoicingService {

    private final InvoiceRepository invoiceRepository;

    public Invoice issue(String orderId, UUID customerId, Payer payer, Set<LineItem> items) {
        if (invoiceRepository.existsByOrderId(orderId)) {
            throw new DomainException(String.format("Invoice already exists for ordeer %s",  orderId));
        }
        return Invoice.issue(orderId, customerId, payer, items);
    }

    public void assignPayment(Invoice invoice, Payment payment) {
        invoice.assignPaymentGatewayCode(payment.getGatewayCode());
        switch (payment.getStatus()) {
            case FAILED -> {
                invoice.cancel("Payment failed");
            }
            case REFUNDED -> {
                invoice.cancel("Payment refunded");
            }
            case PAID -> {
                invoice.markAsPaid();
            }
        }
    }

}
