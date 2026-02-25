package com.smarthome.backend.service;

import com.smarthome.backend.model.BlockchainBlock;
import com.smarthome.backend.repository.BlockchainBlockRepository;
import com.smarthome.backend.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BlockchainService {

    @Autowired
    private BlockchainBlockRepository blockRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Scheduled(fixedRate = 2000)
    public void backupSystemData() {
        double totalLoad = deviceRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getStatus()))
                .mapToDouble(d -> d.getPowerRating() != null ? d.getPowerRating() : 0)
                .sum();

        long deviceCount = deviceRepository.count();
        String data = String.format("{\"totalLoad\": %.2f, \"deviceCount\": %d}", totalLoad, deviceCount);

        Optional<BlockchainBlock> lastBlock = blockRepository.findTopByOrderByIdDesc();
        String previousHash = lastBlock.map(BlockchainBlock::getHash).orElse("0");

        LocalDateTime timestamp = LocalDateTime.now();
        String currentHash = calculateHash(previousHash, timestamp.toString(), data);

        BlockchainBlock newBlock = new BlockchainBlock(timestamp, data, previousHash, currentHash);
        blockRepository.save(newBlock);
    }

    private String calculateHash(String previousHash, String timestamp, String data) {
        String input = previousHash + timestamp + data;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
