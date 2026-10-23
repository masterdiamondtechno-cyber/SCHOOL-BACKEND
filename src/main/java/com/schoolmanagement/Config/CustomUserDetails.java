package com.schoolmanagement.Config;

import com.schoolmanagement.school.entity.AddStudent;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.owner.entity.Owner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private String username;
    private String password;
    private String role;

    // ── School ──────────────────────────────────────────────
    public CustomUserDetails(School school) {
        this.username = school.getEmail();
        this.password = school.getPassword();
        this.role     = school.getRole() != null ? school.getRole().getName() : "SCHOOL";
    }

    // ── Teacher ─────────────────────────────────────────────
    public CustomUserDetails(AddTeacher teacher) {
        this.username = teacher.getEmailId();
        this.password = teacher.getPassword();
        this.role     = teacher.getRole() != null ? teacher.getRole().getName() : "TEACHER";
    }

    // ── Student ─────────────────────────────────────────────
    public CustomUserDetails(AddStudent student) {
        this.username = student.getEmailId();
        this.password = student.getPassword();
        this.role     = student.getRole() != null ? student.getRole().getName() : "STUDENT";
    }

    // ── Owner ────────────────────────────────────────
    public CustomUserDetails(Owner owner) {
        this.username = owner.getEmail();
        this.password = owner.getPassword();
        this.role     = "OWNER";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public String  getPassword()             { return password; }
    @Override public String  getUsername()             { return username; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }

    public String getRole() { return role; }
}