package com.bunny.gochat;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private Toolbar mChatToolbar;
    private DatabaseReference mRootRef;
    private TextView mTitleView,mLastSeen;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUser;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatToolbar = (Toolbar) findViewById(R.id.chatAppBar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mChatUser = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("name");
        //getSupportActionBar().setTitle(userName);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(actionBarView);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mTitleView = (TextView) findViewById(R.id.chatDisplayName);
        mLastSeen = (TextView) findViewById(R.id.chatLastSeen);
        mProfileImage = (CircleImageView) findViewById(R.id.customBarImage);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();

        mChatAddBtn = (ImageButton) findViewById(R.id.chatAddBtn);
        mChatSendBtn = (ImageButton) findViewById(R.id.chatSendBtn);
        mChatMessageView = (EditText) findViewById(R.id.chatMessageView);
        mTitleView.setText(userName);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                if (online.equals("true")){
                    mLastSeen.setText("Online");
                }
                else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    Long lastTime = Long.parseLong(online);
                    String lastSeen = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mLastSeen.setText(lastSeen);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if(!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUser + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUser, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                sendMessage();
            }
        });

    }

    private void sendMessage() {

        String message = mChatMessageView.getText().toString();

        if(!TextUtils.isEmpty(message)) {

            String current_user_ref = "messages/" + mCurrentUser + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUser;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUser).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUser);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatMessageView.setText("");

            mRootRef.child("Chat").child(mCurrentUser).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("Chat").child(mCurrentUser).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("Chat").child(mChatUser).child(mCurrentUser).child("seen").setValue(false);
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    }

                }
            });
        }

    }
}
