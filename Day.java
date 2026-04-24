package flc.model;

/**
 * Represents the two days on which lessons are scheduled at Furzefield Leisure Centre.
 */
public enum Day {
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    private final String displayName;

    Day(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
