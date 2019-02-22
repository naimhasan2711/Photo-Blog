package com.naimsplanet.photoblog;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment {

    private CircleImageView account_profile_image;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private Uri imgaeUri=null;
    private TextView account_display_name;
    private TextView account_country_name;
    private TextView account_display_name_2;
    private TextView account_email;


    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_account, container, false);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        account_profile_image = view.findViewById(R.id.account_profile_image);
        account_display_name = view.findViewById(R.id.account_display_name);
        account_country_name = view.findViewById(R.id.account_country_name);
        //account_display_name_2 = view.findViewById(R.id.account_country_name_2);
        account_email = view.findViewById(R.id.account_email_address);

        user_id = mAuth.getCurrentUser().getUid();

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        String country = task.getResult().getString("country");

                        imgaeUri = Uri.parse(image);

                        account_display_name.setText(name);
                        account_country_name.setText(country);
                        //account_display_name_2.setText(name);
                        account_email.setText(mAuth.getCurrentUser().getEmail().toString());


                        RequestOptions placeHolderRequest = new RequestOptions();
                        placeHolderRequest.placeholder(R.drawable.image_placeholder);
                        Glide.with(getActivity()).setDefaultRequestOptions(placeHolderRequest).load(imgaeUri).into(account_profile_image);
                    }else{
                        Toast.makeText(getActivity(), "Data doesn't exists!!! ", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    String read_error = task.getException().toString();
                    Toast.makeText(getActivity(), "Firestore error " + read_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Inflate the layout for this fragment
        return view;

    }

}

