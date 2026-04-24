package flc.model;

/**
 * Represents the three daily time slots available for lessons.
 * Used to detect booking time conflicts for a given member.
 */
public enum TimeSlot {
    MORNING("Morning", "09:00"),
    AFTERNOON("Afternoon", "13:00"),
    EVENING("Evening", "18:00");

    private final String displayName;
    private final String time;

    TimeSlot(String displayName, String time) {
        this.displayName = displayName;
        this.time = time;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTime() {
        return time;
    }

    @Override
    public String toString() {
        return displayName + " (" + time + ")";
    }
}
