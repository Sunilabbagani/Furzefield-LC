package flc.model;

/**
 * Represents a registered member of Furzefield Leisure Centre.
 * Each member has a unique ID, name, and email address.
 */
public class Member {

    private final String memberId;
    private final String name;
    private final String email;

    public Member(String memberId, String name, String email) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getMemberId() { return memberId; }
    public String getName()     { return name; }
    public String getEmail()    { return email; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", memberId, name, email);
    }
}
