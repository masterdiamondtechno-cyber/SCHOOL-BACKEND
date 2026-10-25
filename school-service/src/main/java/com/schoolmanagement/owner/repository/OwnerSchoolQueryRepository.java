package com.schoolmanagement.owner.repository;

import com.schoolmanagement.owner.dto.OwnerFinanceBranchDTO;
import com.schoolmanagement.owner.dto.OwnerSchoolDTO;
import com.schoolmanagement.owner.dto.OwnerStudentDTO;
import com.schoolmanagement.owner.dto.OwnerTeacherDTO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Native-SQL aggregation queries for the Owner Panel.
 * All queries filter by owner_id, preventing cross-owner data leakage.
 * Uses COALESCE to handle NULL sums safely.
 */
@Repository
public class OwnerSchoolQueryRepository {

    @PersistenceContext
    private EntityManager em;

    // ─────────────────────────────────────────────────────────────
    // DASHBOARD AGGREGATES
    // ─────────────────────────────────────────────────────────────

    public long countSchoolsByOwnerId(Long ownerId) {
        Query q = em.createNativeQuery(
            "SELECT COUNT(*) FROM school WHERE owner_id = :ownerId");
        q.setParameter("ownerId", ownerId);
        return ((Number) q.getSingleResult()).longValue();
    }

    public long countStudentsByOwnerId(Long ownerId) {
        Query q = em.createNativeQuery(
            "SELECT COUNT(s.id) FROM students s " +
            "JOIN school sc ON sc.school_id = s.school_id " +
            "WHERE sc.owner_id = :ownerId");
        q.setParameter("ownerId", ownerId);
        return ((Number) q.getSingleResult()).longValue();
    }

    public double sumRevenueByOwnerIdAndYear(Long ownerId, int year) {
        Query q = em.createNativeQuery(
            "SELECT COALESCE(SUM(fp.amount_paid), 0) " +
            "FROM fee_payment fp " +
            "JOIN school sc ON sc.school_id = fp.school_id " +
            "WHERE sc.owner_id = :ownerId " +
            "  AND EXTRACT(YEAR FROM fp.payment_date) = :year");
        q.setParameter("ownerId", ownerId);
        q.setParameter("year", year);
        return ((Number) q.getSingleResult()).doubleValue();
    }

    public double sumRevenueByOwnerIdAndYearMonth(Long ownerId, int year, int month) {
        Query q = em.createNativeQuery(
            "SELECT COALESCE(SUM(fp.amount_paid), 0) " +
            "FROM fee_payment fp " +
            "JOIN school sc ON sc.school_id = fp.school_id " +
            "WHERE sc.owner_id = :ownerId " +
            "  AND EXTRACT(YEAR FROM fp.payment_date) = :year " +
            "  AND EXTRACT(MONTH FROM fp.payment_date) = :month");
        q.setParameter("ownerId", ownerId);
        q.setParameter("year", year);
        q.setParameter("month", month);
        return ((Number) q.getSingleResult()).doubleValue();
    }

