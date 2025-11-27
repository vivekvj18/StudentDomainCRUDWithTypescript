package com.domaincrud.domaincrud.controller;

import com.domaincrud.domaincrud.entity.Domain;
import com.domaincrud.domaincrud.entity.Student;
import com.domaincrud.domaincrud.repository.StudentRepository;
import com.domaincrud.domaincrud.service.DomainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.domaincrud.domaincrud.exception.ResourceNotFoundException;
import com.domaincrud.domaincrud.dto.DomainRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;



import java.util.List;
import java.util.Optional;


@CrossOrigin(origins = { "http://127.0.0.1:5500", "http://localhost:5500" })

@RestController
@RequestMapping("/api/domains")
public class DomainController {

    private final DomainService domainService;
    private final StudentRepository studentRepository;

    public DomainController(DomainService domainService, StudentRepository studentRepository) {
        this.domainService = domainService;
        this.studentRepository = studentRepository;
    }

    // 1. Create a new domain
    @PostMapping
    public ResponseEntity<Domain> createDomain(@Valid @RequestBody DomainRequest request) {

        // DTO -> Entity mapping
        Domain domain = new Domain();
        domain.setProgram(request.getProgram());
        domain.setBatch(request.getBatch());
        domain.setCapacity(request.getCapacity());
        domain.setQualification(request.getQualification());

        Domain saved = domainService.createDomain(domain);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }


    // 2. Get all domains
    @GetMapping
    public ResponseEntity<List<Domain>> getAllDomains() {
        List<Domain> domains = domainService.getAllDomains();
        return ResponseEntity.ok(domains);
    }

    // 3. Get single domain by ID
    @GetMapping("/{id}")
    public ResponseEntity<Domain> getDomainById(@PathVariable Long id) {
        Domain domain = domainService.getDomainById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found with id: " + id));

        return ResponseEntity.ok(domain);
    }


    // 4. Update domain by ID
    @PutMapping("/{id}")
    public ResponseEntity<Domain> updateDomain(@PathVariable Long id,
                                               @Valid @RequestBody DomainRequest request) {

        Domain domain = new Domain();
        domain.setProgram(request.getProgram());
        domain.setBatch(request.getBatch());
        domain.setCapacity(request.getCapacity());
        domain.setQualification(request.getQualification());

        Domain updated = domainService.updateDomain(id, domain);
        return ResponseEntity.ok(updated);
    }


    // 5. Delete domain by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDomain(@PathVariable Long id) {
        Optional<Domain> existingOpt = domainService.getDomainById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        domainService.deleteDomain(id);
        return ResponseEntity.noContent().build();  // 204 No Content
    }

    // 6. Get students belonging to a particular domain
    @GetMapping("/{id}/students")
    public ResponseEntity<List<Student>> getStudentsByDomain(@PathVariable Long id) {
        List<Student> students = studentRepository.findByDomain_DomainId(id);
        return ResponseEntity.ok(students);
    }
}
