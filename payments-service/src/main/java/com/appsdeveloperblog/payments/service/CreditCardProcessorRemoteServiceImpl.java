package com.appsdeveloperblog.payments.service;

import com.appsdeveloperblog.core.dto.CreditCardProcessRequest;
import com.appsdeveloperblog.core.exceptions.CreditCardProcessorUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CreditCardProcessorRemoteServiceImpl implements CreditCardProcessorRemoteService {
    private static final Logger logger = LoggerFactory.getLogger(CreditCardProcessorRemoteServiceImpl.class);
    private final RestTemplate restTemplate;
    private final String ccpRemoteServiceUrl;

    public CreditCardProcessorRemoteServiceImpl(
            RestTemplate restTemplate,
            @Value("${remote.ccp.url}") String ccpRemoteServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.ccpRemoteServiceUrl = ccpRemoteServiceUrl;
    }

    @Override
    @CircuitBreaker(name = "default", fallbackMethod = "processFallback")
    @Retry(name = "default", fallbackMethod = "processFallback")
    public void process(BigInteger cardNumber, BigDecimal paymentAmount) {
        try {
            var request = new CreditCardProcessRequest(cardNumber, paymentAmount);
            restTemplate.postForObject(ccpRemoteServiceUrl + "/ccp/process", request, CreditCardProcessRequest.class);
        } catch (ResourceAccessException e) {
            throw new CreditCardProcessorUnavailableException(e);
        }
    }

    public void processFallback(BigInteger cardNumber, BigDecimal paymentAmount, Throwable throwable) {
        logger.error("Failed to process payment for card {}. Error: {}", cardNumber, throwable.getMessage());
        throw new CreditCardProcessorUnavailableException(throwable);
    }
}