    /** Per-school breakdown: schoolId, schoolName, district, studentCount, revenue, attendancePct */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getPerformanceMatrix(Long ownerId, int year) {
        Query q = em.createNativeQuery(
            "SELECT sc.school_id, sc.school_name, sc.district, " +
            "       COUNT(DISTINCT s.id) AS student_count, " +
            "       COALESCE(SUM(fp.amount_paid), 0) AS revenue, " +
            "       ROUND( " +
            "           CASE WHEN COUNT(a.attendance_id) = 0 THEN 0 " +
            "                ELSE 100.0 * SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) " +
            "                     / COUNT(a.attendance_id) END , 2) AS attendance_pct " +
            "FROM school sc " +
            "LEFT JOIN students s   ON s.school_id = sc.school_id " +
            "LEFT JOIN fee_payment fp ON fp.school_id = sc.school_id " +
            "     AND EXTRACT(YEAR FROM fp.payment_date) = :year " +
            "LEFT JOIN attendance a ON a.school_id = sc.school_id " +
            "     AND EXTRACT(YEAR FROM a.attendance_date) = :year " +
            "WHERE sc.owner_id = :ownerId " +
            "GROUP BY sc.school_id, sc.school_name, sc.district");
        q.setParameter("ownerId", ownerId);
        q.setParameter("year", year);

        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new HashMap<>();
            m.put("schoolId",       row[0]);
            m.put("schoolName",     row[1]);
            m.put("district",       row[2]);
            m.put("studentCount",   row[3]);
            m.put("revenue",        row[4]);
            m.put("attendancePct",  row[5]);
            result.add(m);
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────
    // SCHOOLS (with filters + pagination)
    // ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<OwnerSchoolDTO> getSchoolsByOwner(Long ownerId,
                                                   String status,
                                                   String city,
                                                   String search,
                                                   Pageable pageable) {
        StringBuilder sql = new StringBuilder(
            "SELECT sc.school_id, sc.school_name, sc.district, sc.state, sc.school_status, " +
            "       sc.subscription_status, sc.phone_no, sc.email, sc.created_at, " +
            "       COUNT(DISTINCT s.id) AS student_count, " +
            "       COUNT(DISTINCT t.id) AS teacher_count, " +
            "       COALESCE(SUM(fp.amount_paid), 0) AS total_revenue, " +
            "       sc.registration_status " +
            "FROM school sc " +
            "LEFT JOIN students s ON s.school_id = sc.school_id " +
            "LEFT JOIN teacher t  ON t.school_id = sc.school_id " +
            "LEFT JOIN fee_payment fp ON fp.school_id = sc.school_id " +
            "WHERE sc.owner_id = :ownerId ");

        if (status != null && !status.isBlank()) {
            sql.append("AND LOWER(sc.school_status) = LOWER(:status) ");
        }
        if (city != null && !city.isBlank()) {
            sql.append("AND LOWER(sc.district) LIKE LOWER(:city) ");
        }
        if (search != null && !search.isBlank()) {
            sql.append("AND (LOWER(sc.school_name) LIKE LOWER(:search) " +
                       "     OR LOWER(sc.email) LIKE LOWER(:search) " +
                       "     OR sc.phone_no LIKE :search) ");
        }
        sql.append("GROUP BY sc.school_id, sc.school_name, sc.district, sc.state, " +
                   "         sc.school_status, sc.subscription_status, sc.phone_no, sc.email, sc.created_at, sc.registration_status ");
        sql.append("ORDER BY sc.school_name ASC ");
        sql.append("LIMIT :limit OFFSET :offset");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("ownerId", ownerId);
        if (status != null && !status.isBlank())  query.setParameter("status",  status);
        if (city   != null && !city.isBlank())    query.setParameter("city",    "%" + city   + "%");
        if (search != null && !search.isBlank())  query.setParameter("search",  "%" + search + "%");
        query.setParameter("limit",  pageable.getPageSize());
        query.setParameter("offset", pageable.getOffset());

        List<Object[]> rows = query.getResultList();
        List<OwnerSchoolDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            OwnerSchoolDTO dto = new OwnerSchoolDTO();
            dto.setSchoolId(row[0] != null ? ((Number) row[0]).longValue() : null);
            dto.setSchoolName((String) row[1]);
            dto.setDistrict((String) row[2]);
            dto.setState((String) row[3]);
            dto.setSchoolStatus((String) row[4]);
            dto.setSubscriptionStatus((String) row[5]);
            dto.setPhoneNo((String) row[6]);
            dto.setEmail((String) row[7]);
            dto.setCreatedAt(row[8] != null ? row[8].toString() : null);
            dto.setStudentCount(row[9] != null ? ((Number) row[9]).longValue() : 0L);
            dto.setTeacherCount(row[10] != null ? ((Number) row[10]).longValue() : 0L);
            dto.setTotalRevenue(row[11] != null ? ((Number) row[11]).doubleValue() : 0.0);
            dto.setRegistrationStatus((String) row[12]);
            result.add(dto);
        }
        return result;
    }

    public long countSchoolsByOwnerWithFilters(Long ownerId, String status, String city, String search) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM school sc WHERE sc.owner_id = :ownerId ");
        if (status != null && !status.isBlank()) sql.append("AND LOWER(sc.school_status) = LOWER(:status) ");
        if (city   != null && !city.isBlank())   sql.append("AND LOWER(sc.district) LIKE LOWER(:city) ");
        if (search != null && !search.isBlank()) sql.append("AND (LOWER(sc.school_name) LIKE LOWER(:search) OR LOWER(sc.email) LIKE LOWER(:search)) ");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("ownerId", ownerId);
        if (status != null && !status.isBlank())  query.setParameter("status",  status);
        if (city   != null && !city.isBlank())    query.setParameter("city",    "%" + city   + "%");
        if (search != null && !search.isBlank())  query.setParameter("search",  "%" + search + "%");
        return ((Number) query.getSingleResult()).longValue();
    }

    // ─────────────────────────────────────────────────────────────
    // TEACHERS (with filters + pagination)
    // ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<OwnerTeacherDTO> getTeachersByOwner(Long ownerId,
                                                     Long schoolId,
                                                     String status,
                                                     String search,
                                                     Pageable pageable) {
        StringBuilder sql = new StringBuilder(
            "SELECT t.id, t.teacher_name, t.email_id, t.mobile_number, t.status, " +
            "       t.monthly_salary, t.joining_date, t.gender, " +
            "       sc.school_id, sc.school_name " +
            "FROM teacher t " +
            "JOIN school sc ON sc.school_id = t.school_id " +
            "WHERE sc.owner_id = :ownerId ");

        if (schoolId != null) sql.append("AND t.school_id = :schoolId ");
        if (status   != null && !status.isBlank())  sql.append("AND LOWER(t.status) = LOWER(:status) ");
        if (search   != null && !search.isBlank())
            sql.append("AND (LOWER(t.teacher_name) LIKE LOWER(:search) " +
                       "     OR LOWER(t.email_id) LIKE LOWER(:search) " +
                       "     OR t.mobile_number LIKE :search) ");

        sql.append("ORDER BY t.teacher_name ASC LIMIT :limit OFFSET :offset");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("ownerId", ownerId);
        if (schoolId != null)                        query.setParameter("schoolId", schoolId);
        if (status   != null && !status.isBlank())   query.setParameter("status",   status);
        if (search   != null && !search.isBlank())   query.setParameter("search",   "%" + search + "%");
        query.setParameter("limit",  pageable.getPageSize());
        query.setParameter("offset", pageable.getOffset());

        List<Object[]> rows = query.getResultList();
        List<OwnerTeacherDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            OwnerTeacherDTO dto = new OwnerTeacherDTO();
            dto.setTeacherId(row[0] != null ? ((Number) row[0]).longValue() : null);
            dto.setTeacherName((String) row[1]);
            dto.setEmailId((String) row[2]);
            dto.setMobileNumber((String) row[3]);
            dto.setStatus((String) row[4]);
            dto.setMonthlySalary((String) row[5]);
            dto.setJoiningDate(row[6] != null ? row[6].toString() : null);
            dto.setGender((String) row[7]);
            dto.setSchoolId(row[8] != null ? ((Number) row[8]).longValue() : null);
            dto.setSchoolName((String) row[9]);
            result.add(dto);
        }
        return result;
    }

    public long countTeachersByOwnerWithFilters(Long ownerId, Long schoolId, String status, String search) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM teacher t " +
            "JOIN school sc ON sc.school_id = t.school_id " +
            "WHERE sc.owner_id = :ownerId ");
        if (schoolId != null)                       sql.append("AND t.school_id = :schoolId ");
        if (status   != null && !status.isBlank())  sql.append("AND LOWER(t.status) = LOWER(:status) ");
        if (search   != null && !search.isBlank())  sql.append("AND LOWER(t.teacher_name) LIKE LOWER(:search) ");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("ownerId", ownerId);
        if (schoolId != null)                       query.setParameter("schoolId", schoolId);
        if (status   != null && !status.isBlank())  query.setParameter("status",   status);
        if (search   != null && !search.isBlank())  query.setParameter("search",   "%" + search + "%");
        return ((Number) query.getSingleResult()).longValue();
    }

    /**
     * Single-query aggregate stats for the teacher stats cards.
     * Returns: totalTeachers, activeTeachers, onLeaveTeachers, avgSalary
     */
    public Map<String, Object> getTeacherStats(Long ownerId) {
        Query q = em.createNativeQuery(
            "SELECT " +
            "    COUNT(*) AS total_teachers, " +
            "    COUNT(CASE WHEN LOWER(t.status) = 'active' THEN 1 END) AS active_teachers, " +
            "    COUNT(CASE WHEN LOWER(t.status) IN ('on_leave', 'on leave') THEN 1 END) AS on_leave_teachers, " +
            "    COALESCE(AVG(CASE WHEN t.monthly_salary ~ '^[0-9]+(\\.[0-9]+)?$' " +
            "        THEN CAST(t.monthly_salary AS NUMERIC) END), 0) AS avg_salary " +
            "FROM teacher t " +
            "JOIN school sc ON sc.school_id = t.school_id " +
            "WHERE sc.owner_id = :ownerId");
        q.setParameter("ownerId", ownerId);

        Object[] row = (Object[]) q.getSingleResult();
        Map<String, Object> res = new HashMap<>();
        res.put("totalTeachers",  row[0] != null ? ((Number) row[0]).longValue()   : 0L);
        res.put("activeTeachers", row[1] != null ? ((Number) row[1]).longValue()   : 0L);
        res.put("onLeaveTeachers",row[2] != null ? ((Number) row[2]).longValue()   : 0L);
        res.put("avgSalary",      row[3] != null ? ((Number) row[3]).doubleValue() : 0.0);
        return res;
    }

    // ─────────────────────────────────────────────────────────────
    // STUDENTS (with filters + pagination)
    // ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<OwnerStudentDTO> getStudentsByOwner(Long ownerId,
                                                     Long schoolId,
                                                     Long classId,
                                                     String feeStatus,
                                                     String gender,
                                                     String search,
                                                     Pageable pageable) {
        StringBuilder sql = new StringBuilder(
            "SELECT s.id, s.student_id, s.first_name, s.last_name, s.gender, s.status, " +
            "       s.total_fee, s.final_fee, " +
            "       COALESCE(SUM(fp.amount_paid), 0) AS amount_paid, " +
            "       sc.school_id, sc.school_name, " +
            "       std.standard_name, div.division_name " +
            "FROM students s " +
            "JOIN school sc ON sc.school_id = s.school_id " +
            "LEFT JOIN add_standard std  ON std.standard_id = s.standard_id " +
            "LEFT JOIN add_division div  ON div.division_id = s.division_id " +
            "LEFT JOIN fee_payment fp    ON fp.student_id  = s.id " +
            "WHERE sc.owner_id = :ownerId ");

        if (schoolId != null) sql.append("AND s.school_id = :schoolId ");
        if (classId  != null) sql.append("AND s.standard_id = :classId ");
        if (gender   != null && !gender.isBlank())    sql.append("AND LOWER(s.gender) = LOWER(:gender) ");
        if (search   != null && !search.isBlank())
            sql.append("AND (LOWER(s.first_name) LIKE LOWER(:search) " +
                       "    OR LOWER(s.last_name) LIKE LOWER(:search) " +
                       "    OR s.mobile_number LIKE :search " +
                       "    OR s.student_id LIKE :search) ");

        sql.append("GROUP BY s.id, s.student_id, s.first_name, s.last_name, s.gender, s.status, " +
                   "         s.total_fee, s.final_fee, sc.school_id, sc.school_name, " +
                   "         std.standard_name, div.division_name ");

        // Fee status filter (PAID / PENDING / PARTIAL) applied post-aggregation
        if (feeStatus != null && !feeStatus.isBlank()) {
            switch (feeStatus.toUpperCase()) {
                case "PAID"    -> sql.append("HAVING COALESCE(SUM(fp.amount_paid),0) >= COALESCE(s.final_fee,0) ");
                case "PENDING" -> sql.append("HAVING COALESCE(SUM(fp.amount_paid),0) = 0 ");
                case "PARTIAL" -> sql.append("HAVING COALESCE(SUM(fp.amount_paid),0) > 0 " +
                                             "   AND COALESCE(SUM(fp.amount_paid),0) < COALESCE(s.final_fee,0) ");
            }
        }

        sql.append("ORDER BY s.first_name ASC LIMIT :limit OFFSET :offset");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("ownerId", ownerId);
        if (schoolId != null)                        query.setParameter("schoolId", schoolId);
        if (classId  != null)                        query.setParameter("classId",  classId);
        if (gender   != null && !gender.isBlank())   query.setParameter("gender",   gender);
        if (search   != null && !search.isBlank())   query.setParameter("search",   "%" + search + "%");
        query.setParameter("limit",  pageable.getPageSize());
        query.setParameter("offset", pageable.getOffset());

        List<Object[]> rows = query.getResultList();
        List<OwnerStudentDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            OwnerStudentDTO dto = new OwnerStudentDTO();
            dto.setId(row[0] != null ? ((Number) row[0]).longValue() : null);
            dto.setStudentId((String) row[1]);
            dto.setFirstName((String) row[2]);
            dto.setLastName((String) row[3]);
            dto.setGender((String) row[4]);
            dto.setStatus((String) row[5]);
            double totalFee    = row[6] != null ? ((Number) row[6]).doubleValue() : 0.0;
            double finalFee    = row[7] != null ? ((Number) row[7]).doubleValue() : 0.0;
            double amountPaid  = row[8] != null ? ((Number) row[8]).doubleValue() : 0.0;
            dto.setTotalFee(totalFee);
            dto.setFinalFee(finalFee);
            dto.setAmountPaid(amountPaid);
            // Compute feeStatus
            if (amountPaid >= finalFee && finalFee > 0)       dto.setFeeStatus("PAID");
            else if (amountPaid == 0)                          dto.setFeeStatus("PENDING");
            else                                               dto.setFeeStatus("PARTIAL");
            dto.setSchoolId(row[9] != null ? ((Number) row[9]).longValue() : null);
            dto.setSchoolName((String) row[10]);
            dto.setStandardName((String) row[11]);
            dto.setDivisionName((String) row[12]);
            result.add(dto);
        }
        return result;
    }

    public long countStudentsByOwnerWithFilters(Long ownerId, Long schoolId, Long classId, String gender, String search) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(DISTINCT s.id) FROM students s " +
            "JOIN school sc ON sc.school_id = s.school_id " +
            "WHERE sc.owner_id = :ownerId ");
        if (schoolId != null)                       sql.append("AND s.school_id = :schoolId ");
        if (classId  != null)                       sql.append("AND s.standard_id = :classId ");
        if (gender   != null && !gender.isBlank())  sql.append("AND LOWER(s.gender) = LOWER(:gender) ");
        if (search   != null && !search.isBlank())  sql.append("AND LOWER(s.first_name) LIKE LOWER(:search) ");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("ownerId", ownerId);
        if (schoolId != null)                       query.setParameter("schoolId", schoolId);
        if (classId  != null)                       query.setParameter("classId",  classId);
        if (gender   != null && !gender.isBlank())  query.setParameter("gender",   gender);
        if (search   != null && !search.isBlank())  query.setParameter("search",   "%" + search + "%");
        return ((Number) query.getSingleResult()).longValue();
    }

    /**
     * Single-query aggregate stats for the student stats cards.
     * Returns: totalStudents, paidStudents, pendingStudents, avgFee
     */
    public Map<String, Object> getStudentStats(Long ownerId) {
        Query q = em.createNativeQuery(
            "WITH student_fees AS (" +
            "    SELECT s.id, " +
            "           COALESCE(s.final_fee, 0) AS final_fee, " +
            "           COALESCE(SUM(fp.amount_paid), 0) AS paid " +
            "    FROM students s " +
            "    JOIN school sc ON sc.school_id = s.school_id " +
            "    LEFT JOIN fee_payment fp ON fp.student_id = s.id " +
            "    WHERE sc.owner_id = :ownerId " +
            "    GROUP BY s.id, s.final_fee" +
            ")" +
            "SELECT " +
            "    COUNT(*) AS total_students, " +
            "    COUNT(CASE WHEN paid >= final_fee AND final_fee > 0 THEN 1 END) AS paid_students, " +
            "    COUNT(CASE WHEN paid = 0 THEN 1 END) AS pending_students, " +
            "    COALESCE(AVG(final_fee), 0) AS avg_fee " +
            "FROM student_fees");
        q.setParameter("ownerId", ownerId);

        Object[] row = (Object[]) q.getSingleResult();
        Map<String, Object> res = new HashMap<>();
        res.put("totalStudents",   row[0] != null ? ((Number) row[0]).longValue()   : 0L);
        res.put("paidStudents",    row[1] != null ? ((Number) row[1]).longValue()   : 0L);
        res.put("pendingStudents", row[2] != null ? ((Number) row[2]).longValue()   : 0L);
        res.put("avgFee",          row[3] != null ? ((Number) row[3]).doubleValue() : 0.0);
        return res;
    }

    // ─────────────────────────────────────────────────────────────
    // FINANCE
    // ─────────────────────────────────────────────────────────────

    public Map<String, Object> getFinanceSummary(Long ownerId, LocalDate startDate, LocalDate endDate, Long schoolId) {
        StringBuilder sql = new StringBuilder(
            "SELECT COALESCE(SUM(fp.amount_paid), 0) AS total_collected, " +
            "       COUNT(fp.payment_id) AS transaction_count, " +
            "       COALESCE(SUM(s.final_fee), 0) AS total_billed " +
            "FROM fee_payment fp " +
            "JOIN school sc ON sc.school_id = fp.school_id " +
            "JOIN students s ON s.id = fp.student_id " +
            "WHERE sc.owner_id = :ownerId ");
        if (startDate != null) sql.append("AND fp.payment_date >= :startDate ");
        if (endDate   != null) sql.append("AND fp.payment_date <= :endDate ");
        if (schoolId  != null) sql.append("AND fp.school_id = :schoolId ");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("ownerId", ownerId);
        if (startDate != null) query.setParameter("startDate", startDate);
        if (endDate   != null) query.setParameter("endDate",   endDate);
        if (schoolId  != null) query.setParameter("schoolId",  schoolId);

        Object[] row = (Object[]) query.getSingleResult();
        Map<String, Object> map = new HashMap<>();
        double collected = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
        long   txCount   = row[1] != null ? ((Number) row[1]).longValue()   : 0L;
        double billed    = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
        map.put("totalCollected",     collected);
        map.put("transactionCount",   txCount);
        map.put("totalBilled",        billed);
        map.put("pendingAmount",      Math.max(0, billed - collected));
        return map;
    }

    @SuppressWarnings("unchecked")
    public List<OwnerFinanceBranchDTO> getBranchWiseFinance(Long ownerId, LocalDate startDate, LocalDate endDate, Long schoolId) {
        StringBuilder sql = new StringBuilder(
            "SELECT sc.school_id, sc.school_name, " +
            "       COALESCE(SUM(fp.amount_paid), 0) AS collected, " +
            "       COALESCE(SUM(s_sub.final_fee), 0) AS billed, " +
            "       COUNT(DISTINCT st.id) AS student_count " +
            "FROM school sc " +
            "LEFT JOIN fee_payment fp ON fp.school_id = sc.school_id " +
            "LEFT JOIN students st    ON st.school_id = sc.school_id " +
            "LEFT JOIN students s_sub ON s_sub.id     = fp.student_id " +
            "WHERE sc.owner_id = :ownerId ");
        if (startDate != null) sql.append("AND (fp.payment_date IS NULL OR fp.payment_date >= :startDate) ");
        if (endDate   != null) sql.append("AND (fp.payment_date IS NULL OR fp.payment_date <= :endDate) ");
        if (schoolId  != null) sql.append("AND sc.school_id = :schoolId ");
        sql.append("GROUP BY sc.school_id, sc.school_name ORDER BY collected DESC");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("ownerId", ownerId);
        if (startDate != null) query.setParameter("startDate", startDate);
        if (endDate   != null) query.setParameter("endDate",   endDate);
        if (schoolId  != null) query.setParameter("schoolId",  schoolId);

        List<Object[]> rows = query.getResultList();
        List<OwnerFinanceBranchDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            OwnerFinanceBranchDTO dto = new OwnerFinanceBranchDTO();
            dto.setSchoolId(row[0] != null ? ((Number) row[0]).longValue() : null);
            dto.setSchoolName((String) row[1]);
            double collected = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            double billed    = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            dto.setCollected(collected);
            dto.setBilled(billed);
            dto.setPending(Math.max(0, billed - collected));
            dto.setStudentCount(row[4] != null ? ((Number) row[4]).longValue() : 0L);
            result.add(dto);
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────
    // REPORTS
    // ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getRevenueReport(Long ownerId, LocalDate startDate, LocalDate endDate,
                                                        Long schoolId, String groupBy) {
        String dateTrunc = buildDateTrunc("fp.payment_date", groupBy);
        StringBuilder sql = new StringBuilder(
            "SELECT " + dateTrunc + " AS period, " +
            "       COALESCE(SUM(fp.amount_paid), 0) AS revenue, " +
            "       COUNT(fp.payment_id) AS transactions " +
            "FROM fee_payment fp " +
            "JOIN school sc ON sc.school_id = fp.school_id " +
            "WHERE sc.owner_id = :ownerId " +
            "  AND fp.payment_date BETWEEN :startDate AND :endDate ");
        if (schoolId != null) sql.append("AND fp.school_id = :schoolId ");
        sql.append("GROUP BY period ORDER BY period ASC");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("ownerId",   ownerId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate",   endDate);
        if (schoolId != null) query.setParameter("schoolId", schoolId);

        return mapRows(query.getResultList(), new String[]{"period", "revenue", "transactions"});
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAttendanceReport(Long ownerId, LocalDate startDate, LocalDate endDate,
                                                          Long schoolId, Long classId, String groupBy) {
        String dateTrunc = buildDateTrunc("a.attendance_date", groupBy);
        StringBuilder sql = new StringBuilder(
            "SELECT " + dateTrunc + " AS period, " +
            "       COUNT(a.attendance_id) AS total_records, " +
            "       SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count, " +
            "       ROUND(100.0 * SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) " +
            "             / NULLIF(COUNT(a.attendance_id), 0), 2) AS attendance_pct " +
            "FROM attendance a " +
            "JOIN school sc ON sc.school_id = a.school_id " +
            "WHERE sc.owner_id = :ownerId " +
            "  AND a.attendance_date BETWEEN :startDate AND :endDate ");
        if (schoolId != null) sql.append("AND a.school_id = :schoolId ");
        if (classId  != null) sql.append("AND a.standard_id = :classId ");
        sql.append("GROUP BY period ORDER BY period ASC");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("ownerId",   ownerId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate",   endDate);
        if (schoolId != null) query.setParameter("schoolId", schoolId);
        if (classId  != null) query.setParameter("classId",  classId);

        return mapRows(query.getResultList(), new String[]{"period", "totalRecords", "presentCount", "attendancePct"});
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getEnrollmentReport(Long ownerId, LocalDate startDate, LocalDate endDate,
                                                          Long schoolId, String groupBy) {
        String dateTrunc = buildDateTrunc("s.admission_date", groupBy);
        StringBuilder sql = new StringBuilder(
            "SELECT " + dateTrunc + " AS period, " +
            "       COUNT(s.id) AS enrollments " +
            "FROM students s " +
            "JOIN school sc ON sc.school_id = s.school_id " +
            "WHERE sc.owner_id = :ownerId " +
            "  AND s.admission_date BETWEEN :startDate AND :endDate ");
        if (schoolId != null) sql.append("AND s.school_id = :schoolId ");
        sql.append("GROUP BY period ORDER BY period ASC");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("ownerId",   ownerId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate",   endDate);
        if (schoolId != null) query.setParameter("schoolId", schoolId);

        return mapRows(query.getResultList(), new String[]{"period", "enrollments"});
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getFeeReport(Long ownerId, LocalDate startDate, LocalDate endDate,
                                                   Long schoolId, Long classId, String groupBy) {
        String dateTrunc = buildDateTrunc("fp.payment_date", groupBy);
        StringBuilder sql = new StringBuilder(
            "SELECT " + dateTrunc + " AS period, " +
            "       fp.payment_mode, " +
            "       COALESCE(SUM(fp.amount_paid), 0) AS amount, " +
            "       COUNT(fp.payment_id) AS count " +
            "FROM fee_payment fp " +
            "JOIN school sc ON sc.school_id = fp.school_id " +
            "JOIN students s ON s.id = fp.student_id " +
            "WHERE sc.owner_id = :ownerId " +
            "  AND fp.payment_date BETWEEN :startDate AND :endDate ");
        if (schoolId != null) sql.append("AND fp.school_id = :schoolId ");
        if (classId  != null) sql.append("AND s.standard_id = :classId ");
        sql.append("GROUP BY period, fp.payment_mode ORDER BY period ASC");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("ownerId",   ownerId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate",   endDate);
        if (schoolId != null) query.setParameter("schoolId", schoolId);
        if (classId  != null) query.setParameter("classId",  classId);

        return mapRows(query.getResultList(), new String[]{"period", "paymentMode", "amount", "count"});
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private String buildDateTrunc(String column, String groupBy) {
        if (groupBy == null) groupBy = "month";
        return switch (groupBy.toLowerCase()) {
            case "day"  -> "DATE_TRUNC('day',  " + column + ")";
            case "year" -> "DATE_TRUNC('year', " + column + ")";
            default     -> "DATE_TRUNC('month'," + column + ")";
        };
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> mapRows(List<Object[]> rows, String[] keys) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new HashMap<>();
            for (int i = 0; i < keys.length && i < row.length; i++) {
                m.put(keys[i], row[i]);
            }
            result.add(m);
        }
        return result;
    }
}
