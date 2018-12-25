package com.example.android.gesprek;




import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mConvList = (RecyclerView) mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);


        // Inflate the layout for this fragment
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");
        FirebaseRecyclerOptions<Conv> options =
                new FirebaseRecyclerOptions.Builder<Conv>()
                        .setQuery(conversationQuery, Conv.class)
                        .build();

        FirebaseRecyclerAdapter<Conv,ConvViewHolder> adapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {


            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);
                return new ConvViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder convViewHolder, int i, @NonNull final Conv conv) {
                final String list_user_id = getRef(i).getKey();

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String data = dataSnapshot.child("message").getValue().toString();

                        convViewHolder.setMessage(data, conv.getSeen(),list_user_id);

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            convViewHolder.setUserOnline(userOnline);

                        }
                        convViewHolder.setTime(conv.getTimestamp());
                        convViewHolder.setName(userName);
                        convViewHolder.setUserImage(userThumb, getContext());

                        convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
                                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        String from=dataSnapshot.child("from").getValue().toString();
                                        Log.d("getFrom",from);
                                        if(!from.equals(mCurrent_user_id))
                                        {
                                            FirebaseDatabase.getInstance().getReference().child("Chat").child(from).child(mCurrent_user_id)
                                                    .child("seen").setValue(true);

                                            FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id).child(from)
                                                    .child("seen").setValue(true);

                                        }




                                    }

                                    @Override
                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });






                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        mConvList.setAdapter(adapter);
        adapter.startListening();

        }
    public  class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConvViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, boolean isSeen){

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(message);

            if(!isSeen){
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);

            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }

        }

        void setMessage(final String message, final Boolean seen, String list_user_id){
            mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
            Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

            lastMessageQuery.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                    String from=dataSnapshot.child("from").getValue().toString();
                    String type=dataSnapshot.child("type").getValue().toString();
                    TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
                    userStatusView.setText(message);
                    Log.d("djk",from);

                    ImageView imageView =(ImageView)mView.findViewById(R.id.chat_seen);
                    if(type.equals("image") && from.equals(mCurrent_user_id)  )
                    {
                        userStatusView.setText("  Photo");
                        setSeen(seen);
                    }

                    if(type.equals("text") && from.equals(mCurrent_user_id))
                    {
                        userStatusView.setText(message);
                        imageView.setImageResource(R.drawable.seen);
                        setSeen(seen);
                    }

                    if(type.equals("image") && !from.equals(mCurrent_user_id)  )
                    {
                        userStatusView.setText("  Photo");

                        if(!seen)
                        {
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.comemsg));

                        }
                        else
                            imageView.setVisibility(View.GONE);

                    }

                    if(type.equals("text") && !from.equals(mCurrent_user_id))
                    {
                        userStatusView.setText(message);
                        if(!seen)
                        {
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.comemsg));
                            Log.d("lastmessage",message);
                            userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);

                        }
                        else
                            imageView.setVisibility(View.GONE);
                        userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
                    }



                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });






        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.noprofile).into(userImageView);

        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }
       public  void  setTime(Long timestamp)
        {

            String time =  DateUtils.formatDateTime( getContext(), timestamp, DateUtils.FORMAT_SHOW_TIME);
            TextView textView = (TextView)mView.findViewById(R.id.chat_time);
            textView.setText(time);

        }
        public  void  setSeen(boolean isSeen)
        {
            ImageView imageView =(ImageView)mView.findViewById(R.id.chat_seen);

            if(!isSeen)
            {
                imageView.setImageResource(R.drawable.unseen);
            }
            else
                imageView.setImageResource(R.drawable.seen);


        }





    }








}
