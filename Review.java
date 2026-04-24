package flc.model;

/**
 * A post-attendance review submitted by a member for a specific lesson.
 * Rating must be between 1 (Very Dissatisfied) and 5 (Very Satisfied).
 */
public class Review {

    private final String memberId;
    private final String lessonId;
    private final int rating;       // 1–5
    private final String comment;

    public Review(String memberId, String lessonId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        this.memberId = memberId;
        this.lessonId = lessonId;
        this.rating   = rating;
        this.comment  = comment;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getMemberId() { return memberId; }
    public String getLessonId() { return lessonId; }
    public int    getRating()   { return rating; }
    public String getComment()  { return comment; }

    /** Human-readable label for the numeric rating. */
    public String getRatingLabel() {
        return switch (rating) {
            case 1 -> "Very Dissatisfied";
            case 2 -> "Dissatisfied";
            case 3 -> "Ok";
            case 4 -> "Satisfied";
            case 5 -> "Very Satisfied";
            default -> "Unknown";
        };
    }

    @Override
    public String toString() {
        return String.format("Rating: %d (%s) | \"%s\"", rating, getRatingLabel(), comment);
    }
}
