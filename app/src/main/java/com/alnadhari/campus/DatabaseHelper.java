
package com.alnadhari.campus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database information
    public static final String DATABASE_NAME = "al_nadhari_campus.db";
    public static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_COURSES = "courses";
    public static final String TABLE_TASKS = "tasks";

    // Course columns
    public static final String COL_ID = "id";
    public static final String COL_COURSE_NAME = "name";
    public static final String COL_COURSE_ID = "course_id";

    // Task columns
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DUE_DATE = "due_date";
    public static final String COL_PRIORITY = "priority";
    public static final String COL_STATUS = "status";

    // Column alias for joined queries
    public static final String COL_COURSE_NAME_ALIAS = "course_name";

    // Task status values
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_COMPLETED = "Completed";

    // Priority values
    public static final String PRIORITY_LOW = "Low";
    public static final String PRIORITY_MEDIUM = "Medium";
    public static final String PRIORITY_HIGH = "High";

    // Common query used to display task information
    private static final String TASK_SELECT =
            "SELECT t.id AS _id, t.id, " +
                    "t.title, t.description, t.course_id, " +
                    "c.name AS course_name, " +
                    "t.due_date, t.priority, t.status " +
                    "FROM tasks t " +
                    "INNER JOIN courses c ON t.course_id = c.id ";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Enable foreign key constraints
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Create database tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String createCoursesTable =
                "CREATE TABLE " + TABLE_COURSES + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_COURSE_NAME +
                        " TEXT NOT NULL UNIQUE COLLATE NOCASE" +
                        ")";

        db.execSQL(createCoursesTable);

        String createTasksTable =
                "CREATE TABLE " + TABLE_TASKS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_TITLE + " TEXT NOT NULL, " +
                        COL_DESCRIPTION + " TEXT NOT NULL DEFAULT '', " +
                        COL_COURSE_ID + " INTEGER NOT NULL, " +
                        COL_DUE_DATE + " TEXT NOT NULL, " +
                        COL_PRIORITY + " TEXT NOT NULL, " +
                        COL_STATUS + " TEXT NOT NULL DEFAULT 'Pending', " +
                        "FOREIGN KEY (" + COL_COURSE_ID + ") " +
                        "REFERENCES " + TABLE_COURSES + "(" + COL_ID + ") " +
                        "ON DELETE RESTRICT" +
                        ")";

        db.execSQL(createTasksTable);

        // Improve searching and date sorting
        db.execSQL(
                "CREATE INDEX idx_tasks_due_date " +
                        "ON tasks(due_date)"
        );

        db.execSQL(
                "CREATE INDEX idx_tasks_course " +
                        "ON tasks(course_id)"
        );

        // Create one default course
        ContentValues defaultCourse = new ContentValues();
        defaultCourse.put(COL_COURSE_NAME, "General");

        db.insert(TABLE_COURSES, null, defaultCourse);
    }

    // Database upgrade
    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        // Database version 1.
        // Future schema changes will use migrations
        // to preserve existing user data.
    }

    // =====================================
    // COURSE OPERATIONS
    // =====================================

    // Insert a new course
    public long insertCourse(String courseName) {

        if (courseName == null ||
                courseName.trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_COURSE_NAME, courseName.trim());

        return db.insertWithOnConflict(
                TABLE_COURSES,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    // Get all courses
    public Cursor getAllCourses() {

        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT id AS _id, id, name " +
                        "FROM courses " +
                        "ORDER BY name COLLATE NOCASE ASC",
                null
        );
    }

    // Get a specific course
    public Cursor getCourseById(long courseId) {

        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT id AS _id, id, name " +
                        "FROM courses WHERE id = ?",
                new String[]{
                        String.valueOf(courseId)
                }
        );
    }

    // Update course name
    public int updateCourse(
            long courseId,
            String courseName
    ) {

        if (courseName == null ||
                courseName.trim().isEmpty()) {
            return 0;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_COURSE_NAME, courseName.trim());

        try {
            return db.update(
                    TABLE_COURSES,
                    values,
                    COL_ID + " = ?",
                    new String[]{
                            String.valueOf(courseId)
                    }
            );
        } catch (SQLiteConstraintException e) {
            return 0;
        }
    }

    // Count tasks belonging to a course
    public int getTaskCountForCourse(long courseId) {

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM tasks " +
                        "WHERE course_id = ?",
                new String[]{
                        String.valueOf(courseId)
                }
        )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    // Delete a course if it has no tasks
    public int deleteCourse(long courseId) {

        if (getTaskCountForCourse(courseId) > 0) {
            return 0;
        }

        SQLiteDatabase db = getWritableDatabase();

        try {
            return db.delete(
                    TABLE_COURSES,
                    COL_ID + " = ?",
                    new String[]{
                            String.valueOf(courseId)
                    }
            );
        } catch (SQLiteConstraintException e) {
            return 0;
        }
    }

    // =====================================
    // TASK OPERATIONS
    // =====================================

    // Build task values for insert and update
    private ContentValues createTaskValues(
            String title,
            String description,
            long courseId,
            String dueDate,
            String priority
    ) {

        ContentValues values = new ContentValues();

        values.put(COL_TITLE, title.trim());

        values.put(
                COL_DESCRIPTION,
                description == null ? "" : description.trim()
        );

        values.put(COL_COURSE_ID, courseId);
        values.put(COL_DUE_DATE, dueDate.trim());
        values.put(COL_PRIORITY, priority);

        return values;
    }

    // Validate basic task data
    private boolean isTaskDataValid(
            String title,
            long courseId,
            String dueDate,
            String priority
    ) {

        if (title == null || title.trim().isEmpty()) {
            return false;
        }

        if (courseId <= 0) {
            return false;
        }

        if (dueDate == null || dueDate.trim().isEmpty()) {
            return false;
        }

        return PRIORITY_LOW.equals(priority) ||
                PRIORITY_MEDIUM.equals(priority) ||
                PRIORITY_HIGH.equals(priority);
    }

    // INSERT: Add a new task
    public long insertTask(
            String title,
            String description,
            long courseId,
            String dueDate,
            String priority
    ) {

        if (!isTaskDataValid(
                title, courseId, dueDate, priority
        )) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = createTaskValues(
                title,
                description,
                courseId,
                dueDate,
                priority
        );

        values.put(COL_STATUS, STATUS_PENDING);

        try {
            return db.insert(
                    TABLE_TASKS,
                    null,
                    values
            );
        } catch (SQLiteConstraintException e) {
            return -1;
        }
    }

    // SELECT ALL: Retrieve all tasks
    public Cursor getAllTasks() {

        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                TASK_SELECT +
                        "ORDER BY t.due_date ASC, t.id DESC",
                null
        );
    }

    // SELECT: Retrieve a specific task
    public Cursor getTaskById(long taskId) {

        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                TASK_SELECT +
                        "WHERE t.id = ?",
                new String[]{
                        String.valueOf(taskId)
                }
        );
    }

    // SEARCH: Search tasks by title, description or course
    public Cursor searchTasks(String keyword) {

        SQLiteDatabase db = getReadableDatabase();

        if (keyword == null ||
                keyword.trim().isEmpty()) {
            return getAllTasks();
        }

        String searchValue = "%" + keyword.trim() + "%";

        return db.rawQuery(
                TASK_SELECT +
                        "WHERE t.title LIKE ? " +
                        "OR t.description LIKE ? " +
                        "OR c.name LIKE ? " +
                        "ORDER BY t.due_date ASC, t.id DESC",
                new String[]{
                        searchValue,
                        searchValue,
                        searchValue
                }
        );
    }

    // UPDATE: Edit an existing task
    public int updateTask(
            long taskId,
            String title,
            String description,
            long courseId,
            String dueDate,
            String priority
    ) {

        if (!isTaskDataValid(
                title, courseId, dueDate, priority
        )) {
            return 0;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = createTaskValues(
                title,
                description,
                courseId,
                dueDate,
                priority
        );

        try {
            return db.update(
                    TABLE_TASKS,
                    values,
                    COL_ID + " = ?",
                    new String[]{
                            String.valueOf(taskId)
                    }
            );
        } catch (SQLiteConstraintException e) {
            return 0;
        }
    }

    // Update task completion status
    public int updateTaskStatus(
            long taskId,
            String status
    ) {

        if (!STATUS_PENDING.equals(status) &&
                !STATUS_COMPLETED.equals(status)) {
            return 0;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);

        return db.update(
                TABLE_TASKS,
                values,
                COL_ID + " = ?",
                new String[]{
                        String.valueOf(taskId)
                }
        );
    }

    // DELETE: Delete a task
    public int deleteTask(long taskId) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_TASKS,
                COL_ID + " = ?",
                new String[]{
                        String.valueOf(taskId)
                }
        );
    }

    // =====================================
    // DASHBOARD STATISTICS
    // =====================================

    // Total tasks
    public int getTotalTaskCount() {

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM tasks",
                null
        )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    // Count tasks by status
    private int getTaskCountByStatus(String status) {

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM tasks " +
                        "WHERE status = ?",
                new String[]{status}
        )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    // Pending tasks
    public int getPendingTaskCount() {

        return getTaskCountByStatus(STATUS_PENDING);
    }

    // Completed tasks
    public int getCompletedTaskCount() {

        return getTaskCountByStatus(STATUS_COMPLETED);
    }
}
