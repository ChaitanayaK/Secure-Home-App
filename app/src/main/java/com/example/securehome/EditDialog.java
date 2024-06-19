package com.example.securehome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

public class EditDialog {

    // Method to create and show a confirmation dialog with text input
    public static void showConfirmationDialog(Context context, String title, String message,
                                              final ConfirmationListener confirmationListener) {
        // Create an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set the title and message for the confirmation dialog
        builder.setTitle(title);
        builder.setMessage(message);

        // Set up the layout for the dialog
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Create an EditText for text input
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(input);

        // Set a button to confirm the action
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInput = input.getText().toString();
                confirmationListener.onConfirm(userInput);
            }
        });

        // Set a button to cancel the action
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                confirmationListener.onCancel();
            }
        });

        // Set the custom layout to the dialog
        builder.setView(layout);

        // Create and show the confirmation dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Interface for confirmation listener
    public interface ConfirmationListener {
        void onConfirm(String userInput);
        void onCancel();
    }
}
