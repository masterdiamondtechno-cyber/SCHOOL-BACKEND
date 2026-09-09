package com.schoolmanagement.school_admin_Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.school_admin_DTO.RoleDTO;
import com.schoolmanagement.school_admin_Service.RoleService;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService service;

    // Commented out — Roles are now auto-seeded on startup via DataSeeder.java
    // @PostMapping("/create")
    // public Object create(@RequestBody RoleDTO dto) {
    //     return service.createRole(dto);
    // }

//    @PostMapping("/all")
//    public Object getAll() {
//        return service.getAllRoles();
//    }
//
//    @PostMapping("/{id}")
//    public Object getById(@PathVariable Long id) {
//        return service.getRoleById(id);
//    }
//
//    @PostMapping("/update")
//    public Object update(@RequestBody RoleDTO dto) {
//        return service.updateRole(dto);
//    }
//
//    @PostMapping("/delete/{id}")
//    public Object delete(@PathVariable Long id) {
//        return service.deleteRole(id);
//    }
}
