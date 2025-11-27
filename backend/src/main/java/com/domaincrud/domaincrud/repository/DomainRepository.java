package com.domaincrud.domaincrud.repository;

import com.domaincrud.domaincrud.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainRepository extends JpaRepository<Domain, Long> {
}

