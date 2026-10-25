package com.schoolmanagement.school.serviceImpl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.schoolmanagement.school.dto.RoleDTO;
import com.schoolmanagement.school.entity.Role;
import com.schoolmanagement.school.repository.RoleRepository;
import com.schoolmanagement.school.service.RoleService;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository repository;

    @Override
    public Object createRole(RoleDTO dto) {

        Map<String, Object> response = new HashMap<>();

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            response.put("code", 400);
            response.put("status", "FAILED");
            response.put("message", "Role name is required");
            response.put("data", null);
            return response;
        }

        if (repository.existsByName(dto.getName())) {
            response.put("code", 400);
            response.put("status", "FAILED");
            response.put("message", "Role already exists");
            response.put("data", null);
            return response;
        }

        Role role = new Role();
        role.setName(dto.getName());
        role.setPermissions(dto.getPermissions());
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        repository.save(role);

        response.put("code", 200);
        response.put("status", "SUCCESS");
        response.put("message", "Role created successfully");
        response.put("data", null);

        return response;
    }

//    @Override
//    public Object getAllRoles() {
//
//        Map<String, Object> response = new HashMap<>();
//        List<Role> roles = repository.findAll();
//
//        response.put("code", 200);
//        response.put("status", "SUCCESS");
//        response.put("message", "data fetched successfully");
//        response.put("data", roles);
//
//        return response;
//    }
//
//    @Override
//    public Object getRoleById(Long roleId) {
//
//        Map<String, Object> response = new HashMap<>();
//
//        Optional<Role> role = repository.findById(roleId);
//
//        if (role.isEmpty()) {
//            response.put("code", 404);
//            response.put("status", "FAILED");
//            response.put("message", "Role not found");
//            response.put("data", null);
//            return response;
//        }
//
//        response.put("code", 200);
//        response.put("status", "SUCCESS");
//        response.put("message", "data fetched successfully");
//        response.put("data", role.get());
//
//        return response;
//    }
//
//    @Override
//    public Object updateRole(RoleDTO dto) {
//
//        Map<String, Object> response = new HashMap<>();
//
//        if (dto.getRoleId() == null) {
//            response.put("code", 400);
//            response.put("status", "FAILED");
//            response.put("message", "RoleId is required");
//            response.put("data", null);
//            return response;
//        }
//
//        Optional<Role> optionalRole = repository.findById(dto.getRoleId());
//
//        if (optionalRole.isEmpty()) {
//            response.put("code", 404);
//            response.put("status", "FAILED");
//            response.put("message", "Role not found");
//            response.put("data", null);
//            return response;
//        }
//
//        Role role = optionalRole.get();
//
//        role.setName(dto.getName());
//        role.setPermissions(dto.getPermissions());
//        role.setStatus(dto.getStatus());
//
//        repository.save(role);
//
//        response.put("code", 200);
//        response.put("status", "SUCCESS");
//        response.put("message", "Role updated successfully");
//        response.put("data", role);
//
//        return response;
//    }
//
//    @Override
//    public Object deleteRole(Long roleId) {
//
//        Map<String, Object> response = new HashMap<>();
//
//        if (!repository.existsById(roleId)) {
//            response.put("code", 404);
//            response.put("status", "FAILED");
//            response.put("message", "Role not found");
//            response.put("data", null);
//            return response;
//        }
//
//        repository.deleteById(roleId);
//
//        response.put("code", 200);
//        response.put("status", "SUCCESS");
//        response.put("message", "Role deleted successfully");
//        response.put("data", null);
//
//        return response;
//    }
}
