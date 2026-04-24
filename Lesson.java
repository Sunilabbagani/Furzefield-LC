package flc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single scheduled group exercise lesson.
 *
 * Key constraints:
 *  - Maximum capacity: 4 members per lesson
 *  - Price is fixed per exercise type (not per time slot)
 *  - Reviews are collected after attendance
 */
public class Lesson {

    public static final int MAX_CAPACITY = 4;

    private final String   lessonId;
    private final String   exerciseType;   // e.g. "Yoga", "Zumba"
    private final Day      day;
    private final TimeSlot timeSlot;
    private final int      weekNumber;     // 1–8 (weekend number)
    private final double   price;

    /** Member IDs currently booked into this lesson (active, non-cancelled). */
    private final List<String> bookedMemberIds = new ArrayList<>();

    /** All reviews left for this lesson (only after attendance). */
    private final List<Review> reviews = new ArrayList<>();

    public Lesson(String lessonId, String exerciseType, Day day,
                  TimeSlot timeSlot, int weekNumber, double price) {
        this.lessonId     = lessonId;
        this.exerciseType = exerciseType;
        this.day          = day;
        this.timeSlot     = timeSlot;
        this.weekNumber   = weekNumber;
        this.price        = price;
    }

    // ── Capacity helpers ─────────────────────────────────────────────────────

    public boolean hasSpace() {
        return bookedMemberIds.size() < MAX_CAPACITY;
    }

    public int getAvailableSpaces() {
        return MAX_CAPACITY - bookedMemberIds.size();
    }

    public boolean isMemberBooked(String memberId) {
        return bookedMemberIds.contains(memberId);
    }

    /** Adds a member to the active booking list. */
    public void addMember(String memberId) {
        if (!hasSpace()) {
            throw new IllegalStateException("Lesson is full.");
        }
        if (isMemberBooked(memberId)) {
            throw new IllegalStateException("Member already booked in this lesson.");
        }
        bookedMemberIds.add(memberId);
    }

    /** Removes a member from the active booking list (cancel / change). */
    public void removeMember(String memberId) {
        bookedMemberIds.remove(memberId);
    }

    // ── Review helpers ───────────────────────────────────────────────────────

    public void addReview(Review review) {
        reviews.add(review);
    }

    /**
     * Calculates average rating across all submitted reviews.
     * Returns 0.0 if no reviews exist.
     */
    public double getAverageRating() {
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    /** Number of times this lesson was attended (review count acts as proxy). */
    public int getAttendanceCount() {
        return reviews.size();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String    getLessonId()     { return lessonId; }
    public String    getExerciseType() { return exerciseType; }
    public Day       getDay()          { return day; }
    public TimeSlot  getTimeSlot()     { return timeSlot; }
    public int       getWeekNumber()   { return weekNumber; }
    public double    getPrice()        { return price; }
    public List<String> getBookedMemberIds() { return new ArrayList<>(bookedMemberIds); }
    public List<Review> getReviews()   { return new ArrayList<>(reviews); }

    @Override
    public String toString() {
        return String.format(
            "%-8s | %-10s | %-9s | %-15s | Week %-2d | £%-6.2f | %d/%d spaces",
            lessonId, exerciseType, day.getDisplayName(),
            timeSlot.getDisplayName(), weekNumber, price,
            bookedMemberIds.size(), MAX_CAPACITY
        );
    }
}
