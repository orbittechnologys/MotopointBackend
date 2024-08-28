package com.ot.moto.service;

import com.ot.moto.dao.BonusDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.AddBonusOrders;
import com.ot.moto.entity.Bonus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class BonusService {

    @Autowired
    private BonusDao bonusDao;

    private static final Logger logger = LoggerFactory.getLogger(BonusService.class);

    public ResponseEntity<ResponseStructure<Object>> addBonusOrders(AddBonusOrders addBonusOrders) {
        try {
            Bonus bonus = new Bonus();
            bonus.setDeliveryCount(addBonusOrders.getDeliveryCount());
            bonus.setBonusAmount(addBonusOrders.getBonusAmount());
            bonus.setSpecialDate(null);
            bonus.setDateBonusAmount(0.0);
            bonus = bonusDao.saveBonus(bonus);
            return ResponseStructure.successResponse(bonus, "Bonus for Orders created successfully");
        } catch (Exception e) {
            logger.error("Error while creating Master", e);
            return ResponseStructure.errorResponse(null, 500, "Error while creating master: " + e.getMessage());
        }
    }



}