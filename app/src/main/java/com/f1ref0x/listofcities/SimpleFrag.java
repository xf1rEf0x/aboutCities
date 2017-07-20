package com.f1ref0x.listofcities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class SimpleFrag extends android.app.DialogFragment {

    TextView textView2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Info about " );
        View v = inflater.inflate(R.layout.info_layout, null);
        textView2 = (TextView) v.findViewById(R.id.textView2);
        textView2.setText(FetchingInfo.description);
        Linkify.addLinks(textView2, Linkify.ALL);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
