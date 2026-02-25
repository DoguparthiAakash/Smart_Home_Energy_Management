package com.smarthome.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "blockchain_ledger")
public class BlockchainBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String data;

    @Column(nullable = false)
    private String hash;

    @Column(nullable = false)
    private String previousHash;

    public BlockchainBlock() {
    }

    public BlockchainBlock(LocalDateTime timestamp, String data, String previousHash, String hash) {
        this.timestamp = timestamp;
        this.data = data;
        this.previousHash = previousHash;
        this.hash = hash;
    }
}
