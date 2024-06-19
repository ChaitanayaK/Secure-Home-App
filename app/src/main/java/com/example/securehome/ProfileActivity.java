package com.example.securehome;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    TextView nameTxt;
    CircleImageView circleImageView;
    String key, deviceid, deviceName, userName;
    DatabaseReference authRef, userRef;
    FirebaseUser currentUser;
    ImageButton button, upgradeBtn, downgradeBtn;
    LinearLayout upgradeLayout, downgradeLayout, buttonLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        key = intent.getStringExtra("KEY");
        deviceid = intent.getStringExtra("DEVICE_ID");
        button = findViewById(R.id.confetti_button_id);
        upgradeBtn = findViewById(R.id.upgrade_btn_id);
        downgradeBtn = findViewById(R.id.downgrade_btn_id);
        buttonLayout = findViewById(R.id.button_layout_id);
        upgradeLayout = findViewById(R.id.upgrade_layout_id);
        downgradeLayout = findViewById(R.id.downgrade_layout_id);
        nameTxt = findViewById(R.id.name_txt_id);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        authRef = FirebaseDatabase.getInstance().getReference("users_auth").child(key);
        userRef = FirebaseDatabase.getInstance().getReference("users");

        upgradeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmationDialog.showConfirmationDialog(ProfileActivity.this, "Confirmation",
                        "Do you really wish to promote " + userName + "?",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Handle confirmation action
                                userRef.child(key).child(deviceName).child("admin").setValue(2);
                                Toast.makeText(ProfileActivity.this, userName + " has been promoted to an Admin", Toast.LENGTH_LONG).show();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Handle cancellation action
                            }
                        });
            }
        });

        downgradeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmationDialog.showConfirmationDialog(ProfileActivity.this, "Confirmation",
                        "Do you really wish to demote " + userName + "?",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Handle confirmation action
                                userRef.child(key).child(deviceName).child("admin").setValue(1);
                                Toast.makeText(ProfileActivity.this, userName + " has been demoted to a Member", Toast.LENGTH_LONG).show();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Handle cancellation action
                            }
                        });
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmationDialog.showConfirmationDialog(ProfileActivity.this, "Confirmation",
                        "Do you really wish to remove " + userName + "?",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Handle confirmation action
                                userRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            for (DataSnapshot idSnapshot : dataSnapshot.getChildren()) {
                                                if (idSnapshot.getKey().equals("id")) {
                                                    String name = idSnapshot.getValue(String.class);
                                                    if (name != null && name.equals(deviceid)) {
                                                        String userId = dataSnapshot.getKey();
                                                        if (userId != null) {
                                                            Toast.makeText(ProfileActivity.this, userId, Toast.LENGTH_SHORT).show();
                                                            userRef.child(key).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    // This block executes when the removal is successful
                                                                    Toast.makeText(ProfileActivity.this, nameTxt.getText() + " removed from group", Toast.LENGTH_SHORT).show();
                                                                    finish();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    // This block executes if the removal fails
                                                                    Toast.makeText(ProfileActivity.this, "Failed to remove user from group", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        // Handle onCancelled event
                                    }
                                });
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Handle cancellation action
                            }
                        });
            }
        });



        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int userInt = 0, profileInt = 0;
                for (DataSnapshot dataSnapshot : snapshot.child(key).getChildren()){
                    for (DataSnapshot idSnapshot : dataSnapshot.getChildren()){
                        if (idSnapshot.getKey().equals("id")){
                            String name = (String) idSnapshot.getValue();
                            if (name.equals(deviceid)){
                                deviceName = dataSnapshot.getKey();
                                profileInt = dataSnapshot.child("admin").getValue(Integer.class);
                            }
                        }
                    }
                }
                for (DataSnapshot dataSnapshot : snapshot.child(currentUser.getUid()).getChildren()){
                    for (DataSnapshot idSnapshot : dataSnapshot.getChildren()){
                        if (idSnapshot.getKey().equals("id")){
                            String name = (String) idSnapshot.getValue();
                            if (name.equals(deviceid)){
                                userInt = dataSnapshot.child("admin").getValue(Integer.class);
                            }
                        }
                    }
                }
                if (userInt > 1 && profileInt < 2){
                    upgradeLayout.setVisibility(View.VISIBLE);
                }
                else{
                    upgradeLayout.setVisibility(View.GONE);
                }
                if (userInt > 1 && profileInt == 2){
                    downgradeLayout.setVisibility(View.VISIBLE);
                }
                else{
                    downgradeLayout.setVisibility(View.GONE);
                }
                if ((userInt < profileInt) || userInt == profileInt){
                    buttonLayout.setVisibility(View.GONE);
                }
                else {
                    buttonLayout.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        authRef = FirebaseDatabase.getInstance().getReference("users_auth").child(key);

        authRef.child("photoURL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String url = (String) snapshot.getValue();
                circleImageView = findViewById(R.id.imageView);
                Picasso.get().load(url).into(circleImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        authRef.child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userName = (String) snapshot.getValue();
                nameTxt.setText((String) snapshot.getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}