package com.example.chatapp_2;

public class Messages {
    String message;
    String senderId; //to keep the record of who is sending the message
    long timestamp;
    String currentTime;

    public Messages() //empty constructor
    { }
    public Messages(String message,String senderId,long timestamp,String currentTime)
    {
        this.message=message;
        this.senderId=senderId;
        this.timestamp=timestamp;
        this.currentTime=currentTime;
    }

    public String getMessage()
    {
        return this.message;
    }

    public void setMessage(String message)
    {
        this.message=message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }
}
