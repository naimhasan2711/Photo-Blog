package com.naimsplanet.photoblog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<Blog> blog_list;
    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private FirebaseFirestoreSettings settings;

    public BlogRecyclerAdapter(List<Blog> blog_list) {
        this.blog_list = blog_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final String blogPostId = blog_list.get(position).BlogPostId;
        final String currentUserId = mAuth.getCurrentUser().getUid();
        String description_data = blog_list.get(position).getDesc();
        holder.setDescriptionText(description_data);
        String blog_post_image = blog_list.get(position).getImage_url();
        String blog_post_thumb = blog_list.get(position).getImage_url();
        holder.setBlogImage(blog_post_image, blog_post_thumb);
        String locationText = blog_list.get(position).getLocation();
        holder.setBlogLocation(locationText);


        String user_id = blog_list.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String username = task.getResult().getString("name");
                    String userimage = task.getResult().getString("image");

                    holder.setUserData(username, userimage);
                } else {

                }
            }
        });

        try {
            long millisecond = blog_list.get(position).getTimestamp().getTime();
            //String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
            String dateString = DateFormat.format("dd/MM/yyyy", new Date(millisecond)).toString();
            holder.setTime(dateString);
        } catch (Exception e) {
            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //get like count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e == null) {
                    if (!documentSnapshots.isEmpty()) {
                        int count = documentSnapshots.size();
                        holder.updateLikeCount(count);

                    } else {
                        holder.updateLikeCount(0);
                    }
                }
            }
        });

        //get likes

        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e == null) {
                    if (documentSnapshot.exists()) {
                        holder.blogLikeButton.setImageDrawable(context.getDrawable(R.mipmap.like_button_accent));
                        //Log.e("MyTag", "Firebase exception", e);
                    } else {
                        holder.blogLikeButton.setImageDrawable(context.getDrawable(R.mipmap.like_button_gray));
                        //Log.e("MyTag", e.toString());
                    }
                }
            }
        });


        //blog like button
        holder.blogLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()) {
                            Map<String, Object> likeMap = new HashMap<>();
                            likeMap.put("timeStamp", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likeMap);

                        } else {
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();
                        }
                    }
                });

            }
        });

        holder.blogCommnetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentIntent = new Intent(context, CommentActivity.class);
                commentIntent.putExtra("blog_post_id", blogPostId);
                context.startActivity(commentIntent);
            }
        });

        //comment_counter
        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e == null) {
                    if (!documentSnapshots.isEmpty()) {
                        int comment_size = documentSnapshots.size();
                        holder.updateCommentCount(comment_size);
                    } else {
                        holder.updateCommentCount(0);
                    }
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView descriptionView;
        private ImageView blogImage;
        private TextView blogDate;
        private TextView userName;
        private CircleImageView userImage;
        private ImageView blogLikeButton;
        private TextView blogLikeCounter;
        private ImageView blogCommnetButton;
        private TextView blogCommentCounter;
        private TextView blogLocation;


        private ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            blogLikeButton = mView.findViewById(R.id.blog_like_button);
            blogCommnetButton = mView.findViewById(R.id.comment_btn);


        }

        private void setDescriptionText(String desc) {
            descriptionView = mView.findViewById(R.id.blog_desc);
            descriptionView.setText(desc);
        }


        private void setBlogImage(String downloadUrl, String thumbUrl) {
            blogImage = mView.findViewById(R.id.blog_image);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUrl).thumbnail(
                    Glide.with(context).load(thumbUrl)
            ).into(blogImage);
        }

        private void setTime(String date) {
            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);
        }

        private void setUserData(String name, String image) {
            userName = mView.findViewById(R.id.blog_user_name);
            userImage = mView.findViewById(R.id.blog_user_image);
            userName.setText(name);
            RequestOptions placeHolder = new RequestOptions();
            placeHolder.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeHolder).load(image).into(userImage);
        }

        private void updateLikeCount(int count) {
            blogLikeCounter = mView.findViewById(R.id.blog_like_count);
            blogLikeCounter.setText(String.valueOf(count));
        }

        private void updateCommentCount(int count) {
            blogCommentCounter = mView.findViewById(R.id.blog_comment_count);
            blogCommentCounter.setText(String.valueOf(count));
        }

        private void setBlogLocation(String location) {
            blogLocation = mView.findViewById(R.id.blog_post_location_text);
            blogLocation.setText(location);
        }
    }
}
