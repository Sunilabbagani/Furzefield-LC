package flc.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds and queries all scheduled lessons across all weekends.
 *
 * Responsibilities:
 *  - Store lessons indexed by lessonId, day, exercise type, and week
 *  - Provide filtered views used by the menu and reports
 */
public class Timetable {

    /** Primary store: lessonId → Lesson */
    private final Map<String, Lesson> lessons = new LinkedHashMap<>();

    // ── Mutation ─────────────────────────────────────────────────────────────

    public void addLesson(Lesson lesson) {
        lessons.put(lesson.getLessonId(), lesson);
    }

    // ── Lookups ───────────────────────────────────────────────────────────────

    public Optional<Lesson> findById(String lessonId) {
        return Optional.ofNullable(lessons.get(lessonId));
    }

    /** All lessons on a specific day, sorted by week then time slot. */
    public List<Lesson> getLessonsByDay(Day day) {
        return lessons.values().stream()
                .filter(l -> l.getDay() == day)
                .sorted(Comparator.comparingInt(Lesson::getWeekNumber)
                        .thenComparing(Lesson::getTimeSlot))
                .collect(Collectors.toList());
    }

    /** All lessons of a given exercise type. */
    public List<Lesson> getLessonsByExerciseType(String exerciseType) {
        return lessons.values().stream()
                .filter(l -> l.getExerciseType().equalsIgnoreCase(exerciseType))
                .sorted(Comparator.comparingInt(Lesson::getWeekNumber)
                        .thenComparing(Lesson::getDay)
                        .thenComparing(Lesson::getTimeSlot))
                .collect(Collectors.toList());
    }

    /** All distinct exercise types currently in the timetable. */
    public List<String> getAllExerciseTypes() {
        return lessons.values().stream()
                .map(Lesson::getExerciseType)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public Collection<Lesson> getAllLessons() {
        return Collections.unmodifiableCollection(lessons.values());
    }

    /**
     * Returns the lesson on a specific day + time slot in a given week,
     * used for conflict detection.
     */
    public Optional<Lesson> findLesson(int weekNumber, Day day, TimeSlot timeSlot) {
        return lessons.values().stream()
                .filter(l -> l.getWeekNumber() == weekNumber
                          && l.getDay() == day
                          && l.getTimeSlot() == timeSlot)
                .findFirst();
    }
}
