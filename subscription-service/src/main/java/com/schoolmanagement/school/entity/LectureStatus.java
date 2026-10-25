package com.schoolmanagement.school.entity;

/**
 * Enum for validating lecture status values in the controller layer.
 * The entity stores status as a plain String to match the StudyMaterial convention.
 */
public enum LectureStatus {

    PUBLISHED("Published"),
    PROCESSING("Processing"),
    DRAFT("Draft"),
    FAILED("Failed");

    private final String label;

    LectureStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Parse from any form: "Published", "PUBLISHED", "published"
     */
    public static LectureStatus fromValue(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Status value cannot be null or blank");

        for (LectureStatus s : values()) {
            if (s.name().equalsIgnoreCase(value) || s.label.equalsIgnoreCase(value))
                return s;
        }
        throw new IllegalArgumentException("Invalid lecture status: " + value
                + ". Allowed values: Published, Processing, Draft, Failed");
    }
}
