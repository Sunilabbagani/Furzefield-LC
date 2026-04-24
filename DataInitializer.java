package flc.data;

import flc.model.*;
import flc.service.SystemManager;

/**
 * Populates the system with realistic sample data:
 *  - 10 members
 *  - 8 weekends × 6 lessons = 48 lessons
 *  - 20+ pre-seeded bookings with attendance and reviews
 *
 * Exercise prices (fixed per type):
 *   Yoga      £12.00
 *   Zumba     £10.00
 *   Aquacise  £9.50
 *   Box Fit   £11.00
 *   Body Blitz £10.50
 */
public class DataInitializer {

    // Fixed prices per exercise type
    private static final double YOGA_PRICE       = 12.00;
    private static final double ZUMBA_PRICE      = 10.00;
    private static final double AQUACISE_PRICE   =  9.50;
    private static final double BOXFIT_PRICE     = 11.00;
    private static final double BODYBLITZ_PRICE  = 10.50;

    public static void populate(SystemManager sm) {
        addMembers(sm);
        addLessons(sm);
        addBookingsAndReviews(sm);
    }

    // ── Members ───────────────────────────────────────────────────────────────

    private static void addMembers(SystemManager sm) {
        sm.addMember(new Member("M001", "Alice Thompson",   "alice@email.com"));
        sm.addMember(new Member("M002", "Bob Patel",        "bob@email.com"));
        sm.addMember(new Member("M003", "Clara Nguyen",     "clara@email.com"));
        sm.addMember(new Member("M004", "David Harris",     "david@email.com"));
        sm.addMember(new Member("M005", "Emma Wilson",      "emma@email.com"));
        sm.addMember(new Member("M006", "Frank Okafor",     "frank@email.com"));
        sm.addMember(new Member("M007", "Grace Kim",        "grace@email.com"));
        sm.addMember(new Member("M008", "Harry Brennan",    "harry@email.com"));
        sm.addMember(new Member("M009", "Isla Fernandez",   "isla@email.com"));
        sm.addMember(new Member("M010", "James O'Brien",    "james@email.com"));
    }

    // ── Lessons (8 weekends, 6 lessons per weekend = 48 total) ───────────────

    private static void addLessons(SystemManager sm) {
        // Pattern per weekend:
        //   Saturday Morning   → Yoga
        //   Saturday Afternoon → Zumba
        //   Saturday Evening   → Box Fit
        //   Sunday   Morning   → Aquacise
        //   Sunday   Afternoon → Body Blitz
        //   Sunday   Evening   → Yoga (different instance)

        for (int w = 1; w <= 8; w++) {
            sm.addLesson(new Lesson("L" + w + "SAM",  "Yoga",       Day.SATURDAY, TimeSlot.MORNING,    w, YOGA_PRICE));
            sm.addLesson(new Lesson("L" + w + "SAA",  "Zumba",      Day.SATURDAY, TimeSlot.AFTERNOON,  w, ZUMBA_PRICE));
            sm.addLesson(new Lesson("L" + w + "SAE",  "Box Fit",    Day.SATURDAY, TimeSlot.EVENING,    w, BOXFIT_PRICE));
            sm.addLesson(new Lesson("L" + w + "SUM",  "Aquacise",   Day.SUNDAY,   TimeSlot.MORNING,    w, AQUACISE_PRICE));
            sm.addLesson(new Lesson("L" + w + "SUA",  "Body Blitz", Day.SUNDAY,   TimeSlot.AFTERNOON,  w, BODYBLITZ_PRICE));
            sm.addLesson(new Lesson("L" + w + "SUE",  "Yoga",       Day.SUNDAY,   TimeSlot.EVENING,    w, YOGA_PRICE));
        }
    }

    // ── Bookings, attendance and reviews ─────────────────────────────────────

    private static void addBookingsAndReviews(SystemManager sm) {
        // Week 1 – Saturday morning Yoga
        attend(sm, "M001", "L1SAM", 5, "Brilliant session, really relaxing!");
        attend(sm, "M002", "L1SAM", 4, "Great instructor, will come back.");
        attend(sm, "M003", "L1SAM", 5, "Best yoga class I have attended.");

        // Week 1 – Saturday afternoon Zumba
        attend(sm, "M004", "L1SAA", 4, "High energy, loved it.");
        attend(sm, "M005", "L1SAA", 3, "Good but music was a bit loud.");

        // Week 1 – Sunday morning Aquacise
        attend(sm, "M006", "L1SUM", 5, "Perfect low-impact workout.");
        attend(sm, "M007", "L1SUM", 4, "Really enjoyed it.");

        // Week 2 – Saturday evening Box Fit
        attend(sm, "M001", "L2SAE", 4, "Intense and fun!");
        attend(sm, "M008", "L2SAE", 5, "Best cardio workout in ages.");
        attend(sm, "M009", "L2SAE", 3, "Quite tough but fair.");

        // Week 2 – Sunday afternoon Body Blitz
        attend(sm, "M010", "L2SUA", 4, "Full body burn – excellent.");
        attend(sm, "M002", "L2SUA", 5, "Loved every minute.");

        // Week 3 – Saturday morning Yoga
        attend(sm, "M003", "L3SAM", 5, "So calming, great way to start the weekend.");
        attend(sm, "M005", "L3SAM", 4, "Very well paced.");

        // Week 3 – Sunday evening Yoga
        attend(sm, "M006", "L3SUE", 3, "Good class but a bit short.");
        attend(sm, "M007", "L3SUE", 4, "Instructor very helpful.");

        // Week 4 – Saturday afternoon Zumba
        attend(sm, "M008", "L4SAA", 5, "Amazing choreography!");
        attend(sm, "M009", "L4SAA", 4, "Great fun.");

        // Week 4 – Sunday morning Aquacise
        attend(sm, "M010", "L4SUM", 5, "Refreshing and effective.");
        attend(sm, "M001", "L4SUM", 4, "Really good for joints.");

        // Week 5 – Saturday evening Box Fit (extra reviews)
        attend(sm, "M004", "L5SAE", 5, "Pushed myself harder than ever.");
        attend(sm, "M002", "L5SAE", 4, "Great class, will book again.");

        // Active (non-attended) bookings so members can be seen in the system
        silentBook(sm, "M003", "L5SAA");  // Zumba week 5
        silentBook(sm, "M005", "L5SUA");  // Body Blitz week 5
        silentBook(sm, "M006", "L6SAM");  // Yoga week 6
        silentBook(sm, "M007", "L6SAE");  // Box Fit week 6
        silentBook(sm, "M008", "L7SAM");  // Yoga week 7
        silentBook(sm, "M009", "L7SUM");  // Aquacise week 7
        silentBook(sm, "M010", "L8SAA");  // Zumba week 8
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Creates a booking and immediately marks it attended with a review.
     * Errors are printed to stderr (should not occur with valid test data).
     */
    private static void attend(SystemManager sm, String memberId,
                                String lessonId, int rating, String comment) {
        try {
            Booking b = sm.bookLesson(memberId, lessonId);
            sm.attendLesson(b.getBookingId(), rating, comment);
        } catch (Exception e) {
            System.err.println("[DataInit] Could not complete attend for " +
                               memberId + "/" + lessonId + ": " + e.getMessage());
        }
    }

    /** Creates an active (not yet attended) booking. */
    private static void silentBook(SystemManager sm, String memberId, String lessonId) {
        try {
            sm.bookLesson(memberId, lessonId);
        } catch (Exception e) {
            System.err.println("[DataInit] Could not book " +
                               memberId + "/" + lessonId + ": " + e.getMessage());
        }
    }
}
