package com.ot.moto.dao;

import com.ot.moto.entity.Orders;
import com.ot.moto.repository.OrdersRepository;
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
public class OrderDao {

    @Autowired
    private OrdersRepository ordersRepository;

    public List<Orders> createOrders(List<Orders> ordersList){
        return ordersRepository.saveAll(ordersList);
    }

    public Orders checkOrderValid(String driverName, LocalDate date){
        Optional<Orders> orders = ordersRepository.findByDateAndDriverName(date,driverName);
        return orders.orElse(null);
    }

    public Page<Orders> findAll(int offset, int pageSize, String field) {
        return ordersRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }
    public long sumTotalOrdersOnDate(LocalDate date) {
        Long sumTotalDate = ordersRepository.sumTotalOrdersOnDate(date);
        return Objects.isNull(sumTotalDate) ? 0 : sumTotalDate;
    }

    public long sumTotalOrdersBetweenDates(LocalDate startOfMonth, LocalDate endOfMonth) {
        Long sumTotalBetweenDates = ordersRepository.sumTotalOrdersBetweenDates(startOfMonth,endOfMonth);
        return Objects.isNull(sumTotalBetweenDates) ? 0: sumTotalBetweenDates;
    }

    public Double getArrearsForToday() {
        LocalDate today = LocalDate.now();
        Double sum = ordersRepository.sumAmountOnDate(today);
        return (sum != null) ? sum : 0;
    }
}
