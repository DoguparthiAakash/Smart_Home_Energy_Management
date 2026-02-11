package com.smarthome.backend.service;

import com.smarthome.backend.model.UsageLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UsageLogServiceTest {

    @InjectMocks
    private UsageLogService usageLogService;

    @Test
    public void testCalculateCost_Tier1_Free() {
        // 50 units (<= 100) should be free
        Double cost = usageLogService.calculateCost(50.0);
        assertEquals(0.0, cost, 0.001);
    }

    @Test
    public void testCalculateCost_Tier2() {
        // 150 units: First 100 free, next 50 @ 4.80
        // Expected: 50 * 4.80 = 240.0
        Double cost = usageLogService.calculateCost(150.0);
        assertEquals(240.0, cost, 0.001);
    }

    @Test
    public void testCalculateCost_Tier3() {
        // 450 units:
        // 100 Free
        // 300 @ 4.80 = 1440
        // 50 @ 6.45 = 322.5
        // Total = 1762.5
        Double cost = usageLogService.calculateCost(450.0);
        assertEquals(1762.5, cost, 0.001);
    }

    @Test
    public void testCalculateCostNull() {
        Double cost = usageLogService.calculateCost(null);
        assertEquals(0.0, cost, 0.001);
    }

    @Test
    public void testCalculateTotalCost() {
        UsageLog log1 = new UsageLog();
        log1.setEnergyKwh(50.0); // Free
        UsageLog log2 = new UsageLog();
        log2.setEnergyKwh(100.0); // 50 Free, 50 @ 4.80
        
        // Total = 150.0
        // First 100 Free
        // Next 50 @ 4.80 = 240.0
        List<UsageLog> logs = Arrays.asList(log1, log2);
        
        Double totalCost = usageLogService.calculateTotalCost(logs);
        assertEquals(240.0, totalCost, 0.001);
    }
}
