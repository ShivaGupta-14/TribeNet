package org.tribenet.tribenet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tribenet.tribenet.model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserId(Long userId);

    Optional<Payment> findByOrderId(String orderId);
}
