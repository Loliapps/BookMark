package com.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.myapplication.objects.BookObject;
import com.squareup.picasso.Picasso;
import java.io.File;



public class BookDetailActivity extends BaseActivity implements View.OnClickListener {

    private ImageView smallCover,bigCover;
    private TextView searchBookBtn;
    private BookObject bookObject;
    private LinearLayout coverLayout;
    private boolean isShowingCover = false;
    private final String IMAGE_URL = "http://www.loliapps.com/images/book_image/";
    public static final String SEARCH_BOOK_BY_NAME = "searchBook";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        createToolBar(this,R.id.my_tool_bar, R.id.TopBar_menu, R.color.publicDark,BaseActivity.PUBLIC_ACTIVITY);
        navigationBar();

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_tool_bar);
        CircularImageView userImage = (CircularImageView) toolbar.findViewById(R.id.TopBar_userImage);
        File fImage = new File(getFilesDir(),"userImage.jpg");
        if(fImage.exists()) {
            userImage.setImageURI(Uri.fromFile(fImage));
        }else{
            userImage.setImageResource(R.drawable.u1);
        }
        bookObject = (BookObject)getIntent().getSerializableExtra("book");

        TextView title = (TextView)findViewById(R.id.singleBook_title);
        title.setText(bookObject.getTitle());
        TextView author = (TextView) findViewById(R.id.singleBook_author);
        author.setText(bookObject.getAuthor());
        TextView genres = (TextView) findViewById(R.id.singleBook_genres);
        switch (bookObject.getGenres()) {

            case "1":
                genres.setText("רומן");
                break;

            case "2":
                genres.setText("מתח");
                break;

            case "3":
                genres.setText("ריגול");
                break;

            case "4":
                genres.setText("מדע בדיוני");
                break;

            case "5":
                genres.setText("פנטזיה/הרפתקאות");
                break;

            case "6":
                genres.setText("ביוגרפיה/היסטוריה");
                break;

            case "7":
                genres.setText("פילוסופיה");
                break;

            case "8":
                genres.setText("תוכנה/מחשבים");
                break;
        }

        smallCover = (ImageView) findViewById(R.id.singleBook_img);
        if (bookObject.getImg().equals("cover.jpg")) {
            Picasso.with(this).load(R.drawable.cover).into(smallCover);
        } else {
            Picasso.with(this).load(IMAGE_URL + bookObject.getImg()).into(smallCover);
        }
        smallCover.setOnClickListener(this);
        coverLayout = (LinearLayout) findViewById(R.id.bCover_layout);
        coverLayout.setVisibility(View.GONE);
        coverLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_UP){
                    isShowingCover = false;
                    coverLayout.setVisibility(View.GONE);
                }

                return true;
            }
        });

        bigCover = (ImageView) findViewById(R.id.bCover_layout_book_image);
        TextView takzir = (TextView) findViewById(R.id.singleBook_takzir);
        takzir.setText((CharSequence) bookObject.getTakzir());

        searchBookBtn = (TextView)findViewById(R.id.singleBook_search_book_inArea);
        searchBookBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if(v == smallCover){
            if(isShowingCover == false && bookObject.getImg() != "cover.jpg"){
                isShowingCover = true;
                Picasso.with(this).load(IMAGE_URL + bookObject.getImg()).into(bigCover);
                coverLayout.setVisibility(View.VISIBLE);
            }
        }

        if(v == searchBookBtn){
           Intent intent = new Intent(this,AreaBookOwners.class);
           intent.putExtra(SEARCH_BOOK_BY_NAME,bookObject.getTitle());
           startActivity(intent);
        }
    }

}
