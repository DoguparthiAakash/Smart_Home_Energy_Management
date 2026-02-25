package com.smarthome.backend.repository;

import com.smarthome.backend.model.BlockchainBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BlockchainBlockRepository extends JpaRepository<BlockchainBlock, Long> {
    Optional<BlockchainBlock> findTopByOrderByIdDesc();
}
