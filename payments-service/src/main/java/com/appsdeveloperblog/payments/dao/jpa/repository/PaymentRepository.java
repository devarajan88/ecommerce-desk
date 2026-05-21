package com.appsdeveloperblog.payments.dao.jpa.repository;

import com.appsdeveloperblog.payments.dao.jpa.entity.PaymentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends MongoRepository<PaymentEntity, UUID> {
}
