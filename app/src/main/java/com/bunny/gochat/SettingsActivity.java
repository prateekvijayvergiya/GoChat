package com.bunny.gochat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    private CircleImageView circleImageView;
    private TextView mName;
    private TextView mStatus;
    private Button mSettingsStatusBtn;
    private Button msettingsImageBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        circleImageView = (CircleImageView) findViewById(R.id.settingsImage);
        mName = (TextView) findViewById(R.id.displayName);
        mStatus = (TextView) findViewById(R.id.statusName);
        mSettingsStatusBtn = (Button) findViewById(R.id.settingsStatusBtn);
        msettingsImageBtn = (Button) findViewById(R.id.settingsImageBtn);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       mSettingsStatusBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               String statusValue = mStatus.getText().toString();
               Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
               statusIntent.putExtra("status",statusValue);
               startActivity(statusIntent);
           }
       });

    }
}
