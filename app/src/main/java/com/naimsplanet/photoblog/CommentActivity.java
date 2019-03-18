package com.naimsplanet.photoblog;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView comment_list;
    private EditText commnetText;
    private ImageView commnetButton;
    private String blog_post_id;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String current_user_id;
    private List<Comment> commentList;
    private CommentRecyclerAdapter commentRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        comment_list = findViewById(R.id.comment_list);
        commnetText = findViewById(R.id.comment_text);
        commnetButton = findViewById(R.id.send_commnet_button);

        mToolbar = findViewById(R.id.comment_toolbar);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        blog_post_id = getIntent().getStringExtra("blog_post_id");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Comments");

        //firebase recycler
        commentList = new ArrayList<>();
        commentRecyclerAdapter = new CommentRecyclerAdapter(commentList);
        comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(this));
        comment_list.setAdapter(commentRecyclerAdapter);


        firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
                .addSnapshotListener(CommentActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (!documentSnapshots.isEmpty()) {
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String commentId = doc.getDocument().getId();
                                    Comment comment = doc.getDocument().toObject(Comment.class);
                                    commentList.add(comment);
                                    commentRecyclerAdapter.notifyDataSetChanged();

                                }
                            }
                        }
                    }
                });

        commnetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment_message = commnetText.getText().toString();
                if (!TextUtils.isEmpty(comment_message)) {

                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("message", comment_message);
                    commentMap.put("user_id", current_user_id);
                    commentMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            if (!task.isSuccessful()) {
                                Toast.makeText(CommentActivity.this, "Error posting commnet : " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                            } else {
                                commnetText.setText("");
                            }

                        }
                    });

                }
            }
        });

    }
}
