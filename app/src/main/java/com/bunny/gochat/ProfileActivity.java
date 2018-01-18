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

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName,profileStatus,profileFriends;
    private ImageView profileImage;
    private Button sendRequestBtn,declineBtn;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private String currentState;
    private DatabaseReference friendReqDatabase;
    private FirebaseUser currentUser;
    private DatabaseReference friendDatabase;
    private DatabaseReference notificationDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String userId = getIntent().getStringExtra("userId");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        friendReqDatabase = FirebaseDatabase.getInstance().getReference().child("friend_requsts");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");

        profileName = (TextView) findViewById(R.id.profileDisplayName);
        profileStatus = (TextView) findViewById(R.id.profileStatus);
        profileFriends = (TextView) findViewById(R.id.profileTotalFriends);
        profileImage = (ImageView) findViewById(R.id.profileImage);
        sendRequestBtn = (Button) findViewById(R.id.profileRequestBtn);
        declineBtn = (Button) findViewById(R.id.profileDeclineFriendRequestBtn);

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

                //-----------Friends List-------------------
                friendReqDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(userId)){

                            String requestType = dataSnapshot.child(userId).child("request type").getValue().toString();

                            if (requestType.equals("received")){

                                currentState = "request received";
                                sendRequestBtn.setText("Accept Friend Request");
                                declineBtn.setVisibility(View.VISIBLE);
                                declineBtn.setEnabled(true);

                            }else if(requestType.equals("sent")) {

                                    currentState = "request sent";
                                    sendRequestBtn.setText("Cancel Friend Request");
                                    declineBtn.setVisibility(View.INVISIBLE);
                                    declineBtn.setEnabled(false);
                            }
                            progressDialog.dismiss();
                        }else {

                            friendDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userId)){

                                        currentState = "friends";
                                        sendRequestBtn.setText("Unfriend");

                                        declineBtn.setVisibility(View.INVISIBLE);
                                        declineBtn.setEnabled(false);
                                    }
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    progressDialog.dismiss();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendRequestBtn.setEnabled(false);

                //..........Not Friends State...........
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

                                        HashMap<String,String> notificationData = new HashMap<>();
                                        notificationData.put("from",currentUser.getUid());
                                        notificationData.put("type","request");
                                        notificationDatabase.child(userId).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                currentState = "request sent";
                                                sendRequestBtn.setText("Cancel Friend Request");

                                                declineBtn.setVisibility(View.INVISIBLE);
                                                declineBtn.setEnabled(false);
                                            }
                                        });


                                        //Toast.makeText(ProfileActivity.this,"Request sent successfully"+currentState,Toast.LENGTH_LONG).show();
                                    }
                                });
                            }else {
                                Toast.makeText(ProfileActivity.this,"Sending Failed",Toast.LENGTH_LONG).show();
                            }
                            sendRequestBtn.setEnabled(true);
                        }
                    });

                }


                //..........Cancel Friends State...........
                if (currentState.equals("request sent")){

                    friendReqDatabase.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendReqDatabase.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    sendRequestBtn.setEnabled(true);
                                    currentState = "not sent";
                                    sendRequestBtn.setText("Send Friend Request");

                                    declineBtn.setVisibility(View.INVISIBLE);
                                    declineBtn.setEnabled(false);

                                }
                            });
                        }
                    });
                }


                //---------------req Received State------------
                if (currentState.equals("request sent")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    friendDatabase.child(currentUser.getUid()).child(userId).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendDatabase.child(userId).child(currentUser.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    friendReqDatabase.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            friendReqDatabase.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    sendRequestBtn.setEnabled(true);
                                                    currentState = "friends";
                                                    sendRequestBtn.setText("Unfriend");

                                                    declineBtn.setVisibility(View.INVISIBLE);
                                                    declineBtn.setEnabled(false);

                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });

    }
}
