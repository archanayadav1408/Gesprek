package com.example.android.gesprek;



import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by AkshayeJH on 24/07/17.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private FirebaseAuth mAuth;
    private List<messages> mMessageList;
    private DatabaseReference mUserDatabase,msg;
    private Context mContext;





    public MessageAdapter(List<messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_message_single_layout ,parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public LinearLayout message;
       public TextView displayName;
        public ImageView messageImage;
  public TextView timeText;
        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            mAuth=FirebaseAuth.getInstance();
            //displayName = (TextView) view.findViewById(R.id.name_text_layout);
            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);
            //timeText =(TextView)view.findViewById(R.id.time_text_layout);


        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {
         String current_User_Id= mAuth.getCurrentUser().getUid();
        messages c = mMessageList.get(i);

        String from_user = c.getFrom();
        String message_type = c.getType();
        
       if(from_user.equals(current_User_Id)){



            viewHolder.messageText.setBackgroundResource(R.drawable.usermes);
            viewHolder.messageText.setTextColor(Color.WHITE);

        }
        else
        {
            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
            viewHolder.messageText.setTextColor(Color.WHITE);
        }
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        msg= FirebaseDatabase.getInstance().getReference().child("messages").child(from_user).child(current_User_Id);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String image = dataSnapshot.child("thumb_image").getValue().toString();





                Picasso.get().load(image)
                        .placeholder(R.drawable.noprofile).into(viewHolder.profileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
       // Log.d("time",from_user);
        //GetTimeAgo getTimeAgo = new GetTimeAgo();
        //String lastSeenTime;

        //lastSeenTime = getTimeAgo.getTimeAgo(c.getTime(),mContext.getApplicationContext());

       // viewHolder.timeText.setText(c.getTime()+" ");

        if(message_type.equals("text")) {

            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageImage.setVisibility(View.INVISIBLE);


        } else {

            viewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.get().load(c.getMessage())
                    .placeholder(R.drawable.noprofile).into(viewHolder.messageImage);

        }

    }







    @Override
    public int getItemCount() {
        return mMessageList.size();
    }






}