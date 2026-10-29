package com.schoolmanagement.school.service.impl;

import com.schoolmanagement.admin.service.EmailService;
import com.schoolmanagement.school.dto.*;
import com.schoolmanagement.school.entity.*;
import com.schoolmanagement.school.repository.*;
import com.schoolmanagement.school.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private SchoolRepository schoolRepository;
    @Autowired private AddStandardRepository standardRepository;
    @Autowired private AddDivisionRepository divisionRepository;
    @Autowired private AddTeacherRepository teacherRepository;
    @Autowired private AddStudentRepository studentRepository;
    @Autowired private EmailService emailService;

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }

    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }

    private String generateTicketId() {
        Optional<Ticket> lastTicket = ticketRepository.findTopByOrderByIdDesc();
        int nextId = 101;
        if (lastTicket.isPresent()) {
            String lastId = lastTicket.get().getTicketId();
            try {
                nextId = Integer.parseInt(lastId.replace("tick_", "")) + 1;
            } catch (Exception ignored) {}
        }
        return "tick_" + nextId;
    }

    @Override
    public void createTicket(TicketRequestDTO dto) {
        String username = getCurrentUsername();
        String role = getCurrentRole();
        if (username == null || role == null) throw new RuntimeException("Unauthorized");

        Ticket ticket = new Ticket();
        ticket.setTicketId(generateTicketId());
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.LOW);

        String creatorEmail = null;
        String creatorName = null;

        if (role.equals("ROLE_STUDENT")) {
            AddStudent student = studentRepository.findByEmailIdOrMobileNumber(username, username)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            ticket.setRaisedById(student.getId());
            ticket.setRaisedByRole("STUDENT");
            ticket.setSchool(student.getSchool());
            ticket.setStandard(student.getAddStandard());
            ticket.setDivision(student.getAddDivision());
            creatorEmail = student.getEmailId();
            creatorName = student.getFirstName() + " " + student.getLastName();
        } else if (role.equals("ROLE_TEACHER")) {
            AddTeacher teacher = teacherRepository.findByEmailIdOrMobileNumber(username, username)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            ticket.setRaisedById(teacher.getId());
            ticket.setRaisedByRole("TEACHER");
            ticket.setSchool(teacher.getSchool());
            creatorEmail = teacher.getEmailId();
            creatorName = teacher.getTeacherName();
        } else if (role.equals("ROLE_PARENT")) {
            // Future implementation for Parent role
            ticket.setRaisedByRole("PARENT");
            // Placeholder: resolution logic for parent's school and ID
            throw new RuntimeException("Parent ticket creation not yet implemented");
        } else if (role.equals("ROLE_SCHOOL")) {
            School school = schoolRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("School not found"));
            ticket.setRaisedById(school.getSchoolId());
            ticket.setRaisedByRole("SCHOOL");
            ticket.setSchool(school);
            creatorEmail = school.getEmail();
            creatorName = school.getSchoolName();
        } else {
            throw new RuntimeException("Only Students, Teachers, Parents, and Schools can raise tickets");
        }

        // Override standard/division if provided in DTO (optional for creation)
        if (dto.getStandardId() != null) {
            ticket.setStandard(standardRepository.findById(dto.getStandardId()).orElse(null));
        }
        if (dto.getDivisionId() != null) {
            ticket.setDivision(divisionRepository.findById(dto.getDivisionId()).orElse(null));
        }

        ticketRepository.save(ticket);

        if (creatorEmail != null && !creatorEmail.trim().isEmpty()) {
            try {
                emailService.sendTicketCreationEmailAsync(
                        creatorEmail,
                        creatorName != null ? creatorName : "User",
                        ticket.getTicketId()
                );
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(TicketServiceImpl.class)
                        .error("Failed to trigger creation email for ticket: {}", ticket.getTicketId(), e);
            }
        }
    }

    private void verifyTicketNotClosed(Ticket ticket) {
        if (TicketStatus.CLOSED.equals(ticket.getStatus())) {
            throw new RuntimeException("Ticket is CLOSED and cannot be modified. Please reopen it first.");
        }
    }

    @Override
    public void updatePriority(Long id, TicketPriority priority) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        verifyTicketNotClosed(ticket);
        ticket.setPriority(priority);
        ticketRepository.save(ticket);
    }

    @Override
    public void updateStatus(Long id, TicketStatus newStatus) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        TicketStatus currentStatus = ticket.getStatus();

        // Prevent modification if closed (except reopen via its own method)
        verifyTicketNotClosed(ticket);

        // Life Cycle Flow: OPEN → IN_PROGRESS → RESOLVED → CLOSED
        // Validation rules:
        if (newStatus == TicketStatus.IN_PROGRESS && currentStatus != TicketStatus.OPEN) {
            throw new RuntimeException("Can only move to IN_PROGRESS from OPEN");
        }
        if (newStatus == TicketStatus.RESOLVED && currentStatus != TicketStatus.IN_PROGRESS) {
            throw new RuntimeException("Can only move to RESOLVED from IN_PROGRESS");
        }
        if (newStatus == TicketStatus.CLOSED && currentStatus != TicketStatus.RESOLVED) {
            throw new RuntimeException("Can only move to CLOSED from RESOLVED");
        }
        if (newStatus == TicketStatus.OPEN && currentStatus != TicketStatus.CLOSED) {
            // Reopening logic is handled in reopen() method, but if someone calls updateStatus(OPEN)
            // we should probably allow it if it's already OPEN or just enforce the flow.
            // The requirement says CLOSED -> OPEN for reopen.
            if (currentStatus != TicketStatus.OPEN) {
                throw new RuntimeException("Initial status is already OPEN or invalid transition attempted");
            }
        }

        // If closing, set audit fields
        if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(java.time.LocalDateTime.now());
            ticket.setClosedBy(getCurrentUsername());
        }

        ticket.setStatus(newStatus);
        ticketRepository.save(ticket);

        boolean wasNotClosedOrResolved = (currentStatus != TicketStatus.RESOLVED && currentStatus != TicketStatus.CLOSED);
        boolean isNowClosedOrResolved = (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED);

        if (wasNotClosedOrResolved && isNowClosedOrResolved) {
            sendResolutionEmailIfApplicable(ticket, ticket.getInternalNotes());
        }
    }

    @Override
    public void updateStatusAndNotes(Long id, TicketStatus newStatus, String internalNotes) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        TicketStatus currentStatus = ticket.getStatus();

        verifyTicketNotClosed(ticket);

        if (newStatus == TicketStatus.IN_PROGRESS && currentStatus != TicketStatus.OPEN) {
            throw new RuntimeException("Can only move to IN_PROGRESS from OPEN");
        }
        if (newStatus == TicketStatus.RESOLVED && currentStatus != TicketStatus.IN_PROGRESS) {
            throw new RuntimeException("Can only move to RESOLVED from IN_PROGRESS");
        }
        if (newStatus == TicketStatus.CLOSED && currentStatus != TicketStatus.RESOLVED) {
            throw new RuntimeException("Can only move to CLOSED from RESOLVED");
        }
        if (newStatus == TicketStatus.OPEN && currentStatus != TicketStatus.CLOSED) {
            if (currentStatus != TicketStatus.OPEN) {
                throw new RuntimeException("Initial status is already OPEN or invalid transition attempted");
            }
        }

        if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(java.time.LocalDateTime.now());
            ticket.setClosedBy(getCurrentUsername());
        }

        ticket.setStatus(newStatus);
        if (internalNotes != null) {
            ticket.setInternalNotes(internalNotes);
        }
        ticketRepository.save(ticket);

        boolean wasNotClosedOrResolved = (currentStatus != TicketStatus.RESOLVED && currentStatus != TicketStatus.CLOSED);
        boolean isNowClosedOrResolved = (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED);

        if (wasNotClosedOrResolved && isNowClosedOrResolved) {
            sendResolutionEmailIfApplicable(ticket, internalNotes);
        }
    }

    private void sendResolutionEmailIfApplicable(Ticket ticket, String resolutionNotes) {
        try {
            String email = null;
            String name = null;
            if ("STUDENT".equalsIgnoreCase(ticket.getRaisedByRole())) {
                Optional<AddStudent> studentOpt = studentRepository.findById(ticket.getRaisedById());
                if (studentOpt.isPresent()) {
                    AddStudent student = studentOpt.get();
                    email = student.getEmailId();
                    name = student.getFirstName() + " " + student.getLastName();
                }
            } else if ("TEACHER".equalsIgnoreCase(ticket.getRaisedByRole())) {
                Optional<AddTeacher> teacherOpt = teacherRepository.findById(ticket.getRaisedById());
                if (teacherOpt.isPresent()) {
                    AddTeacher teacher = teacherOpt.get();
                    email = teacher.getEmailId();
                    name = teacher.getTeacherName();
                }
            } else if ("SCHOOL".equalsIgnoreCase(ticket.getRaisedByRole())) {
                Optional<School> schoolOpt = schoolRepository.findById(ticket.getRaisedById());
                if (schoolOpt.isPresent()) {
                    School school = schoolOpt.get();
                    email = school.getEmail();
                    name = school.getSchoolName();
                }
            }

            if (email != null && !email.trim().isEmpty()) {
                emailService.sendTicketResolutionEmailAsync(
                        email,
                        name != null ? name : "User",
                        ticket.getTicketId(),
                        resolutionNotes
                );
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(TicketServiceImpl.class)
                    .error("Failed to trigger resolution email for ticket: {}", ticket.getTicketId(), e);
        }
    }

    @Override
    public void updateNotes(Long id, String internalNotes) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setInternalNotes(internalNotes);
        ticketRepository.save(ticket);
    }

    @Override
    public void reopen(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getStatus() != TicketStatus.CLOSED) {
            throw new RuntimeException("Only CLOSED tickets can be reopened");
        }

        ticket.setStatus(TicketStatus.OPEN);
        // Requirement: "Maintain history (do NOT overwrite old data)" for reopen.
        // But user clarification: "Update the timestamp to the lastes" for closedAt on closure.
        // So on reopen, we just change status to OPEN and keep the existing closedAt/closedBy
        // until it is closed again.
        ticketRepository.save(ticket);
    }

    @Override
    public Page<TicketResponseDTO> getTickets(Long schoolId, String role, Long standardId, Long divisionId, TicketPriority priority, TicketStatus status, int page, int limit) {
        
        Sort sort = Sort.by(Sort.Direction.DESC, "priority").and(Sort.by(Sort.Direction.DESC, "createdAt"));
        PageRequest pageable = PageRequest.of(page - 1, limit, sort);

        Specification<Ticket> spec = Specification.where(null);

        if (schoolId != null) spec = spec.and(TicketSpecification.hasSchoolId(schoolId));
        if (role != null) spec = spec.and(TicketSpecification.hasRaisedByRole(role));
        if (standardId != null) spec = spec.and(TicketSpecification.hasStandardId(standardId));
        if (divisionId != null) spec = spec.and(TicketSpecification.hasDivisionId(divisionId));
        if (priority != null) spec = spec.and(TicketSpecification.hasPriority(priority));
        
        // Filtering Behavior:
        // - Exclude CLOSED tickets by default if no status specified
        // - If status=CLOSED specified, return only closed
        if (status != null) {
            spec = spec.and(TicketSpecification.hasStatus(status));
        } else {
            spec = spec.and(TicketSpecification.isNotClosed());
        }

        return ticketRepository.findAll(spec, pageable).map(this::toResponseDTO);
    }

    private TicketResponseDTO toResponseDTO(Ticket ticket) {
        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setId(ticket.getId());
        dto.setTicketId(ticket.getTicketId());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus().name());
        dto.setPriority(ticket.getPriority().name());
        dto.setRaisedByRole(ticket.getRaisedByRole());
        dto.setSchool(ticket.getSchool().getSchoolName());
        dto.setStandard(ticket.getStandard() != null ? ticket.getStandard().getStandard() : null);
        dto.setDivision(ticket.getDivision() != null ? ticket.getDivision().getDivisionName() : null);
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setInternalNotes(ticket.getInternalNotes());
        return dto;
    }
}
