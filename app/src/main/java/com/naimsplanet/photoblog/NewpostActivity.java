package com.naimsplanet.photoblog;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class NewpostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageView mBlogpostImage;
    private EditText mBlogPostDescription;
    private Button mPostBlogButton;
    private Uri mPostUri = null;
    private ProgressBar mNewPostProgressBar;
    private StorageReference mStorageReference;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseAuth mAuth;
    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpost);

        mToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBlogpostImage = findViewById(R.id.blogPost_image);
        mBlogPostDescription = findViewById(R.id.blogPost_description);
        mPostBlogButton = findViewById(R.id.post_blog_button);
        mNewPostProgressBar = findViewById(R.id.newPostProgressbar);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        current_user_id = mAuth.getCurrentUser().getUid();

        mBlogpostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFromGallery();
            }
        });

        mPostBlogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postBlog();
            }
        });


    }

    private void selectImageFromGallery() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512, 512)
                .setAspectRatio(2, 1)
                .start(NewpostActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mPostUri = result.getUri();
                mBlogpostImage.setImageURI(mPostUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void postBlog() {
        final String postDescription = mBlogPostDescription.getText().toString();

        if (!TextUtils.isEmpty(postDescription) && mPostUri != null) {
            mNewPostProgressBar.setVisibility(View.VISIBLE);
            String randomNumber = FieldValue.serverTimestamp().toString();
            StorageReference file_path = mStorageReference.child("post_image").child(randomNumber + ".jpg");
            file_path.putFile(mPostUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        String downloadUri = task.getResult().getDownloadUrl().toString();

                        Map<String, Object> postMap = new HashMap<>();
                        postMap.put("image_uri", downloadUri);
                        postMap.put("post_description", postDescription);
                        postMap.put("user_id", current_user_id);
                        postMap.put("timeStamp", FieldValue.serverTimestamp());

                        mFirebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(NewpostActivity.this, "Post Added Successfully!", Toast.LENGTH_SHORT).show();
                                    sentToMain();
                                } else {
                                    String error_message = task.getException().getMessage();
                                    Toast.makeText(NewpostActivity.this, "Error " + error_message, Toast.LENGTH_SHORT).show();
                                }
                                mNewPostProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });

                    } else {
                        mNewPostProgressBar.setVisibility(View.INVISIBLE);
                    }
                }
            });

        }

    }

    private void sentToMain() {
        Intent mainIntent = new Intent(NewpostActivity.this, HomeActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
