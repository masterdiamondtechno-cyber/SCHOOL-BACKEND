package com.schoolmanagement.school_admin_Controller;

import com.schoolmanagement.school_admin_DTO.*;
import com.schoolmanagement.school_admin_Entity.TicketPriority;
import com.schoolmanagement.school_admin_Entity.TicketStatus;
import com.schoolmanagement.school_admin_Repo.SchoolRepository;
import com.schoolmanagement.school_admin_Service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private SchoolRepository schoolRepository;

    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return null;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }

    private Long resolveSchoolId(Principal principal) {
        if (principal == null)
            return null;
        return schoolRepository.findByEmail(principal.getName())
                .map(s -> s.getSchoolId())
                .orElse(null);
    }

    private Map<String, Object> buildResponse(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code);
        r.put("status", status);
        r.put("message", message);
        r.put("data", data);
        return r;
    }

    // 1. Ticket Creation
    @PostMapping(consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'PARENT', 'SCHOOL')")
    public ResponseEntity<Map<String, Object>> createTicket(@RequestBody TicketRequestDTO dto) {
        try {
            ticketService.createTicket(dto);
            return ResponseEntity.ok(buildResponse(200, "SUCCESS", "Ticket created successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildResponse(400, "FAILED", e.getMessage(), null));
        }
    }

    // 2. Update Priority
    @PostMapping(value = "/priority", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'APP_ADMIN')")
    public ResponseEntity<Map<String, Object>> updatePriority(
            @RequestBody TicketPriorityRequestDTO dto) {
        try {
            ticketService.updatePriority(dto.getTicketId(), dto.getPriority());
            return ResponseEntity.ok(buildResponse(200, "SUCCESS", "Priority updated", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildResponse(400, "FAILED", e.getMessage(), null));
        }
    }
    // 3. Update Status
    @PostMapping(value = "/status", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'APP_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @RequestBody TicketStatusRequestDTO dto) {
        try {
            ticketService.updateStatus(dto.getTicketId(), dto.getStatus());
            return ResponseEntity.ok(buildResponse(200, "SUCCESS", "Status updated", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildResponse(400, "FAILED", e.getMessage(), null));
        }
    }

    // New Reopen API
    @PostMapping("/reopen")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'APP_ADMIN')")
    public ResponseEntity<Map<String, Object>> reopenTicket(
            @RequestBody TicketReopenRequestDTO dto) {
        try {
            ticketService.reopen(dto.getTicketId());
            return ResponseEntity.ok(buildResponse(200, "SUCCESS", "Ticket reopened", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildResponse(400, "FAILED", e.getMessage(), null));
        }
    }

    // PUT mappings for Axios client integration
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'APP_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateStatusPut(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            String statusStr = (String) body.get("status");
            TicketStatus status = TicketStatus.valueOf(statusStr.toUpperCase());
            String internalNotes = (String) body.get("internalNotes");
            ticketService.updateStatusAndNotes(id, status, internalNotes);
            return ResponseEntity.ok(buildResponse(200, "SUCCESS", "Status updated", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildResponse(400, "FAILED", e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/priority")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'APP_ADMIN')")
    public ResponseEntity<Map<String, Object>> updatePriorityPut(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            String priorityStr = (String) body.get("priority");
            TicketPriority priority = TicketPriority.valueOf(priorityStr.toUpperCase());
            ticketService.updatePriority(id, priority);
            return ResponseEntity.ok(buildResponse(200, "SUCCESS", "Priority updated", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildResponse(400, "FAILED", e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'APP_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateNotesPut(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String internalNotes = body.get("internalNotes");
            ticketService.updateNotes(id, internalNotes);
            return ResponseEntity.ok(buildResponse(200, "SUCCESS", "Internal notes updated", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildResponse(400, "FAILED", e.getMessage(), null));
        }
    }

    // 4. Get Tickets (Role-Based + Filtering)
    @PostMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'APP_ADMIN')")
    public ResponseEntity<Map<String, Object>> getTickets(
            @RequestBody TicketFilterRequestDTO request,
            Principal principal) {

        try {
            String currentRole = getCurrentRole();
            Long resolvedSchoolId = request.getSchoolId();

            if ("ROLE_SCHOOL".equals(currentRole)) {
                resolvedSchoolId = resolveSchoolId(principal);
                if (resolvedSchoolId == null) {
                    return ResponseEntity.status(401)
                            .body(buildResponse(401, "FAILED", "Unauthorized: School not found", null));
                }
                // School can only filter by standard/division/priority/status
                // schoolId and role parameters from request are ignored for non-admins
            } else if ("ROLE_ADMIN".equals(currentRole) || "ROLE_APP_ADMIN".equals(currentRole)) {
                // Admin can filter by anything
            } else {
                return ResponseEntity.status(403).body(buildResponse(403, "FAILED", "Access denied", null));
            }

            Page<TicketResponseDTO> tickets = ticketService.getTickets(
                    resolvedSchoolId, request.getRole(), request.getStandardId(),
                    request.getDivisionId(), request.getPriority(), request.getStatus(),
                    request.getPage(), request.getLimit());

            return ResponseEntity.ok(buildResponse(200, "SUCCESS", "Tickets fetched", tickets.getContent()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildResponse(400, "FAILED", e.getMessage(), null));
        }
    }
}
