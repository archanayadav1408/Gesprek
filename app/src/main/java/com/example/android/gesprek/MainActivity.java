package com.example.android.gesprek;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private DatabaseReference mUserRef;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Gesprek");

        if (mAuth.getCurrentUser() != null) {


            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }

        mViewPager = (ViewPager) findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);


    }

    @Override
    public void onStart() {
        super.onStart();
        String s="dc";
        Log.d("hjcd",s);
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {

            Intent main_intent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(main_intent);
            finish();

        } else {

            mUserRef.child("online").setValue("true");

        }



    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_btn) {

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

            FirebaseAuth.getInstance().signOut();

            Intent main_intent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(main_intent);
            finish();
        }
        if(item.getItemId()==R.id.main_settings_btn)
        {
            Intent newIntent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(newIntent);
        }
        if(item.getItemId()==R.id.main_all_btn)
        {
            Intent newIntent = new Intent(MainActivity.this,UsersActivity.class);
            startActivity(newIntent);
        }
        return true;
    }



}
