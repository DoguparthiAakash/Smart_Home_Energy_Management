package com.smarthome.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String twoFactorSecret;
    private String idDocumentPath;
    private boolean isApproved = true; // Default to true, Technician will be false

    private Double maxWattage = 5000.0; // Default limit per user home

    public enum Role {
        ADMIN,
        HOMEOWNER,
        TECHNICIAN,
        GUEST
    }
}
