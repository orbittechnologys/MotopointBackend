package com.ot.moto.service;

import com.ot.moto.dao.MasterDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateMasterReq;
import com.ot.moto.entity.Admin;
import com.ot.moto.entity.Master;
import com.ot.moto.repository.MasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MasterService {

    @Autowired
    private MasterDao masterDao;

    @Autowired
    private MasterRepository masterRepository;

    private static final Logger logger = LoggerFactory.getLogger(MasterService.class);

    public ResponseEntity<ResponseStructure<Object>> createMaster(CreateMasterReq req){

        try{
            Master master = masterDao.getMasterBySlab(req.getSlab());

            if(Objects.nonNull(master)){
                logger.error("Slab already exists", req.getSlab());
                return ResponseStructure.errorResponse(null, 409, "Slab already exists"+ req.getSlab());
            }

            master = buildMasterFromRequest(req);
            master = masterDao.createMaster(master);

            return ResponseStructure.successResponse(master,"Master created successfully");
        }catch (Exception e){
            logger.error("Error while creating Master", e);
            return ResponseStructure.errorResponse(null, 500, "Error while creating master: " + e.getMessage());
        }

    }

    private Master buildMasterFromRequest(CreateMasterReq req){
        Master master = new Master();
        master.setSlab(req.getSlab());
        master.setStartKm(req.getStartKm());
        master.setEndKm(req.getEndKm());
        master.setJahezPaid(req.getJahezPaid());
        master.setMotoPaid(req.getMotoPaid());

        return master;

    }

    public ResponseEntity<ResponseStructure<Object>> getMasterBySlab(String slab){
        try{
            Master master = masterDao.getMasterBySlab(slab);

            if (Objects.isNull(master)) {
                return ResponseStructure.errorResponse(null,404,"Master not found with slab:"+slab);
            }

            return ResponseStructure.successResponse(master,"Master Fetched successfully");
        }catch (Exception e){
            logger.error("Error while fetching Master", e);
            return ResponseStructure.errorResponse(null, 500, "Error while fetching master: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getAllByMaster(int page, int size, String field) {
        try {

            Page<Master> payments = masterDao.findAll(page, size, field);
            if (payments.isEmpty()) {
                logger.warn("No Master Data found.");
                return ResponseStructure.errorResponse(null, 404, "No Master Data found");
            }
            return ResponseStructure.successResponse(payments, "Master Data  found");
        } catch (Exception e) {
            logger.error("Error fetching Master Data ", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> deleteAdmin(Long masterId) {
        try {

            Master master = masterDao.getMasterById(masterId);
            if (master == null) {
                logger.warn("Master not found with ID: {}", masterId);
                return ResponseStructure.errorResponse(null, 404, "Master ID not found");
            }

            masterDao.delete(master);
            logger.info("Master data deleted successfully with ID: {}", masterId);
            return ResponseStructure.successResponse(null, "Master ID deleted successfully");

        } catch (Exception e) {
            logger.error("Error deleting Master data with ID: {}", masterId, e);
            return ResponseStructure.errorResponse(null, 500, "Failed to delete Master ID: " + masterId);
        }
    }

}
