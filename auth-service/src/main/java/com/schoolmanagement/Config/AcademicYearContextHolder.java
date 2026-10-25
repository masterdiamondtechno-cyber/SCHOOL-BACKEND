package com.schoolmanagement.Config;

public class AcademicYearContextHolder {

    private static final ThreadLocal<Long> currentAcademicYearId = new ThreadLocal<>();

    public static void setCurrentAcademicYearId(Long id) {
        currentAcademicYearId.set(id);
    }

    public static Long getCurrentAcademicYearId() {
        return currentAcademicYearId.get();
    }

    public static void clear() {
        currentAcademicYearId.remove();
    }
}
