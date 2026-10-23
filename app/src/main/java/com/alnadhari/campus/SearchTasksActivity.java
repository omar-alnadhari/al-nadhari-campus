
package com.alnadhari.campus;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchTasksActivity extends AppCompatActivity {

    private static final String TAG = "SearchTasksActivity";

    // Search screen components
    private EditText etSearchQuery;

    private Button btnBack;
    private Button btnSearch;
    private Button btnClearSearch;

    private TextView tvResultCount;
    private TextView tvEmptyTitle;
    private TextView tvEmptyMessage;

    private View layoutEmptyState;
    private RecyclerView recyclerViewSearchResults;

    // SQLite database
    private DatabaseHelper databaseHelper;

    // RecyclerView adapter
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_tasks);

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

        // Initialize screen components
        initializeViews();

        // Configure RecyclerView
        setupRecyclerView();

        // Configure buttons
        setupButtonListeners();

        // Configure live search
        setupLiveSearch();
    }

    // Connect Java variables to XML elements
    private void initializeViews() {

        etSearchQuery = findViewById(
                R.id.etSearchQuery
        );

        btnBack = findViewById(
                R.id.btnBack
        );

        btnSearch = findViewById(
                R.id.btnSearch
        );

        btnClearSearch = findViewById(
                R.id.btnClearSearch
        );

        tvResultCount = findViewById(
                R.id.tvResultCount
        );

        tvEmptyTitle = findViewById(
                R.id.tvEmptyTitle
        );

        tvEmptyMessage = findViewById(
                R.id.tvEmptyMessage
        );

        layoutEmptyState = findViewById(
                R.id.layoutEmptyState
        );

        recyclerViewSearchResults = findViewById(
                R.id.recyclerViewSearchResults
        );
    }

    // Configure RecyclerView with TaskAdapter
    private void setupRecyclerView() {

        recyclerViewSearchResults.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerViewSearchResults.setHasFixedSize(false);

        taskAdapter = new TaskAdapter(
                new ArrayList<>(),
                taskId -> openTaskDetails(taskId)
        );

        recyclerViewSearchResults.setAdapter(taskAdapter);
    }

    // Configure navigation and search buttons
    private void setupButtonListeners() {

        // Return to Dashboard
        btnBack.setOnClickListener(view -> {
            finish();
        });

        // Search button
        btnSearch.setOnClickListener(view -> {

            performSearch();
            hideKeyboard();
        });

        // Clear search and display all tasks
        btnClearSearch.setOnClickListener(view -> {

            etSearchQuery.setText("");
            hideKeyboard();
            etSearchQuery.clearFocus();
        });

        // Search using keyboard Search action
        etSearchQuery.setOnEditorActionListener(
                (textView, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                        performSearch();
                        hideKeyboard();

                        return true;
                    }

                    return false;
                }
        );
    }

    // Search automatically while typing
    private void setupLiveSearch() {

        etSearchQuery.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                        // No action needed
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        performSearch();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                        // No action needed
                    }
                }
        );
    }

    // Read the keyword from the search field
    private void performSearch() {

        String keyword = etSearchQuery
                .getText()
                .toString()
                .trim();

        loadSearchResults(keyword);
    }

    // Search tasks using SQLite
    private void loadSearchResults(String keyword) {

        List<TaskAdapter.TaskItem> searchResults =
                new ArrayList<>();

        boolean isSearchEmpty =
                keyword == null || keyword.trim().isEmpty();

        try (
                Cursor cursor = isSearchEmpty
                        ? databaseHelper.getAllTasks()
                        : databaseHelper.searchTasks(keyword)
        ) {

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

            // Read matching SQLite records
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

                // Create a task item
                TaskAdapter.TaskItem task =
                        new TaskAdapter.TaskItem(
                                id,
                                title,
                                courseName,
                                dueDate,
                                priority,
                                status
                        );

                searchResults.add(task);
            }

        } catch (Exception exception) {

            Log.e(
                    TAG,
                    "Failed to search tasks",
                    exception
            );

            Toast.makeText(
                    this,
                    "Unable to search tasks",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Update RecyclerView with search results
        taskAdapter.updateTasks(searchResults);

        // Update result counter
        int resultCount = searchResults.size();

        if (resultCount == 1) {

            tvResultCount.setText("1 Result");

        } else {

            tvResultCount.setText(
                    resultCount + " Results"
            );
        }

        // Display empty state when there are no results
        updateEmptyState(
                resultCount,
                isSearchEmpty
        );
    }

    // Show or hide the empty state
    private void updateEmptyState(
            int resultCount,
            boolean isSearchEmpty
    ) {

        if (resultCount > 0) {

            layoutEmptyState.setVisibility(
                    View.GONE
            );

            recyclerViewSearchResults.setVisibility(
                    View.VISIBLE
            );

            return;
        }

        // No matching tasks
        layoutEmptyState.setVisibility(
                View.VISIBLE
        );

        recyclerViewSearchResults.setVisibility(
                View.GONE
        );

        if (isSearchEmpty) {

            tvEmptyTitle.setText(
                    "No Tasks Yet"
            );

            tvEmptyMessage.setText(
                    "You have not added any assignments yet. " +
                            "Create a task to get started."
            );

        } else {

            tvEmptyTitle.setText(
                    "No Tasks Found"
            );

            tvEmptyMessage.setText(
                    "No assignments match your search. " +
                            "Try another keyword."
            );
        }
    }

    // Open Task Details and pass the task ID
    private void openTaskDetails(long taskId) {

        Intent intent = new Intent(
                SearchTasksActivity.this,
                TaskDetailsActivity.class
        );

        intent.putExtra(
                TaskDetailsActivity.EXTRA_TASK_ID,
                taskId
        );

        startActivity(intent);
    }

    // Hide the Android keyboard
    private void hideKeyboard() {

        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );

        if (inputMethodManager != null) {

            inputMethodManager.hideSoftInputFromWindow(
                    etSearchQuery.getWindowToken(),
                    0
            );
        }
    }

    // Refresh search results when returning
    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null &&
                taskAdapter != null) {

            performSearch();
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
