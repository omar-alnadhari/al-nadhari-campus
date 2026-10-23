
package com.alnadhari.campus;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Dashboard statistics
    private TextView tvTotalTasks;
    private TextView tvPendingTasks;
    private TextView tvCompletedTasks;

    // Dashboard navigation buttons
    private Button btnAddTask;
    private Button btnAllTasks;
    private Button btnSearchTasks;
    private Button btnCourses;

    // SQLite database
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handle Android system bars
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

        // Connect Java variables to XML elements
        initializeViews();

        // Configure navigation buttons
        setupButtonListeners();
    }

    // Initialize dashboard components
    private void initializeViews() {

        tvTotalTasks = findViewById(
                R.id.tvTotalTasks
        );

        tvPendingTasks = findViewById(
                R.id.tvPendingTasks
        );

        tvCompletedTasks = findViewById(
                R.id.tvCompletedTasks
        );

        btnAddTask = findViewById(
                R.id.btnAddTask
        );

        btnAllTasks = findViewById(
                R.id.btnAllTasks
        );

        btnSearchTasks = findViewById(
                R.id.btnSearchTasks
        );

        btnCourses = findViewById(
                R.id.btnCourses
        );
    }

    // Configure navigation using Intent
    private void setupButtonListeners() {

        // Open Add New Task screen
        btnAddTask.setOnClickListener(view -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    AddEditTaskActivity.class
            );

            startActivity(intent);
        });

        // Open All Tasks screen
        btnAllTasks.setOnClickListener(view -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    TaskListActivity.class
            );

            startActivity(intent);
        });

        // Open Search Tasks screen
        btnSearchTasks.setOnClickListener(view -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    SearchTasksActivity.class
            );

            startActivity(intent);
        });

        // Open Manage Courses screen
        btnCourses.setOnClickListener(view -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    CoursesActivity.class
            );

            startActivity(intent);
        });
    }

    // Load dashboard statistics from SQLite
    private void loadDashboardStatistics() {

        try {

            int totalTasks =
                    databaseHelper.getTotalTaskCount();

            int pendingTasks =
                    databaseHelper.getPendingTaskCount();

            int completedTasks =
                    databaseHelper.getCompletedTaskCount();

            tvTotalTasks.setText(
                    String.valueOf(totalTasks)
            );

            tvPendingTasks.setText(
                    String.valueOf(pendingTasks)
            );

            tvCompletedTasks.setText(
                    String.valueOf(completedTasks)
            );

        } catch (Exception exception) {

            Log.e(
                    TAG,
                    "Failed to load dashboard statistics",
                    exception
            );

            showMessage(
                    "Unable to load dashboard statistics"
            );
        }
    }

    // Refresh statistics when Dashboard becomes active
    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null) {
            loadDashboardStatistics();
        }
    }

    // Display Toast feedback
    private void showMessage(String message) {

        Toast.makeText(
                MainActivity.this,
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
