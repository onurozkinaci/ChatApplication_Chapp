package com.example.chatapp_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SpecificChat extends AppCompatActivity {

    EditText getMessage;
    ImageButton btnSendMessage;

    CardView sendMessageCardView;
    androidx.appcompat.widget.Toolbar toolbarOfSpecificChat;
    ImageView  imageViewOfSpecificUser;
    TextView nameOfSpecificUser;

    private String enteredMessage; //to hold current entered message by user
    Intent intent; //to get the sent parameters(values) from ChatFragment.java
    String receiverName,senderName,receiveruid,senderuid;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase; //will work with RealTimeDb
    String senderRoom,receiverRoom; //*to work with Db we need to create rooms!

    ImageButton backButtonOfSC;
    RecyclerView messageRecyclerView; //to show all messages(old and current)
    //MessagesAdapter

    String currentTime;//with the usage of Calendar
    Calendar calendar;//to get current time for messages
    SimpleDateFormat simpleDateFormat;//to get time in exact format


    MessagesAdapter messagesAdapter;
    ArrayList<Messages>messagesArrayList;

    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_chat);

        getMessage=findViewById(R.id.getMessage);
        sendMessageCardView=findViewById(R.id.cardViewOfSendMessage);
        btnSendMessage=findViewById(R.id.imageViewOfSendMessage);
        toolbarOfSpecificChat=findViewById(R.id.toolbarOfSpecificChat);
        nameOfSpecificUser=findViewById(R.id.nameOfSpecificUser);
        imageViewOfSpecificUser=findViewById(R.id.specificUserImageInImageView);
        backButtonOfSC=findViewById(R.id.backButtonOfSC);

        firebaseFirestore=FirebaseFirestore.getInstance();

        messagesArrayList=new ArrayList<Messages>();
        messageRecyclerView=findViewById(R.id.recyclerviewofspecificchat);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); //to start RecyclerView from reverse
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        messagesAdapter=new MessagesAdapter(SpecificChat.this,messagesArrayList);
        messageRecyclerView.setAdapter(messagesAdapter);


        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        intent=getIntent();

        setSupportActionBar(toolbarOfSpecificChat);
        toolbarOfSpecificChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Toolbar is clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        calendar=Calendar.getInstance();
        simpleDateFormat=new SimpleDateFormat("hh:mm a"); //'a' means to change the format inside hours and minutes for 24 hours format
        senderuid=firebaseAuth.getUid();
        receiveruid=intent.getStringExtra("receiveruid");
        receiverName=intent.getStringExtra("name");

        senderRoom=senderuid+receiveruid;//onurother
        receiverRoom=receiveruid+senderuid;//otheronur


        //**own--Getting the old and current datas(all data) from the Db by using adapter for recycler view;
        DatabaseReference databaseReference=firebaseDatabase.getReference().child("chats").child(senderRoom).child("messages");
        //We're the logged in(Sender) user since we'll send the message on our phone.
        messagesAdapter=new MessagesAdapter(SpecificChat.this,messagesArrayList);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear(); //clears the previous arraylist
                for(DataSnapshot snapshot1: snapshot.getChildren())
                {
                    Messages messages=snapshot1.getValue(Messages.class);
                    messagesArrayList.add(messages);
                    messageRecyclerView.scrollToPosition(messagesArrayList.size() - 1);//to show new message by scrolling the page automatically
                    //onceki mesajlardan Arraylist'e 7 tane iletilmis olsun ve en son bir tane daha ilettiysen guncel olarak 8 tane mesaj olmus oluyor. Arraylist'e bu 8 mesaj aktarildiginda,
                    //Recycler View'in en son iletilen mesajin oldugu kisma dogru kaymasi/scroll icin bu Arraylist'in size'i-1 degerini(7.index gibi) alarak son mesaji gostermis oluyoruz!
                }
                messagesAdapter.notifyDataSetChanged(); //Arraylist'teki degisikligi bildirmek adina,her Db'den mesaj icin data cekildiginde(yeni mesaj iletilmesine vb bagli olarak gerceklesen degisiklik icin).
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        backButtonOfSC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); //finish this activity and back to previous activity
            }
        });

        //DatabaseReference databaseReference=firebaseDatabase.getReference(firebaseAuth.Uid());
        nameOfSpecificUser.setText(receiverName);
        String uri=intent.getStringExtra("imageuri");
        if(uri.isEmpty())
        {
            Toast.makeText(getApplicationContext(), "null image is received!", Toast.LENGTH_SHORT).show();
        }
        else{ //image is taken from ChatFragment
            Picasso.get().load(uri).into(imageViewOfSpecificUser);
        }

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enteredMessage=getMessage.getText().toString();
                if (enteredMessage.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Enter message first", Toast.LENGTH_SHORT).show();
                }

                else{
                   Date date=new Date();
                   currentTime=simpleDateFormat.format(calendar.getTime());

                   //**Sending Message;
                   //Adding messages to db for both sender and receiver seperately!
                    // (if message can be added to senderRoom on Db, then it can be added to receiverRoom on db too!)
                   //To work with RealTime Db,modal class('Messages.java') must be created;
                   Messages messages=new Messages(enteredMessage,firebaseAuth.getUid(),date.getTime(),currentTime);
                   firebaseDatabase.getReference().child("chats")
                           .child(senderRoom)
                           .child("messages")
                           .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           firebaseDatabase.getReference().child("chats")
                                   .child(receiverRoom)
                                   .child("messages")
                                   .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {

                               }
                           });
                       }
                   });
                   getMessage.setText(null); //make the send message field empty after it is sent
                   //closeKeyboard(); //atilan mesaj sonrasinda keyboard'u kapatmak icin
                   //messageRecyclerView.smoothScrollToPosition(messageRecyclerView.getAdapter().getItemCount()-1); //to show new message by scrolling the page automatically

                }
            }
        });

    }

    /*private void closeKeyboard()
    {
      View view=this.getCurrentFocus();
      if(view!=null)
      {
          InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(view.getWindowToken(),0);
      }
    }*/

    //**Chatler icin RealTime db veya Firestore kullanmak senin kararin,RealTime db kullanilacagi icin
    //Messages.java class'i gerekli(bir object olsuturmalisin);


    @Override
    public void onStart() { //***so important override for running the App!!!(both below methods)
        super.onStart();
        messagesAdapter.notifyDataSetChanged();

        //Make user Online;
        //The below part is given to show whether user is Online when he/she is not on the one-to-one chat screen/activity;
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("Status","Online").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "User is Online now!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if(messagesAdapter!=null) //veriler zaten cekildiyse dur diyoruz yani!
        {
            messagesAdapter.notifyDataSetChanged();
        }

        //The below part is given to show whether user is Offline when he/she is not on the one-to-one chat screen/activity;
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("Status","Offline").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "User is Offline now!", Toast.LENGTH_SHORT).show();
            }
        });
    }

   /* @Override
    protected void onDestroy() {
        super.onDestroy();
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("Status","Offline").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "User is Offline now!", Toast.LENGTH_SHORT).show();
            }
        });
    }*/
}