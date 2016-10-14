package com.example.topwise.hotfixcat;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Cat mcat;
    private TextView mm;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mcat = new Cat();
        mContext = getApplicationContext();
        mm = (TextView) findViewById(R.id.btn);
        mm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("warlock"," OnClick");
                clickBtn();
            }
        });
    }

    private void clickBtn (){
        Toast.makeText(mContext,mcat.eat(),Toast.LENGTH_LONG).show();
    }
}
