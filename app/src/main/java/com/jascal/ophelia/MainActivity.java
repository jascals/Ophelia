package com.jascal.ophelia;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jascal.ophelia_annotation.BindView;
import com.jascal.ophelia_api.Ophelia;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.textview)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Ophelia.bind(this);

        textView.setText("successful!");
    }
}
