package com.example.chatapp_2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Messages> messagesArrayList; //it will hold the messages

    //To identify user send or receive a message;
    int ITEM_SEND=1;
    int ITEM_RECEIVE=2;

    public MessagesAdapter(Context context, ArrayList<Messages> messagesArrayList) {
        this.context = context;
        this.messagesArrayList = messagesArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==ITEM_SEND)
        {
            View view= LayoutInflater.from(context).inflate(R.layout.senderchatlayout,parent,false);
            return new SenderViewHolder(view);
        }
        else{
            View view=LayoutInflater.from(context).inflate(R.layout.receiverchatlayout,parent,false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages messages=messagesArrayList.get(position);
        if(holder.getClass()==SenderViewHolder.class)
        {
            SenderViewHolder viewHolder=(SenderViewHolder)holder;
            viewHolder.textViewMessage.setText(messages.getMessage());
            viewHolder.timeOfMessage.setText(messages.getCurrentTime());
        }
        else{
            ReceiverViewHolder viewHolder=(ReceiverViewHolder)holder;
            viewHolder.textViewMessage.setText(messages.getMessage());
            viewHolder.timeOfMessage.setText(messages.getCurrentTime());
        }
    }

    @Override
    public int getItemViewType(int position) {
       Messages messages=messagesArrayList.get(position);
       if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messages.getSenderId()))
       {
           //guncel olarak baglanan(logged in) kisi mesaji gonderen kisiye esitse,
           //zaten  receiver olmasi halinde logged in olan kisi olmuyor;
           return ITEM_SEND;
       }
       else{
           return ITEM_RECEIVE;
       }
    }

    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    //Two ViewHolder is required since there are 2 different layouts for sender and receiver
    //(senderchatlayout.xml and receiverchatlayout.xml)

    class SenderViewHolder extends RecyclerView.ViewHolder
    {
        TextView textViewMessage;
        TextView timeOfMessage;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage=itemView.findViewById(R.id.senderMessage); //from senderchatlayout.xml
            timeOfMessage=itemView.findViewById(R.id.timeOfMessage);
        }
    }

    class ReceiverViewHolder extends RecyclerView.ViewHolder
    {
        //same id of views in these layouts(e.g. senderMessage) will not cause to crash since they will
        //be used/called seperately;
        TextView textViewMessage;
        TextView timeOfMessage;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage=itemView.findViewById(R.id.senderMessage); //from receiverchatlayout.xml
            timeOfMessage=itemView.findViewById(R.id.timeOfMessage);
        }
    }
}
