
package com.alnadhari.campus;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DatabaseHelperInstrumentedTest {

    private SQLiteDatabase memoryDatabase;
    private DatabaseHelper databaseHelper;

    @Before
    public void setUp() {

        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        // Every test uses a fresh in-memory database.
        // The application's real database remains untouched.
        memoryDatabase = SQLiteDatabase.create(null);

        databaseHelper = new DatabaseHelper(context) {

            @Override
            public synchronized SQLiteDatabase getWritableDatabase() {
                return memoryDatabase;
            }

            @Override
            public synchronized SQLiteDatabase getReadableDatabase() {
                return memoryDatabase;
            }
        };

        databaseHelper.onConfigure(memoryDatabase);
        databaseHelper.onCreate(memoryDatabase);
    }

    @After
    public void tearDown() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        if (memoryDatabase != null && memoryDatabase.isOpen()) {
            memoryDatabase.close();
        }
    }

    @Test
    public void createsDefaultCourse() {

        try (Cursor cursor = databaseHelper.getAllCourses()) {

            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_COURSE_NAME
                    )
            );

            assertEquals("General", name);
        }
    }

    @Test
    public void canCreateEditAndDeleteCourse() {

        long courseId = databaseHelper.insertCourse(
                "  Mathematics  "
        );

        assertTrue(courseId > 0);

        // Course names are unique, ignoring case.
        assertEquals(
                -1L,
                databaseHelper.insertCourse("mathematics")
        );

        assertEquals(
                1,
                databaseHelper.updateCourse(
                        courseId,
                        "Applied Mathematics"
                )
        );

        try (Cursor cursor = databaseHelper.getCourseById(courseId)) {

            assertTrue(cursor.moveToFirst());

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_COURSE_NAME
                    )
            );

            assertEquals("Applied Mathematics", name);
        }

        assertEquals(
                1,
                databaseHelper.deleteCourse(courseId)
        );

        try (Cursor cursor = databaseHelper.getCourseById(courseId)) {
            assertEquals(0, cursor.getCount());
        }
    }

    @Test
    public void canCreateEditCompleteAndDeleteTask() {

        long courseId = databaseHelper.insertCourse(
                "Programming"
        );

        assertTrue(courseId > 0);

        long taskId = databaseHelper.insertTask(
                "  Build Android App  ",
                null,
                courseId,
                "2026-11-15",
                DatabaseHelper.PRIORITY_HIGH
        );

        assertTrue(taskId > 0);

        assertEquals(1, databaseHelper.getTotalTaskCount());
        assertEquals(1, databaseHelper.getPendingTaskCount());
        assertEquals(0, databaseHelper.getCompletedTaskCount());

        try (Cursor cursor = databaseHelper.getTaskById(taskId)) {

            assertTrue(cursor.moveToFirst());

            assertEquals(
                    "Build Android App",
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_TITLE
                            )
                    )
            );

            assertEquals(
                    "",
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_DESCRIPTION
                            )
                    )
            );

            assertEquals(
                    DatabaseHelper.STATUS_PENDING,
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_STATUS
                            )
                    )
            );
        }

        assertEquals(
                1,
                databaseHelper.updateTask(
                        taskId,
                        "Build Android Application",
                        "Implement application features",
                        courseId,
                        "2026-11-20",
                        DatabaseHelper.PRIORITY_MEDIUM
                )
        );

        try (Cursor cursor = databaseHelper.getTaskById(taskId)) {

            assertTrue(cursor.moveToFirst());

            assertEquals(
                    "Build Android Application",
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_TITLE
                            )
                    )
            );
        }

        assertEquals(
                1,
                databaseHelper.updateTaskStatus(
                        taskId,
                        DatabaseHelper.STATUS_COMPLETED
                )
        );

        assertEquals(0, databaseHelper.getPendingTaskCount());
        assertEquals(1, databaseHelper.getCompletedTaskCount());

        assertEquals(
                1,
                databaseHelper.deleteTask(taskId)
        );

        assertEquals(0, databaseHelper.getTotalTaskCount());
    }

    @Test
    public void rejectsInvalidTaskData() {

        long courseId = databaseHelper.insertCourse(
                "Programming"
        );

        assertTrue(courseId > 0);

        assertEquals(
                -1L,
                databaseHelper.insertTask(
                        "",
                        "",
                        courseId,
                        "2026-11-15",
                        DatabaseHelper.PRIORITY_LOW
                )
        );

        assertEquals(
                -1L,
                databaseHelper.insertTask(
                        "Valid Title",
                        "",
                        -1,
                        "2026-11-15",
                        DatabaseHelper.PRIORITY_LOW
                )
        );

        assertEquals(
                -1L,
                databaseHelper.insertTask(
                        "Valid Title",
                        "",
                        courseId,
                        "",
                        DatabaseHelper.PRIORITY_LOW
                )
        );

        assertEquals(
                -1L,
                databaseHelper.insertTask(
                        "Valid Title",
                        "",
                        courseId,
                        "2026-11-15",
                        "Invalid Priority"
                )
        );

        // A task cannot reference a nonexistent course.
        assertEquals(
                -1L,
                databaseHelper.insertTask(
                        "Valid Title",
                        "",
                        999999L,
                        "2026-11-15",
                        DatabaseHelper.PRIORITY_LOW
                )
        );

        assertEquals(0, databaseHelper.getTotalTaskCount());
    }

    @Test
    public void preventsDeletingCourseWithTasks() {

        long courseId = databaseHelper.insertCourse(
                "Programming"
        );

        long taskId = databaseHelper.insertTask(
                "Complete Feature",
                "",
                courseId,
                "2026-11-15",
                DatabaseHelper.PRIORITY_MEDIUM
        );

        assertTrue(courseId > 0);
        assertTrue(taskId > 0);

        assertEquals(
                1,
                databaseHelper.getTaskCountForCourse(courseId)
        );

        // The course must remain while the task exists.
        assertEquals(
                0,
                databaseHelper.deleteCourse(courseId)
        );

        assertEquals(1, databaseHelper.deleteTask(taskId));

        // Deletion is allowed once the course has no tasks.
        assertEquals(
                1,
                databaseHelper.deleteCourse(courseId)
        );
    }

    @Test
    public void searchesTasksByTitleDescriptionAndCourse() {

        long programmingId = databaseHelper.insertCourse(
                "Programming"
        );

        long readingId = databaseHelper.insertCourse(
                "Reading"
        );

        assertTrue(programmingId > 0);
        assertTrue(readingId > 0);

        assertTrue(
                databaseHelper.insertTask(
                        "Build Feature",
                        "Implement sorting algorithm",
                        programmingId,
                        "2026-11-10",
                        DatabaseHelper.PRIORITY_HIGH
                ) > 0
        );

        assertTrue(
                databaseHelper.insertTask(
                        "Read Book",
                        "Notes about history",
                        readingId,
                        "2026-11-12",
                        DatabaseHelper.PRIORITY_LOW
                ) > 0
        );

        assertSearchHasOneTask("Build", "Build Feature");
        assertSearchHasOneTask("sorting", "Build Feature");
        assertSearchHasOneTask("Programming", "Build Feature");
        assertSearchHasOneTask("history", "Read Book");

        try (Cursor cursor = databaseHelper.searchTasks("Not Found")) {
            assertEquals(0, cursor.getCount());
        }
    }

    @Test
    public void sortsTasksByDueDate() {

        long courseId = databaseHelper.insertCourse(
                "Planning"
        );

        assertTrue(courseId > 0);

        assertTrue(databaseHelper.insertTask(
                "Later Task",
                "",
                courseId,
                "2026-12-01",
                DatabaseHelper.PRIORITY_LOW
        ) > 0);

        assertTrue(databaseHelper.insertTask(
                "Earlier Task",
                "",
                courseId,
                "2026-11-01",
                DatabaseHelper.PRIORITY_HIGH
        ) > 0);

        try (Cursor cursor = databaseHelper.getAllTasks()) {

            assertEquals(2, cursor.getCount());
            assertTrue(cursor.moveToFirst());

            int titleIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_TITLE
            );

            assertEquals(
                    "Earlier Task",
                    cursor.getString(titleIndex)
            );

            assertTrue(cursor.moveToNext());

            assertEquals(
                    "Later Task",
                    cursor.getString(titleIndex)
            );
        }
    }

    @Test
    public void rejectsInvalidTaskStatus() {

        long courseId = databaseHelper.insertCourse(
                "Planning"
        );

        long taskId = databaseHelper.insertTask(
                "Plan Week",
                "",
                courseId,
                "2026-11-15",
                DatabaseHelper.PRIORITY_MEDIUM
        );

        assertTrue(taskId > 0);

        assertEquals(
                0,
                databaseHelper.updateTaskStatus(
                        taskId,
                        "Unknown"
                )
        );

        assertEquals(1, databaseHelper.getPendingTaskCount());
        assertEquals(0, databaseHelper.getCompletedTaskCount());
    }

    private void assertSearchHasOneTask(
            String keyword,
            String expectedTitle
    ) {

        try (Cursor cursor = databaseHelper.searchTasks(keyword)) {

            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());

            String actualTitle = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_TITLE
                    )
            );

            assertEquals(expectedTitle, actualTitle);
        }
    }
}
