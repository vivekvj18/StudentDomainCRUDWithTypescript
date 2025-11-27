package com.domaincrud.domaincrud.service;

import com.domaincrud.domaincrud.entity.Domain;
import com.domaincrud.domaincrud.entity.Student;
import com.domaincrud.domaincrud.exception.ResourceNotFoundException;
import com.domaincrud.domaincrud.repository.DomainRepository;
import com.domaincrud.domaincrud.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DomainService {

    private final DomainRepository domainRepository;
    private final StudentRepository studentRepository;

    public DomainService(DomainRepository domainRepository,
                         StudentRepository studentRepository) {
        this.domainRepository = domainRepository;
        this.studentRepository = studentRepository;
    }

    // 1. Get all domains
    public List<Domain> getAllDomains() {
        return domainRepository.findAll();
    }

    // 2. Get domain by id
    public Optional<Domain> getDomainById(Long id) {
        return domainRepository.findById(id);
    }

    // 3. Create new domain
    public Domain createDomain(Domain domain) {
        return domainRepository.save(domain);
    }

    // 4. Update existing domain
    public Domain updateDomain(Long id, Domain updatedData) {
        Domain existing = domainRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Domain not found with id: " + id));

        existing.setProgram(updatedData.getProgram());
        existing.setBatch(updatedData.getBatch());
        existing.setCapacity(updatedData.getCapacity());
        existing.setQualification(updatedData.getQualification());

        return domainRepository.save(existing);
    }

    // 5. Delete domain (Safe Mode: Students ko delete nahi karega, bas unlink karega)
    public void deleteDomain(Long id) {
        if (!domainRepository.existsById(id)) {
            throw new ResourceNotFoundException("Domain not found with id: " + id);
        }

        // 1. Is domain ke students nikalo
        List<Student> students = studentRepository.findByDomain_DomainId(id);

        // 2. Har student ka domain NULL set karo (Unlink)
        if (!students.isEmpty()) {
            for (Student student : students) {
                student.setDomain(null);
            }
            // 3. Updated students ko save karo
            studentRepository.saveAll(students);
        }

        // 4. Ab Domain delete karo (Ab koi error nahi aayega)
        domainRepository.deleteById(id);
    }

    // 6. Get students belonging to a domain
    public List<Student> getStudentsByDomain(Long domainId) {
        return studentRepository.findByDomain_DomainId(domainId);
    }
}
