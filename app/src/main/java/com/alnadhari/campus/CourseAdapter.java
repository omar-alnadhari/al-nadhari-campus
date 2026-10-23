
package com.alnadhari.campus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CourseAdapter
        extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    // List of courses displayed in RecyclerView
    private final ArrayList<CourseItem> courseList;

    // Listener for course actions
    private final OnCourseActionListener listener;

    // Interface for editing and deleting courses
    public interface OnCourseActionListener {

        void onEditCourse(
                long courseId,
                String courseName
        );

        void onDeleteCourse(
                long courseId,
                String courseName,
                int taskCount
        );
    }

    // Model representing one course
    public static class CourseItem {

        public final long id;
        public final String name;
        public final int taskCount;

        public CourseItem(
                long id,
                String name,
                int taskCount
        ) {
            this.id = id;
            this.name = name;
            this.taskCount = taskCount;
        }
    }

    // Adapter constructor
    public CourseAdapter(
            List<CourseItem> courses,
            OnCourseActionListener listener
    ) {

        this.courseList = new ArrayList<>();

        if (courses != null) {
            this.courseList.addAll(courses);
        }

        this.listener = listener;

        // Use database IDs as stable item IDs
        setHasStableIds(true);
    }

    // Create a ViewHolder for each course
    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_course,
                        parent,
                        false
                );

        return new CourseViewHolder(view);
    }

    // Display course information
    @Override
    public void onBindViewHolder(
            @NonNull CourseViewHolder holder,
            int position
    ) {

        CourseItem course = courseList.get(position);

        // Display course name
        holder.tvCourseName.setText(course.name);

        // Display the number of tasks
        String taskCountText = String.format(
                Locale.US,
                "%d %s",
                course.taskCount,
                course.taskCount == 1 ? "Task" : "Tasks"
        );

        holder.tvCourseTaskCount.setText(taskCountText);

        // Edit button
        holder.btnEditCourse.setOnClickListener(view -> {

            int currentPosition =
                    holder.getBindingAdapterPosition();

            if (currentPosition != RecyclerView.NO_POSITION
                    && listener != null) {

                CourseItem selectedCourse =
                        courseList.get(currentPosition);

                listener.onEditCourse(
                        selectedCourse.id,
                        selectedCourse.name
                );
            }
        });

        // Delete button
        holder.btnDeleteCourse.setOnClickListener(view -> {

            int currentPosition =
                    holder.getBindingAdapterPosition();

            if (currentPosition != RecyclerView.NO_POSITION
                    && listener != null) {

                CourseItem selectedCourse =
                        courseList.get(currentPosition);

                listener.onDeleteCourse(
                        selectedCourse.id,
                        selectedCourse.name,
                        selectedCourse.taskCount
                );
            }
        });
    }

    // Return the total number of courses
    @Override
    public int getItemCount() {

        return courseList.size();
    }

    // Return the SQLite ID of a course
    @Override
    public long getItemId(int position) {

        return courseList.get(position).id;
    }

    // Update courses efficiently using DiffUtil
    public void updateCourses(
            List<CourseItem> newCourses
    ) {

        List<CourseItem> oldList =
                new ArrayList<>(courseList);

        List<CourseItem> updatedList =
                new ArrayList<>();

        if (newCourses != null) {
            updatedList.addAll(newCourses);
        }

        DiffUtil.DiffResult diffResult =
                DiffUtil.calculateDiff(
                        new DiffUtil.Callback() {

                            @Override
                            public int getOldListSize() {
                                return oldList.size();
                            }

                            @Override
                            public int getNewListSize() {
                                return updatedList.size();
                            }

                            @Override
                            public boolean areItemsTheSame(
                                    int oldItemPosition,
                                    int newItemPosition
                            ) {

                                return oldList.get(
                                        oldItemPosition
                                ).id == updatedList.get(
                                        newItemPosition
                                ).id;
                            }

                            @Override
                            public boolean areContentsTheSame(
                                    int oldItemPosition,
                                    int newItemPosition
                            ) {

                                CourseItem oldCourse =
                                        oldList.get(
                                                oldItemPosition
                                        );

                                CourseItem newCourse =
                                        updatedList.get(
                                                newItemPosition
                                        );

                                return Objects.equals(
                                        oldCourse.name,
                                        newCourse.name
                                ) && oldCourse.taskCount ==
                                        newCourse.taskCount;
                            }
                        }
                );

        courseList.clear();
        courseList.addAll(updatedList);

        diffResult.dispatchUpdatesTo(this);
    }

    // ViewHolder for one course card
    public static class CourseViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvCourseName;
        TextView tvCourseTaskCount;

        Button btnEditCourse;
        Button btnDeleteCourse;

        public CourseViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvCourseName = itemView.findViewById(
                    R.id.tvCourseName
            );

            tvCourseTaskCount = itemView.findViewById(
                    R.id.tvCourseTaskCount
            );

            btnEditCourse = itemView.findViewById(
                    R.id.btnEditCourse
            );

            btnDeleteCourse = itemView.findViewById(
                    R.id.btnDeleteCourse
            );
        }
    }
}
