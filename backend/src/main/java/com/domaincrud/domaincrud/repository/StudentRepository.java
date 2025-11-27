package com.domaincrud.domaincrud.repository;

import com.domaincrud.domaincrud.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // later: use this to get students of a particular domain
    List<Student> findByDomain_DomainId(Long domainId);
}
