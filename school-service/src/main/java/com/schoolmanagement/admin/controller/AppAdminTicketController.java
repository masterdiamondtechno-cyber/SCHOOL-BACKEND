package com.schoolmanagement.admin.controller;

import com.schoolmanagement.school.entity.Ticket;
import com.schoolmanagement.school.entity.TicketStatus;
import com.schoolmanagement.school.repository.TicketRepository;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/app_admin/tickets")
public class AppAdminTicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AddStudentRepository studentRepository;

    @Autowired
    private AddTeacherRepository teacherRepository;

    private Map<String, Object> buildResponse(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code);
        r.put("status", status);
        r.put("message", message);
        r.put("data", data);
        return r;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('APP_ADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getGlobalTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        try {
            List<Ticket> tickets = ticketRepository.findAll();

            // Filter by status if provided and not ALL
            if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                try {
                    TicketStatus targetStatus = TicketStatus.valueOf(status.toUpperCase());
                    tickets = tickets.stream()
                            .filter(t -> t.getStatus() == targetStatus)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException ignored) {}
            }

            // Filter by search term if provided
            if (search != null && !search.isBlank()) {
                String searchLower = search.toLowerCase();
                tickets = tickets.stream()
                        .filter(t -> {
                            boolean matchTitle = t.getTitle() != null && t.getTitle().toLowerCase().contains(searchLower);
                            boolean matchDesc = t.getDescription() != null && t.getDescription().toLowerCase().contains(searchLower);
                            boolean matchId = t.getTicketId() != null && t.getTicketId().toLowerCase().contains(searchLower);
                            boolean matchSchool = t.getSchool() != null && t.getSchool().getSchoolName() != null 
                                    && t.getSchool().getSchoolName().toLowerCase().contains(searchLower);
                            return matchTitle || matchDesc || matchId || matchSchool;
                        })
                        .collect(Collectors.toList());
            }

            // Sort by priority first (e.g. URGENT, HIGH, MEDIUM, LOW) and then creation date descending
            // Sort by priority first (e.g. URGENT, HIGH, MEDIUM, LOW) and then creation date descending
            tickets.sort((t1, t2) -> {
                String p1Str = t1.getPriority() != null ? t1.getPriority().name() : "LOW";
                String p2Str = t2.getPriority() != null ? t2.getPriority().name() : "LOW";
                int p1 = getPriorityWeight(p1Str);
                int p2 = getPriorityWeight(p2Str);
                if (p1 != p2) {
                    return Integer.compare(p2, p1); // Higher weight first
                }
                if (t1.getCreatedAt() != null && t2.getCreatedAt() != null) {
                    return t2.getCreatedAt().compareTo(t1.getCreatedAt()); // Newest first
                }
                return 0;
            });

            // Map to response maps compatible with frontend expectation
            List<Map<String, Object>> responseList = tickets.stream().map(t -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", t.getId());
                map.put("ticketId", t.getTicketId() != null ? t.getTicketId() : "N/A");
                map.put("issue", t.getTitle() != null ? t.getTitle() : "No Title");
                map.put("title", t.getTitle() != null ? t.getTitle() : "No Title");
                map.put("description", t.getDescription() != null ? t.getDescription() : "");
                map.put("priority", t.getPriority() != null ? t.getPriority().name() : "LOW");
                map.put("status", t.getStatus() != null ? t.getStatus().name() : "OPEN");
                map.put("schoolName", t.getSchool() != null ? t.getSchool().getSchoolName() : "N/A");
                map.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt() : java.time.LocalDateTime.now());
                map.put("internalNotes", t.getInternalNotes() != null ? t.getInternalNotes() : "");

                // Resolve creator info
                String createdBy = t.getRaisedByRole() != null ? t.getRaisedByRole() : "UNKNOWN";
                try {
                    if ("STUDENT".equalsIgnoreCase(t.getRaisedByRole()) && t.getRaisedById() != null) {
                        var studentOpt = studentRepository.findById(t.getRaisedById());
                        if (studentOpt.isPresent()) {
                            createdBy = studentOpt.get().getFirstName() + " " + studentOpt.get().getLastName() + " (Student)";
                        }
                    } else if ("TEACHER".equalsIgnoreCase(t.getRaisedByRole()) && t.getRaisedById() != null) {
                        var teacherOpt = teacherRepository.findById(t.getRaisedById());
                        if (teacherOpt.isPresent()) {
                            createdBy = teacherOpt.get().getTeacherName() + " (Teacher)";
                        }
                    } else if ("SCHOOL".equalsIgnoreCase(t.getRaisedByRole())) {
                        if (t.getSchool() != null) {
                            createdBy = t.getSchool().getSchoolName() + " (School Admin)";
                        }
                    }
                } catch (Exception ignored) {}

                map.put("createdBy", createdBy);
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(buildResponse(200, "SUCCESS", "Global tickets fetched successfully", responseList));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(buildResponse(400, "FAILED", e.getMessage() != null ? e.getMessage() : e.toString(), null));
        }
    }

    private int getPriorityWeight(String priority) {
        if (priority == null) return 0;
        switch (priority.toUpperCase()) {
            case "URGENT":
            case "CRITICAL":
                return 4;
            case "HIGH":
                return 3;
            case "MEDIUM":
                return 2;
            case "LOW":
                return 1;
            default:
                return 0;
        }
    }
}
