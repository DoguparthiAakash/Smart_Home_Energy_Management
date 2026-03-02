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
    public void testCalculateCost_Tier1() {
        // 50 units @ 5.0 = 250.0
        Double cost = usageLogService.calculateCost(50.0);
        assertEquals(250.0, cost, 0.001);
    }

    @Test
    public void testCalculateCost_Tier2() {
        // 150 units:
        // 50 @ 5.0 = 250
        // 100 @ 7.0 = 700
        // Total = 950.0
        Double cost = usageLogService.calculateCost(150.0);
        assertEquals(950.0, cost, 0.001);
    }

    @Test
    public void testCalculateCost_Tier3() {
        // 450 units:
        // 50 @ 5.0 = 250
        // 150 @ 7.0 = 1050
        // 250 @ 9.0 = 2250
        // Total = 3550.0
        Double cost = usageLogService.calculateCost(450.0);
        assertEquals(3550.0, cost, 0.001);
    }

    @Test
    public void testCalculateCostNull() {
        Double cost = usageLogService.calculateCost(null);
        assertEquals(0.0, cost, 0.001);
    }

    @Test
    public void testCalculateTotalCost() {
        UsageLog log1 = new UsageLog();
        log1.setEnergyKwh(50.0);
        UsageLog log2 = new UsageLog();
        log2.setEnergyKwh(100.0);

        // Total = 150.0
        // 50 @ 5.0 = 250
        // 100 @ 7.0 = 700
        // Total = 950.0
        List<UsageLog> logs = Arrays.asList(log1, log2);

        Double totalCost = usageLogService.calculateTotalCost(logs);
        assertEquals(950.0, totalCost, 0.001);
    }
}
