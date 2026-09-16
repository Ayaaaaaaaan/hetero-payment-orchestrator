package com.ayaan.execution.provider;

import com.ayaan.execution.model.ExecutionRequest;
import com.ayaan.execution.model.ExecutionResult;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PayPalProvider implements PaymentProvider {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Override
    public ExecutionResult processPayment(ExecutionRequest request) {
        try {
            APIContext apiContext = new APIContext(clientId, clientSecret, mode);

            Amount amount = new Amount();
            amount.setCurrency(request.getCurrency());
            amount.setTotal(String.format("%.2f", request.getAmount()));

            Transaction transaction = new Transaction();
            transaction.setDescription("Payment for: " + request.getTransactionId());
            transaction.setAmount(amount);

            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);

            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");

            Payment payment = new Payment();
            payment.setIntent("sale");
            payment.setPayer(payer);
            payment.setTransactions(transactions);

            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setCancelUrl("http://localhost:3000/cancel");
            redirectUrls.setReturnUrl("http://localhost:3000/success");
            payment.setRedirectUrls(redirectUrls);

            Payment createdPayment = payment.create(apiContext);

            log.info("PayPal payment created: {}", createdPayment.getId());

            return ExecutionResult.builder()
                    .transactionId(request.getTransactionId())
                    .providerTransactionId(createdPayment.getId())
                    .status("SUCCESS")
                    .provider("paypal")
                    .message("PayPal payment created. Approve at: " +
                            createdPayment.getLinks().stream()
                                    .filter(l -> "approval_url".equals(l.getRel()))
                                    .findFirst()
                                    .map(Links::getHref)
                                    .orElse("N/A"))
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (PayPalRESTException e) {
            log.error("PayPal error: {}", e.getMessage());
            return ExecutionResult.builder()
                    .transactionId(request.getTransactionId())
                    .status("FAILED")
                    .provider("paypal")
                    .message("PayPal error: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    @Override
    public String getProviderName() {
        return "paypal";
    }
}