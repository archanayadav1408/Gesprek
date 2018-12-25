package com.example.android.gesprek;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersViewHolder extends RecyclerView.ViewHolder{
    View mView;


    public UsersViewHolder(View viewe) {
        super(viewe);

        mView = viewe;

    }
    public void setDisplayName(String name){

        TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
        userNameView.setText(name);

    }

    public void setUserStatus(String status){

        TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
        userStatusView.setText(status);
        TextView textView = (TextView)mView.findViewById(R.id.chat_time);
        textView.setVisibility(View.INVISIBLE);
        ImageView imageView =(ImageView)mView.findViewById(R.id.chat_seen);
        imageView.setVisibility(View.INVISIBLE);

    }

    public void setUserImage(final String thumb_image, Context ctx){

        final CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);

        Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.noprofile).into(userImageView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {

                Picasso.get().load(thumb_image).placeholder(R.drawable.noprofile).into(userImageView);
            }
        });

}


}

