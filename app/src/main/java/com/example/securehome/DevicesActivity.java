package com.example.securehome;

// MainActivity.java
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class DevicesActivity extends AppCompatActivity implements EditDialog.ConfirmationListener{

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private DeviceAdapter adapter;
    private ArrayList<Map.Entry<String, Object>> deviceList;
    ImageButton addBtn;
    FirebaseAuth auth;
    String qrScannedString;

    ActivityResultLauncher<ScanOptions> cameraLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            qrScannedString = result.getContents();
            DatabaseReference deviceReference = FirebaseDatabase.getInstance().getReference("device");
            deviceReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Check if the key exists under the "device" parent node
                    if (dataSnapshot.hasChild(qrScannedString)) {
                        // The key exists, return true or perform your desired action
                        EditDialog.showConfirmationDialog(DevicesActivity.this, "Device Name", "What would be the name of your device?", DevicesActivity.this);

                    } else {
                        Toast.makeText(DevicesActivity.this, "No such device exists", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors
                }
            });
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressBar = findViewById(R.id.progress_bar_id);

        addBtn = findViewById(R.id.add_btn_id);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showScanOptionsDialog();
            }
        });

        // Initialize Firebase database reference
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference("device");

        // Read data from Firebase and populate RecyclerView
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                deviceList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Add key-value pairs to the list
                    deviceList.add(new AbstractMap.SimpleEntry<>(snapshot.getKey(), snapshot.child("id").getValue()));
                }
                // Set up RecyclerView adapter
                adapter = new DeviceAdapter(DevicesActivity.this, deviceList);
                recyclerView.setAdapter(adapter);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(DevicesActivity.this, "Failed to read data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showScanOptionsDialog() {
        scanCode();
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        cameraLauncher.launch(options);
    }

    @Override
    public void onConfirm(String userInput) {
        if (!userInput.equals("")) {
            auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            userRef.child(userInput).child("id").setValue(qrScannedString);
            userRef.child(userInput).child("admin").setValue(3);

            Toast.makeText(this, "Device Added Successfully", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCancel() {

    }
}

