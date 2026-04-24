package flc;

import flc.data.DataInitializer;
import flc.model.*;
import flc.service.SystemManager;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for the FLC Booking System.
 *
 * Covers:
 *  - Booking creation (happy path)
 *  - Capacity enforcement (max 4 per lesson)
 *  - Duplicate booking prevention
 *  - Time-conflict detection
 *  - Change booking
 *  - Cancel booking
 *  - Attend lesson and review submission
 *  - Rating validation
 *  - Report generation
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FLCSystemTest {

    private SystemManager sm;

    // ── Test fixtures ─────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        sm = new SystemManager();

        // Add 5 members
        sm.addMember(new Member("T001", "Test Alice",   "alice@t.com"));
        sm.addMember(new Member("T002", "Test Bob",     "bob@t.com"));
        sm.addMember(new Member("T003", "Test Clara",   "clara@t.com"));
        sm.addMember(new Member("T004", "Test David",   "david@t.com"));
        sm.addMember(new Member("T005", "Test Emma",    "emma@t.com"));

        // Two lessons at the same time slot (week 1, Saturday morning)
        sm.addLesson(new Lesson("TST001", "Yoga",    Day.SATURDAY, TimeSlot.MORNING,   1, 12.00));
        sm.addLesson(new Lesson("TST002", "Zumba",   Day.SATURDAY, TimeSlot.AFTERNOON, 1, 10.00));
        sm.addLesson(new Lesson("TST003", "Box Fit", Day.SATURDAY, TimeSlot.EVENING,   1, 11.00));
        sm.addLesson(new Lesson("TST004", "Yoga",    Day.SATURDAY, TimeSlot.MORNING,   2, 12.00));
        sm.addLesson(new Lesson("TST005", "Aquacise",Day.SUNDAY,   TimeSlot.MORNING,   1,  9.50));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BOOKING — HAPPY PATH
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("Book a lesson successfully")
    void testBookLessonSuccess() {
        Booking b = sm.bookLesson("T001", "TST001");

        assertNotNull(b, "Booking should not be null");
        assertEquals("T001",    b.getMemberId());
        assertEquals("TST001",  b.getLessonId());
        assertEquals(BookingStatus.BOOKED, b.getStatus());
        assertTrue(b.isActive());
    }

    @Test
    @Order(2)
    @DisplayName("Booking ID is unique across multiple bookings")
    void testBookingIdUniqueness() {
        Booking b1 = sm.bookLesson("T001", "TST001");
        Booking b2 = sm.bookLesson("T002", "TST001");

        assertNotEquals(b1.getBookingId(), b2.getBookingId(),
                "Each booking must have a unique ID");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CAPACITY
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(3)
    @DisplayName("Enforce maximum capacity of 4 per lesson")
    void testCapacityEnforcement() {
        sm.bookLesson("T001", "TST001");
        sm.bookLesson("T002", "TST001");
        sm.bookLesson("T003", "TST001");
        sm.bookLesson("T004", "TST001");

        // 5th booking must fail
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> sm.bookLesson("T005", "TST001"));

        assertTrue(ex.getMessage().contains("FULL"),
                "Error message should state the lesson is full");
    }

    @Test
    @Order(4)
    @DisplayName("Available spaces decrease after each booking")
    void testAvailableSpaces() {
        flc.model.Lesson lesson = sm.getTimetable().findById("TST001").orElseThrow();
        assertEquals(4, lesson.getAvailableSpaces());

        sm.bookLesson("T001", "TST001");
        assertEquals(3, lesson.getAvailableSpaces());

        sm.bookLesson("T002", "TST001");
        assertEquals(2, lesson.getAvailableSpaces());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DUPLICATE BOOKING PREVENTION
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("Prevent duplicate booking by same member in same lesson")
    void testDuplicateBookingPrevention() {
        sm.bookLesson("T001", "TST001");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> sm.bookLesson("T001", "TST001"));

        assertTrue(ex.getMessage().toLowerCase().contains("duplicate"),
                "Error message should mention duplicate booking");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TIME CONFLICT
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(6)
    @DisplayName("Detect time conflict: same week, day, and time slot")
    void testTimeConflictDetected() {
        // TST001 and a hypothetical second lesson at the same slot
        sm.addLesson(new Lesson("TST_CONF", "Body Blitz", Day.SATURDAY, TimeSlot.MORNING, 1, 10.50));
        sm.bookLesson("T001", "TST001");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> sm.bookLesson("T001", "TST_CONF"));

        assertTrue(ex.getMessage().toLowerCase().contains("conflict"),
                "Error message should mention time conflict");
    }

    @Test
    @Order(7)
    @DisplayName("No conflict for same day but different time slots")
    void testNoConflictDifferentTimeSlots() {
        sm.bookLesson("T001", "TST001"); // Saturday Morning
        Booking b = sm.bookLesson("T001", "TST002"); // Saturday Afternoon
        assertNotNull(b);
    }

    @Test
    @Order(8)
    @DisplayName("No conflict for same slot but different weeks")
    void testNoConflictDifferentWeeks() {
        sm.bookLesson("T001", "TST001"); // Week 1 Saturday Morning
        Booking b = sm.bookLesson("T001", "TST004"); // Week 2 Saturday Morning
        assertNotNull(b);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CHANGE BOOKING
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(9)
    @DisplayName("Change booking updates lesson assignment and status")
    void testChangeBooking() {
        Booking original = sm.bookLesson("T001", "TST001");
        String bookingId = original.getBookingId();

        sm.changeBooking(bookingId, "TST005"); // Aquacise Sunday

        Booking updated = sm.findBookingById(bookingId).orElseThrow();
        assertEquals("TST005",             updated.getLessonId());
        assertEquals(BookingStatus.CHANGED, updated.getStatus());

        // Old lesson should have freed the slot
        flc.model.Lesson oldLesson = sm.getTimetable().findById("TST001").orElseThrow();
        assertFalse(oldLesson.isMemberBooked("T001"),
                "Member should be removed from old lesson");

        // New lesson should have the member
        flc.model.Lesson newLesson = sm.getTimetable().findById("TST005").orElseThrow();
        assertTrue(newLesson.isMemberBooked("T001"),
                "Member should be in new lesson");
    }

    @Test
    @Order(10)
    @DisplayName("Cannot change to a full lesson")
    void testChangeToFullLesson() {
        sm.bookLesson("T001", "TST002");
        sm.bookLesson("T002", "TST002");
        sm.bookLesson("T003", "TST002");
        sm.bookLesson("T004", "TST002"); // now full

        Booking b = sm.bookLesson("T005", "TST001");

        assertThrows(IllegalStateException.class,
                () -> sm.changeBooking(b.getBookingId(), "TST002"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CANCEL BOOKING
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(11)
    @DisplayName("Cancel booking sets status to CANCELLED and frees slot")
    void testCancelBooking() {
        Booking b = sm.bookLesson("T001", "TST001");
        sm.cancelBooking(b.getBookingId());

        Booking cancelled = sm.findBookingById(b.getBookingId()).orElseThrow();
        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
        assertFalse(cancelled.isActive());

        flc.model.Lesson lesson = sm.getTimetable().findById("TST001").orElseThrow();
        assertFalse(lesson.isMemberBooked("T001"),
                "Slot should be freed after cancellation");
    }

    @Test
    @Order(12)
    @DisplayName("Cannot cancel an already cancelled booking")
    void testCannotCancelTwice() {
        Booking b = sm.bookLesson("T001", "TST001");
        sm.cancelBooking(b.getBookingId());

        assertThrows(IllegalStateException.class,
                () -> sm.cancelBooking(b.getBookingId()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ATTEND LESSON & REVIEWS
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(13)
    @DisplayName("Attend lesson marks status ATTENDED and records review")
    void testAttendLesson() {
        Booking b = sm.bookLesson("T001", "TST001");
        sm.attendLesson(b.getBookingId(), 5, "Amazing class!");

        Booking attended = sm.findBookingById(b.getBookingId()).orElseThrow();
        assertEquals(BookingStatus.ATTENDED, attended.getStatus());
        assertTrue(attended.isAttended());

        flc.model.Lesson lesson = sm.getTimetable().findById("TST001").orElseThrow();
        assertEquals(1, lesson.getAttendanceCount());
        assertEquals(5.0, lesson.getAverageRating(), 0.001);
    }

    @Test
    @Order(14)
    @DisplayName("Average rating is calculated correctly across multiple reviews")
    void testAverageRating() {
        Booking b1 = sm.bookLesson("T001", "TST001");
        Booking b2 = sm.bookLesson("T002", "TST001");
        Booking b3 = sm.bookLesson("T003", "TST001");

        sm.attendLesson(b1.getBookingId(), 4, "Good");
        sm.attendLesson(b2.getBookingId(), 2, "Not great");
        sm.attendLesson(b3.getBookingId(), 3, "Ok");

        flc.model.Lesson lesson = sm.getTimetable().findById("TST001").orElseThrow();
        assertEquals(3.0, lesson.getAverageRating(), 0.001, "(4+2+3)/3 = 3.0");
    }

    // ── Rating validation ─────────────────────────────────────────────────────

    @Test
    @Order(15)
    @DisplayName("Rating below 1 throws IllegalArgumentException")
    void testRatingTooLow() {
        Booking b = sm.bookLesson("T001", "TST001");
        assertThrows(IllegalArgumentException.class,
                () -> sm.attendLesson(b.getBookingId(), 0, "Test"));
    }

    @Test
    @Order(16)
    @DisplayName("Rating above 5 throws IllegalArgumentException")
    void testRatingTooHigh() {
        Booking b = sm.bookLesson("T001", "TST001");
        assertThrows(IllegalArgumentException.class,
                () -> sm.attendLesson(b.getBookingId(), 6, "Test"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  REPORTS
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(17)
    @DisplayName("Income report contains champion exercise type")
    void testIncomeReportContainsChampion() {
        Booking b = sm.bookLesson("T001", "TST001"); // Yoga £12
        sm.attendLesson(b.getBookingId(), 5, "Great");

        String report = sm.generateIncomeReport();
        assertTrue(report.contains("CHAMPION"), "Report should identify the income champion");
        assertTrue(report.contains("Yoga"),     "Yoga should appear in the report");
    }

    @Test
    @Order(18)
    @DisplayName("Lesson report contains attendance count and rating")
    void testLessonReportContainsData() {
        Booking b = sm.bookLesson("T001", "TST001");
        sm.attendLesson(b.getBookingId(), 4, "Nice");

        String report = sm.generateLessonReport();
        assertTrue(report.contains("TST001"), "Report should contain lesson ID");
        assertTrue(report.contains("Yoga"),   "Report should contain exercise name");
    }

    @Test
    @Order(19)
    @DisplayName("Non-attended bookings do not appear in income report")
    void testNonAttendedNotCountedInIncome() {
        sm.bookLesson("T001", "TST001"); // booked but not attended

        String report = sm.generateIncomeReport();
        assertFalse(report.contains("Yoga"),
                "Yoga should not appear when no attended bookings exist");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TIMETABLE QUERIES
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(20)
    @DisplayName("View timetable by day returns correct lessons")
    void testViewTimetableByDay() {
        List<Lesson> saturdayLessons = sm.viewTimetableByDay(Day.SATURDAY);

        assertTrue(saturdayLessons.size() >= 3,
                "Should have at least 3 Saturday lessons");
        saturdayLessons.forEach(l ->
                assertEquals(Day.SATURDAY, l.getDay()));
    }

    @Test
    @Order(21)
    @DisplayName("View timetable by exercise type filters correctly")
    void testViewTimetableByExercise() {
        List<Lesson> yogaLessons = sm.viewTimetableByExercise("Yoga");

        assertFalse(yogaLessons.isEmpty(), "Should find Yoga lessons");
        yogaLessons.forEach(l ->
                assertEquals("Yoga", l.getExerciseType()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FULL SYSTEM DATA
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(22)
    @DisplayName("DataInitializer creates at least 10 members and 48 lessons")
    void testDataInitializerCreatesRequiredData() {
        SystemManager fullSm = new SystemManager();
        DataInitializer.populate(fullSm);

        assertTrue(fullSm.getAllMembers().size() >= 10,
                "At least 10 members required");

        long lessonCount = fullSm.getTimetable().getAllLessons().size();
        assertTrue(lessonCount >= 48,
                "At least 48 lessons required (8 weekends × 6)");
    }

    @Test
    @Order(23)
    @DisplayName("DataInitializer seeds at least 20 reviews")
    void testDataInitializerSeeds20Reviews() {
        SystemManager fullSm = new SystemManager();
        DataInitializer.populate(fullSm);

        long reviewCount = fullSm.getTimetable().getAllLessons().stream()
                .mapToLong(l -> l.getReviews().size())
                .sum();

        assertTrue(reviewCount >= 20,
                "At least 20 reviews required, found: " + reviewCount);
    }
}
