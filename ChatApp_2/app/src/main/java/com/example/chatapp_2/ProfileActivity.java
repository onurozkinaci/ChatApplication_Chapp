package com.example.chatapp_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
  EditText viewUserName;
  TextView updateProfile;

  FirebaseAuth firebaseAuth;
  FirebaseDatabase firebaseDatabase; //to fetch the name of user
  FirebaseFirestore firebaseFirestore;
  FirebaseStorage firebaseStorage;

  ImageView viewuserimageinimageview;
  StorageReference storageReference; //to fetch the user image
  private String ImageURIaccessToken;//helps to fetch user image
  androidx.appcompat.widget.Toolbar toolbarofPA;
  ImageButton backButtonOfPA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        viewUserName=findViewById(R.id.viewUserName);
        updateProfile=findViewById(R.id.txtUpdateProfile);
        viewuserimageinimageview=findViewById(R.id.viewUserImageInImageView);
        backButtonOfPA=findViewById(R.id.backButtonOfPA);
        toolbarofPA=findViewById(R.id.toolbarOfProfileActivity);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        //firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();
        storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic") //taken from SetProfile.java-sendImageToStorage()
        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageURIaccessToken=uri.toString();
                Picasso.get().load(uri).into(viewuserimageinimageview);//finds and puts the image inside of our ImageView on it's accessToken!
            }
        });

        DatabaseReference databaseReference=firebaseDatabase.getReference(firebaseAuth.getUid());//to fetch the name of user
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile userProfile=snapshot.getValue(UserProfile.class);//RealTimeDb kullanmak icin bir .java class kullanmalisin!
                viewUserName.setText(userProfile.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                 //if something wrong happened;
                Toast.makeText(getApplicationContext(), "Something wrong happened for fetching!", Toast.LENGTH_SHORT).show();
            }
        });

        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProfileActivity.this,UpdateProfile.class);
                intent.putExtra("nameOfUser",viewUserName.getText().toString());
                startActivity(intent);
            }
        });


        setSupportActionBar(toolbarofPA);//sets a Toolbar to act as the ActionBar for this Activity window.
        backButtonOfPA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); //direkt bulunulan aktiviteyi bitirip onceki adima doner
            }
        });

    }

   /* //To update user status whether he/she Online or Offline;
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