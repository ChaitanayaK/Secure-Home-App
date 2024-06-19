package com.example.securehome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ConfirmationDialog {

    // Method to create and show a confirmation dialog
    public static void showConfirmationDialog(Context context, String title, String message,
                                              DialogInterface.OnClickListener confirmListener,
                                              DialogInterface.OnClickListener cancelListener) {
        // Create an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set the title and message for the confirmation dialog
        builder.setTitle(title);
        builder.setMessage(message);

        // Set a button to confirm the action
        builder.setPositiveButton("Confirm", confirmListener);

        // Set a button to cancel the action
        builder.setNegativeButton("Cancel", cancelListener);

        // Create and show the confirmation dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
