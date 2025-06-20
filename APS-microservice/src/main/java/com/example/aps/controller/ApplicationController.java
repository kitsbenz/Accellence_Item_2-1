package com.example.aps.controller;

import com.example.aps.dto.ApplicationRequest;
import com.example.aps.dto.ApplicationResponse;
import com.example.aps.entity.Application;
import com.example.aps.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/applications")
@CrossOrigin(origins = "http://localhost:4200")
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> submitApplication(@Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = service.submitApplication(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/applications/{id}/credit-check-status")
    public ResponseEntity<String> updateCreditCheckStatus(@PathVariable Long id,
                                                          @RequestBody String newStatus) {
        Optional<Application> appOpt = service.getApplication(id);
        if (appOpt.isPresent()) {
            Application app = appOpt.get();
            app.setCreditCheckStatus(newStatus.toUpperCase());
            service.saveApplication(app);
            return ResponseEntity.ok("Credit check status updated to " + newStatus);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplication(@PathVariable Long id) {
        Optional<Application> appOpt = service.getApplication(id);
        return appOpt.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        List<Application> applications = service.getAllApplications();
        return ResponseEntity.ok(applications);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Application> updateApplication(@PathVariable Long id, @RequestBody Application updatedApp) {
        Optional<Application> existingAppOpt = service.getApplication(id);
        if (existingAppOpt.isPresent()) {
            Application existingApp = existingAppOpt.get();
            existingApp.setApplicantName(updatedApp.getApplicantName());
            existingApp.setMonthlyIncome(updatedApp.getMonthlyIncome());
            existingApp.setMonthlyExpense(updatedApp.getMonthlyExpense());
            existingApp.setRepaymentCapacity(updatedApp.getMonthlyIncome() - updatedApp.getMonthlyExpense());
            existingApp.setUpdatedAt(LocalDateTime.now());
            existingApp.setFinancialStatus(existingApp.getRepaymentCapacity() > 0 ? "SUFFICIENT" : "INSUFFICIENT");
            existingApp.setCreditCheckStatus(updatedApp.getCreditCheckStatus()); // อาจเลือกไม่ให้แก้ไขก็ได้

            Application saved = service.saveApplication(existingApp);
            return ResponseEntity.ok(saved);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        boolean deleted = service.deleteApplication(id);
        if (deleted) {
            return ResponseEntity.noContent().build(); // 204
        } else {
            return ResponseEntity.notFound().build(); // 404
        }
    }


}
