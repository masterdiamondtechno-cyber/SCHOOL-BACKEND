package com.schoolmanagement.school_admin_Service;

import java.util.List;

import com.schoolmanagement.school_admin_DTO.AddStudentDTO;
import com.schoolmanagement.school_admin_DTO.ChangePasswordDTO;
import com.schoolmanagement.school_admin_DTO.ResetPasswordDTO;
import com.schoolmanagement.school_admin_DTO.ResetPasswordsBulkDTO;
import com.schoolmanagement.school_admin_DTO.UpdatePasswordDTO;

public interface AddStudentService {

    void createStudent(AddStudentDTO dto);
    AddStudentDTO getStudentByIdAndSchoolId(AddStudentDTO dto);
    void updateStudentByIdAndSchoolId(AddStudentDTO dto);
    void deleteStudentByIdAndSchoolId(AddStudentDTO dto);
    List<AddStudentDTO> getAllActiveStudentsBySchoolId(AddStudentDTO dto);
    List<AddStudentDTO> getAllInactiveStudentsBySchoolId(AddStudentDTO dto);
    Long countActiveStudentsBySchoolId(AddStudentDTO dto);
    Long countInactiveStudentsBySchoolId(AddStudentDTO dto);
    Long totalRegisteredStudentsBySchoolId(AddStudentDTO dto);
    List<AddStudentDTO> getAllStudentsBySchoolId(AddStudentDTO dto);
    boolean isEmailAlreadyPresent(AddStudentDTO dto);
    boolean isMobileNumberAlreadyPresent(AddStudentDTO dto);
    boolean isSchoolIdPresent(Long schoolId);
    void updateStudentStatus(AddStudentDTO dto);
    List<AddStudentDTO> getStudentsByDivision(Long schoolId, Long standardId, Long divisionId);
    void changePassword(ChangePasswordDTO dto);
    void resetPassword(ResetPasswordDTO dto);
    int resetPasswordsBulk(ResetPasswordsBulkDTO dto);
    void updatePassword(UpdatePasswordDTO dto);
    int migratePasswordsToDob(Long schoolId);
}
