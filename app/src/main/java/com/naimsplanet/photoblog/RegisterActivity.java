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

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    private EditText mRegisterEmailText;
    private EditText mRegisterPasswordText;
    private EditText mRegisterConfirmPasswordText;
    private Button mRegisterButton;
    private Button mAlreadyHaveAccountButton;
    private FirebaseAuth mAuth;
    private ProgressBar mRegisterProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mRegisterEmailText = findViewById(R.id.register_email_text);
        mRegisterPasswordText = findViewById(R.id.register_password_text);
        mRegisterConfirmPasswordText = findViewById(R.id.register_confirm_password_text);
        mRegisterButton = findViewById(R.id.register_button);
        mAlreadyHaveAccountButton = findViewById(R.id.already_have_account_button);
        mAuth = FirebaseAuth.getInstance();
        mRegisterProgressBar = findViewById(R.id.registerProgressBar);


        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerAccount();
            }
        });

        mAlreadyHaveAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sentToLogin();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            sentToMain();
        }
    }

    private void registerAccount() {
        String email = mRegisterEmailText.getText().toString();
        String password = mRegisterPasswordText.getText().toString();
        String confirmPassword = mRegisterConfirmPasswordText.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)) {
            if (password.equals(confirmPassword)) {
                mRegisterProgressBar.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sentToSetup();
                        } else {
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, "Error : " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                        mRegisterProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                Toast.makeText(this, "Confirm Password Filed Doesn't Match!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sentToMain() {
        Intent registerIntent = new Intent(RegisterActivity.this, HomeActivity.class);
        startActivity(registerIntent);
        finish();
    }

    private void sentToLogin() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void sentToSetup() {
        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        startActivity(setupIntent);
        finish();
    }
}
