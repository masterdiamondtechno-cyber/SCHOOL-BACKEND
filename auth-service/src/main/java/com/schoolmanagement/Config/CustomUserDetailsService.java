package com.schoolmanagement.Config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.schoolmanagement.owner.entity.Owner;
import com.schoolmanagement.owner.repository.OwnerRepository;
import com.schoolmanagement.school.entity.AddStudent;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.SchoolRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired private SchoolRepository      schoolRepository;
    @Autowired private AddTeacherRepository  teacherRepository;
    @Autowired private AddStudentRepository  studentRepository;
    @Autowired private OwnerRepository       ownerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. School
        Optional<School> schoolOpt = schoolRepository.findByEmailOrPhoneNo(username, username);
        if (schoolOpt.isPresent())
            return new CustomUserDetails(schoolOpt.get());

        // 2. Teacher
        Optional<AddTeacher> teacherOpt = teacherRepository.findByEmailIdOrMobileNumber(username, username);
        if (teacherOpt.isPresent())
            return new CustomUserDetails(teacherOpt.get());

        // 3. Student
        Optional<AddStudent> studentOpt = studentRepository.findByEmailIdOrMobileNumber(username, username);
        if (studentOpt.isPresent())
            return new CustomUserDetails(studentOpt.get());

        // 4. Owner (Franchise Manager)
        Optional<Owner> ownerOpt = ownerRepository.findByEmailOrPhoneNo(username, username);
        if (ownerOpt.isPresent())
            return new CustomUserDetails(ownerOpt.get());

        throw new UsernameNotFoundException("User not found: " + username);
    }
}