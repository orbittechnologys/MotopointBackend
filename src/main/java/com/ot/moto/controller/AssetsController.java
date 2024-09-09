package com.ot.moto.controller;


import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.Assets;
import com.ot.moto.service.AssetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assets")
public class AssetsController {

    @Autowired
    private AssetsService assetsService;

    @PostMapping("/saveAllForDriver/{driverId}")
    public ResponseEntity<ResponseStructure<Object>> saveAllAssetsForDriver(@PathVariable Long driverId,
                                                                            @RequestBody List<Assets> assetsList) {
        return assetsService.saveAllAssetsForDriver(driverId, assetsList);
    }

    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAllAssets(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  @RequestParam(defaultValue = "id") String sortBy) {
        return assetsService.getAllAssets(page, size, sortBy);
    }

    @GetMapping("findById/{id}")
    public ResponseEntity<ResponseStructure<Object>> getAssetById(@PathVariable Long id) {
        return assetsService.getAssetById(id);
    }
}
