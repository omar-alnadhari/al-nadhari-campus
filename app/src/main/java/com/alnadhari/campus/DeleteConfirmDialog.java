
package com.alnadhari.campus;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DeleteConfirmDialog extends DialogFragment {

    public static final String TAG = "DeleteConfirmDialog";

    public static final String REQUEST_KEY =
            "delete_task_request";

    public static final String RESULT_TASK_ID =
            "confirmed_task_id";

    private static final String ARG_TASK_ID =
            "task_id";

    // Create a dialog for a specific task
    public static DeleteConfirmDialog newInstance(
            long taskId
    ) {

        DeleteConfirmDialog dialog =
                new DeleteConfirmDialog();

        Bundle arguments = new Bundle();

        arguments.putLong(
                ARG_TASK_ID,
                taskId
        );

        dialog.setArguments(arguments);

        return dialog;
    }

    // Build the confirmation dialog
    @NonNull
    @Override
    public Dialog onCreateDialog(
            @Nullable Bundle savedInstanceState
    ) {

        long taskId = requireArguments().getLong(
                ARG_TASK_ID,
                -1
        );

        return new AlertDialog.Builder(requireContext())

                .setTitle("Delete Task")

                .setMessage(
                        "Are you sure you want to delete " +
                                "this task? This action cannot " +
                                "be undone."
                )

                .setNegativeButton(
                        "Cancel",
                        (dialog, which) -> dialog.dismiss()
                )

                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            Bundle result = new Bundle();

                            result.putLong(
                                    RESULT_TASK_ID,
                                    taskId
                            );

                            getParentFragmentManager()
                                    .setFragmentResult(
                                            REQUEST_KEY,
                                            result
                                    );
                        }
                )

                .create();
    }
}
