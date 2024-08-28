package com.ot.moto.service;

import com.ot.moto.dao.BonusDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.AddBonusDate;
import com.ot.moto.dto.request.AddBonusOrders;
import com.ot.moto.dto.request.UpdateBonusDate;
import com.ot.moto.dto.request.UpdateBonusOrders;
import com.ot.moto.entity.Bonus;
import com.ot.moto.entity.Master;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
            bonus.setDateBonusAmount(null);
            bonus = bonusDao.saveBonus(bonus);
            return ResponseStructure.successResponse(bonus, "Bonus for Orders created successfully");
        } catch (Exception e) {
            logger.error("Error while creating Master", e);
            return ResponseStructure.errorResponse(null, 500, "Error while creating master: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> addBonusDate(AddBonusDate addBonusDate) {
        try {
            Bonus bonus = new Bonus();
            bonus.setDeliveryCount(null);
            bonus.setBonusAmount(null);
            bonus.setSpecialDate(addBonusDate.getSpecialDate());
            bonus.setDateBonusAmount(addBonusDate.getDateBonusAmount());
            bonus = bonusDao.saveBonus(bonus);
            return ResponseStructure.successResponse(bonus, "Bonus for Date created successfully");
        } catch (Exception e) {
            logger.error("Error while creating Master", e);
            return ResponseStructure.errorResponse(null, 500, "Error while creating master: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> updateBonusOrders(UpdateBonusOrders updateBonusOrders) {
        try {
            Bonus bonus = fetchBonus(updateBonusOrders.getId());
            if (Objects.isNull(bonus)) {
                logger.warn("No bonus found with id: {}", updateBonusOrders.getId());
                return ResponseStructure.errorResponse(null, 404, "Bonus not found with id: " + updateBonusOrders.getId());
            }
            bonus.setDeliveryCount(updateBonusOrders.getDeliveryCount());
            bonus.setBonusAmount(updateBonusOrders.getBonusAmount());
            bonus.setSpecialDate(null);
            bonus.setDateBonusAmount(null);
            bonus = bonusDao.saveBonus(bonus);
            return ResponseStructure.successResponse(bonus, "Bonus for Orders Updated successfully");
        } catch (Exception e) {
            logger.error("Error while Updating Bonus Orders", e);
            return ResponseStructure.errorResponse(null, 500, "Error while Updating Bonus Orders: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> updateBonusDate(UpdateBonusDate updateBonusDate) {
        try {
            Bonus bonus = fetchBonus(updateBonusDate.getId());
            if (Objects.isNull(bonus)) {
                logger.warn("No bonus found with id: {}", updateBonusDate.getId());
                return ResponseStructure.errorResponse(null, 404, "Bonus not found with id: " + updateBonusDate.getId());
            }
            bonus.setDeliveryCount(null);
            bonus.setBonusAmount(null);
            bonus.setSpecialDate(updateBonusDate.getSpecialDate());
            bonus.setDateBonusAmount(updateBonusDate.getDateBonusAmount());
            bonus = bonusDao.saveBonus(bonus);
            return ResponseStructure.successResponse(bonus, "Bonus for Date Updated successfully");
        } catch (Exception e) {
            logger.error("Error while Updating Bonus Date", e);
            return ResponseStructure.errorResponse(null, 500, "Error while Updating Bonus Date: " + e.getMessage());
        }
    }

    private Bonus fetchBonus(Long id) {
        Bonus bonus = bonusDao.findById(id);
        if (Objects.isNull(bonus)) {
            logger.warn("No Master found. Invalid ID: {}", id);
            return null;
        }
        return bonus;
    }

    public ResponseEntity<ResponseStructure<Object>> getAll(int page, int size, String field) {
        try {
            Page<Bonus> bonuses = bonusDao.findAll(page, size, field);
            if (bonuses.isEmpty()) {
                logger.warn("No Master Data found.");
                return ResponseStructure.errorResponse(null, 404, "No Bonus Data found");
            }
            return ResponseStructure.successResponse(bonuses, "Master Data  found");
        } catch (Exception e) {
            logger.error("Error fetching Master Data ", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> deleteBonusById(Long id) {
        try {
            Bonus bonus = fetchBonus(id);
            if (bonus == null) {
                logger.warn("Bonus not found with ID: {}", id);
                return ResponseStructure.errorResponse(null, 404, "Bonus ID not found");
            }
            bonusDao.delete(bonus);
            logger.info("Bonus data deleted successfully with ID: {}", id);
            return ResponseStructure.successResponse(null, "Bonus ID deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting Bonus data with ID: {}", id, e);
            return ResponseStructure.errorResponse(null, 500, "Failed to delete Bonus ID: " + id);
        }
    }

}