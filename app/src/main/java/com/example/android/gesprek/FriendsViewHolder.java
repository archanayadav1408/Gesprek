package com.example.android.gesprek;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

class FriendsViewHolder extends RecyclerView.ViewHolder {


    View mView;

    public FriendsViewHolder(View itemView) {
        super(itemView);

        mView = itemView;

    }

    public void setDate(String date){

        TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
        userStatusView.setText(date);

    }

    public void setName(String name){

        TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
        userNameView.setText(name);
        TextView textView = (TextView)mView.findViewById(R.id.chat_time);
        textView.setVisibility(View.INVISIBLE);
        ImageView imageView =(ImageView)mView.findViewById(R.id.chat_seen);
        imageView.setVisibility(View.INVISIBLE);

    }

    public void setUserImage(String thumb_image, Context ctx){

        CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
        Picasso.get().load(thumb_image).placeholder(R.drawable.noprofile).into(userImageView);

    }

    public void setUserOnline(String online_status)
    {
        ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);

        if(online_status.equals("true")){

            userOnlineView.setVisibility(View.VISIBLE);

        } else {

            userOnlineView.setVisibility(View.INVISIBLE);

        }
    }

}

