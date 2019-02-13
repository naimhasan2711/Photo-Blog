package com.naimsplanet.photoblog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private Toolbar mSetupToolbar;
    private FirebaseAuth mAuth;
    private CircleImageView mCircleImageView;
    private Uri mMainImageUri = null;
    private EditText mSetupName;
    private Button mSetupButton;
    private StorageReference mStorageRef;
    private ProgressBar mSetupProgressBar;
    private FirebaseFirestore mFirebaseFirestore;
    private String user_id;
    private boolean isChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mSetupToolbar = findViewById(R.id.setup_toolbar);
        mCircleImageView = findViewById(R.id.profile_image);
        mAuth = FirebaseAuth.getInstance();
        mSetupName = findViewById(R.id.setupName);
        mSetupButton = findViewById(R.id.setupButton);
        mSetupProgressBar = findViewById(R.id.setup_progressbar);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        user_id = mAuth.getCurrentUser().getUid();

        mSetupProgressBar.setVisibility(View.VISIBLE);
        mSetupButton.setEnabled(false);

        setSupportActionBar(mSetupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        mFirebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mMainImageUri = Uri.parse(image);

                        mSetupName.setText(name);
                        mSetupButton.setVisibility(View.GONE);
                        mSetupName.setCursorVisible(false);
                        mSetupName.setLongClickable(false);
                        mSetupName.setClickable(false);
                        mSetupName.setFocusable(false);
                        mSetupName.setBackgroundResource(android.R.color.transparent);
                        RequestOptions placeHolderRequest = new RequestOptions();
                        placeHolderRequest.placeholder(R.drawable.profile_image);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeHolderRequest).load(image).into(mCircleImageView);
                    } else {
                        Toast.makeText(SetupActivity.this, "Data doesn't exists!!! ", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    String read_error = task.getException().toString();
                    Toast.makeText(SetupActivity.this, "Firestore error " + read_error, Toast.LENGTH_SHORT).show();
                }
                mSetupProgressBar.setVisibility(View.INVISIBLE);
                mSetupButton.setEnabled(true);
            }
        });

        mCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(SetupActivity.this, "Permission Denined", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        BringImagePicker();
                    }
                } else {
                    BringImagePicker();
                }
            }
        });

        mSetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String user_name = mSetupName.getText().toString();
                if (!TextUtils.isEmpty(user_name) && mMainImageUri != null) {
                    mSetupProgressBar.setVisibility(View.VISIBLE);
                    if (isChanged) {
                        user_id = mAuth.getCurrentUser().getUid();
                        StorageReference image_path = mStorageRef.child("profile_image").child(user_id + ".jpg");
                        image_path.putFile(mMainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    uploadToFirestore(task, user_name);
                                } else {
                                    String image_error = task.getException().toString();
                                    Toast.makeText(SetupActivity.this, "Error : " + image_error, Toast.LENGTH_SHORT).show();
                                    mSetupProgressBar.setVisibility(View.INVISIBLE);
                                }

                            }
                        });
                    } else {
                        uploadToFirestore(null, user_name);
                    }
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mMainImageUri = result.getUri();
                mCircleImageView.setImageURI(mMainImageUri);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);
    }

    private void sentToMain() {
        Intent mainIntent = new Intent(SetupActivity.this, HomeActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void uploadToFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String user_name) {
        Uri download_uri;
        if (task != null) {
            download_uri = task.getResult().getDownloadUrl();
        } else {
            download_uri = mMainImageUri;
        }

        Map<String, String> user = new HashMap<>();
        user.put("name", user_name);
        user.put("image", download_uri.toString());

        mFirebaseFirestore.collection("Users").document(user_id).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SetupActivity.this, "The user Settings are uploaded!", Toast.LENGTH_SHORT).show();
                    sentToMain();
                } else {
                    String firestore_error = task.getException().toString();
                    Toast.makeText(SetupActivity.this, "Error " + firestore_error, Toast.LENGTH_SHORT).show();
                }
                mSetupProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

}
