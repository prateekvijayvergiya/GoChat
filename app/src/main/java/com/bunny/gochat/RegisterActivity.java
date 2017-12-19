package com.bunny.gochat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mName;
    private TextInputLayout mEmail;
    private TextInputLayout mPass;
    private Button mBtn;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName = (TextInputLayout) findViewById(R.id.regName);
        mEmail = (TextInputLayout) findViewById(R.id.regEmail);
        mPass = (TextInputLayout) findViewById(R.id.regPass);
        mBtn = (Button) findViewById(R.id.regBtn);
        mAuth = FirebaseAuth.getInstance();

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

                registerUser(name,email,password);
            }
        });

    }

    private void registerUser(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(RegisterActivity.this, "Authentication Done.", Toast.LENGTH_SHORT).show();
                            Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                            startActivity(mainIntent);
                            finish();
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "createUserWithEmail:success");
                            // FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }
}
