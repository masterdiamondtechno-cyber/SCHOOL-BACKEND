package com.schoolmanagement.Config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth

                                                // ── Open / Public endpoints ──────────────────────────────────
                                                .requestMatchers(
                                                                "/api/auth/**",
                                                                "/api/auth/admin/**",
                                                                "/api/AddSchool/create",
                                                                "/api/AddSchool/sendOtp",
                                                                "/api/AddSchool/verifyOtp",
                                                                "/api/AddSchool/updatePassword",
                                                                "/api/status",
                                                                "/api/public/razorpay/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/v3/api-docs/**",
                                                                "/actuator/health",
                                                                "/actuator/info")
                                                .permitAll()

                                                // ── App Admin / Super Admin protected endpoints ───────────────
                                                .requestMatchers(
                                                                "/api/app_admin/**",
                                                                "/dashboard/item/save",
                                                                "/app-admin/dashboard/**",
                                                                "/api/AddSchool/getAll",
                                                                "/api/AddSchool/delete",
                                                                "/api/AddSchool/resend-registration-email",
                                                                "/api/system/**")
                                                .hasAuthority("ROLE_APP_ADMIN")

                                                // ── School profile endpoints (School Admins & Super Admin) ──────
                                                .requestMatchers(
                                                                "/api/AddSchool/getBySchoolId",
                                                                "/api/AddSchool/update",
                                                                "/api/admin/subscription-plans/**")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_ADMIN", "ROLE_APP_ADMIN")

                                                // ── School only ──────────────────────────────────────────────
                                                .requestMatchers(
                                                                "/academicYear/create",
                                                                "/academicYear/update",
                                                                "/academicYear/delete",
                                                                "/academicYear/activate",
                                                                "/academicYear/archive",
                                                                "/promotion/promoteStudents",
                                                                "/standard/addStandard",
                                                                "/standard/updateStandard",
                                                                "/standard/deleteStandard",
                                                                "/division/addDivision",
                                                                "/division/updateDivision",
                                                                "/division/deleteDivision",
                                                                "/batch/addBatch",
                                                                "/batch/updateBatch",
                                                                "/batch/deleteBatch",
                                                                "/subject/addSubject",
                                                                "/subject/updateSubject",
                                                                "/subject/deleteSubject",
                                                                "/teacher/create",
                                                                "/teacher/delete",
                                                                "/teacher/getActive",
                                                                "/teacher/getInactive",
                                                                "/teacher/countActive",
                                                                "/teacher/countInactive",
                                                                "/teacher/countTotal",
                                                                "/teacher/checkMobile",
                                                                "/teacher/checkEmail",
                                                                "/teacher/changeStatus",
                                                                "/student/create",
                                                                "/student/update",
                                                                "/student/delete",
                                                                "/student/active",
                                                                "/student/inactive",
                                                                "/student/count/active",
                                                                "/student/count/inactive",
                                                                "/student/count/total",
                                                                "/student/check/email",
                                                                "/student/check/mobile",
                                                                "/student/update/status",
                                                                "/attendance/update",
                                                                "/fee/structure/create",
                                                                "/fee/structure/update",
                                                                "/fee/structure/delete",
                                                                "/fee/payment/create",
                                                                "/fee/payment/getAll",
                                                                "/fee/payment/getPageData",
                                                                "/fee/payment/update",
                                                                "/fee/payment/delete",
                                                                "/timetable/create",
                                                                "/timetable/createBulk",
                                                                "/timetable/update",
                                                                "/timetable/delete",
                                                                "/excel/**",
                                                                "/usermanagement/getAllUsers",
                                                                "/teacher-payment/getPageData",
                                                                "/teacher-payment/create",
                                                                "/teacher-payment/update",
                                                                "/teacher-payment/delete",
                                                                "/teacher-payment/markAsPaid",
                                                                "/teacher-payment/getByTeacher",
                                                                "/student/resetPassword",
                                                                "/student/migratePasswords",
                                                                "/student/resetPasswordBulk",
                                                                "/teacher-leave/approve",
                                                                "/teacher-leave/reject",
                                                                "/teacher-leave/getAll",
                                                                "/teacher-leave/getPending",
                                                                "/teacher-leave/getSchoolStats",
                                                                "/student-leave/getAll",
                                                                "/student-leave/getPending",
                                                                "/student-leave/getSchoolStats",
                                                                "/api/master-setup/**")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_ADMIN")

                                                // ── Noticeboard / Notification endpoints ────────────────────
                                                .requestMatchers(
                                                                "/notification/create",
                                                                "/notification/getAll",
                                                                "/notification/update",
                                                                "/notification/delete",
                                                                "/notification/deactivate")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_ADMIN", "ROLE_TEACHER")

                                                // ── Student Leave Approvals ──────────────────────────────────
                                                .requestMatchers(
                                                                "/student-leave/approve",
                                                                "/student-leave/reject",
                                                                "/student-leave/getClassStudentLeaves")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_ADMIN", "ROLE_TEACHER")

                                                // Teacher Only
                                                .requestMatchers("/teacher-leave/apply",
                                                                "/teacher-leave/cancel",
                                                                "/teacher-leave/getMyLeaves",
                                                                "/teacher-leave/getMyStats")
                                                .hasAuthority("ROLE_TEACHER")

                                                // School Only (Teacher Reset Passwords)
                                                .requestMatchers("/teacher/resetPasswordBulk")
                                                .hasAuthority("ROLE_SCHOOL")

                                                // ── School + Teacher ─────────────────────────────────────────
                                                .requestMatchers(
                                                                "/teacher/getAllBySchoolId",
                                                                "/teacher/classes",
                                                                "/teacher/getById",
                                                                "/teacher/update",
                                                                "/teacher/dashboard/**",
                                                                "/timetable/teacher-app",
                                                                "/student/getall",
                                                                "/attendance/markBulk",
                                                                "/exam/create",
                                                                "/exam/update",
                                                                "/exam/delete",
                                                                "/exam/marks/saveBulk",
                                                                "/fee/structure/getByStandard",
                                                                "/timetable/getByTeacher",
                                                                "/syllabus/create",
                                                                "/syllabus/update",
                                                                "/syllabus/delete",
                                                                "/syllabus/getById",
                                                                "/syllabus/getAll",
                                                                "/syllabus/updateProgress",
                                                                "/syllabus/stats",
                                                                "/syllabus/download",
                                                                "/syllabus/updateStatus",
                                                                "/teacher-leave/getById",
                                                                "/teacher-leave/getByTeacher")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_TEACHER")

                                                // ── School + Teacher + Student ───────────────────────────────
                                                .requestMatchers(
                                                                "/student/getbyschoolid/studentid",
                                                                "/attendance/getByDateAndSubject",
                                                                "/attendance/getByDate",
                                                                "/attendance/summary/today",
                                                                "/exam/getBySchool",
                                                                "/exam/getByBatch",
                                                                "/exam/hallTicket",
                                                                "/exam/marks/getByExam",
                                                                "/notification/getActive",
                                                                "/timetable/getByBatch",
                                                                "/timetable/getByClass",
                                                                "/timetable/getBySchool",
                                                                "/student-leave/getById",
                                                                "/student-leave/getByStudent")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_TEACHER", "ROLE_STUDENT")

                                                // ── School + Teacher + Student + Parent ──────────────────────
                                                .requestMatchers(
                                                                "/attendance/getByStudent",
                                                                "/exam/getByStudent",
                                                                "/exam/marks/getByStudent",
                                                                "/fee/payment/getByStudent",
                                                                "/timetable/getByStudent",
                                                                "/dashboard/data",
                                                                "/dashboard/summary",
                                                                "/dashboard/item/save")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_TEACHER", "ROLE_STUDENT",
                                                                "ROLE_PARENT")

                                                // ── Parent endpoints ─────────────────────────────────────────
                                                .requestMatchers(
                                                                "/parent/report-card")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_PARENT")

                                                // ── School + Teacher + Student + Admin ───────────────────────
                                                .requestMatchers(
                                                                "/academicYear/getAll",
                                                                "/academicYear/getCurrent",
                                                                "/standard/getAllBySchoolId",
                                                                "/standard/getByStandardId",
                                                                "/division/getAllBySchoolId",
                                                                "/division/getAllDivisionsByStandardId",
                                                                "/division/getByDivisionId",
                                                                "/batch/getAllBySchoolId",
                                                                "/batch/getAllBatchesByStandardId",
                                                                "/batch/getByBatchId",
                                                                "/subject/getAll",
                                                                "/subject/getAllBySchoolId",
                                                                "/subject/getBySubjectId",
                                                                "/teacher/getTeacherIdNameBySchoolId")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_TEACHER", "ROLE_STUDENT",
                                                                "ROLE_ADMIN")

                                                // ── Workspace: School + Teacher only (manage
                                                // assignments/resources/groups/grade) ─
                                                .requestMatchers(
                                                                "/workspace/assignment/create",
                                                                "/workspace/assignment/update",
                                                                "/workspace/assignment/delete",
                                                                "/workspace/assignment/close",
                                                                "/workspace/assignment/getbyteacher",
                                                                "/workspace/submission/grade",
                                                                "/workspace/submission/byassignment",
                                                                "/workspace/resource/create",
                                                                "/workspace/resource/update",
                                                                "/workspace/resource/delete",
                                                                "/workspace/studygroup/create",
                                                                "/workspace/studygroup/update",
                                                                "/workspace/studygroup/deactivate",
                                                                "/workspace/studygroup/addstudent",
                                                                "/workspace/studygroup/addstudents",
                                                                "/workspace/studygroup/removestudent")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_TEACHER")

                                                // ── Workspace: Student only (submit/edit/mygroups) ───────────────
                                                .requestMatchers(
                                                                "/workspace/submission/submit",
                                                                "/workspace/submission/edit",
                                                                "/workspace/studygroup/mygroups",
                                                                "/student/changePassword",
                                                                "/student/updatePassword",
                                                                "/student-leave/apply",
                                                                "/student-leave/cancel",
                                                                "/student-leave/getMyLeaves",
                                                                "/student-leave/getMyStats")
                                                .hasAuthority("ROLE_STUDENT")

                                                // ── Workspace: School + Teacher + Student (read/browse) ─────────
                                                .requestMatchers(
                                                                "/workspace/assignment/get",
                                                                "/workspace/assignment/getbyclassdivision",
                                                                "/workspace/assignment/active",
                                                                "/workspace/submission/history",
                                                                "/workspace/studygroup/get",
                                                                "/workspace/studygroup/active",
                                                                "/workspace/studygroup/bystandard",
                                                                "/workspace/studygroup/members",
                                                                "/workspace/resource/get",
                                                                "/workspace/resource/getall",
                                                                "/workspace/resource/bystandard",
                                                                "/workspace/resource/bysubject",
                                                                "/workspace/resource/bytype")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_TEACHER", "ROLE_STUDENT")

                                                // ── Study Material: School + Teacher (upload / edit / delete) ──
                                                .requestMatchers(
                                                                "/study-material/create",
                                                                "/study-material/update",
                                                                "/study-material/delete",
                                                                "/study-material/stats")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_TEACHER")

                                                // ── Study Material: School + Teacher + Student (read/download) ─
                                                .requestMatchers(
                                                                "/study-material/getall",
                                                                "/study-material/getbyid",
                                                                "/study-material/download")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_TEACHER", "ROLE_STUDENT")

                                                // ── Recorded Lecture: School + Teacher (Management) ──
                                                .requestMatchers(
                                                                "/recorded-lecture/create",
                                                                "/recorded-lecture/update",
                                                                "/recorded-lecture/delete",
                                                                "/recorded-lecture/stats",
                                                                "/recorded-lecture/viewers")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_TEACHER")

                                                // ── Recorded Lecture: School + Teacher + Student (Consumption) ──
                                                .requestMatchers(
                                                                "/recorded-lecture/getall",
                                                                "/recorded-lecture/getbyid",
                                                                "/recorded-lecture/stream")
                                                .hasAnyAuthority("ROLE_SCHOOL", "ROLE_TEACHER", "ROLE_STUDENT")

                                                // ── Ticket Management ────────────────────────────────────────
                                                .requestMatchers(org.springframework.http.HttpMethod.POST, "/tickets")
                                                .hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_PARENT", "ROLE_SCHOOL"	)
                                                .requestMatchers("/tickets/**")
                                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_SCHOOL", "ROLE_APP_ADMIN")

                                                // ── Owner (Franchise Manager) ─────────────────────────────────
                                                .requestMatchers("/owner/**")
                                                .hasAuthority("ROLE_OWNER")

                                                // ── Everything else requires a valid token ────────────────────
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of(
                                "http://localhost:5173",
                                "http://localhost:8081",
                                "https://masterdiamond.in",
                                "https://www.masterdiamond.in",
                                "https://admin.masterdiamond.in"
                ));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}