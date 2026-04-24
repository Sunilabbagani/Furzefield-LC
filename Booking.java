package flc.model;

/**
 * Represents a single booking linking a Member to a Lesson.
 *
 * Booking IDs are unique and are never reused, even after cancellation.
 * Status lifecycle: BOOKED → CHANGED | CANCELLED | ATTENDED
 */
public class Booking {

    private final String        bookingId;
    private final String        memberId;
    private       String        lessonId;
    private       BookingStatus status;

    public Booking(String bookingId, String memberId, String lessonId) {
        this.bookingId = bookingId;
        this.memberId  = memberId;
        this.lessonId  = lessonId;
        this.status    = BookingStatus.BOOKED;
    }

    // ── State transitions ─────────────────────────────────────────────────────

    /**
     * Moves the booking to a new lesson.
     * The caller is responsible for updating capacity on the old and new lessons.
     */
    public void changeTo(String newLessonId) {
        this.lessonId = newLessonId;
        this.status   = BookingStatus.CHANGED;
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    public void markAttended() {
        this.status = BookingStatus.ATTENDED;
    }

    // ── Predicates ───────────────────────────────────────────────────────────

    public boolean isActive() {
        return status == BookingStatus.BOOKED || status == BookingStatus.CHANGED;
    }

    public boolean isCancelled() {
        return status == BookingStatus.CANCELLED;
    }

    public boolean isAttended() {
        return status == BookingStatus.ATTENDED;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String        getBookingId() { return bookingId; }
    public String        getMemberId()  { return memberId; }
    public String        getLessonId()  { return lessonId; }
    public BookingStatus getStatus()    { return status; }

    @Override
    public String toString() {
        return String.format("Booking[%s] Member:%s Lesson:%s Status:%s",
                bookingId, memberId, lessonId, status);
    }
}
