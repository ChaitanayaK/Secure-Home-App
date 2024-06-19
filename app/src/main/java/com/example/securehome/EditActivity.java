package com.example.securehome;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class EditActivity extends AppCompatActivity implements EditDialog.ConfirmationListener{

    TextView deviceTxt;
    ImageButton lockBtn, deleteBtn, editBtn, shareBtn, powerBtn;
    AppCompatButton groupBtn;
    String key, val;
    DatabaseReference deviceRef, userRef;
    FirebaseDatabase database;
    FirebaseUser currentUser;
    FirebaseAuth auth;
    boolean off;
    boolean value = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        deviceRef = database.getReference("device");
        userRef = database.getReference("users").child(currentUser.getUid());

        Intent intent = getIntent();

        // Check if Intent contains extra data
        if (intent != null && intent.hasExtra("KEY")) {

            key = intent.getStringExtra("KEY");
            val = intent.getStringExtra("VALUE");

            deviceTxt = findViewById(R.id.device_txt_id);
            deviceTxt.setText(key);

            powerBtn = findViewById(R.id.power_btn_id);
            powerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deviceRef.child(val).child("off").setValue(!off);
                }
            });

            deviceRef.child(val).child("off").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    off = Boolean.TRUE.equals(dataSnapshot.getValue(Boolean.class));
                    if (off){
                        powerBtn.setBackgroundResource(R.drawable.power_off);
                    }
                    else{
                        powerBtn.setBackgroundResource(R.drawable.power_on);
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

            deviceRef.child(val).child("fingerprint").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    value = Boolean.TRUE.equals(dataSnapshot.getValue(Boolean.class));
                    if (!value){
                        lockBtn.setBackgroundResource(R.drawable.lock_icon);
                        Toast.makeText(EditActivity.this, key + " is now LOCKED", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        lockBtn.setBackgroundResource(R.drawable.unlock_icon);
                        Toast.makeText(EditActivity.this, key + " is now UNLOCKED", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

            groupBtn = findViewById(R.id.group_btn_id);
            groupBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(EditActivity.this, GroupActivity.class);
                    intent.putExtra("DEVICE_ID", val);
                    startActivity(intent);
                }
            });


            lockBtn = findViewById(R.id.lock_btn_id);
            lockBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!value){
                        Intent intent = new Intent(EditActivity.this, FingerprintActivity.class);
                        intent.putExtra("DEVICE_ID", val);
                        startActivity(intent);
                    }
                    else{
                        deviceRef.child(val).child("fingerprint").setValue(false);
                        deviceRef.child(val).child("face").setValue(true);
                    }

                }
            });

            shareBtn = findViewById(R.id.share_btn_id);
            shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long currentTimeMillis = System.currentTimeMillis();
                    long expirationTimeMillis = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes from now

                    Uri link = Uri.parse("https://SecureHome.com?param=" + val + "&timestamp=" + expirationTimeMillis);

                    FirebaseDynamicLinks.getInstance().createDynamicLink()
                            .setLink(link)
                            .setDomainUriPrefix("https://securehome.page.link")
                            .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.example.securehome").build())
                            .buildShortDynamicLink()
                            .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                                @Override
                                public void onSuccess(ShortDynamicLink shortDynamicLink) {
                                    Uri shortLink = shortDynamicLink.getShortLink();
                                    Log.d("DynamicLink", "Short link: " + shortLink);

                                    String message = "Connect to device by clicking on the link: \n" + shortLink;

                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.putExtra(Intent.EXTRA_TEXT, message);
                                    intent.setType("text/plain");
                                    startActivity(Intent.createChooser(intent, "Share via"));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle error
                                    Log.e("DynamicLink", "Error generating dynamic link", e);
                                }
                            });

                }

            });

            editBtn = findViewById(R.id.edit_btn_id);
            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditDialog.showConfirmationDialog(EditActivity.this, "Edit Device Name", "Enter the new name for your device.", EditActivity.this);
                }
            });

            deleteBtn = findViewById(R.id.delete_btn_id);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConfirmationDialog.showConfirmationDialog(EditActivity.this, "Confirmation",
                            "Are you sure you want to delete this device?",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Handle confirmation action
                                    userRef.child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Node deleted successfully
                                            Toast.makeText(EditActivity.this, "Device Deleted Successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                            startActivity(new Intent(EditActivity.this, DevicesActivity.class));
                                            Log.d(TAG, "Node deleted successfully");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Failed to delete the node
                                            Log.e(TAG, "Failed to delete node: " + e.getMessage());
                                        }
                                    });
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Handle cancellation action
                                    Toast.makeText(EditActivity.this, "Deletion process Cancelled", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }

    }

    @Override
    public void onConfirm(String userInput) {
        // Get the value associated with the old key
        if (!userInput.equals("")) {
            userRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String value = dataSnapshot.child("id").getValue(String.class);
                    int intVal = dataSnapshot.child("admin").getValue(Integer.class);
                    if (value != null) {
                        // Set the value to the new key
                        userRef.child(userInput).child("admin").setValue(intVal);
                        userRef.child(userInput).child("id").setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // If the update is successful, remove the old key-value pair
                                    userRef.child(key).removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                            if (error == null) {
                                                Toast.makeText(EditActivity.this, "Device name has been updated", Toast.LENGTH_SHORT).show();
                                                finish();
                                                startActivity(new Intent(EditActivity.this, DevicesActivity.class));
                                            } else {
                                                Toast.makeText(EditActivity.this, "Failed to update device name", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(EditActivity.this, "Failed to update device name", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(EditActivity.this, "Old device name not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("TAG", "Failed to read value.", error.toException());
                    Toast.makeText(EditActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onCancel() {

    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, DevicesActivity.class));
    }

}