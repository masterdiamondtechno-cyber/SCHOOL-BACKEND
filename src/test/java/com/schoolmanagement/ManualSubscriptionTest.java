package com.schoolmanagement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.schoolmanagement.school_admin_Entity.School;

class ManualSubscriptionTest {

    @Test
    void testActiveTrialIsNotExpired() {
        School school = new School();
        school.setSubscriptionSource("TRIAL");
        school.setTrialEndDate(LocalDateTime.now().plusDays(2));

        assertFalse(school.isSubscriptionExpired(), "Active trial should not be expired");
    }

    @Test
    void testExpiredTrialIsExpired() {
        School school = new School();
        school.setSubscriptionSource("TRIAL");
        school.setTrialEndDate(LocalDateTime.now().minusDays(1));

        assertTrue(school.isSubscriptionExpired(), "Trial ended in the past should be expired");
    }

    @Test
    void testNullTrialIsNotExpired() {
        School school = new School();
        school.setSubscriptionSource("TRIAL");
        school.setTrialEndDate(null);

        assertFalse(school.isSubscriptionExpired(), "Trial with null end date should not be expired (for backward compatibility)");
    }

    @Test
    void testActiveManualIsNotExpired() {
        School school = new School();
        school.setSubscriptionSource("MANUAL");
        school.setSubscriptionStartDate(LocalDateTime.now().minusDays(5));
        school.setSubscriptionEndDate(LocalDateTime.now().plusDays(10));

        assertFalse(school.isSubscriptionExpired(), "Active manual subscription should not be expired");
    }

    @Test
    void testExpiredManualIsExpired() {
        School school = new School();
        school.setSubscriptionSource("MANUAL");
        school.setSubscriptionStartDate(LocalDateTime.now().minusDays(15));
        school.setSubscriptionEndDate(LocalDateTime.now().minusDays(2));

        assertTrue(school.isSubscriptionExpired(), "Manual subscription ended in the past should be expired");
    }

    @Test
    void testNullManualIsNotExpired() {
        School school = new School();
        school.setSubscriptionSource("MANUAL");
        school.setSubscriptionEndDate(null);

        assertFalse(school.isSubscriptionExpired(), "Manual subscription with null end date should not be expired (for backward compatibility)");
    }

    @Test
    void testDefaultCompatBehavior() {
        // If source is null, it should fallback to TRIAL behavior
        School school = new School();
        school.setSubscriptionSource(null);
        school.setTrialEndDate(LocalDateTime.now().plusDays(1));

        assertFalse(school.isSubscriptionExpired(), "Null source should fallback to active trial check");

        school.setTrialEndDate(LocalDateTime.now().minusDays(1));
        assertTrue(school.isSubscriptionExpired(), "Null source should fallback to expired trial check");
    }
}
