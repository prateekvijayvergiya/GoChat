package com.bunny.gochat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName,profileStatus,profileFriends;
    private ImageView profileImage;
    private Button sendRequestBtn;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private String currentState;
    private DatabaseReference friendReqDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String userId = getIntent().getStringExtra("userId");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        friendReqDatabase = FirebaseDatabase.getInstance().getReference().child("friend_requsts");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        profileName = (TextView) findViewById(R.id.profileDisplayName);
        profileStatus = (TextView) findViewById(R.id.profileStatus);
        profileFriends = (TextView) findViewById(R.id.profileTotalFriends);
        profileImage = (ImageView) findViewById(R.id.profileImage);
        sendRequestBtn = (Button) findViewById(R.id.profileRequestBtn);

        currentState = "not Friends";

        progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.setTitle("Loading User Details");
        progressDialog.setMessage("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String displayName = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                profileName.setText(displayName);
                profileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.profile_icon).into(profileImage);
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentState.equals("not Friends")){

                    friendReqDatabase.child(currentUser.getUid()).child(userId).child("user_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                friendReqDatabase.child(userId).child(currentUser.getUid()).child("request_type")
                                        .setValue("Received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(ProfileActivity.this,"Request sent successfully",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }else {
                                Toast.makeText(ProfileActivity.this,"Sending Failed",Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            }
        });

    }
}
