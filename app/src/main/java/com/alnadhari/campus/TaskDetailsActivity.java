
package com.alnadhari.campus;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TaskDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";

    private static final String TAG = "TaskDetailsActivity";

    // Task information
    private TextView tvTaskTitle;
    private TextView tvTaskCourse;
    private TextView tvTaskDueDate;
    private TextView tvTaskPriority;
    private TextView tvTaskStatus;
    private TextView tvTaskDescription;

    // Action buttons
    private Button btnBack;
    private Button btnEditTask;
    private Button btnToggleStatus;
    private Button btnDeleteTask;

    // Database and current task
    private DatabaseHelper databaseHelper;

    private long taskId = -1;

    private String currentStatus =
            DatabaseHelper.STATUS_PENDING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_details);

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

        // Receive task ID from Intent
        taskId = getIntent().getLongExtra(
                EXTRA_TASK_ID,
                -1
        );

        // Validate task ID
        if (taskId <= 0) {

            showMessage("Invalid task selected");
            finish();
            return;
        }

        // Initialize SQLite
        databaseHelper = new DatabaseHelper(this);

        // Connect XML elements
        initializeViews();

        // Configure actions
        setupButtonListeners();

        // Register confirmation result listener
        setupDeleteConfirmation();
    }

    // Connect Java variables to XML views
    private void initializeViews() {

        tvTaskTitle = findViewById(R.id.tvTaskTitle);
        tvTaskCourse = findViewById(R.id.tvTaskCourse);
        tvTaskDueDate = findViewById(R.id.tvTaskDueDate);

        tvTaskPriority = findViewById(
                R.id.tvTaskPriority
        );

        tvTaskStatus = findViewById(
                R.id.tvTaskStatus
        );

        tvTaskDescription = findViewById(
                R.id.tvTaskDescription
        );

        btnBack = findViewById(R.id.btnBack);
        btnEditTask = findViewById(R.id.btnEditTask);

        btnToggleStatus = findViewById(
                R.id.btnToggleStatus
        );

        btnDeleteTask = findViewById(
                R.id.btnDeleteTask
        );
    }

    // Configure button listeners
    private void setupButtonListeners() {

        // Return to task list
        btnBack.setOnClickListener(view -> {
            finish();
        });

        // Edit the selected task
        btnEditTask.setOnClickListener(view -> {

            Intent intent = new Intent(
                    TaskDetailsActivity.this,
                    AddEditTaskActivity.class
            );

            intent.putExtra(
                    AddEditTaskActivity.EXTRA_TASK_ID,
                    taskId
            );

            startActivity(intent);
        });

        // Change task completion status
        btnToggleStatus.setOnClickListener(view -> {
            toggleTaskStatus();
        });

        // Show confirmation dialog before deletion
        btnDeleteTask.setOnClickListener(view -> {
            showDeleteDialog();
        });
    }

    // Load task details from SQLite
    private void loadTaskDetails() {

        try (Cursor cursor =
                     databaseHelper.getTaskById(taskId)) {

            if (!cursor.moveToFirst()) {

                showMessage("Task not found");
                finish();
                return;
            }

            String title = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_TITLE
                    )
            );

            String description = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_DESCRIPTION
                    )
            );

            String course = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_COURSE_NAME_ALIAS
                    )
            );

            String dueDate = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_DUE_DATE
                    )
            );

            String priority = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_PRIORITY
                    )
            );

            currentStatus = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_STATUS
                    )
            );

            // Display task information
            tvTaskTitle.setText(title);
            tvTaskCourse.setText(course);
            tvTaskDueDate.setText(dueDate);
            tvTaskPriority.setText(priority);
            tvTaskStatus.setText(currentStatus);

            if (description == null ||
                    description.trim().isEmpty()) {

                tvTaskDescription.setText(
                        "No description provided."
                );

            } else {

                tvTaskDescription.setText(description);
            }

            // Update colors and buttons
            updatePriorityAppearance(priority);
            updateStatusAppearance();

        } catch (Exception exception) {

            Log.e(
                    TAG,
                    "Failed to load task details",
                    exception
            );

            showMessage("Unable to load task details");
            finish();
        }
    }

    // Display priority with a suitable color
    private void updatePriorityAppearance(
            String priority
    ) {

        int priorityColor;

        if (DatabaseHelper.PRIORITY_HIGH.equals(
                priority)) {

            priorityColor = Color.parseColor(
                    "#DC2626"
            );

        } else if (
                DatabaseHelper.PRIORITY_MEDIUM.equals(priority)
        ) {

            priorityColor = Color.parseColor(
                    "#D97706"
            );

        } else {

            priorityColor = Color.parseColor(
                    "#16A34A"
            );
        }

        tvTaskPriority.setTextColor(priorityColor);
    }

    // Update status label and action button
    private void updateStatusAppearance() {

        if (DatabaseHelper.STATUS_COMPLETED.equals(
                currentStatus)) {

            tvTaskStatus.setTextColor(
                    Color.parseColor("#16A34A")
            );

            btnToggleStatus.setText(
                    "Mark as Pending"
            );

            btnToggleStatus.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.parseColor("#D97706")
                    )
            );

        } else {

            tvTaskStatus.setTextColor(
                    Color.parseColor("#D97706")
            );

            btnToggleStatus.setText(
                    "Mark as Completed"
            );

            btnToggleStatus.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.parseColor("#16A34A")
                    )
            );
        }
    }

    // Change task status in SQLite
    private void toggleTaskStatus() {

        String newStatus;

        if (DatabaseHelper.STATUS_COMPLETED.equals(
                currentStatus)) {

            newStatus = DatabaseHelper.STATUS_PENDING;

        } else {

            newStatus = DatabaseHelper.STATUS_COMPLETED;
        }

        try {

            int updatedRows =
                    databaseHelper.updateTaskStatus(
                            taskId,
                            newStatus
                    );

            if (updatedRows > 0) {

                currentStatus = newStatus;

                updateStatusAppearance();
                tvTaskStatus.setText(currentStatus);

                setResult(RESULT_OK);

                showMessage(
                        "Task status updated successfully"
                );

            } else {

                showMessage(
                        "Unable to update task status"
                );
            }

        } catch (Exception exception) {

            Log.e(
                    TAG,
                    "Failed to update task status",
                    exception
            );

            showMessage("Database operation failed");
        }
    }

    // Open DialogFragment
    private void showDeleteDialog() {

        if (getSupportFragmentManager()
                .findFragmentByTag(
                        DeleteConfirmDialog.TAG
                ) != null) {

            return;
        }

        DeleteConfirmDialog dialog =
                DeleteConfirmDialog.newInstance(taskId);

        dialog.show(
                getSupportFragmentManager(),
                DeleteConfirmDialog.TAG
        );
    }

    // Receive confirmation from DialogFragment
    private void setupDeleteConfirmation() {

        getSupportFragmentManager()
                .setFragmentResultListener(
                        DeleteConfirmDialog.REQUEST_KEY,
                        this,
                        (requestKey, result) -> {

                            long confirmedTaskId =
                                    result.getLong(
                                            DeleteConfirmDialog
                                                    .RESULT_TASK_ID,
                                            -1
                                    );

                            if (confirmedTaskId != taskId) {
                                return;
                            }

                            deleteConfirmedTask();
                        }
                );
    }

    // Delete the task from SQLite
    private void deleteConfirmedTask() {

        try {

            int deletedRows =
                    databaseHelper.deleteTask(taskId);

            if (deletedRows > 0) {

                showMessage(
                        "Task deleted successfully"
                );

                setResult(RESULT_OK);
                finish();

            } else {

                showMessage(
                        "Unable to delete task"
                );
            }

        } catch (Exception exception) {

            Log.e(
                    TAG,
                    "Failed to delete task",
                    exception
            );

            showMessage("Database operation failed");
        }
    }

    // Refresh details after returning from Edit Task
    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && taskId > 0) {
            loadTaskDetails();
        }
    }

    // Display Toast feedback
    private void showMessage(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    // Close database helper
    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}


