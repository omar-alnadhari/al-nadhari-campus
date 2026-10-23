
package com.alnadhari.campus;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TaskListActivity extends AppCompatActivity {

    private static final String TAG = "TaskListActivity";

    // Screen components
    private RecyclerView recyclerViewTasks;
    private TextView tvTaskCount;
    private View layoutEmptyState;
    private Button btnBack;
    private Button btnAddTask;

    // Database and adapter
    private DatabaseHelper databaseHelper;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_list);

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

        // Initialize SQLite
        databaseHelper = new DatabaseHelper(this);

        // Initialize screen elements
        initializeViews();

        // Configure RecyclerView
        setupRecyclerView();

        // Configure navigation buttons
        setupButtonListeners();
    }

    // Connect XML elements
    private void initializeViews() {

        recyclerViewTasks = findViewById(
                R.id.recyclerViewTasks
        );

        tvTaskCount = findViewById(
                R.id.tvTaskCount
        );

        layoutEmptyState = findViewById(
                R.id.layoutEmptyState
        );

        btnBack = findViewById(
                R.id.btnBack
        );

        btnAddTask = findViewById(
                R.id.btnAddTask
        );
    }

    // Configure RecyclerView with Adapter
    private void setupRecyclerView() {

        recyclerViewTasks.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerViewTasks.setHasFixedSize(false);

        taskAdapter = new TaskAdapter(
                new ArrayList<>(),
                taskId -> openTaskDetails(taskId)
        );

        recyclerViewTasks.setAdapter(taskAdapter);
    }

    // Configure navigation
    private void setupButtonListeners() {

        // Back to Dashboard
        btnBack.setOnClickListener(view -> {
            finish();
        });

        // Open Add New Task screen
        btnAddTask.setOnClickListener(view -> {

            Intent intent = new Intent(
                    TaskListActivity.this,
                    AddEditTaskActivity.class
            );

            startActivity(intent);
        });
    }

    // Load all tasks from SQLite
    private void loadTasks() {

        ArrayList<TaskAdapter.TaskItem> tasks =
                new ArrayList<>();

        try (Cursor cursor = databaseHelper.getAllTasks()) {

            int idIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_ID
            );

            int titleIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_TITLE
            );

            int courseIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_COURSE_NAME_ALIAS
            );

            int dateIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_DUE_DATE
            );

            int priorityIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_PRIORITY
            );

            int statusIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_STATUS
            );

            // Read database records
            while (cursor.moveToNext()) {

                long id = cursor.getLong(idIndex);

                String title = cursor.getString(
                        titleIndex
                );

                String courseName = cursor.getString(
                        courseIndex
                );

                String dueDate = cursor.getString(
                        dateIndex
                );

                String priority = cursor.getString(
                        priorityIndex
                );

                String status = cursor.getString(
                        statusIndex
                );

                // Create task item
                TaskAdapter.TaskItem task =
                        new TaskAdapter.TaskItem(
                                id,
                                title,
                                courseName,
                                dueDate,
                                priority,
                                status
                        );

                tasks.add(task);
            }

        } catch (Exception exception) {

            Log.e(
                    TAG,
                    "Failed to load tasks",
                    exception
            );

            Toast.makeText(
                    this,
                    "Unable to load tasks",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Update RecyclerView
        taskAdapter.updateTasks(tasks);

        // Update task counter
        int count = tasks.size();

        if (count == 1) {

            tvTaskCount.setText("1 Task");

        } else {

            tvTaskCount.setText(
                    count + " Tasks"
            );
        }

        // Handle empty task list
        if (tasks.isEmpty()) {

            layoutEmptyState.setVisibility(
                    View.VISIBLE
            );

            recyclerViewTasks.setVisibility(
                    View.GONE
            );

        } else {

            layoutEmptyState.setVisibility(
                    View.GONE
            );

            recyclerViewTasks.setVisibility(
                    View.VISIBLE
            );
        }
    }

    // Open Task Details and pass task ID
    private void openTaskDetails(long taskId) {

        Intent intent = new Intent(
                TaskListActivity.this,
                TaskDetailsActivity.class
        );

        intent.putExtra(
                TaskDetailsActivity.EXTRA_TASK_ID,
                taskId
        );

        startActivity(intent);
    }

    // Refresh task list after returning
    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null &&
                taskAdapter != null) {

            loadTasks();
        }
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
