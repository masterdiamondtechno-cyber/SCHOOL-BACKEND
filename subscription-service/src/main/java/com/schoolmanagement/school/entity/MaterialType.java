package com.schoolmanagement.school.entity;

/**
 * Enum representing all valid study-material types.
 *
 * Stored in the database as the enum name (e.g. "NOTES", "PRACTICE_SHEET").
 * The {@code label} field holds the human-readable display name (e.g. "Practice Sheet")
 * that is returned in API responses and accepted in API requests.
 *
 * Accepted input formats (case-insensitive):
 *   - Enum name  : "PRACTICE_SHEET"
 *   - Display label: "Practice Sheet"
 */
public enum MaterialType {

    NOTES("Notes"),
    WORKSHEET("Worksheet"),
    PRACTICE_SHEET("Practice Sheet"),
    REFERENCE("Reference"),
    ASSIGNMENT("Assignment"),
    REVISION("Revision");

    private final String label;

    MaterialType(String label) {
        this.label = label;
    }

    /** Human-readable name sent in API responses. */
    public String getLabel() {
        return label;
    }

    /**
     * Parses from either the enum name ("PRACTICE_SHEET") or the
     * display label ("Practice Sheet"), case-insensitively.
     *
     * @throws IllegalArgumentException if {@code value} matches neither.
     */
    public static MaterialType fromValue(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("'materialType' must not be blank");

        for (MaterialType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())
                    || type.label.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }

        // Build a helpful error listing all valid labels
        StringBuilder valid = new StringBuilder();
        for (MaterialType t : values()) {
            if (valid.length() > 0) valid.append(", ");
            valid.append(t.label);
        }
        throw new IllegalArgumentException(
                "Invalid materialType '" + value + "'. Valid values: " + valid);
    }

    /** Convenience: returns the display label instead of the enum name in toString(). */
    @Override
    public String toString() {
        return label;
    }
}
