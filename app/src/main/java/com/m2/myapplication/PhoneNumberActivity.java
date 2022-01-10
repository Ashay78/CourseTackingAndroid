package com.m2.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PhoneNumberActivity extends AppCompatActivity {

    private EditText editPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        this.editPhoneNumber = findViewById(R.id.edit_phone_number);

        SharedPreferences sharedPreferences = this.getSharedPreferences("courseTracking", Context.MODE_PRIVATE);

        if(sharedPreferences != null) {
            String phoneNumber = sharedPreferences.getString("phoneNumber", "");
            if (phoneNumber != null || !phoneNumber.equals("")) {
                this.editPhoneNumber.setText(phoneNumber);
            }
        }
    }

    public void savePhoneNumber(View view) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("courseTracking", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("phoneNumber", this.editPhoneNumber.getText().toString());
        editor.apply();

        Toast.makeText(this,"Save",Toast.LENGTH_LONG).show();
        this.finish();
    }
}
