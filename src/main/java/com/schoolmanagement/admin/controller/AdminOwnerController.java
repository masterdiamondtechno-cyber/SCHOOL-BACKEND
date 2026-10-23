package com.schoolmanagement.admin.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.schoolmanagement.admin.dto.GetAllOwnersRequestDTO;
import com.schoolmanagement.admin.dto.OwnerApprovalRequestDTO;
import com.schoolmanagement.admin.dto.OwnerListDTO;
import com.schoolmanagement.owner.dto.PagedResponseDTO;

import com.schoolmanagement.owner.service.OwnerService;

/**
 * Super Admin Controller for managing Franchise Owners.
 * Requires ROLE_APP_ADMIN authentication.
 */
@RestController
@RequestMapping("/api/app_admin/owner")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_APP_ADMIN')")
public class AdminOwnerController {

    @Autowired
    private OwnerService ownerService;

    @PostMapping("/update-status")
    public ResponseEntity<?> approveOrRejectOwner(@RequestBody OwnerApprovalRequestDTO request) {
        Map<String, Object> response = ownerService.approveOrRejectOwner(request);

        if ("SUCCESS".equals(response.get("status"))) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOwner(@RequestBody Map<String, String> request) {
        Map<String, Object> response = ownerService.createOwner(request);
        if ("SUCCESS".equals(response.get("status"))) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<PagedResponseDTO<OwnerListDTO>> getAllOwners(@RequestBody GetAllOwnersRequestDTO req) {
        Sort sort = Sort.by(Sort.Direction.fromString(req.getSortDirection() != null ? req.getSortDirection() : "asc"),
                req.getSortBy() != null ? req.getSortBy() : "name");
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        PagedResponseDTO<OwnerListDTO> result = ownerService.getAllOwners(req.getStatus(), req.getSearch(), pageable);
        return ResponseEntity.ok(result);
    }
}
