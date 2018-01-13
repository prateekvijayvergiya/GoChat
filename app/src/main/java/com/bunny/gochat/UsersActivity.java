package com.bunny.gochat;

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
    }


}
