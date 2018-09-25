package com.jascal.ophelia;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jascal.ophelia_annotation.BindView;
import com.jascal.ophelia_annotation.OnClick;
import com.jascal.ophelia_api.Ophelia;


public class BlankFragment extends Fragment {

//    @BindView(R.id.textview)
//    TextView textView;

    @OnClick(R.id.button_f)
    void test(View view) {
        Toast.makeText(getActivity(), "text", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        Ophelia.bind(this, view);

//        textView.setText("successful!");
        return view;
    }

}