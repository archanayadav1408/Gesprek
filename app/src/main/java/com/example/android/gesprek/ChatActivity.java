package com.example.android.gesprek;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity {
    private String mChatUser;
    private Toolbar mChatToolbar;
    private DatabaseReference mRootRef;
    private TextView mTitleView;
    private DatabaseReference mUserRef;
private FirebaseAuth mAuth;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage,messageProfile;
    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private RecyclerView mMessagesList;
    private EditText mChatMessageView;
    private final List<messages> messagesList = new ArrayList<messages>();
    String mCurrentUserId;
   private LinearLayoutManager mLinearLayout;
   private  static final int TOTAL_ITEMS_TO_LOAD = 10;
   private  int mCurrentPage = 1;

   private MessageAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    //New Solution
    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";
    private static final int GALLERY_PICK = 1;

    // Storage Firebase
    private StorageReference mImageStorage;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();




        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mChatUser = getIntent().getStringExtra("user_id");
        String UserName = getIntent().getStringExtra("user_name");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        //getSupportActionBar().setTitle(UserName);


        LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflator.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        //--custom action bar----//
        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);
        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);

        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);

        mAdapter=new MessageAdapter(messagesList);
        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().getReference();


        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        mLinearLayout= new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);


        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mTitleView.setText(UserName);
        loadMessages();
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                if (online.equals("true")) {
                    mLastSeenView.setText("online");
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    mLastSeenView.setText(lastSeenTime);
                }
                Picasso.get().load(image)
                        .placeholder(R.drawable.noprofile).into(mProfileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUsermap = new HashMap();
                    chatUsermap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUsermap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUsermap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }

                        }
                    });


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


//-----send button---//

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                sendMessage();

            }


        });
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;
                loadMoreMessages();




            }
        });
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });





    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK) // Check There Is Everuthing Fine Or Not
        {
            Uri imguri = data.getData(); // Getting Selected Image In Uri Form

            CropImage.activity(imguri)

                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
        CropImage.ActivityResult result = CropImage.getActivityResult(data);

        if (resultCode == RESULT_OK) {
            Uri resultUri = result.getUri();

            File thumb_filePath = new File(resultUri.getPath());

            Bitmap thumb_bitmap = null;
            try {
                thumb_bitmap = new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(40)
                        .compressToBitmap(thumb_filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            final byte[] thumb_byte = baos.toByteArray();

            // Setting Progressing Bar To Load data
          final ProgressDialog  progressDialog = new ProgressDialog(ChatActivity.this);
            progressDialog.setTitle("Sending....");
            progressDialog.setMessage("Please Wait While Sending Your Picture");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();





            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();


            StorageReference filepath = mImageStorage.child("message_images").child( push_id + ".jpg");
  filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful())
                    {
                         String download_url = task.getResult().getDownloadUrl().toString();


                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mChatMessageView.setText("");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null){

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });

                        progressDialog.dismiss();


                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this,"Error In Uploading ",Toast.LENGTH_SHORT).show();

                    }

                }
            }); // Putting Data To FireBase


        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

            Exception error = result.getError();
        }
    }

    }
    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                messages message = dataSnapshot.getValue(messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){

                    messagesList.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;

                }


                if(itemPos == 1) {

                    mLastKey = messageKey;

                }


                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10, 0);

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

    private void loadMessages() {

        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserId).child(mChatUser);

      Query messageQuery = messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                messages message = dataSnapshot.getValue(messages.class);

                itemPos++;

               if(itemPos == 1){

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);

                mRefreshLayout.setRefreshing(false);

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





    private void sendMessage() {


            String message = mChatMessageView.getText().toString();

            if(!TextUtils.isEmpty(message)){
               //final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
                String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

                DatabaseReference user_message_push = mRootRef.child("messages")
                        .child(mCurrentUserId).child(mChatUser).push();

                String push_id = user_message_push.getKey();

                Map messageMap = new HashMap();
                messageMap.put("message", message);
                messageMap.put("seen", false);
                messageMap.put("type", "text");
                messageMap.put("time",ServerValue.TIMESTAMP);
                messageMap.put("from", mCurrentUserId);

                Map messageUserMap = new HashMap();
                messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                mChatMessageView.setText("");

                mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(false);
                mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

                mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
                mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if(databaseError != null){

                            Log.d("CHAT_LOG", databaseError.getMessage().toString());

                        }

                    }
                });

            }
    }
    @Override
    protected void onStart() {
        super.onStart();
        final int[] positionitem = new int[1];

        Query query = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);


        FirebaseRecyclerOptions<messages> options = new FirebaseRecyclerOptions.Builder<messages>()
                .setQuery(query,messages.class)
                .build();

        final FirebaseRecyclerAdapter<messages,MsgViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<messages, MsgViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final MsgViewHolder holder, int position, @NonNull final messages model) {

                positionitem[0] = position;
                String  id = model.getFrom().toString();
                String msg = model.getMessage().toString();
                final String type  = model.getType().toString();
                Boolean seen = model.getSeen().booleanValue();
                // Long time = model.getTimestamp().longValue();
                if(!id.equals(mCurrentUserId))  // if msg is received then fetching sender profile and message
                { mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String image = dataSnapshot.child("image").getValue().toString();
                            holder.setImg(image);
                            holder.setMsg(model.getMessage().toString(),"receive",type);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else {   // if message is sent
                    mRootRef.child("Users").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String image = dataSnapshot.child("image").getValue().toString();
                            holder.setImg(image);
                            holder.setMsg(model.getMessage().toString(),"send",type);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }

            }

            @Override
            public MsgViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.activity_message_single_layout, parent, false);

                return new MsgViewHolder(view);
            }
        };

        mMessagesList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mMessagesList.smoothScrollToPosition(firebaseRecyclerAdapter.getItemCount());
            }
        });


        firebaseRecyclerAdapter.notifyDataSetChanged();





    }









public static class MsgViewHolder extends RecyclerView.ViewHolder
{
    View view;
    Context c;


    public MsgViewHolder(View itemView) {
        super(itemView);
        this.view=itemView;
    }

    public void setMsg(final String msg , String msg_type , String type)
    {
        TextView textView = (TextView) view.findViewById(R.id.message_text_layout);
        final ImageView imageView = (ImageView)view.findViewById(R.id.message_image_layout);

        if((msg_type.equals("send") || msg_type.equals("receive")) && type.equals("image"))
        {



            textView.setVisibility(View.INVISIBLE);
            Log.d("archana",msg);

            Picasso.get().load(msg)
                    .placeholder(R.drawable.noprofile).into(imageView);

            Picasso.get().load(msg).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.noprofile).into(imageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(msg).placeholder(R.drawable.noprofile).into(imageView);
                }
            });

        }

        if(msg_type.equals("send") && type.equals("text") ) {

            textView.setGravity(View.FOCUS_RIGHT);
            textView.setText(msg);
            imageView.setVisibility(View.GONE);
            textView.setTextColor(Color.WHITE);
            textView.setBackgroundResource(R.drawable.usermes);
        }

        if (msg_type.equals("receive") && type.equals("text"))
        {
            textView.setText(msg);
            imageView.setVisibility(View.INVISIBLE);
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundResource(R.drawable.message_text_background);

        }



    }


    public  void setImg(final String img)
    {
        final CircleImageView circleImageView = (CircleImageView)view.findViewById(R.id.message_profile_layout);
        //Picasso.with(c).load(img).into(circleImageView);
        Picasso.get().load(img).networkPolicy(NetworkPolicy.OFFLINE) .into(circleImageView);
    }
    public View getView() {
        return view;
    }
}
}





