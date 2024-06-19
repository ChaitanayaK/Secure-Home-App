package com.example.securehome;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private Context context;
    String deviceUID;
    DatabaseReference deviceRef;
    FirebaseDatabase database;

    // Constructor
    public FingerprintHandler(Context mContext) {
        context = mContext;
    }

    // Fingerprint authentication starts here..
    public void Authentication(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject, String UID) {
        deviceUID = UID;
        CancellationSignal cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    // On authentication failed
    @Override
    public void onAuthenticationFailed() {
        this.update("Authentication Failed!!!", false);
    }

    // On successful authentication
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        this.update("Successfully Authenticated...", true);
        database = FirebaseDatabase.getInstance();
        deviceRef = database.getReference("device");
        deviceRef.child(deviceUID).child("fingerprint").setValue(true);
    }

    // This method is used to update the text message
    // depending on the authentication result
    public void update(String e, Boolean success){
        TextView textView = (TextView) ((Activity)context).findViewById(R.id.textMsg);
        textView.setText(e);
        if(success){
            textView.setTextColor(ContextCompat.getColor(context,R.color.black));
            ((Activity)context).finish();
        }
    }
}
