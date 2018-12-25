package com.example.android.gesprek;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.graphics.Color.BLUE;
import static android.graphics.Color.WHITE;


public class RequestsFragment extends Fragment {
   private DatabaseReference databaseReference,getUserdatabase,friend_request;
    private DatabaseReference userdatabase;
    private RecyclerView recyclerView;
  private String  current_userid;
    private  FirebaseAuth firebaseAuth;
    private View view;
    private Boolean flag = false;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       view = inflater.inflate(R.layout.fragment_requests, container, false);


        recyclerView = (RecyclerView)view.findViewById(R.id.request_recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);


        //Setting Firease
        firebaseAuth = FirebaseAuth.getInstance();
        current_userid = firebaseAuth.getCurrentUser().getUid().toString();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(current_userid);
        databaseReference.keepSynced(true);
        getUserdatabase =  FirebaseDatabase.getInstance().getReference().child("Users");
        getUserdatabase.keepSynced(true);
        return  view;


    }
    public void onStart() {
        super.onStart();

        Query query = databaseReference.limitToLast(50);


        FirebaseRecyclerOptions<requests> options =
                new FirebaseRecyclerOptions.Builder<requests>()
                        .setQuery(query, requests.class)
                        .build();

        FirebaseRecyclerAdapter<requests,RequestViewHolder> adapter = new FirebaseRecyclerAdapter<requests, RequestViewHolder>(options) {
            @Override
            public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.friend_request_single_show, parent, false);



                return new RequestViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull requests model) {

                final String user_id = getRef(position).getKey().toString();


                Log.d("copppp",user_id);

                getUserdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                      String  user_name = dataSnapshot.child(user_id).child("name").getValue().toString();
                        String img = dataSnapshot.child(user_id).child("image").getValue().toString();
                        holder.setImage(img);
                        holder.setName(user_name);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                Query friendrequest =databaseReference.child(user_id);

            friendrequest.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String  type = dataSnapshot.child("request_type").getValue().toString();
                        if(type.equals("received"))
                        {
                            holder.setButton("received",user_id);

                        }

                        else if(type.equals("sent"))
                        {
                            holder.setButton("sent",user_id);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });





            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    public  class RequestViewHolder extends  RecyclerView.ViewHolder
    {

        View mView;
        public RequestViewHolder(View itemView) {
            super(itemView);
            this.mView=itemView;
        }

        public void setImage(final String image)
        {
            final CircleImageView img = (CircleImageView) mView.findViewById(R.id.single_profile_friend_request);


            Picasso.get().load(image).placeholder(R.drawable.noprofile).networkPolicy(NetworkPolicy.OFFLINE)
                    .into(img, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception ex) {
                            Picasso.get().load(image).placeholder(R.drawable.noprofile).resize(100,100).centerCrop().into(img); // this is use to
                        }
                    });
        }

        public  void  setName(String name)
        {
            TextView textView = (TextView)mView.findViewById(R.id.single_name_friend);
            textView.setText(name);
        }

        public  void  setButton(String type, final String send_user_id)
        {
            final Button accept = (Button)mView.findViewById(R.id.single_accept_request);
            Button reject = (Button)mView.findViewById(R.id.single_reject_request);


            if(type.equals("sent"))
            {
                accept.setText("Cancel Sent Request");
                accept.setBackgroundColor(BLUE);
                accept.setTextColor(WHITE);
                reject.setVisibility(View.GONE);
                accept.setPadding(10,10,10,10);

            }

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(accept.getText().toString().equals("Cancel Sent Request"))
                    {
                        friend_request = FirebaseDatabase.getInstance().getReference().child("Friend_req");
                        friend_request.child(current_userid).child(send_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful())
                                {
                                    friend_request.child(send_user_id).child(current_userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(getContext()," Friend Request Cancelled ",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                    else
                    {
                        final String date = java.text.DateFormat.getDateTimeInstance().format(new Date());

                        String  UidRoot  = "Friends" + "/" + send_user_id + "/" + current_userid;
                        String  UserRoot = "Friends" + "/" + current_userid + "/" + send_user_id;
                        DatabaseReference mRoot = FirebaseDatabase.getInstance().getReference();

                        Map map = new HashMap();
                        map.put("date",date);

                        mRoot.child(UidRoot).updateChildren(map);
                        mRoot.child(UserRoot).updateChildren(map);



                        // Sending Images To DataBase

                        //Toast.makeText(getContext(), "m  coming dude "+send_user_id, Toast.LENGTH_SHORT).show();
                        userdatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_userid);
                        userdatabase.child(send_user_id).child("date").setValue(date).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    friend_request = FirebaseDatabase.getInstance().getReference().child("Friend_req");
                                    friend_request.child(current_userid).child(send_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                friend_request.child(send_user_id).child(current_userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {


                                                        userdatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(send_user_id);
                                                        userdatabase.child(current_userid).child("date").setValue(date);
                                                        Toast.makeText(getContext(), "Friend Request Accepted", Toast.LENGTH_SHORT).show();


                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }

                }
            });

            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    friend_request = FirebaseDatabase.getInstance().getReference().child("Friend_req");
                    friend_request.child(current_userid).child(send_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful())
                            {
                                friend_request.child(send_user_id).child(current_userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getContext()," Friend Request Deleted ",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });

                }
            });



        }
    }

}
