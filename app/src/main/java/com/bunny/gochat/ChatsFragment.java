package com.bunny.gochat;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View mMainView;
    private FirebaseAuth mAuth;
    private RecyclerView mChatList;
    private String mCurrentUserId;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mChatDatabase;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);
        mChatList = (RecyclerView) mMainView.findViewById(R.id.chatList);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mChatDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUserId);
        mChatDatabase.keepSynced(true);

        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends,ChatsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, ChatsViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                ChatsViewHolder.class,
                mChatDatabase

        ) {
            @Override
            protected void populateViewHolder(final ChatsViewHolder viewHolder, Friends model, int position) {

                viewHolder.setDate(model.getDate());
                final String listUserId = getRef(position).getKey();

                mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        viewHolder.setName(userName);
                        viewHolder.setUserImage(userThumb, getContext());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                chatIntent.putExtra("userId",listUserId);
                                chatIntent.putExtra("name",userName);
                                startActivity(chatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mChatList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ChatsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date) {
            TextView userNameView = (TextView) mView.findViewById(R.id.userSingleStatus);
            userNameView.setText(date);
        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.userSingleName);
            userNameView.setText(name);
        }

        public void setUserImage(String thumbImage, Context ctx) {

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.userSingleImage);
            Picasso.with(ctx).load(thumbImage).placeholder(R.drawable.profile_icon).into(userImageView);
        }
    }


}
