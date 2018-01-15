package com.bunny.gochat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class UsersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView usersList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        toolbar = (Toolbar) findViewById(R.id.usersAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Users List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        usersList = (RecyclerView) findViewById(R.id.usersList);
        usersList.setHasFixedSize(true);
        usersList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
               Users.class,R.layout.users_single_layout,UsersViewHolder.class,databaseReference
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setUserImage(model.getThumbImage(),getApplicationContext());

                final String userId = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("userId",userId);
                        startActivity(profileIntent );
                    }
                });
            }
        };

        usersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.userSingleName);
            userNameView.setText(name);
        }

        public void setStatus(String status) {

            TextView userStatusView = (TextView) mView.findViewById(R.id.userSingleStatus);
            userStatusView.setText(status);
        }

        public void setUserImage(String thumbImage, Context ctx) {

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.userSingleImage);
            Picasso.with(ctx).load(thumbImage).placeholder(R.drawable.profile_icon).into(userImageView);
        }
    }


}
