package com.example.chatapp_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    EditText phoneNO;
    Button sendOTP;
    CountryCodePicker ccpicker;
    String countryCode;
    String phoneNumber;

    FirebaseAuth firebaseAuth;
    ProgressBar pbMain;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks callBacks;
    String codeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNO=(EditText) findViewById(R.id.getphonenumber);
        ccpicker=findViewById(R.id.countrycodepicker);
        sendOTP=(Button) findViewById(R.id.sendotpButton);
        pbMain=(ProgressBar) findViewById(R.id.pbOfMain);

        firebaseAuth=FirebaseAuth.getInstance(); //to get the instance of current user
        countryCode=ccpicker.getSelectedCountryCodeWithPlus(); //app:ccp_areaCodeDetectedCountry="true" detects it directly
        ccpicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countryCode=ccpicker.getSelectedCountryCodeWithPlus();
                //if the selected country code is changed,detects it.
            }
        });

        sendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number;
                number=phoneNO.getText().toString();
                if (number.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Please enter your number!", Toast.LENGTH_SHORT).show();
                }
                else if(number.length()<10)
                {
                    Toast.makeText(getApplicationContext(), "Please enter correct number!", Toast.LENGTH_SHORT).show();
                }
                else{ //no problem
                   pbMain.setVisibility(View.VISIBLE);
                   phoneNumber=countryCode+number; //final phone number

                    //settimeout icerisinde 60sn sonra tekrar OTP yollanabilir
                    //demis oluyorsun
                    PhoneAuthOptions options=PhoneAuthOptions.newBuilder(firebaseAuth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(MainActivity.this)
                            .setCallbacks(callBacks)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options); //girilen numarayi verify etmek/dogrulamak icin
                }
            }
        });

        callBacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                //how to automatically fetch OTP,without getting it from user?


            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(getApplicationContext(), "OTP is sent!", Toast.LENGTH_SHORT).show();
                pbMain.setVisibility(View.INVISIBLE);
                codeSent=s;
                Intent intent=new Intent(MainActivity.this,OTPAuthentication.class);
                intent.putExtra("otp",codeSent);
                startActivity(intent);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        //to not send OTP code to the user again which logged in and verified before;
        //OnStart icerisinde verilme sebebi de bu,bastan kontrol saglayip tekrar gonderilmesni engellemek icin!
        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            Intent intent=new Intent(MainActivity.this,ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //this flag will cause any existing task that would be associated with the activity
            //to be cleared before the new activity is started. That is, the activity
            //becomes the new root of an otherwise empty task, and any
            //old activities are finished.
            startActivity(intent);
        }
    }
}
