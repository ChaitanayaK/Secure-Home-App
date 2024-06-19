package com.example.securehome;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private Context context;
    private ArrayList<Map.Entry<String, Object>> memberList;
    DatabaseReference deviceRef, userRef, authRef;
    FirebaseDatabase database;
    FirebaseUser currentUser;
    FirebaseAuth auth;

    public MemberAdapter(Context context, ArrayList<Map.Entry<String, Object>> memberList) {
        this.context = context;
        this.memberList = memberList;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_members, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        final Map.Entry<String, Object> member = memberList.get(position);
//        holder.textView.setText((String) member.getValue());

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        deviceRef = database.getReference("device");
        authRef = FirebaseDatabase.getInstance().getReference("users_auth").child(member.getKey());
        userRef = database.getReference("user").child(currentUser.getUid());

//        Toast.makeText(context, member.getKey(), Toast.LENGTH_SHORT).show();
        authRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = (String) snapshot.child("name").getValue();
                holder.textView.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        authRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String url = (String) snapshot.child("photoURL").getValue();
                Picasso.get().load(url).into(holder.imageView);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("KEY", member.getKey());
                intent.putExtra("DEVICE_ID", (String) member.getValue());
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return memberList.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
//        ImageView imageView;
        CircleImageView imageView;
        LinearLayout linearLayout;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text20);
            imageView = itemView.findViewById(R.id.imageViewid);
            linearLayout = itemView.findViewById(R.id.linearlayout_group_id);
        }
    }

}
