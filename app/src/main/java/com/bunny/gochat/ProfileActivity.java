package com.bunny.gochat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Map;

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
    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String userId = getIntent().getStringExtra("userId");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        friendReqDatabase = FirebaseDatabase.getInstance().getReference().child("friend_requsts");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();

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

                            String requestType = dataSnapshot.child(userId).child("request_type").getValue().toString();

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


                    DatabaseReference mNotificationRef = mRootRef.child("Notifications").child(userId).push();
                    String mNotificationId = mNotificationRef.getKey();

                    final HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("from",currentUser.getUid());
                    notificationData.put("type","request");

                    Map requestMap = new HashMap();
                    requestMap.put("FriendReq/" + currentUser.getUid() + "/" + userId + "requestType", "sent");
                    requestMap.put("FriendReq/" + userId + "/" + currentUser.getUid() + "requestType", "received");
                    requestMap.put("Notifications/" + userId + "/" + mNotificationId, notificationData);

                    friendReqDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(final DatabaseError databaseError, DatabaseReference databaseReference) {

                            mRootRef.child(userId).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    if (databaseError != null){

                                        Toast.makeText(ProfileActivity.this, "There is some Error",Toast.LENGTH_LONG).show();
                                    }
                                    sendRequestBtn.setEnabled(true );
                                    currentState = "request sent";
                                    sendRequestBtn.setText("Cancel Friend Request");
                                }
                            });
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
                    Map friendsMap = new HashMap();
                    friendsMap.put("FriendReq/" + currentUser.getUid() + "/" + userId + "/date", currentDate);
                    friendsMap.put("FriendReq/" + userId + "/" + currentUser.getUid() + "/date", currentDate);

                    friendsMap.put("FriendReq/" + currentUser.getUid() + "/" + userId, null);
                    friendsMap.put("FriendReq/" + userId + "/" + currentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null){

                                sendRequestBtn.setEnabled(true);
                                currentState = "friends";
                                sendRequestBtn.setText("Unfriend");

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            }else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }



                //---------------UnFriend------------------------------------

                if (currentState.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + currentUser.getUid() + "/" + userId, null);
                    unfriendMap.put("Friends/" + userId + "/" + currentUser.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null){


                                currentState = "not Friends";
                                sendRequestBtn.setText("Send Friend Request");

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            }else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_LONG).show();
                            }sendRequestBtn.setEnabled(true);
                        }
                    });
                }
            }
        });

    }
}
