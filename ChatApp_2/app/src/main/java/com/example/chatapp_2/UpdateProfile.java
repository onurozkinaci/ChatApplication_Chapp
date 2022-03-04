package com.example.chatapp_2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateProfile extends AppCompatActivity {
    private EditText newUserName;
    private TextView updateProfile;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase; //to fetch the name of user
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;

    private ImageView newuserimageinimageview;
    private StorageReference storageReference; //to fetch the user image
    private String ImageURIaccessToken;//helps to fetch user image
    private androidx.appcompat.widget.Toolbar toolbarofUP;
    private ImageButton backButtonOfUP;
    private ProgressBar pbOfUp;

    private android.widget.Button btnUpdateProfile;

    private Uri imagepath;
    Intent intent;

    private static int pick_image=123;

    String newName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        toolbarofUP=findViewById(R.id.toolbarOfUpdateProfile);
        backButtonOfUP=findViewById(R.id.backButtonOfUP);
        pbOfUp=findViewById(R.id.pbOfUpdateProfile);
        newuserimageinimageview=findViewById(R.id.updatedUserImageInImageView);

        newUserName=findViewById(R.id.updatedUserName);
        btnUpdateProfile=findViewById(R.id.updateProfileButton);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();

        intent=getIntent();//gets the sent value from ProfileActivity
        setSupportActionBar(toolbarofUP);
        newUserName.setText(intent.getStringExtra("nameOfUser")); //previous name is set

        backButtonOfUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); //finish the current activity and back to previous activity.
            }
        });

        DatabaseReference databaseReference=firebaseDatabase.getReference(firebaseAuth.getUid());
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newName=newUserName.getText().toString();
                if(newName.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Name is empty!", Toast.LENGTH_SHORT).show();
                }
                else if(imagepath!=null) //new image is selected
                {
                    //both name and image is filled
                   pbOfUp.setVisibility(View.VISIBLE);
                   UserProfile userProfile=new UserProfile(newName,FirebaseAuth.getInstance().getUid());
                   databaseReference.setValue(userProfile);//updates the name of user on database

                    UpdateImageToStorage();//to update the image on Storage before update it for Firestore database
                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                    pbOfUp.setVisibility(View.INVISIBLE);
                    Intent intent =new Intent(UpdateProfile.this,ChatActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{ //name is filled but imagepath is null
                    pbOfUp.setVisibility(View.VISIBLE);
                    UserProfile userProfile=new UserProfile(newName,FirebaseAuth.getInstance().getUid());
                    databaseReference.setValue(userProfile);//updates the name of user on database
                    updateNameOnCloudFireStore();
                    Toast.makeText(getApplicationContext(), "Name is Updated", Toast.LENGTH_SHORT).show();
                    pbOfUp.setVisibility(View.INVISIBLE);
                    Intent intent =new Intent(UpdateProfile.this,ChatActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        newuserimageinimageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
               startActivityForResult(intent,pick_image);
            }
        });

        //*To get the previous image(recorded image on database before update);
        storageReference=firebaseStorage.getReference();
        storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
               ImageURIaccessToken=uri.toString();
                Picasso.get().load(uri).into(newuserimageinimageview);
            }
        });
    }

    //To control user's image selection from gallery;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==pick_image && resultCode==RESULT_OK)
        {
            //'RESULT_OK' means user selected the image successfully;
            imagepath = data.getData(); //gives URI inside the imagepath
            newuserimageinimageview.setImageURI(imagepath);
            //put the image to ImageView.
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void UpdateImageToStorage()
    {
        StorageReference imageRef=storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic");

        //Image compression(e.g. to make 10mb(high quality) to 300kb);
        Bitmap bitmap=null;

        try {
            bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),imagepath);
            //a bitmap is an array of binary data representing the values of pixels in an image or display
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,25,byteArrayOutputStream); //the image will be decreased to 25px(decrease for size not quality)
        byte[] data=byteArrayOutputStream.toByteArray();

        //putting image to storage;
        UploadTask uploadTask=imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageURIaccessToken =uri.toString(); //access token of image(uri) url gibi bir sey ve browserda ayrica arattiginda da resmi acar,Firestore'a
                        //yuklenen image'i eklemeden once storage'i kullanma sebebimiz de bunu elde etmek.
                        Toast.makeText(getApplicationContext(), "URI is obtained successfully", Toast.LENGTH_SHORT).show();
                        updateNameOnCloudFireStore(); //sending the image data to Firestore for the chat interface by using 'access token of image'.
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "URI cannot be obtained!", Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(getApplicationContext(), "Image is updated succesffully", Toast.LENGTH_SHORT).show();
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Image cannot be updated!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNameOnCloudFireStore()
    {
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String,Object> userData= new HashMap<>();
        userData.put("name",newName);
        userData.put("image",ImageURIaccessToken); //taken on sendImageToStorage
        userData.put("uid",firebaseAuth.getUid()); //zorunlu(compulsary)
        userData.put("Status","Online"); //when user sets his/her profile the status is assigned as 'Online'

        documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Profile is updated successfully ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //To update user status whether he/she Online or Offline;
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
    }
}