package com.naimsplanet.photoblog;

import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

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
    private Bitmap compressedImageFile;

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

        /*
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

        */

        final String desc = mBlogPostDescription.getText().toString();

        if (!TextUtils.isEmpty(desc) && mPostUri != null) {

            mNewPostProgressBar.setVisibility(View.VISIBLE);

            final String randomName = UUID.randomUUID().toString();

            // PHOTO UPLOAD
            File newImageFile = new File(mPostUri.getPath());
            try {

                compressedImageFile = new Compressor(NewpostActivity.this)
                        .setMaxHeight(720)
                        .setMaxWidth(720)
                        .setQuality(50)
                        .compressToBitmap(newImageFile);

            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            // PHOTO UPLOAD

            UploadTask filePath = mStorageReference.child("post_images").child(randomName + ".jpg").putBytes(imageData);
            filePath.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                    final String downloadUri = task.getResult().getDownloadUrl().toString();

                    if (task.isSuccessful()) {

                        File newThumbFile = new File(mPostUri.getPath());
                        try {

                            compressedImageFile = new Compressor(NewpostActivity.this)
                                    .setMaxHeight(100)
                                    .setMaxWidth(100)
                                    .setQuality(1)
                                    .compressToBitmap(newThumbFile);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] thumbData = baos.toByteArray();

                        UploadTask uploadTask = mStorageReference.child("post_images/thumbs")
                                .child(randomName + ".jpg").putBytes(thumbData);

                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                String downloadthumbUri = taskSnapshot.getDownloadUrl().toString();

                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("image_url", downloadUri);
                                postMap.put("image_thumb", downloadthumbUri);
                                postMap.put("desc", desc);
                                postMap.put("user_id", current_user_id);
                                //postMap.put("user_name",mAuth.getCurrentUser().getDisplayName());
                                postMap.put("timestamp", FieldValue.serverTimestamp());

                                mFirebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {

                                        if (task.isSuccessful()) {

                                            Toast.makeText(NewpostActivity.this, "Post was added", Toast.LENGTH_LONG).show();
                                            sentToMain();

                                        } else {


                                        }

                                        mNewPostProgressBar.setVisibility(View.INVISIBLE);

                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                //Error handling

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
