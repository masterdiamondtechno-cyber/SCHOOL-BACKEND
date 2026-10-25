package com.schoolmanagement.school.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.schoolmanagement.school.entity.School;



public interface SchoolRepository extends JpaRepository<School, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhoneNo(String phoneNo);

    Optional<School> findBySchoolId(Long schoolId);
    
    boolean existsByUdiseNo(String udiseNo);
    
    boolean existsByEmailAndSchoolIdNot(String email, Long schoolId);
    boolean existsByPhoneNoAndSchoolIdNot(String phoneNo, Long schoolId);
    
    
    //for getall active and inactive School
    List<School> findByschoolStatusIgnoreCase(String schoolStatus);
    
    //for count active and inactive School
    long countBySchoolStatusIgnoreCase(String schoolStatus);

    List<School> findByRegistrationStatusIgnoreCase(String registrationStatus);
    long countByRegistrationStatusIgnoreCase(String registrationStatus);
    
    Optional<School> findByEmailOrPhoneNo(String email, String phoneNo);

    Optional<School> findByToken(String token);
    
        // 🔹 Email duplicate check (case insensitive - recommended)
        boolean existsByEmailIgnoreCase(String email);

        // 🔹 Phone duplicate check


        // 🔹 Optional - find by email
        Optional<School> findByEmailIgnoreCase(String email);

        // 🔹 Optional - find by phone
        Optional<School> findByPhoneNo(String phoneNo);

        // 🔹 Optional - find by UDISE
        Optional<School> findByUdiseNo(String udiseNo);
        
        Optional<School> findByEmail(String email);
    }
    
