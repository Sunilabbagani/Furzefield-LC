package flc.ui;

import flc.model.*;
import flc.service.SystemManager;

import java.util.*;

public class SmartConsoleUI {

    private final SystemManager manager;
    private final Scanner input;

    public SmartConsoleUI(SystemManager manager) {
        this.manager = manager;
        this.input = new Scanner(System.in);
    }

    // ================= ENTRY =================

    public void start() {
        showWelcome();

        while (true) {
            int option = menu();

            if (option == 0) {
                exitSystem();
                break;
            }

            execute(option);
        }
    }

    // ================= MENU =================

    private int menu() {
        System.out.println("\n========== FLC SYSTEM ==========");
        System.out.println("1 → Timetable");
        System.out.println("2 → Book");
        System.out.println("3 → Modify Booking");
        System.out.println("4 → Cancel Booking");
        System.out.println("5 → Attend + Review");
        System.out.println("6 → My Bookings");
        System.out.println("7 → Members");
        System.out.println("8 → Lesson Report");
        System.out.println("9 → Revenue Report");
        System.out.println("0 → Exit");

        return getInt("Select option: ", 0, 9);
    }

    private void execute(int choice) {
        switch (choice) {
            case 1 -> handleTimetable();
            case 2 -> handleBooking();
            case 3 -> handleModification();
            case 4 -> handleCancellation();
            case 5 -> handleAttendance();
            case 6 -> showUserBookings();
            case 7 -> showMembers();
            case 8 -> System.out.println(manager.generateLessonReport());
            case 9 -> System.out.println(manager.generateIncomeReport());
        }
    }

    // ================= FEATURES =================

    private void handleTimetable() {
        System.out.println("\n[ TIMETABLE VIEW ]");
        System.out.println("1. Filter by Day");
        System.out.println("2. Filter by Exercise");

        int type = getInt("Choose: ", 1, 2);

        List<Lesson> lessons;

        if (type == 1) {
            lessons = manager.viewTimetableByDay(pickDay());
        } else {
            lessons = manager.viewTimetableByExercise(pickExercise());
        }

        displayLessons(lessons);
    }

    private void handleBooking() {
        System.out.println("\n[ BOOK LESSON ]");

        String member = getMember();
        listAvailableLessons();

        String lesson = getText("Lesson ID: ");

        try {
            Booking b = manager.bookLesson(member, lesson);
            success("Booked! ID: " + b.getBookingId());
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    private void handleModification() {
        System.out.println("\n[ MODIFY BOOKING ]");

        String member = getMember();
        List<Booking> list = manager.getActiveBookingsForMember(member);

        if (list.isEmpty()) {
            error("No active bookings.");
            return;
        }

        displayBookings(list);

        String bookingId = getText("Booking ID: ");
        listAvailableLessons();
        String newLesson = getText("New Lesson ID: ");

        try {
            manager.changeBooking(bookingId, newLesson);
            success("Updated successfully.");
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    private void handleCancellation() {
        System.out.println("\n[ CANCEL BOOKING ]");

        String member = getMember();
        List<Booking> list = manager.getActiveBookingsForMember(member);

        if (list.isEmpty()) {
            error("Nothing to cancel.");
            return;
        }

        displayBookings(list);

        String id = getText("Booking ID: ");

        try {
            manager.cancelBooking(id);
            success("Cancelled.");
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    private void handleAttendance() {
        System.out.println("\n[ ATTEND SESSION ]");

        String member = getMember();
        List<Booking> list = manager.getActiveBookingsForMember(member);

        if (list.isEmpty()) {
            error("No sessions found.");
            return;
        }

        displayBookings(list);

        String id = getText("Booking ID: ");
        int rating = getInt("Rating (1-5): ", 1, 5);
        String review = getText("Comment: ");

        try {
            manager.attendLesson(id, rating, review);
            success("Recorded.");
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    private void showUserBookings() {
        System.out.println("\n[ MY BOOKINGS ]");

        String member = getMember();
        List<Booking> list = manager.getAllBookingsForMember(member);

        if (list.isEmpty()) {
            System.out.println("No records.");
            return;
        }

        displayBookings(list);
    }

    private void showMembers() {
        System.out.println("\n[ MEMBERS LIST ]");
        manager.getAllMembers().forEach(System.out::println);
    }

    // ================= DISPLAY =================

    private void displayLessons(List<Lesson> lessons) {

        System.out.println("\n══════════════════════════════════════════════════════════════════════════════");
        System.out.println("                               LESSON TIMETABLE");
        System.out.println("══════════════════════════════════════════════════════════════════════════════");

        System.out.printf("│ %-6s │ %-10s │ %-9s │ %-10s │ %-4s │ %-7s │ %-9s │%n",
                "ID", "Exercise", "Day", "Time", "Week", "Price", "Slots");

        System.out.println("├────────┼────────────┼───────────┼────────────┼──────┼─────────┼───────────┤");

        for (Lesson l : lessons) {
            System.out.printf("│ %-6s │ %-10s │ %-9s │ %-10s │ %-4d │ £%-6.2f │ %-9s │%n",
                    l.getLessonId(),
                    l.getExerciseType(),
                    l.getDay().getDisplayName(),
                    l.getTimeSlot().getDisplayName(),
                    l.getWeekNumber(),
                    l.getPrice(),
                    l.getBookedMemberIds().size() + "/" + Lesson.MAX_CAPACITY
            );
        }

        System.out.println("══════════════════════════════════════════════════════════════════════════════\n");
    }
    private void displayBookings(List<Booking> list) {
        System.out.println("\nBookingID | LessonID | Status");

        for (Booking b : list) {
            System.out.printf("%s | %s | %s\n",
                    b.getBookingId(),
                    b.getLessonId(),
                    b.getStatus());
        }
    }

    private void listAvailableLessons() {
        System.out.println("\nAvailable:");

        manager.getTimetable().getAllLessons().stream()
                .filter(Lesson::hasSpace)
                .forEach(l -> System.out.println(
                        l.getLessonId() + " (" + l.getExerciseType() + ")"
                ));
    }

    // ================= INPUT =================

    private String getMember() {
        return getText("Member ID (M001-M010): ");
    }

    private Day pickDay() {
        return getInt("1-Sat  2-Sun: ", 1, 2) == 1 ? Day.SATURDAY : Day.SUNDAY;
    }

    private String pickExercise() {
        List<String> list = manager.getAllExerciseTypes();

        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ". " + list.get(i));
        }

        return list.get(getInt("Select: ", 1, list.size()) - 1);
    }

    private String getText(String msg) {
        System.out.print(msg);
        return input.nextLine().trim().toUpperCase();
    }

    private int getInt(String msg, int min, int max) {
        while (true) {
            try {
                int val = Integer.parseInt(getText(msg));
                if (val >= min && val <= max) return val;
            } catch (Exception ignored) {}
            System.out.println("Invalid input.");
        }
    }

    // ================= MESSAGES =================

    private void showWelcome() {
        System.out.println("\n=== FLC BOOKING SYSTEM ===\n");
    }

    private void success(String msg) {
        System.out.println("✔ " + msg);
    }

    private void error(String msg) {
        System.out.println("✖ " + msg);
    }

    private void exitSystem() {
        System.out.println("\nExiting system...\n");
    }
}