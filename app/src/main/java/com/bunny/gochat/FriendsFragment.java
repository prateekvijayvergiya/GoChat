package com.bunny.gochat;


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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private DatabaseReference mFriendsDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView mFriendsList;
    private String mCurrentUserId;
    private View mMainView;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friendsList);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_single_layout,FriendsViewHolder.class,
                mFriendsDatabase

        ) {
            @Override
            protected void populateViewHolder(FriendsViewHolder viewHolder, Friends model, int position) {

                viewHolder.setDate(model.getDate());
            }
        };

        mFriendsList.setAdapter(friendsRecyclerViewAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date){

            TextView userNameView = (TextView) mView.findViewById(R.id.userSingleStatus);
            userNameView.setText(date);
        }
    }
}
