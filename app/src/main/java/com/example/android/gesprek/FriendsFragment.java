package com.example.android.gesprek;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);


        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);


        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = mFriendsDatabase.orderByChild("date");



        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query, Friends.class)
                        .build();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {


            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, final int position, @NonNull Friends model) {


       holder.setDate(model.getDate());
       final String list_user_id = getRef(position).getKey();
       mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               final String userName = dataSnapshot.child("name").getValue().toString();
               String userThumb = dataSnapshot.child("thumb_image").getValue().toString();


               holder.setName(userName);
               holder.setUserImage(userThumb, getContext());
               if (dataSnapshot.hasChild("online")) {
                   String userOnline= dataSnapshot.child("online").getValue().toString();
                   holder.setUserOnline(userOnline);
               }
               holder.mView.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       CharSequence options[]= new CharSequence[]{"Open Profile","Send Message"};
                       AlertDialog.Builder builder =new AlertDialog.Builder(getContext());
                       builder.setTitle("Select Options");
                       builder.setItems(options, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int i) {
                               if(i==0)
                               {
                                   Intent profileinte= new Intent(getContext(),ProfileActivity.class);
                                   profileinte.putExtra("user_id",list_user_id);
                                   startActivity(profileinte);
                               }
                            if(i==1){
                                Log.d("archana",list_user_id);
                                Intent chatinte= new Intent(getContext(),ChatActivity.class);
                                chatinte.putExtra("user_id",list_user_id);
                                chatinte.putExtra("user_name",userName);
                                startActivity(chatinte);

                            }

                           }
                       });
                   builder.show();
                   }
               });
           }
           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });


            }

            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new FriendsViewHolder(view);
            }
        };

        mFriendsList.setAdapter(adapter);
        adapter.startListening();



    }

}




