package com.ot.moto.dao;

import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Payment;
import com.ot.moto.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class PaymentDao {

    @Autowired
    public PaymentRepository paymentRepository;


    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Payment getById(Long id) {
        Optional<Payment> payment = paymentRepository.findById(id);
        return payment.orElse(null);
    }

    public Page<Payment> findAll(int offset, int pageSize, String field) {
        return paymentRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }

    public boolean existsByDriverIdAndDate(Long driverId, LocalDate date) {
        return paymentRepository.existsByDriverIdAndDate(driverId, date);
    }

    public Double getSumAmountForYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Double sum = paymentRepository.sumAmountOnDate(yesterday);
        return Objects.isNull(sum) ? 0 : sum;
    }

    public Double getSumOfCurrentMonth(LocalDate startDate, LocalDate endDate) {
        Double sumCurrentMonth = paymentRepository.sumAmountForCurrentMonth(startDate, endDate);
        return Objects.isNull(sumCurrentMonth) ? 0 : sumCurrentMonth;
    }

    public List<Payment> findPaymentByDriverNameContaining(String name) {
        return paymentRepository.findPaymentsByDriverNameContaining(name);
    }

    public Double findTotalBenefitAmountByDriver(Long driverId){
        Double amount = paymentRepository.findTotalAmount();
        return Objects.isNull(amount) ? 0 : amount ;
    }

    public Double findTotalBenefitAmount(){
        Double amount = paymentRepository.findTotalAmount();
        return Objects.isNull(amount) ? 0 : amount ;
    }

    public Payment findByDriverAndDate(Driver driver, LocalDate date){
        Optional<Payment> paymentOptional = paymentRepository.findByDriverAndDate(driver,date);
        return paymentOptional.orElse(null);
    }
}