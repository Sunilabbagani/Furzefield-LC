package flc.service;

import flc.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Central controller for the Furzefield Leisure Centre booking system.
 *
 * Design pattern: Façade — provides a single, clean API over the domain
 * objects (Member, Lesson, Booking, Timetable, Review).
 *
 * All business rules are enforced here:
 *  - Capacity checks
 *  - Duplicate booking prevention
 *  - Time-conflict detection
 *  - Booking ID uniqueness
 *  - Report generation
 */
public class SystemManager {

    // ── Domain stores ─────────────────────────────────────────────────────────

    private final Map<String, Member>  members  = new LinkedHashMap<>();
    private final Timetable            timetable = new Timetable();
    /** bookingId → Booking (IDs are never reused) */
    private final Map<String, Booking> bookings = new LinkedHashMap<>();

    private int bookingCounter = 1;

    // ══════════════════════════════════════════════════════════════════════════
    //  MEMBER MANAGEMENT
    // ══════════════════════════════════════════════════════════════════════════

    public void addMember(Member member) {
        members.put(member.getMemberId(), member);
    }

    public Optional<Member> findMemberById(String memberId) {
        return Optional.ofNullable(members.get(memberId));
    }

    public Collection<Member> getAllMembers() {
        return Collections.unmodifiableCollection(members.values());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TIMETABLE
    // ══════════════════════════════════════════════════════════════════════════

    public void addLesson(Lesson lesson) {
        timetable.addLesson(lesson);
    }

    public Timetable getTimetable() {
        return timetable;
    }

    /** Returns all lessons on the given day. */
    public List<Lesson> viewTimetableByDay(Day day) {
        return timetable.getLessonsByDay(day);
    }

    /** Returns all lessons for the given exercise type (case-insensitive). */
    public List<Lesson> viewTimetableByExercise(String exerciseType) {
        return timetable.getLessonsByExerciseType(exerciseType);
    }

    public List<String> getAllExerciseTypes() {
        return timetable.getAllExerciseTypes();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BOOKING
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Books a lesson for a member.
     *
     * Preconditions checked:
     *  1. Member exists
     *  2. Lesson exists
     *  3. Lesson has capacity
     *  4. Member has not already booked this lesson (active booking)
     *  5. No time conflict (same week, day, time slot)
     *
     * @return the new Booking on success
     * @throws IllegalArgumentException / IllegalStateException on violations
     */
    public Booking bookLesson(String memberId, String lessonId) {
        Member member = requireMember(memberId);
        Lesson lesson = requireLesson(lessonId);

        // 1. Capacity
        if (!lesson.hasSpace()) {
            throw new IllegalStateException(
                "Sorry, the lesson '" + lesson.getExerciseType() +
                "' on " + lesson.getDay() + " " + lesson.getTimeSlot().getDisplayName() +
                " (Week " + lesson.getWeekNumber() + ") is FULL.");
        }

        // 2. Duplicate booking
        boolean alreadyBooked = bookings.values().stream()
                .anyMatch(b -> b.getMemberId().equals(memberId)
                            && b.getLessonId().equals(lessonId)
                            && b.isActive());
        if (alreadyBooked) {
            throw new IllegalStateException(
                "Duplicate booking not allowed: " + member.getName() +
                " already has an active booking for lesson " + lessonId + ".");
        }

        // 3. Time conflict
        checkTimeConflict(memberId, lesson, null);

        // All checks passed — create booking
        String bookingId = generateBookingId();
        Booking booking = new Booking(bookingId, memberId, lessonId);
        bookings.put(bookingId, booking);
        lesson.addMember(memberId);

        return booking;
    }

    /**
     * Changes an existing booking to a different lesson.
     *
     * Rules:
     *  - Booking must be active (BOOKED or CHANGED)
     *  - New lesson must have space
     *  - No time conflict with other active bookings
     */
    public Booking changeBooking(String bookingId, String newLessonId) {
        Booking booking = requireActiveBooking(bookingId);
        Lesson  oldLesson = requireLesson(booking.getLessonId());
        Lesson  newLesson = requireLesson(newLessonId);

        if (booking.getLessonId().equals(newLessonId)) {
            throw new IllegalArgumentException("New lesson must be different from the current lesson.");
        }

        if (!newLesson.hasSpace()) {
            throw new IllegalStateException(
                "The new lesson '" + newLesson.getExerciseType() +
                "' is FULL. Change not possible.");
        }

        // Check conflict ignoring the old booking (it will be released)
        checkTimeConflict(booking.getMemberId(), newLesson, booking.getBookingId());

        // Apply change
        oldLesson.removeMember(booking.getMemberId());
        newLesson.addMember(booking.getMemberId());
        booking.changeTo(newLessonId);

        return booking;
    }

    /**
     * Cancels a booking and releases the slot.
     * The booking record is retained with CANCELLED status.
     */
    public void cancelBooking(String bookingId) {
        Booking booking = requireActiveBooking(bookingId);
        Lesson lesson   = requireLesson(booking.getLessonId());

        lesson.removeMember(booking.getMemberId());
        booking.cancel();
    }

    /**
     * Marks a booking as attended and records the member's review.
     *
     * @param rating  integer 1–5
     * @param comment free-text review
     */
    public void attendLesson(String bookingId, int rating, String comment) {
        Booking booking = requireActiveBooking(bookingId);
        Lesson lesson   = requireLesson(booking.getLessonId());

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        booking.markAttended();
        Review review = new Review(booking.getMemberId(), lesson.getLessonId(), rating, comment);
        lesson.addReview(review);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  REPORTS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Lesson Attendance & Rating Report.
     *
     * For each lesson that has at least one attended booking, prints:
     *  - Lesson details
     *  - Number of attendees (= number of reviews)
     *  - Average rating
     */
    public String generateLessonReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("═".repeat(80)).append("\n");
        sb.append("  LESSON ATTENDANCE & AVERAGE RATING REPORT\n");
        sb.append("═".repeat(80)).append("\n");
        sb.append(String.format("%-8s %-12s %-10s %-15s %-6s %-11s %s%n",
                "ID", "Exercise", "Day", "Time Slot", "Week", "Attendees", "Avg Rating"));
        sb.append("─".repeat(80)).append("\n");

        timetable.getAllLessons().stream()
                .filter(l -> l.getAttendanceCount() > 0)
                .sorted(Comparator.comparingInt(Lesson::getWeekNumber)
                        .thenComparing(Lesson::getDay)
                        .thenComparing(Lesson::getTimeSlot))
                .forEach(lesson -> sb.append(String.format(
                        "%-8s %-12s %-10s %-15s %-6d %-11d %.2f%n",
                        lesson.getLessonId(),
                        lesson.getExerciseType(),
                        lesson.getDay().getDisplayName(),
                        lesson.getTimeSlot().getDisplayName(),
                        lesson.getWeekNumber(),
                        lesson.getAttendanceCount(),
                        lesson.getAverageRating())));

        sb.append("═".repeat(80)).append("\n");
        return sb.toString();
    }

    /**
     * Income Report — finds the exercise type with the highest total income.
     *
     * Income is calculated only from ATTENDED bookings
     * (price × number of attendees per lesson, summed per exercise type).
     */
    public String generateIncomeReport() {
        // Build income map: exerciseType → total income
        Map<String, Double> incomeByType = new LinkedHashMap<>();

        for (Lesson lesson : timetable.getAllLessons()) {
            int attendedCount = lesson.getAttendanceCount();
            if (attendedCount == 0) continue;

            double income = lesson.getPrice() * attendedCount;
            incomeByType.merge(lesson.getExerciseType(), income, Double::sum);
        }

        if (incomeByType.isEmpty()) {
            return "No income data available yet.";
        }

        // Find champion
        String champion = Collections.max(incomeByType.entrySet(),
                Map.Entry.comparingByValue()).getKey();

        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("═".repeat(60)).append("\n");
        sb.append("  EXERCISE INCOME REPORT\n");
        sb.append("═".repeat(60)).append("\n");

        // Sort by income descending
        incomeByType.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> {
                    String marker = e.getKey().equals(champion) ? " ★ CHAMPION" : "";
                    sb.append(String.format("  %-15s £%,.2f%s%n",
                            e.getKey(), e.getValue(), marker));
                });

        sb.append("─".repeat(60)).append("\n");
        sb.append(String.format("  Highest income exercise: %s  (£%,.2f)%n",
                champion, incomeByType.get(champion)));
        sb.append("═".repeat(60)).append("\n");
        return sb.toString();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /** Returns all active bookings for a member. */
    public List<Booking> getActiveBookingsForMember(String memberId) {
        return bookings.values().stream()
                .filter(b -> b.getMemberId().equals(memberId) && b.isActive())
                .collect(Collectors.toList());
    }

    /** Returns all bookings (all statuses) for a member. */
    public List<Booking> getAllBookingsForMember(String memberId) {
        return bookings.values().stream()
                .filter(b -> b.getMemberId().equals(memberId))
                .collect(Collectors.toList());
    }

    public Optional<Booking> findBookingById(String bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    public Collection<Booking> getAllBookings() {
        return Collections.unmodifiableCollection(bookings.values());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private String generateBookingId() {
        return String.format("BK%04d", bookingCounter++);
    }

    private Member requireMember(String memberId) {
        return findMemberById(memberId).orElseThrow(
            () -> new IllegalArgumentException("Member not found: " + memberId));
    }

    private Lesson requireLesson(String lessonId) {
        return timetable.findById(lessonId).orElseThrow(
            () -> new IllegalArgumentException("Lesson not found: " + lessonId));
    }

    private Booking requireActiveBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found: " + bookingId);
        }
        if (!booking.isActive()) {
            throw new IllegalStateException(
                "Booking " + bookingId + " is not active (status: " + booking.getStatus() + ").");
        }
        return booking;
    }

    /**
     * Checks whether a member already has an active booking at the same
     * week/day/timeSlot as {@code targetLesson}.
     *
     * @param excludeBookingId if non-null, that booking is ignored (used during change)
     */
    private void checkTimeConflict(String memberId, Lesson targetLesson,
                                   String excludeBookingId) {
        bookings.values().stream()
                .filter(b -> b.getMemberId().equals(memberId))
                .filter(b -> b.isActive())
                .filter(b -> !b.getBookingId().equals(excludeBookingId))
                .forEach(b -> {
                    Lesson existing = timetable.findById(b.getLessonId()).orElse(null);
                    if (existing != null
                            && existing.getWeekNumber() == targetLesson.getWeekNumber()
                            && existing.getDay()        == targetLesson.getDay()
                            && existing.getTimeSlot()   == targetLesson.getTimeSlot()) {
                        throw new IllegalStateException(
                            "Time conflict: " + memberId + " already has a booking at " +
                            targetLesson.getDay() + " " +
                            targetLesson.getTimeSlot().getDisplayName() +
                            " in Week " + targetLesson.getWeekNumber() + ".");
                    }
                });
    }
}
