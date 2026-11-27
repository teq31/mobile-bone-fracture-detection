package com.bonefracture.api;

import com.bonefracture.performance.OperationStats;
import com.bonefracture.performance.PerformanceMonitor;
import com.bonefracture.security.RequiresRole;
import com.bonefracture.user.Role;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// timings from PerformanceMonitoringAspect, like /api/cache/stats for the cache
@RestController
@RequestMapping("/api/performance/stats")
public class PerformanceController {

    @GetMapping
    @RequiresRole(Role.ADMINISTRATOR)
    public List<OperationStats> stats() {
        return PerformanceMonitor.all();
    }

    @DeleteMapping
    @RequiresRole(Role.ADMINISTRATOR)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset() {
        PerformanceMonitor.reset();
    }
}
