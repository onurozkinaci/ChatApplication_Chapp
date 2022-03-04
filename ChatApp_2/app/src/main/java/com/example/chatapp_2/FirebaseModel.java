package com.example.chatapp_2;

public class FirebaseModel {
String name,image,uid,Status; //same name with sent variables to Firestore on SetProfile.java-sendDataToCloudFirestore()

    public FirebaseModel() {
    }

    public FirebaseModel(String name, String image, String uid, String status) {
        this.name = name;
        this.image = image;
        this.uid = uid;
        Status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
