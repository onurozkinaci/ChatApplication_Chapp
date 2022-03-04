package com.example.chatapp_2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

public class ChatFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    LinearLayoutManager linearLayoutManager;
    private FirebaseAuth firebaseAuth;

    ImageView imageviewOfUser;

    FirestoreRecyclerAdapter<FirebaseModel,NoteViewHolder> chatAdapter; //RecyclerView adapter that listens to a FirestoreArray and displays its data in real time.
    //A 'ViewHolder'(e.g NoteViewHolder) describes an item view and metadata about its place within the RecyclerView.
    //RecyclerView. Adapter implementations should subclass ViewHolder and add fields for caching potentially expensive View. findViewById(int) results.

    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.chatfragment,null);
        View view=inflater.inflate(R.layout.chatfragment,container,false);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        recyclerView=view.findViewById(R.id.recyclerView);

        //Fetching all user from Firestore and showing them;
        //Query query=firebaseFirestore.collection("Users"); //all users fetched.Thats why we dont use getUid() which provides to get specific user
        //*We want to fetch all user except the logined user;
        Query query=firebaseFirestore.collection("Users").whereNotEqualTo("uid",firebaseAuth.getUid());
        FirestoreRecyclerOptions<FirebaseModel>allUserName=new FirestoreRecyclerOptions.Builder<FirebaseModel>()
                .setQuery(query,FirebaseModel.class).build();
        chatAdapter=new FirestoreRecyclerAdapter<FirebaseModel, NoteViewHolder>(allUserName) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull FirebaseModel firebaseModel) {
                //set the data on variables;
                noteViewHolder.particularUserName.setText(firebaseModel.getName());
                String uri=firebaseModel.getImage();
                Picasso.get().load(uri).into(imageviewOfUser);//sets image to View(component) by using it's access token via applying Picasso library!
                if(firebaseModel.getStatus().equals("Online"))
                {
                    noteViewHolder.statusOfUser.setText(firebaseModel.getStatus());
                    noteViewHolder.statusOfUser.setTextColor(Color.GREEN);
                }
                else{
                    noteViewHolder.statusOfUser.setText(firebaseModel.getStatus()); //offline user
                }

                //to provide chat activity by clicking of user,we need to open a new Activity;
                noteViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                //own:Uzerine tikladigin kullaniciya ait bilgileri getirecek;
                    @Override
                    public void onClick(View view) {
                        //getActivity() should be used instead of getApplicationContext for Fragments;
                        //Toast.makeText(getActivity(), "Item is clicked!", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(getActivity(),SpecificChat.class);
                        intent.putExtra("name",firebaseModel.getName());
                        intent.putExtra("receiveruid",firebaseModel.getUid());
                        intent.putExtra("imageuri",firebaseModel.getImage());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.chatviewlayout,parent,false);
                return new NoteViewHolder(view); //it will every time return the new object of this view by NoteViewHolder and set the data.
                //**Own Note:RecyclerView kullanimi icin olusturdugumuz chatviewlayout.xml'deki formati chatfragment.xml icerisine aktararak
                //o formatta gosterimi sagladigimiz kisim burasi gibi dusunebilirsin!
            }
        };
        recyclerView.setHasFixedSize(true);
        linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(chatAdapter);

        return view;
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder
    {
        //we created the below variables according to the chatviewlayout.xml
        //layout's Views(properties);
        private TextView particularUserName;
        private TextView statusOfUser;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            particularUserName=itemView.findViewById(R.id.nameOfUser);
            statusOfUser=itemView.findViewById(R.id.statusOfUser);
            imageviewOfUser=itemView.findViewById(R.id.imageviewOfUser);
        }
    }

    @Override
    public void onStart() { //***so important override for running the App!!!(both below methods)
        super.onStart();
        chatAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(chatAdapter!=null) //veriler zaten cekildiyse dur diyoruz yani!
        {
            chatAdapter.stopListening();
        }
    }
}
