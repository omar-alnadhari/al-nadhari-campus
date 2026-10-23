
package com.alnadhari.campus;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TaskModelsTest {

    @Test
    public void taskItemStoresAllProperties() {

        TaskAdapter.TaskItem task = new TaskAdapter.TaskItem(
                101L,
                "Build Android App",
                "Programming",
                "2026-11-15",
                DatabaseHelper.PRIORITY_HIGH,
                DatabaseHelper.STATUS_PENDING
        );

        assertEquals(101L, task.id);
        assertEquals("Build Android App", task.title);
        assertEquals("Programming", task.courseName);
        assertEquals("2026-11-15", task.dueDate);
        assertEquals(DatabaseHelper.PRIORITY_HIGH, task.priority);
        assertEquals(DatabaseHelper.STATUS_PENDING, task.status);
    }

    @Test
    public void courseItemStoresAllProperties() {

        CourseAdapter.CourseItem course =
                new CourseAdapter.CourseItem(
                        15L,
                        "Programming",
                        3
                );

        assertEquals(15L, course.id);
        assertEquals("Programming", course.name);
        assertEquals(3, course.taskCount);
    }

    @Test
    public void taskItemsHaveIndependentValues() {

        TaskAdapter.TaskItem first = new TaskAdapter.TaskItem(
                1L,
                "Read Documentation",
                "Programming",
                "2026-11-10",
                DatabaseHelper.PRIORITY_LOW,
                DatabaseHelper.STATUS_PENDING
        );

        TaskAdapter.TaskItem second = new TaskAdapter.TaskItem(
                2L,
                "Build Feature",
                "Programming",
                "2026-11-12",
                DatabaseHelper.PRIORITY_HIGH,
                DatabaseHelper.STATUS_COMPLETED
        );

        assertNotEquals(first.id, second.id);
        assertNotEquals(first.title, second.title);
        assertNotEquals(first.status, second.status);
    }
}
