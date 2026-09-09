package com.schoolmanagement.school_admin_ServiceImpl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.schoolmanagement.school_admin_Entity.AddStudent;
import com.schoolmanagement.school_admin_Entity.StudentAcademicYear;
import com.schoolmanagement.school_admin_Repo.AddStudentRepository;
import com.schoolmanagement.school_admin_Repo.StudentAcademicYearRepository;
import com.schoolmanagement.school_admin_Service.DataMigrationService;

@Service
public class DataMigrationServiceImpl implements DataMigrationService {

    private final AddStudentRepository studentRepository;
    private final StudentAcademicYearRepository mappingRepository;

    public DataMigrationServiceImpl(AddStudentRepository studentRepository, StudentAcademicYearRepository mappingRepository) {
        this.studentRepository = studentRepository;
        this.mappingRepository = mappingRepository;
    }

    @Override
    @Transactional
    public void migrateStudentAcademicHistory() {
        List<AddStudent> students = studentRepository.findAll();
        
        for (AddStudent student : students) {
            // Only migrate if they currently have an active academic year assigned in their core row
            if (student.getAcademicYear() != null && student.getAddStandard() != null && student.getAddDivision() != null) {
                
                // Check if mapping already exists to prevent duplicate entries if script runs twice
                boolean exists = mappingRepository.findByStudent_IdAndAcademicYear_AcademicYearId(
                        student.getId(), student.getAcademicYear().getAcademicYearId()).isPresent();

                if (!exists) {
                    StudentAcademicYear mapping = new StudentAcademicYear();
                    mapping.setStudent(student);
                    mapping.setAcademicYear(student.getAcademicYear());
                    mapping.setStandard(student.getAddStandard());
                    mapping.setDivision(student.getAddDivision());
                    mapping.setRollNo(student.getRollNumber());
                    mapping.setStatus("ACTIVE");
                    mappingRepository.save(mapping);
                }
            }
        }
    }
}
