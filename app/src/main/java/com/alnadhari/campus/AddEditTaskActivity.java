
package com.alnadhari.campus;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";

    private EditText etTaskTitle;
    private EditText etTaskDescription;
    private Spinner spinnerCourses;
    private TextView tvDueDate;
    private TextView tvFormTitle;
    private TextView tvFormSubtitle;
    private RadioGroup rgPriority;

    private Button btnSaveTask;
    private Button btnCancelTask;

    private DatabaseHelper databaseHelper;

    private final ArrayList<Long> courseIds = new ArrayList<>();
    private final ArrayList<String> courseNames = new ArrayList<>();

    private String selectedDueDate = "";
    private long editingTaskId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_task);

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

        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        loadCourses();
        setupListeners();

        editingTaskId = getIntent().getLongExtra(
                EXTRA_TASK_ID, -1
        );

        if (editingTaskId > 0) {
            loadTaskForEditing();
        }
    }

    // Initialize all XML components
    private void initializeViews() {

        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);

        spinnerCourses = findViewById(R.id.spinnerCourses);
        tvDueDate = findViewById(R.id.tvDueDate);

        tvFormTitle = findViewById(R.id.tvFormTitle);
        tvFormSubtitle = findViewById(R.id.tvFormSubtitle);

        rgPriority = findViewById(R.id.rgPriority);

        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnCancelTask = findViewById(R.id.btnCancelTask);
    }

    // Load courses from SQLite database
    private void loadCourses() {

        courseIds.clear();
        courseNames.clear();

        try (Cursor cursor = databaseHelper.getAllCourses()) {

            int idIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_ID
            );

            int nameIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_COURSE_NAME
            );

            while (cursor.moveToNext()) {

                long courseId = cursor.getLong(idIndex);
                String courseName = cursor.getString(nameIndex);

                courseIds.add(courseId);
                courseNames.add(courseName);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                courseNames
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerCourses.setAdapter(adapter);

        if (courseIds.isEmpty()) {
            showMessage("Please create a course first");
            btnSaveTask.setEnabled(false);
        } else {
            btnSaveTask.setEnabled(true);
        }
    }

    // Configure buttons and date selection
    private void setupListeners() {

        tvDueDate.setOnClickListener(
                view -> showDatePicker()
        );

        btnSaveTask.setOnClickListener(
                view -> saveTask()
        );

        btnCancelTask.setOnClickListener(
                view -> finish()
        );
    }

    // Display calendar for due date
    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        if (!selectedDueDate.isEmpty()) {

            try {
                String[] dateParts = selectedDueDate.split("-");

                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]) - 1;
                int day = Integer.parseInt(dateParts[2]);

                calendar.set(year, month, day);

            } catch (Exception ignored) {
                calendar = Calendar.getInstance();
            }
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    selectedDueDate = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );

                    tvDueDate.setText(selectedDueDate);
                    tvDueDate.setTextColor(
                            Color.parseColor("#1E293B")
                    );
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    // Get selected task priority
    private String getSelectedPriority() {

        int selectedId = rgPriority.getCheckedRadioButtonId();

        if (selectedId == R.id.rbLow) {
            return DatabaseHelper.PRIORITY_LOW;
        }

        if (selectedId == R.id.rbHigh) {
            return DatabaseHelper.PRIORITY_HIGH;
        }

        return DatabaseHelper.PRIORITY_MEDIUM;
    }

    // Save new task or update existing task
    private void saveTask() {

        String title = etTaskTitle.getText()
                .toString().trim();

        String description = etTaskDescription.getText()
                .toString().trim();

        // Validate task title
        if (title.isEmpty()) {

            etTaskTitle.setError("Task title is required");
            etTaskTitle.requestFocus();
            return;
        }

        // Validate selected course
        int selectedPosition =
                spinnerCourses.getSelectedItemPosition();

        if (selectedPosition < 0 ||
                selectedPosition >= courseIds.size()) {

            showMessage("Please select a course");
            return;
        }

        long courseId = courseIds.get(selectedPosition);

        // Validate due date
        if (selectedDueDate.isEmpty()) {

            showMessage("Please select a due date");
            return;
        }

        String priority = getSelectedPriority();

        // Prevent duplicate save taps
        btnSaveTask.setEnabled(false);

        try {

            if (editingTaskId > 0) {

                int result = databaseHelper.updateTask(
                        editingTaskId,
                        title,
                        description,
                        courseId,
                        selectedDueDate,
                        priority
                );

                if (result > 0) {

                    showMessage("Task updated successfully");
                    setResult(RESULT_OK);
                    finish();
                    return;

                } else {

                    showMessage("Unable to update task");
                }

            } else {

                long result = databaseHelper.insertTask(
                        title,
                        description,
                        courseId,
                        selectedDueDate,
                        priority
                );

                if (result > 0) {

                    showMessage("Task saved successfully");
                    setResult(RESULT_OK);
                    finish();
                    return;

                } else {

                    showMessage("Unable to save task");
                }
            }

        } catch (Exception exception) {

            showMessage("Database operation failed");

        } finally {

            btnSaveTask.setEnabled(true);
        }
    }

    // Load existing task for editing
    private void loadTaskForEditing() {

        try (Cursor cursor =
                     databaseHelper.getTaskById(editingTaskId)) {

            if (!cursor.moveToFirst()) {

                showMessage("Task not found");
                finish();
                return;
            }

            tvFormTitle.setText("Edit Task");
            tvFormSubtitle.setText(
                    "Update your assignment details below."
            );

            btnSaveTask.setText("Update Task");

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

            long courseId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_COURSE_ID
                    )
            );

            selectedDueDate = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_DUE_DATE
                    )
            );

            String priority = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_PRIORITY
                    )
            );

            etTaskTitle.setText(title);
            etTaskDescription.setText(description);

            tvDueDate.setText(selectedDueDate);
            tvDueDate.setTextColor(
                    Color.parseColor("#1E293B")
            );

            // Select saved course
            for (int i = 0; i < courseIds.size(); i++) {

                if (courseIds.get(i) == courseId) {
                    spinnerCourses.setSelection(i);
                    break;
                }
            }

            // Select saved priority
            if (DatabaseHelper.PRIORITY_LOW.equals(priority)) {

                rgPriority.check(R.id.rbLow);

            } else if (
                    DatabaseHelper.PRIORITY_HIGH.equals(priority)
            ) {

                rgPriority.check(R.id.rbHigh);

            } else {

                rgPriority.check(R.id.rbMedium);
            }
        }
    }

    // Show Toast message
    private void showMessage(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
