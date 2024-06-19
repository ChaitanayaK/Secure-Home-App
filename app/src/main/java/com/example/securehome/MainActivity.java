package com.example.securehome;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Map;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity implements EditDialog.ConfirmationListener{

    GoogleSignInClient googleSignInClient;
    FirebaseAuth auth;
    public FirebaseDatabase database;
    private ImageButton deviceBtn, settingBtn;
//    public static FirebaseUser currentUser = null;
    private ArrayList<Map.Entry<String, Object>> deviceList;
    FirebaseUser currentUser;
    private String newDeviceid = null;
    TextView welcomeTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        currentUser = auth.getCurrentUser();

        welcomeTxt = findViewById(R.id.welcome_txt_id);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Enable the lock button after the sleep duration
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    // User is signed in, offer sign-out option
//                    FirebaseAuth.getInstance().signOut();
                    GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken("104807508804-bd5af16usfcdui1nrsphedethb0ct9h5.apps.googleusercontent.com")
                            .requestEmail()
                            .build();
                    googleSignInClient = GoogleSignIn.getClient(MainActivity.this, googleSignInOptions);
                    Intent googleIntent = googleSignInClient.getSignInIntent();

                    startActivityForResult(googleIntent, 100);
                }
                else {
                    welcomeTxt.setText("Welcome, " + currentUser.getDisplayName());
//                    Toast.makeText(MainActivity.this, currentUser.getDisplayName() + " is signed in...", Toast.LENGTH_SHORT).show();
                }

                Intent intent = getIntent();
                Uri uri = intent.getData();
                if (uri != null) {
                    newDeviceid = uri.getQueryParameter("param");
                    String timestampStr = uri.getQueryParameter("timestamp");
                    if (newDeviceid != null && timestampStr != null) {
                        long timestamp = Long.parseLong(timestampStr);
                        long currentTimeMillis = System.currentTimeMillis();
                        if (currentTimeMillis < timestamp) {
                            DatabaseReference parentNodeRef = FirebaseDatabase.getInstance().getReference("user").child(currentUser.getUid());
                            parentNodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    boolean valueFound = false;
                                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                        String value = childSnapshot.getValue(String.class);
                                        if (value != null && value.equals(newDeviceid)) {
                                            valueFound = true;
                                            break;
                                        }
                                    }
                                    if (valueFound) {
                                        // Do something if the value is found
                                        Toast.makeText(MainActivity.this, "Device is already registered", Toast.LENGTH_SHORT).show();
                                        Log.d("ValueCheck", "The node with the specific value exists.");
                                    } else {
                                        // Do something if the value is not found
                                        EditDialog.showConfirmationDialog(MainActivity.this, "Name Device", "Give a name to your new device.", MainActivity.this);
                                        Log.d("ValueCheck", "The node with the specific value does not exist.");
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle potential errors
                                    Log.e("ValueCheck", "Error reading database: " + databaseError.getMessage());
                                }
                            });
                        } else {
                            // Handle the case where the link has expired
                            Toast.makeText(MainActivity.this, "Link has expired", Toast.LENGTH_SHORT).show();
                            Log.d("ExpirationCheck", "Link has expired");
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Link is not recognised...", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }, 500);


        settingBtn = findViewById(R.id.settings_btn_id);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                finish();
            }
        });

        deviceBtn = findViewById(R.id.devices_btn_id);
        deviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    startActivity(new Intent(MainActivity.this, DevicesActivity.class));
                }
                else{
                    Toast.makeText(MainActivity.this, "Please create an account", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check condition
        if (requestCode == 100) {
            // When request code is equal to 100 initialize task
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            // check condition
            if (signInAccountTask.isSuccessful()) {
                // When google sign in successful initialize string
                String s = "Google sign in successful";
                // Display Toast
                displayToast(s);
                // Initialize sign in account
                try {
                    // Initialize sign in account
                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                    // Check condition
                    if (googleSignInAccount != null) {
                        // When sign in account is not equal to null initialize auth credential
                        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                        // Check credential
                        FirebaseAuth.getInstance().signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Check condition
                                if (task.isSuccessful()) {
                                    // When task is successful redirect to profile activity display Toast
                                    displayToast("Firebase authentication successful");
                                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                                    DatabaseReference authRef = FirebaseDatabase.getInstance().getReference("users_auth").child(currentUser.getUid());

                                    authRef.child("name").setValue(currentUser.getDisplayName());
                                    authRef.child("email").setValue(currentUser.getEmail());
                                    authRef.child("photoURL").setValue(currentUser.getPhotoUrl().toString());
                                } else {
                                    // When task is unsuccessful display Toast
                                    displayToast("Authentication Failed :" + task.getException().getMessage());
                                }
                            }
                        });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onConfirm(String userInput) {
        if (userInput != ""){
            auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child(userInput);
            userRef.child("id").setValue(newDeviceid);
            userRef.child("admin").setValue(1);
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