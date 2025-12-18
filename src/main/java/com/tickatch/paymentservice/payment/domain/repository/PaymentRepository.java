package com.tickatch.paymentservice.payment.domain.repository;

import com.tickatch.paymentservice.payment.domain.Payment;
import com.tickatch.paymentservice.payment.domain.PaymentId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, PaymentId> {

  Optional<Payment> findByOrderId(UUID orderId);

  @Query(
      """
        select distinct p
        from Payment p join p.links l
        where l.reservationId in :reservationIds
      """)
  List<Payment> findPaymentsByReservationIds(@Param("reservationIds") List<String> reservationIds);
}
