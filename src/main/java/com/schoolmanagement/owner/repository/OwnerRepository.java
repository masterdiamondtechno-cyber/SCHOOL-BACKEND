package com.schoolmanagement.owner.repository;

import com.schoolmanagement.owner.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    Optional<Owner> findByEmail(String email);

    Optional<Owner> findByPhoneNo(String phoneNo);

    Optional<Owner> findByEmailOrPhoneNo(String email, String phoneNo);

    Optional<Owner> findByToken(String token);

    boolean existsByEmail(String email);

    boolean existsByPhoneNo(String phoneNo);

    boolean existsByEmailAndOwnerIdNot(String email, Long ownerId);

    boolean existsByPhoneNoAndOwnerIdNot(String phoneNo, Long ownerId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT new com.schoolmanagement.admin.dto.OwnerListDTO(" +
            "o.ownerId, o.name, o.email, o.phoneNo, COUNT(s.schoolId)) " +
            "FROM Owner o LEFT JOIN School s ON s.owner.ownerId = o.ownerId " +
            "WHERE (:status IS NULL OR o.status = :status) " +
            "AND (:search IS NULL OR " +
            "LOWER(o.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "o.phoneNo LIKE CONCAT('%', :search, '%')) " +
            "GROUP BY o.ownerId, o.name, o.email, o.phoneNo",
            countQuery = "SELECT count(o.ownerId) FROM Owner o " +
            "WHERE (:status IS NULL OR o.status = :status) " +
            "AND (:search IS NULL OR " +
            "LOWER(o.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "o.phoneNo LIKE CONCAT('%', :search, '%'))")
    org.springframework.data.domain.Page<com.schoolmanagement.admin.dto.OwnerListDTO> findAllOwnersWithSchoolCount(
            @org.springframework.data.repository.query.Param("status") String status,
            @org.springframework.data.repository.query.Param("search") String search,
            org.springframework.data.domain.Pageable pageable);
}
