package com.example.android.gesprek;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class PasswordReset extends AppCompatActivity {

    android.support.v7.widget.Toolbar toolbar;
    Button send;
    EditText email;
    ProgressDialog progressDialog1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        // Setting Toolbar
        toolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.reset_password_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reset Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        send = (Button)findViewById(R.id.send_password_rest);
        email = (EditText)findViewById(R.id.email_to_sent_reset_password);


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(email.getText().toString().trim()))
                {
                    email.setError("Enter Your Email Please");
                }

                else
                {
                    progressDialog1 = new ProgressDialog(PasswordReset.this);
                    progressDialog1.setTitle("Sending Password Reset Link");
                    progressDialog1.setMessage("after this Check Your Email!");
                    progressDialog1.show();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String emailAddress = email.getText().toString().trim();

                    auth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog1.dismiss();
                                        Toast.makeText(PasswordReset.this,"Password Link Has Been Sent To  Your Email",Toast.LENGTH_LONG).show();
                                        Intent i = new Intent(PasswordReset.this,LoginActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // fOR User Not Going To Previous Page
                                        startActivity(i);
                                        finish();
                                    }
                                    else
                                    {
                                        progressDialog1.dismiss();
                                        Toast.makeText(PasswordReset.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }

            }
        });
    }
}