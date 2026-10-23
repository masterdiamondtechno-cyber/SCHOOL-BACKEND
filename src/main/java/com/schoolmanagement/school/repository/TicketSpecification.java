package com.schoolmanagement.school.repository;

import com.schoolmanagement.school.entity.Ticket;
import com.schoolmanagement.school.entity.TicketPriority;
import com.schoolmanagement.school.entity.TicketStatus;
import org.springframework.data.jpa.domain.Specification;

public class TicketSpecification {

    public static Specification<Ticket> hasSchoolId(Long schoolId) {
        return (root, query, cb) -> cb.equal(root.get("school").get("schoolId"), schoolId);
    }

    public static Specification<Ticket> hasRaisedByRole(String role) {
        return (root, query, cb) -> cb.equal(root.get("raisedByRole"), role);
    }

    public static Specification<Ticket> hasStandardId(Long standardId) {
        return (root, query, cb) -> cb.equal(root.get("standard").get("standardId"), standardId);
    }

    public static Specification<Ticket> hasDivisionId(Long divisionId) {
        return (root, query, cb) -> cb.equal(root.get("division").get("divisionId"), divisionId);
    }

    public static Specification<Ticket> hasPriority(TicketPriority priority) {
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    public static Specification<Ticket> hasStatus(TicketStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Ticket> isNotClosed() {
        return (root, query, cb) -> cb.notEqual(root.get("status"), TicketStatus.CLOSED);
    }
}
