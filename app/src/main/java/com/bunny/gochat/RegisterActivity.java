package com.bunny.gochat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mName;
    private TextInputLayout mEmail;
    private TextInputLayout mPass;
    private Button mBtn;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ProgressDialog mRegProgress;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName = (TextInputLayout) findViewById(R.id.regName);
        mEmail = (TextInputLayout) findViewById(R.id.regEmail);
        mPass = (TextInputLayout) findViewById(R.id.regPass);
        mBtn = (Button) findViewById(R.id.regBtn);
        mAuth = FirebaseAuth.getInstance();
        mRegProgress = new ProgressDialog(this);

        mToolbar = (Toolbar) findViewById(R.id.registerToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBtn.setOnClickListener(    new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPass.getEditText().getText().toString();

                if (!TextUtils.isEmpty(name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Loading");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    registerUser(name,email,password);
                }

            }
        });

    }

    private void registerUser(final String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();

                            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String,String> userMap = new HashMap<>();
                            userMap.put("name",name);
                            userMap.put("status","Hi there I am using GoChat");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");
                            databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(RegisterActivity.this, "Authentication Done.", Toast.LENGTH_SHORT).show();
                                    mRegProgress.dismiss();
                                    Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            });

                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "createUserWithEmail:success");
                            // FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            mRegProgress.hide();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }
}
