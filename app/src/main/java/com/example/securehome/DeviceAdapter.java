package com.example.securehome;// DeviceAdapter.java
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.BoringLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private Context context;
    private ArrayList<Map.Entry<String, Object>> deviceList;
    DatabaseReference deviceRef, userRef;
    FirebaseDatabase database;
    FirebaseUser currentUser;
    FirebaseAuth auth;
    Boolean fingerprintValue = false, faceValue = false;

    public DeviceAdapter(Context context, ArrayList<Map.Entry<String, Object>> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        final Map.Entry<String, Object> device = deviceList.get(position);
        holder.textView.setText(device.getKey());

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        deviceRef = database.getReference("device");
        userRef = database.getReference("user").child(currentUser.getUid()).child("id");

        deviceRef.child(String.valueOf(device.getValue())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the "fingerprint" and "face" nodes exist
                if (dataSnapshot.hasChild("fingerprint") && dataSnapshot.hasChild("face")) {
                    Boolean fingerprintValue = dataSnapshot.child("fingerprint").getValue(Boolean.class);
                    Boolean faceValue = dataSnapshot.child("face").getValue(Boolean.class);

                    // Update imageView based on fingerprint and face values
                    if (fingerprintValue && faceValue) {
                        holder.imageView.setBackgroundResource(R.drawable.green_dot_icon);
                        holder.imageView2.setBackgroundResource(R.drawable.unlock_key_icon);

                    } else {
                        holder.imageView.setBackgroundResource(R.drawable.red_dot_icon);
                        holder.imageView2.setBackgroundResource(R.drawable.lock_key_icon);
                    }
                }
                if (dataSnapshot.hasChild("type")){
                    String type = dataSnapshot.child("type").getValue(String.class);
                    if (type.equals("computer")){
                        holder.typeImageView.setBackgroundResource(R.drawable.computer_icon);
                    } else if (type.equals("door")) {
                        holder.typeImageView.setBackgroundResource(R.drawable.door_icon);
                    } else if (type.equals("mobile")) {

                    }
                    else{

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditActivity.class);
                String val = (String) device.getValue();
                intent.putExtra("KEY", device.getKey());
                intent.putExtra("VALUE", val);
                context.startActivity(intent);
                ((Activity) context).finish();
            }
        });
    }


    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView, imageView2, typeImageView;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
            imageView = itemView.findViewById(R.id.status1); // Initialize the ImageView
            imageView2 = itemView.findViewById(R.id.status2);
            typeImageView = itemView.findViewById(R.id.type_icon_id);
        }
    }

}
