package com.domaincrud.domaincrud.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "roll_number", nullable = false, unique = true)
    private String rollNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "photograph_path")
    private String photographPath;

    private Double cgpa;

    @Column(name = "total_credits")
    private Integer totalCredits;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @ManyToOne
    @JoinColumn(name = "domain_id")
    private Domain domain;

    // ✅ MANUALLY ADD THIS SETTER (To fix the error)
    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    // ✅ MANUALLY ADD GETTER (Just in case)
    public Domain getDomain() {
        return this.domain;
    }
}