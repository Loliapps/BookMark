package com.myapplication;

import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.myapplication.objects.MyBookObject;
import com.myapplication.threads.BaseThread;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;



public class MyBooksListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<MyBookObject> myBookObject;
    private int ownerId;
    private Context context;
    private ListView bListView;
    private LinearLayout fragment;
    private DBOpenHelper helper;
    private SQLiteDatabase database;

    private static final String IMAGE_URL = "http://www.loliapps.com/images/";
    private static final int NO_BORROWER = 0;
    private static final int BORROWER = 1;


   public MyBooksListAdapter(Context context, ArrayList<MyBookObject> myBookObject, int ownerId, LinearLayout fragment, ListView bListView) {
        this.context      = context;
        this.myBookObject = myBookObject;
        this.ownerId      = ownerId;
        this.fragment     = fragment;
        this.bListView    = bListView;
        helper = new DBOpenHelper(context);
        database = helper.getReadableDatabase();

   }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;
        RecyclerView.ViewHolder holder = null;

        if(viewType == NO_BORROWER){
            view = LayoutInflater.from(context).inflate(R.layout.available_book_status, parent, false);
            holder = new NoBorrowerViewHolder(view);
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.not_available_book_status, parent, false);
            holder = new BorrowerViewHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final MyBookObject book = myBookObject.get(position);

        if(holder instanceof NoBorrowerViewHolder){
            final NoBorrowerViewHolder nbvh = (NoBorrowerViewHolder)holder;

            nbvh.bTitle.setText(myBookObject.get(position).getTitle());
            nbvh.isHidden = myBookObject.get(position).getHidden();

            if(myBookObject.get(position).getHidden() == true){
                nbvh.infoLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0));
            }else {
                nbvh.infoLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, nbvh.maxHeight));
                nbvh.arrowBtn.animate().setDuration(0).rotation(-90).start();
            }

            nbvh.mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(nbvh.isHidden == true){
                        nbvh.arrowBtn.animate().setDuration(200).rotation(-90).start();
                        animateInfoLayout(nbvh.infoLayout,"down",nbvh.maxHeight);
                    }else{
                        nbvh.arrowBtn.animate().setDuration(200).rotation(0).start();
                        animateInfoLayout(nbvh.infoLayout,"up",nbvh.maxHeight);
                    }
                    nbvh.isHidden = !nbvh.isHidden;
                    myBookObject.get(position).setHidden(nbvh.isHidden);
                }
            });

            nbvh.toggleButton.setChecked(false);
            nbvh.pickDate.setText("");
            nbvh.pickDate.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(nbvh.pickDate.getWindowToken(), 0);
                    return true;
                    }else if(event.getAction() == MotionEvent.ACTION_UP){

                        Calendar calendar = Calendar.getInstance();
                        int year  = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day   = calendar.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog dialog = new DatePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_MinWidth, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                String stringDay   = day+"";
                                String stringMonth = month+"";

                                if(day < 10){
                                    stringDay = "0" + day;
                                }

                                if(month < 9){
                                    stringMonth = "0" + (month+1);
                                }
                                else if(month == 9){
                                    stringMonth = "10";
                                }
                                else{ stringMonth = "" + (month+1);}

                                nbvh.pickDate.setText(stringDay+"-"+stringMonth+"-"+year);
                            }
                        }, year, month, day);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();
                        return true;
                    }else{
                        return false;
                    }
                }
            });

            nbvh.borrowerList.setText("");
            nbvh.borrowerList.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(nbvh.borrowerList.getWindowToken(), 0);
                        nbvh.list = readFromDB();
                        return true;
                    }else if(event.getAction() == MotionEvent.ACTION_UP){
                        fragment.setVisibility(View.VISIBLE);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,nbvh.list);
                        bListView.setAdapter(adapter);

                        bListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                nbvh.borrowerList.setText(nbvh.list.get(position));
                                nbvh.list.clear();
                                fragment.setVisibility(View.GONE);
                            }
                        });
                        return true;
                    }else{
                        return false;
                    }
                }});
            nbvh.updateStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!nbvh.pickDate.getText().toString().isEmpty() && !nbvh.borrowerList.getText().toString().isEmpty()) {

                        Cursor cursor = database.rawQuery(helper.getChatterData(nbvh.borrowerList.getText().toString()), null);
                        cursor.moveToFirst();
                        int bId     = cursor.getInt(cursor.getColumnIndex(helper.COLUMN_CHATTER_ID));
                        String bImg = cursor.getString(cursor.getColumnIndex(helper.COLUMN_CHATTER_IMAGE));
                        cursor.close();

                        myBookObject.get(position).setbDate(nbvh.pickDate.getText().toString());
                        myBookObject.get(position).setBorrower(nbvh.borrowerList.getText().toString());
                        myBookObject.get(position).setBorrowerId(bId);
                        myBookObject.get(position).setbImage(bImg);
                        myBookObject.get(position).setIsAvailable(1);
                        myBookObject.get(position).setHidden(false);

                        Intent intent = new Intent("reloadMyBooks");
                        context.sendBroadcast(intent);

                        Map<String, String> params = new HashMap<String, String>();
                        params.put("u_id", ownerId + "");
                        params.put("b_title", myBookObject.get(position).getTitle());
                        params.put("b_date", nbvh.pickDate.getText().toString());
                        params.put("b_id", bId+"");

                        BaseThread updateMyBookLibrary = new BaseThread(BaseThread.request.UPDATE_MY_BOOKS, params, null);
                        updateMyBookLibrary.start();
                    }else{
                        return;
                    }


                }
            });

        }else if (holder instanceof BorrowerViewHolder){

            final BorrowerViewHolder bvh = (BorrowerViewHolder)holder;

            bvh.bTitle.setText(myBookObject.get(position).getTitle());
            bvh.isHidden = myBookObject.get(position).getHidden();
            if(myBookObject.get(position).getHidden() == true){
                bvh.infoLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0));
            }else {
                bvh.infoLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, bvh.maxHeight));
                bvh.arrowBtn.animate().setDuration(0).rotation(-90).start();
            }

            bvh.mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bvh.isHidden == true) {
                        bvh.arrowBtn.animate().setDuration(200).rotation(-90).start();
                        animateInfoLayout(bvh.infoLayout, "down", bvh.maxHeight);
                    } else {
                        bvh.arrowBtn.animate().setDuration(200).rotation(0).start();
                        animateInfoLayout(bvh.infoLayout, "up", bvh.maxHeight);
                    }
                    bvh.isHidden = !bvh.isHidden;
                    myBookObject.get(position).setHidden(bvh.isHidden);
                }
            });



            bvh.bDate.setText("תאריך השאלה: " + myBookObject.get(position).getbDate());
            Picasso.with(context).load("http://www.loliapps.com/images/" + myBookObject.get(position).getbImage()).networkPolicy(NetworkPolicy.NO_CACHE).error(R.drawable.u1).into(bvh.borrowerImage);

            bvh.borrowerName.setText(myBookObject.get(position).getBorrower());
            bvh.sendMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context,ChatRoomActivity.class);
                    intent.putExtra("chatter_id",myBookObject.get(position).getbId());
                    intent.putExtra("chatter_name",myBookObject.get(position).getBorrower());
                    context.startActivity(intent);
                }
            });

            bvh.toggleButton.setChecked(true);
            bvh.updateStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(bvh.toggleButton.isChecked() == false){

                        myBookObject.get(position).setbDate(null);
                        myBookObject.get(position).setbImage(null);
                        myBookObject.get(position).setBorrower(null);
                        myBookObject.get(position).setBorrowerId(0);
                        myBookObject.get(position).setIsAvailable(0);
                        myBookObject.get(position).setHidden(false);

                        Intent intent = new Intent("reloadMyBooks");
                        context.sendBroadcast(intent);

                        Map<String,String> params = new HashMap<String, String>();
                        params.put("u_id",ownerId+"");
                        params.put("b_title",myBookObject.get(position).getTitle());
                        params.put("b_date","none");
                        params.put("b_name","none");

                        BaseThread updateMyBookLibrary = new BaseThread(BaseThread.request.UPDATE_MY_BOOKS,params,null);
                        updateMyBookLibrary.start();

                    }else{
                        return;
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {

        if(myBookObject != null){
            return myBookObject.size();
        }else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        int status = myBookObject.get(position).getIsAvailable();
        if(status == 0){
            return NO_BORROWER;
        }else {
            return BORROWER;
        }
    }

    private class BorrowerViewHolder extends RecyclerView.ViewHolder {

        TextView bTitle, borrowerName,bDate;
        Button arrowBtn, sendMsg, updateStatus;
        com.mikhaellopez.circularimageview.CircularImageView borrowerImage;
        ToggleButton toggleButton;
        LinearLayout infoLayout, parentLayout, mainLayout;
        Boolean isHidden;
        int maxHeight = 0;


        public BorrowerViewHolder(View itemView) {
            super(itemView);

            parentLayout  = (LinearLayout) itemView.findViewById(R.id.my_books_list_view_cell_parent_layout);
            infoLayout    = (LinearLayout) itemView.findViewById(R.id.my_books_list_view_cell_info_layout);
            mainLayout    = (LinearLayout) itemView.findViewById(R.id.my_books_list_view_cell_main);
            bTitle        = (TextView) itemView.findViewById(R.id.my_books_list_view_cell_book_name);
            bDate         = (TextView) itemView.findViewById(R.id.my_books_list_view_cell_bDate);
            borrowerName  = (TextView) itemView.findViewById(R.id.my_books_list_view_cell_borrower_name);
            borrowerImage = (com.mikhaellopez.circularimageview.CircularImageView) itemView.findViewById(R.id.my_books_list_view_cell_borrowerImg);
            toggleButton  = (ToggleButton) itemView.findViewById(R.id.my_books_list_view_cell_switch);
            arrowBtn      = (Button) itemView.findViewById(R.id.my_books_list_view_cell_more_info_btn);
            sendMsg       = (Button) itemView.findViewById(R.id.my_books_list_view_cell_send_msg_btn);
            updateStatus  = (Button) itemView.findViewById(R.id.my_books_list_view_cell_update_btn);

            maxHeight     =  infoLayout.getLayoutParams().height;

        }
    }

    private class NoBorrowerViewHolder extends RecyclerView.ViewHolder {

        TextView bTitle;
        EditText pickDate;
        AutoCompleteTextView borrowerList;
        Button arrowBtn, updateStatus;
        ToggleButton toggleButton;
        LinearLayout infoLayout, parentLayout, mainLayout;
        ArrayList<String>list = new ArrayList<>();
        Boolean isHidden;
        int maxHeight = 0;


        public NoBorrowerViewHolder(View itemView) {
            super(itemView);

            parentLayout  = (LinearLayout) itemView.findViewById(R.id.my_books_list_view_cell_parent_layout);
            infoLayout    = (LinearLayout) itemView.findViewById(R.id.my_books_list_view_cell_info_layout);
            mainLayout    = (LinearLayout) itemView.findViewById(R.id.my_books_list_view_cell_main);
            bTitle        = (TextView) itemView.findViewById(R.id.my_books_list_view_cell_book_name);
            toggleButton  = (ToggleButton) itemView.findViewById(R.id.my_books_list_view_cell_switch);
            arrowBtn      = (Button) itemView.findViewById(R.id.my_books_list_view_cell_more_info_btn);
            borrowerList  = (AutoCompleteTextView) itemView.findViewById(R.id.my_books_list_view_cell_borrower_name_editText);
            pickDate      = (EditText) itemView.findViewById(R.id.my_books_list_view_cell_datePicker);
            updateStatus  = (Button) itemView.findViewById(R.id.my_books_list_view_cell_update_btn);

            maxHeight     =  infoLayout.getLayoutParams().height;
        }
    }

    private ArrayList<String> readFromDB(){

        ArrayList<String>list = new ArrayList<>();

        Cursor tableCursor = database.rawQuery(helper.readFromChatters(), null);
        if(tableCursor.getCount() > 0) {

            tableCursor.moveToFirst();
            list.add(tableCursor.getString(tableCursor.getColumnIndex(helper.COLUMN_CHATTER_NAME)));

            while (tableCursor.moveToNext()) {
                list.add(tableCursor.getString(tableCursor.getColumnIndex(helper.COLUMN_CHATTER_NAME)));
            }
        }

        return list;
    }


    private void animateInfoLayout(final LinearLayout layout, String layoutDirection, int height){

        ValueAnimator animator = null;

        switch (layoutDirection){

            case "up":
                animator = ValueAnimator.ofInt(height,0);
                break;
            case "down":
                animator = ValueAnimator.ofInt(0,height);
        }
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                layout.getLayoutParams().height = value.intValue();
                layout.requestLayout();
            }
        });
        animator.start();
    }

}




