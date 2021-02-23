package com.college.geteat.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.college.geteat.R;
import com.college.geteat.utils.SharedPreferencesManager;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("pttt", "onDestroy: locginActivity ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferencesManager.init(this);



    }

}