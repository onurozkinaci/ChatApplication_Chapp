package com.example.chatapp_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class ChatActivity extends AppCompatActivity {
TabLayout tabLayout; //fragmentlari barindiran nesne
TabItem chat,status,call;
ViewPager viewPager;
PagerAdapter pagerAdapter; //created to control switch between fragments
androidx.appcompat.widget.Toolbar toolBar; //will be used instead of ActionBar

FirebaseAuth firebaseAuth;
FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        tabLayout=findViewById(R.id.include); //activity_chat icerisinde olusturulan TabLayout'un id'si="include"
        chat=findViewById(R.id.chat); //id from created TabItem on activity_chat.xml
        status=findViewById(R.id.status);
        call=findViewById(R.id.calls);
        viewPager=findViewById(R.id.fragmentcontainer);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();

        toolBar=findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);//set a Toolbar to act as the ActionBar for this Activity window

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_baseline_more_vert_24);
        toolBar.setOverflowIcon(drawable); //adding createed res-menu to the activity

        pagerAdapter=new PagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);//controls swiping between fragments
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition()==0||tab.getPosition()==1||tab.getPosition()==2)
                {
                    pagerAdapter.notifyDataSetChanged(); //as RecyclerView
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.profile:
                Intent intent=new Intent(ChatActivity.this,ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.settings:
                Toast.makeText(getApplicationContext(), "Clicked for Settings", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return true;
    }


    //**To update user status whether he/she Online or Offline;
    @Override
    protected void onStart() {
        //only run when the activity starts before onCreate(acc.to priority);
        super.onStart();
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("Status","Online").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "User is Online now!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* The code block to show whether user is offline is given on the onStop() method of SpecificChat!
    @Override
    protected void onStop() {
        super.onStop();
        //uygulamadan cikman veya geri acip bu activity'ye donmen gibi durumlarda 'offline'
        //olmus oluyorsun;
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("Status","Offline").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "User is Offline now!", Toast.LENGTH_SHORT).show();
            }
        });
    }*/
}