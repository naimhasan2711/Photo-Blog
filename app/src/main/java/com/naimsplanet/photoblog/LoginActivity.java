package com.naimsplanet.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmailText;
    private EditText mPasswordText;
    private Button mLoginButton;
    private Button mNeedAccountButton;
    private ProgressBar mProgressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mEmailText = findViewById(R.id.reg_email_text);
        mPasswordText = findViewById(R.id.login_password_text);

        mLoginButton = findViewById(R.id.login_button);
        mNeedAccountButton = findViewById(R.id.need_account_button);

        mProgressBar = findViewById(R.id.loginProgressBar);

        loginAccount();

        needAccount();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            sentToMain();
        }
    }


    private void loginAccount() {
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String loginEmail = mEmailText.getText().toString();
                String loginPassword = mPasswordText.getText().toString();

                if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword)) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                sentToMain();

                            } else {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error : " + errorMessage, Toast.LENGTH_SHORT).show();

                            }

                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }

            }
        });
    }

    private void needAccount() {
        mNeedAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sentToRegister();
            }
        });
    }

    private void sentToMain() {
        Intent loginIntent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void sentToRegister() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
        finish();
    }
}
