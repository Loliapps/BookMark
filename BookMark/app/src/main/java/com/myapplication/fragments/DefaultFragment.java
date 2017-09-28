package com.myapplication.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.myapplication.MyBookList;
import com.myapplication.R;



public class DefaultFragment extends DialogFragment implements View.OnClickListener{

    private Context context;
    private TextView confirmBtn, anouncement;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog builder = new Dialog(context);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setCanceledOnTouchOutside(false);

        View view = LayoutInflater.from(context).inflate(R.layout.default_fragment, null);
        anouncement = (TextView) view.findViewById(R.id.default_fragment_textView);
        if (context instanceof MyBookList){
            anouncement.setText("אין ספרים רשומים תחת שם\nמשתמש זה.על מנת להכניס ספרים יש לחזור למסך הספרים שלי וללחוץ על סמל הפלוס.");
        }
        confirmBtn = (TextView)view.findViewById(R.id.default_fragment_btn);
        confirmBtn.setOnClickListener(this);
        builder.setContentView(view);
        return builder;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
            super.onDetach();
        }


    @Override
    public void onClick(View v) {
        if (context instanceof MyBookList){
           getActivity().finish();
        }
        dismiss();
    }

    //TODO check on detach
}


