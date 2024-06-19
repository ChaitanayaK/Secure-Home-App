package com.example.securehome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {

    AppCompatButton logoutBtn;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        logoutBtn = findViewById(R.id.log_out_btn_id);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                clearApplicationData();
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                finish();
            }
        });

    }

    private void clearApplicationData() {
        try {
            // Clear data and cache
            Runtime.getRuntime().exec("pm clear com.example.securehome");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}