package com.ot.moto.dao;

import com.ot.moto.entity.Orders;
import com.ot.moto.repository.OrdersRepository;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
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
}
