package com.smarthome.backend.controller;

import com.smarthome.backend.model.BlockchainBlock;
import com.smarthome.backend.repository.BlockchainBlockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/blockchain")
public class BlockchainController {

    @Autowired
    private BlockchainBlockRepository blockRepository;

    @GetMapping
    public List<BlockchainBlock> getLedger() {
        return blockRepository.findAll();
    }
}
