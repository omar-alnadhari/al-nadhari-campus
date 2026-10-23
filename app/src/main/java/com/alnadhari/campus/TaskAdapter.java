
package com.alnadhari.campus;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter
        extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    // List of tasks displayed in RecyclerView
    private final ArrayList<TaskItem> taskList;

    // Listener for task selection
    private final OnTaskClickListener listener;

    // Interface for opening task details
    public interface OnTaskClickListener {
        void onTaskClick(long taskId);
    }

    // Constructor
    public TaskAdapter(
            List<TaskItem> tasks,
            OnTaskClickListener listener
    ) {
        this.taskList = new ArrayList<>();

        if (tasks != null) {
            this.taskList.addAll(tasks);
        }

        this.listener = listener;
    }

    // Model for one task item
    public static class TaskItem {

        public final long id;
        public final String title;
        public final String courseName;
        public final String dueDate;
        public final String priority;
        public final String status;

        public TaskItem(
                long id,
                String title,
                String courseName,
                String dueDate,
                String priority,
                String status
        ) {
            this.id = id;
            this.title = title;
            this.courseName = courseName;
            this.dueDate = dueDate;
            this.priority = priority;
            this.status = status;
        }
    }

    // Create a new ViewHolder
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_task,
                        parent,
                        false
                );

        return new TaskViewHolder(view);
    }

    // Bind task data to card views
    @Override
    public void onBindViewHolder(
            @NonNull TaskViewHolder holder,
            int position
    ) {

        TaskItem task = taskList.get(position);

        holder.tvTaskTitle.setText(task.title);

        holder.tvCourseName.setText(
                "Course: " + task.courseName
        );

        holder.tvDueDate.setText(
                "Due Date: " + task.dueDate
        );

        holder.tvTaskPriority.setText(
                "Priority: " + task.priority
        );

        holder.tvTaskStatus.setText(task.status);

        // Change priority text color
        if (DatabaseHelper.PRIORITY_HIGH.equals(
                task.priority)) {

            holder.tvTaskPriority.setTextColor(
                    Color.parseColor("#DC2626")
            );

        } else if (DatabaseHelper.PRIORITY_MEDIUM.equals(
                task.priority)) {

            holder.tvTaskPriority.setTextColor(
                    Color.parseColor("#D97706")
            );

        } else {

            holder.tvTaskPriority.setTextColor(
                    Color.parseColor("#16A34A")
            );
        }

        // Change task status color
        if (DatabaseHelper.STATUS_COMPLETED.equals(
                task.status)) {

            holder.tvTaskStatus.setTextColor(
                    Color.parseColor("#16A34A")
            );

        } else {

            holder.tvTaskStatus.setTextColor(
                    Color.parseColor("#D97706")
            );
        }

        // Open task details when card is clicked
        holder.itemView.setOnClickListener(view -> {

            int currentPosition =
                    holder.getBindingAdapterPosition();

            if (currentPosition != RecyclerView.NO_POSITION
                    && listener != null) {

                long selectedTaskId =
                        taskList.get(currentPosition).id;

                listener.onTaskClick(selectedTaskId);
            }
        });
    }

    // Return number of tasks
    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // Update the task list
    public void updateTasks(List<TaskItem> newTasks) {

        taskList.clear();

        if (newTasks != null) {
            taskList.addAll(newTasks);
        }

        notifyDataSetChanged();
    }

    // ViewHolder for task cards
    public static class TaskViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvTaskTitle;
        TextView tvTaskStatus;
        TextView tvCourseName;
        TextView tvDueDate;
        TextView tvTaskPriority;

        public TaskViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            tvTaskTitle = itemView.findViewById(
                    R.id.tvTaskTitle
            );

            tvTaskStatus = itemView.findViewById(
                    R.id.tvTaskStatus
            );

            tvCourseName = itemView.findViewById(
                    R.id.tvCourseName
            );

            tvDueDate = itemView.findViewById(
                    R.id.tvDueDate
            );

            tvTaskPriority = itemView.findViewById(
                    R.id.tvTaskPriority
            );
        }
    }
}
