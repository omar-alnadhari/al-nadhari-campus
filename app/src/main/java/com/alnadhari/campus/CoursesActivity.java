
package com.alnadhari.campus;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CoursesActivity extends AppCompatActivity {

    private static final String TAG = "CoursesActivity";

    // Screen components
    private EditText etCourseName;
    private Button btnAddCourse;
    private Button btnBack;

    private TextView tvCourseCount;
    private View layoutEmptyState;
    private RecyclerView recyclerViewCourses;

    // Database and RecyclerView adapter
    private DatabaseHelper databaseHelper;
    private CourseAdapter courseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_courses);

        // Handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (view, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Initialize SQLite database
        databaseHelper = new DatabaseHelper(this);

        // Connect Java variables to XML components
        initializeViews();

        // Configure RecyclerView
        setupRecyclerView();

        // Configure screen buttons
        setupButtonListeners();
    }

    // Connect Java variables with XML views
    private void initializeViews() {

        etCourseName = findViewById(
                R.id.etCourseName
        );

        btnAddCourse = findViewById(
                R.id.btnAddCourse
        );

        btnBack = findViewById(
                R.id.btnBack
        );

        tvCourseCount = findViewById(
                R.id.tvCourseCount
        );

        layoutEmptyState = findViewById(
                R.id.layoutEmptyState
        );

        recyclerViewCourses = findViewById(
                R.id.recyclerViewCourses
        );
    }

    // Set up RecyclerView and CourseAdapter
    private void setupRecyclerView() {

        recyclerViewCourses.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerViewCourses.setHasFixedSize(false);

        courseAdapter = new CourseAdapter(
                new ArrayList<>(),
                new CourseAdapter.OnCourseActionListener() {

                    @Override
                    public void onEditCourse(
                            long courseId,
                            String courseName
                    ) {

                        showEditCourseDialog(
                                courseId,
                                courseName
                        );
                    }

                    @Override
                    public void onDeleteCourse(
                            long courseId,
                            String courseName,
                            int taskCount
                    ) {

                        confirmDeleteCourse(
                                courseId,
                                courseName
                        );
                    }
                }
        );

        recyclerViewCourses.setAdapter(courseAdapter);
    }

    // Configure button actions
    private void setupButtonListeners() {

        // Return to Dashboard
        btnBack.setOnClickListener(view -> {
            finish();
        });

        // Add a new course
        btnAddCourse.setOnClickListener(view -> {
            addNewCourse();
        });

        // Add course using keyboard Done action
        etCourseName.setOnEditorActionListener(
                (textView, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_DONE) {

                        addNewCourse();
                        return true;
                    }

                    return false;
                }
        );
    }

    // ======================================
    // INSERT COURSE
    // ======================================

    private void addNewCourse() {

        String courseName = etCourseName
                .getText()
                .toString()
                .trim();

        // Validate course name
        if (courseName.isEmpty()) {

            etCourseName.setError(
                    "Course name is required"
            );

            etCourseName.requestFocus();
            return;
        }

        try {

            long result = databaseHelper.insertCourse(
                    courseName
            );

            if (result > 0) {

                showMessage(
                        "Course added successfully"
                );

                // Clear input field
                etCourseName.setText("");

                // Refresh course list
                loadCourses();

            } else {

                etCourseName.setError(
                        "Course name already exists"
                );

                showMessage(
                        "Unable to add course"
                );
            }

        } catch (Exception exception) {

            Log.e(
                    TAG,
                    "Failed to insert course",
                    exception
            );

            showMessage(
                    "Database operation failed"
            );
        }
    }

    // ======================================
    // SELECT ALL COURSES
    // ======================================

    private void loadCourses() {

        ArrayList<CourseAdapter.CourseItem> courses =
                new ArrayList<>();

        try (Cursor cursor =
                     databaseHelper.getAllCourses()) {

            int idIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_ID
            );

            int nameIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_COURSE_NAME
            );

            // Read all courses
            while (cursor.moveToNext()) {

                long courseId = cursor.getLong(idIndex);

                String courseName = cursor.getString(
                        nameIndex
                );

                // Count tasks for this course
                int taskCount =
                        databaseHelper.getTaskCountForCourse(
                                courseId
                        );

                // Create course item
                CourseAdapter.CourseItem course =
                        new CourseAdapter.CourseItem(
                                courseId,
                                courseName,
                                taskCount
                        );

                courses.add(course);
            }

        } catch (Exception exception) {

            Log.e(
                    TAG,
                    "Failed to load courses",
                    exception
            );

            showMessage(
                    "Unable to load courses"
            );

            return;
        }

        // Update RecyclerView
        courseAdapter.updateCourses(courses);

        // Update course counter
        int count = courses.size();

        if (count == 1) {

            tvCourseCount.setText("1 Course");

        } else {

            tvCourseCount.setText(
                    count + " Courses"
            );
        }

        // Show or hide the empty state
        if (courses.isEmpty()) {

            layoutEmptyState.setVisibility(
                    View.VISIBLE
            );

            recyclerViewCourses.setVisibility(
                    View.GONE
            );

        } else {

            layoutEmptyState.setVisibility(
                    View.GONE
            );

            recyclerViewCourses.setVisibility(
                    View.VISIBLE
            );
        }
    }

    // ======================================
    // UPDATE COURSE
    // ======================================

    private void showEditCourseDialog(
            long courseId,
            String currentName
    ) {

        // Create text input
        EditText input = new EditText(this);

        input.setSingleLine(true);

        input.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS
        );

        input.setFilters(
                new InputFilter[]{
                        new InputFilter.LengthFilter(100)
                }
        );

        input.setText(currentName);
        input.selectAll();

        // Add padding around input
        LinearLayout container = new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        int horizontalPadding = dpToPx(20);
        int verticalPadding = dpToPx(8);

        container.setPadding(
                horizontalPadding,
                verticalPadding,
                horizontalPadding,
                verticalPadding
        );

        container.addView(input);

        // Create editing dialog
        AlertDialog dialog =
                new AlertDialog.Builder(this)

                        .setTitle("Edit Course")

                        .setMessage(
                                "Enter the new course name."
                        )

                        .setView(container)

                        .setNegativeButton(
                                "Cancel",
                                (dialogInterface, which) -> {
                                    // Close without saving
                                }
                        )

                        .setPositiveButton(
                                "Save",
                                null
                        )

                        .create();

        dialog.setOnShowListener(ignored -> {

            Button saveButton = dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            );

            saveButton.setOnClickListener(view -> {

                String newName = input
                        .getText()
                        .toString()
                        .trim();

                // Validate updated course name
                if (newName.isEmpty()) {

                    input.setError(
                            "Course name is required"
                    );

                    return;
                }

                // No changes needed
                if (newName.equals(currentName)) {

                    dialog.dismiss();
                    return;
                }

                try {

                    int updatedRows =
                            databaseHelper.updateCourse(
                                    courseId,
                                    newName
                            );

                    if (updatedRows > 0) {

                        showMessage(
                                "Course updated successfully"
                        );

                        dialog.dismiss();

                        // Refresh course list
                        loadCourses();

                    } else {

                        input.setError(
                                "Name already exists or update failed"
                        );
                    }

                } catch (Exception exception) {

                    Log.e(
                            TAG,
                            "Failed to update course",
                            exception
                    );

                    showMessage(
                            "Database operation failed"
                    );
                }
            });
        });

        dialog.show();
    }

    // ======================================
    // DELETE COURSE CONFIRMATION
    // ======================================

    private void confirmDeleteCourse(
            long courseId,
            String courseName
    ) {

        try {

            // Check current task count
            int taskCount =
                    databaseHelper.getTaskCountForCourse(
                            courseId
                    );

            // Prevent deletion of courses with tasks
            if (taskCount > 0) {

                new AlertDialog.Builder(this)

                        .setTitle(
                                "Cannot Delete Course"
                        )

                        .setMessage(
                                "This course has " +
                                        taskCount +
                                        " assignment(s).\n\n" +
                                        "Please delete or move " +
                                        "the assignments before " +
                                        "deleting this course."
                        )

                        .setPositiveButton(
                                "OK",
                                null
                        )

                        .show();

                return;
            }

            // Ask user to confirm deletion
            new AlertDialog.Builder(this)

                    .setTitle("Delete Course")

                    .setMessage(
                            "Are you sure you want to delete \"" +
                                    courseName +
                                    "\"?\n\n" +
                                    "This action cannot be undone."
                    )

                    .setNegativeButton(
                            "Cancel",
                            null
                    )

                    .setPositiveButton(
                            "Delete",
                            (dialog, which) -> {

                                deleteCourse(courseId);
                            }
                    )

                    .show();

        } catch (Exception exception) {

            Log.e(
                    TAG,
                    "Failed to verify course deletion",
                    exception
            );

            showMessage(
                    "Unable to delete course"
            );
        }
    }

    // ======================================
    // DELETE COURSE
    // ======================================

    private void deleteCourse(long courseId) {

        try {

            int deletedRows =
                    databaseHelper.deleteCourse(courseId);

            if (deletedRows > 0) {

                showMessage(
                        "Course deleted successfully"
                );

                // Refresh list after deletion
                loadCourses();

            } else {

                showMessage(
                        "Cannot delete this course. " +
                                "It may contain assignments."
                );

                // Refresh the current information
                loadCourses();
            }

        } catch (Exception exception) {

            Log.e(
                    TAG,
                    "Failed to delete course",
                    exception
            );

            showMessage(
                    "Database operation failed"
            );
        }
    }

    // Convert dp to pixels
    private int dpToPx(int dp) {

        float density = getResources()
                .getDisplayMetrics()
                .density;

        return Math.round(dp * density);
    }

    // Display Toast feedback
    private void showMessage(String message) {

        Toast.makeText(
                CoursesActivity.this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    // Refresh courses when returning
    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null &&
                courseAdapter != null) {

            loadCourses();
        }
    }

    // Close database connection
    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
