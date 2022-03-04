package com.example.chatapp_2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SetProfile extends AppCompatActivity {
    private CardView getUserImage;
    private ImageView getUserImageInImageView;
    private static int PICK_IMAGE=123;
    private Uri imagepath;

    private EditText getUserName;
    private android.widget.Button saveProfile;

    private FirebaseAuth firebaseAuth;
    private String name;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private String imageUriAccessToken;

    private FirebaseFirestore firebaseFirestore;

    private ProgressBar pbSetProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference(); //helps to save images on database
        firebaseFirestore=FirebaseFirestore.getInstance();


        getUserImage=findViewById(R.id.getuserimage);
        getUserImageInImageView=findViewById(R.id.getuserimageinimageview);
        getUserName=findViewById(R.id.getusername);
        saveProfile=findViewById(R.id.saveProfile);
        pbSetProfile=findViewById(R.id.pbOfSetProfile);

        getUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                //it will open the galery
                startActivityForResult(intent,PICK_IMAGE); //galeriden fotografi sectikten sonra activity'ye donus icin.

            }
        });

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name=getUserName.getText().toString();
                if(name.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Empty Name!", Toast.LENGTH_SHORT).show();
                }
                else if(imagepath==null)
                {
                    //secilmisse(null degilse) onActivityResult icerisinde imagepath'e atiyor.
                    Toast.makeText(getApplicationContext(), "Image has not been selected!", Toast.LENGTH_SHORT).show();
                }
                else{ //everything is fine;
                    pbSetProfile.setVisibility(View.VISIBLE);
                    sendDataForNewUser(); //send(add) to Realtime db(for name),storage(for image) and Firestore(for both name and image)!
                    pbSetProfile.setVisibility(View.INVISIBLE);//after the data has been sent/added to db.
                    Intent intent=new Intent(SetProfile.this,ChatActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void sendDataForNewUser()
    {
      sendDataToRealTimeDb(); //saveProfile.setOnClickListener icerisinde bu sendDataForNewUser() cagrildiginda
        //bu metot cagrildigindan name bosluksuz(trim()) ve dogru bir sekilde alinmis olacak. 'sendImageToStorage()'
        //icerisinde 'sendDataToCloudFirestore()' cagrildiginda da bu name ile birlikte image'in access token'i
        //Firestore'a aktarilmis olacak.
    }

    private void sendDataToRealTimeDb()
    {
       //we have to create a class('UserProfile') to send a data to RealTimeDb unlike sending data to Firebase and Firestore;
        name=getUserName.getText().toString().trim();
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        DatabaseReference databaseReference=firebaseDatabase.getReference(firebaseAuth.getUid());

        UserProfile userProfile=new UserProfile(name,firebaseAuth.getUid());
        databaseReference.setValue(userProfile);
        Toast.makeText(getApplicationContext(), "User Profile is succesfully added!", Toast.LENGTH_SHORT).show();
        sendImageToStorage();//image is not sending to RealTimeDb,it will be sent to storage,image'i saklamak icin storage'i,
        //user_name'i saklamak icin RealTimeDB'yi kullaniyoruz da denebilir yani.
        //cloudfirestore uzerinde hem name hem image saklanacak cunku chat uzerinde her ikisi de gozukmeli,
        //user profiline girip sadece image gormuyor yani name'ini de goruyor.FireStore'a eklemeden once Image'i Storage'a
        //ekleme sebebimiz, eklenen image icin 'access token' alip daha sonrasinda firestore uzerinde bu image access token'i spesifik bir
        //kullaniciya/user'a aktarmis oluyoruz.
    }
    private void sendImageToStorage()
    {
        //getUserImageInImageView.setDrawingCacheEnabled(true);//own
        //getUserImageInImageView.buildDrawingCache();//own

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

        //asagidaki kod blogunu yorumdan cikar sonrasinda;
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,25,byteArrayOutputStream); //the image will be decreased to 25px(decrease for size not quality)
        byte[] data=byteArrayOutputStream.toByteArray();

        /*
        Bitmap bitmap = ((BitmapDrawable) getUserImageInImageView.getDrawable()).getBitmap();//own
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//own
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//own
        byte[] data=baos.toByteArray();*/


        //putting image to storage;
        UploadTask uploadTask=imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUriAccessToken=uri.toString(); //access token of image(uri) url gibi bir sey ve browserda ayrica arattiginda da resmi acar,Firestore'a
                        //yuklenen image'i eklemeden once storage'i kullanma sebebimiz de bunu elde etmek.
                        Toast.makeText(getApplicationContext(), "URI is obtained successfully", Toast.LENGTH_SHORT).show();
                        sendDataToCloudFirestore(); //sending the image data to Firestore for the chat interface by using 'access token of image'.
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "URI cannot be obtained!", Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(getApplicationContext(), "Image is uploaded succesffully", Toast.LENGTH_SHORT).show();
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Image cannot be uploaded!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendDataToCloudFirestore()
    {
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String,Object>userData= new HashMap<>();
        userData.put("name",name);
        userData.put("image",imageUriAccessToken); //taken on sendImageToStorage
        userData.put("uid",firebaseAuth.getUid()); //zorunlu(compulsary)
        userData.put("Status","Online"); //when user sets his/her profile the status is assigned as 'Online'

        documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Data is sent to Cloud Firestore successfully ", Toast.LENGTH_SHORT).show();
            }
        });
        //uid kullanimi ile onceden kaydin varsa onu override etmeyi,yoksa sifirdan
        //olusturmayi saglamis oluyorsun!
    }

    //To control user's image selection from gallery;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK)
        {
            //'RESULT_OK' means user selected the image successfully;
            imagepath = data.getData(); //gives URI inside the imagepath
            getUserImageInImageView.setImageURI(imagepath);
            //put the image to ImageView.
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}