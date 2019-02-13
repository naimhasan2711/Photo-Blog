package com.naimsplanet.photoblog;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView mBlog_list_view;
    private List<Blog> blog_list;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private FirebaseAuth mAuth;
    private DocumentSnapshot lastVisible;
    private Boolean isFirtPageFirstLoad = true;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        blog_list = new ArrayList<>();
        mBlog_list_view = view.findViewById(R.id.blog_list_view);
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        mBlog_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        mBlog_list_view.setAdapter(blogRecyclerAdapter);
        mBlog_list_view.setHasFixedSize(true);
        mAuth = FirebaseAuth.getInstance();


        if (mAuth.getCurrentUser() != null ) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            mBlog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean bottomPost = !recyclerView.canScrollVertically(1);

                    if (bottomPost) {
                        loadMorePost();
                    }
                }
            });
            Query firstQuery = firebaseFirestore.collection("Posts").
                    orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(3);

            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (documentSnapshots != null) {

                        if (isFirtPageFirstLoad) {
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                            blog_list.clear();
                        }

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blogPostId = doc.getDocument().getId();
                                Blog blog = doc.getDocument().toObject(Blog.class).withID(blogPostId);
                                if (isFirtPageFirstLoad) {
                                    blog_list.add(blog);
                                } else {
                                    blog_list.add(0, blog);
                                }

                                blogRecyclerAdapter.notifyDataSetChanged();
                            }
                        }

                        isFirtPageFirstLoad = false;
                    }
                }
            });
        }

        return view;
    }

    private void loadMorePost() {

        if (mAuth.getCurrentUser() != null) {
            Query nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blogPostId = doc.getDocument().getId();
                                Blog blog = doc.getDocument().toObject(Blog.class).withID(blogPostId);
                                blog_list.add(blog);
                                blogRecyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            });
        }
    }


}
