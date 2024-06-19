package com.example.securehome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class GroupActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MemberAdapter adapter;
    private ProgressBar progressBar;
    private ArrayList<Map.Entry<String, Object>> memberList;
    FirebaseAuth auth;
    public String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        recyclerView = findViewById(R.id.recyclerView_grp_id);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressBar = findViewById(R.id.progress_bar_id);

        Intent intent = getIntent();
        key = intent.getStringExtra("DEVICE_ID");

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference authRef = FirebaseDatabase.getInstance().getReference("users_auth");
        DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference("device");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                memberList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    boolean found = false;
                    // Add key-value pairs to the list directly
                    for (DataSnapshot childSnapShot : snapshot.getChildren()) {
                        if (childSnapShot.child("id").getValue().toString().equals(key)) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        found = false;
                        authRef.child(snapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot newSnapshot : dataSnapshot.getChildren()) {
                                    if (newSnapshot.getKey().toString().equals("name")) {
                                        memberList.add(new AbstractMap.SimpleEntry<>(snapshot.getKey(), key));
                                        break;
                                    }
                                }
                                // Notify RecyclerView adapter of data changes
                                adapter.notifyDataSetChanged();
                                // Reset the found flag
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle error
                                Toast.makeText(GroupActivity.this, "Failed to read data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
                // Set up RecyclerView adapter
                adapter = new MemberAdapter(GroupActivity.this, memberList);
                recyclerView.setAdapter(adapter);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(GroupActivity.this, "Failed to read data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });


    }
}