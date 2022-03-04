package com.example.chatapp_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OTPAuthentication extends AppCompatActivity {
TextView changeNo;
EditText getOTPNo;
android.widget.Button verifyOTPBtn;
String enteredOTP;

FirebaseAuth firebaseAuth;
ProgressBar pbOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpauthentication);
        changeNo=findViewById(R.id.changenumber);
        getOTPNo=findViewById(R.id.getOTP);
        verifyOTPBtn=findViewById(R.id.verifyOTP);
        pbOTP=findViewById(R.id.pbOfOTPAuth);

        firebaseAuth=FirebaseAuth.getInstance();

        changeNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(OTPAuthentication.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        verifyOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enteredOTP=getOTPNo.getText().toString();
                if (enteredOTP.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Enter your OTP Code!", Toast.LENGTH_SHORT).show();
                }
                else{
                    pbOTP.setVisibility(View.VISIBLE);
                    String codeRecieved=getIntent().getStringExtra("otp");//sent on MainActivity
                    PhoneAuthCredential credential= PhoneAuthProvider.getCredential(codeRecieved,enteredOTP);//to compare them
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    pbOTP.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Successfully logged in", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(OTPAuthentication.this,SetProfile.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                    {
                        pbOTP.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}