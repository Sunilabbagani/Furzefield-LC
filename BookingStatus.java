package flc.model;

/**
 * Lifecycle states for a booking.
 * Only ATTENDED bookings contribute to income and rating reports.
 */
public enum BookingStatus {
    BOOKED,
    CHANGED,
    CANCELLED,
    ATTENDED
}
