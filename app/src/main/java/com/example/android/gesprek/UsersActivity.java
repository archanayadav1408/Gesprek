package com.example.android.gesprek;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView mUsersList;

    private DatabaseReference mUsersDatabase;

    EditText search_text;
    String searching_friend;
    ImageView search_btn;

    private LinearLayoutManager mLayoutManager;
    private DatabaseReference mUserRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        search_text =(EditText)findViewById(R.id.search_user_text);
        search_btn = (ImageView)findViewById(R.id.search_button) ;


        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                searching_friend = s.toString();
                onStart();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searching_friend = s.toString();
                onStart();
            }

            @Override
            public void afterTextChanged(Editable s) {
                searching_friend = s.toString();
                onStart();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        Query query = mUsersDatabase.orderByChild("name")
                .startAt(searching_friend)
                .endAt(searching_friend + "\uf8ff")
                ;



        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, Users.class)
                        .build();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> adapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {


            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, final int position, @NonNull Users model) {

                holder.setDisplayName(model.getName());
                holder.setUserStatus(model.getStatus());
                holder.setUserImage(model.getThumb_image(), getApplicationContext());
                final String user_id = getRef(position).getKey();

              holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);

                    }
                });




            }

            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new UsersViewHolder(view);
            }
        };

     mUsersList.setAdapter(adapter);
        adapter.startListening();



    }


}