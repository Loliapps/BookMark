package com.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.io.File;



public class BaseActivity extends AppCompatActivity {

    public static SharedPreferences settings;
    public static SharedPreferences.Editor editor;
    public static int PUBLIC_ACTIVITY = 1;
    public static int PERSONAL_ACTIVITY = 2;
    public static int MY_BOOKS_ACTIVITY = 3;
    public static int CHAT_ACTIVITY = 4;

    public PopupWindow popupWindow;


    private int public_icon = R.drawable.pu_icon;
    private int personal_icon = R.drawable.pr_icon;
    private int book_icon = R.drawable.b_icon;
    private int chat_icon = R.drawable.chat_icon2;
    private int home_icon = R.drawable.home_icon;
    private int logout_icon = R.drawable.logout_icon;



// ----------------------------- set navigation bar ------------------------------


    public void navigationBar() {

        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setNavigationBarColor(getApplicationContext().getResources().getColor(R.color.half_transparent, null));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getApplicationContext().getResources().getColor(R.color.half_transparent));
        }
    }



// ----------------------------- set toolBar -----------------------------------------


    public Toolbar createToolBar(final Context context, int rToolBar, int rMenu, int rColor, int activity) {

        final Toolbar toolbar = (Toolbar) findViewById(rToolBar);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            toolbar.setBackgroundColor(context.getResources().getColor(rColor, null));
        }else {
            toolbar.setBackgroundColor(getResources().getColor(rColor));
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        final View menu = LayoutInflater.from(context).inflate(R.layout.drop_down_menu,null);
        LinearLayout btn = (LinearLayout) menu.findViewById(R.id.DropDown_Buttons_container);

        TextView activityName = (TextView) findViewById(R.id.TopBar_activity_name);
        com.mikhaellopez.circularimageview.CircularImageView circularImageView = (com.mikhaellopez.circularimageview.CircularImageView) findViewById(R.id.TopBar_userImage);
        circularImageView.setBorderColor(rColor);
        circularImageView.setBorderWidth(1);

        File fImage = new File(getFilesDir(),"userImage.jpg");
        if(fImage.exists()) {
            circularImageView.setImageURI(Uri.fromFile(fImage));

        }else{
            circularImageView.setImageResource(R.drawable.u1);
        }

        ImageView imageMenu = (ImageView)findViewById(rMenu);

        if (activity == PUBLIC_ACTIVITY) {
            activityName.setText("איזור ציבורי");
            circularImageView.setImageResource(R.drawable.search_icon);
            imageMenu.setImageResource(R.drawable.pu_icon);

            btn.addView(createMenuButtons(context, R.color.personal, personal_icon, "איזור\nאישי"));
            btn.addView(createMenuButtons(context, R.color.darkGreen, book_icon, "הספרים\nשלי"));
            btn.addView(createMenuButtons(context, R.color.brightBackground, chat_icon, "ההודעות\nשלי"));
            btn.addView(createMenuButtons(context, R.color.darkGray, home_icon, "תפריט\nראשי"));
            btn.addView(createMenuButtons(context, R.color.red, logout_icon, "התנתק"));
        }

        if (activity == PERSONAL_ACTIVITY) {
            activityName.setText("איזור אישי");
            imageMenu.setImageResource(R.drawable.pr_icon);

            btn.addView(createMenuButtons(context, R.color.publicDark, public_icon, "איזור\nציבורי"));
            btn.addView(createMenuButtons(context, R.color.darkGreen, book_icon, "הספרים שלי"));
            btn.addView(createMenuButtons(context, R.color.brightBackground, chat_icon, "ההודעות שלי"));
            btn.addView(createMenuButtons(context, R.color.darkGray, home_icon, "תפריט ראשי"));
            btn.addView(createMenuButtons(context, R.color.red, logout_icon, "התנתק"));
        }

        if (activity == MY_BOOKS_ACTIVITY) {
            activityName.setText("הספרים שלי");
            imageMenu.setImageResource(R.drawable.b_icon);

            btn.addView(createMenuButtons(context, R.color.publicDark, public_icon, "איזור\nציבורי"));
            btn.addView(createMenuButtons(context, R.color.personal, personal_icon, "איזור\nאישי"));
            btn.addView(createMenuButtons(context, R.color.brightBackground, chat_icon, "ההודעות שלי"));
            btn.addView(createMenuButtons(context, R.color.darkGray, home_icon, "תפריט ראשי"));
            btn.addView(createMenuButtons(context, R.color.red, logout_icon, "התנתק"));
        }

        if (activity == CHAT_ACTIVITY) {
            activityName.setText("ההודעות שלי");
            imageMenu.setImageResource(R.drawable.chat_icon2);

            btn.addView(createMenuButtons(context, R.color.publicDark, public_icon, "איזור\nציבורי"));
            btn.addView(createMenuButtons(context, R.color.personal, personal_icon, "איזור\nאישי"));
            btn.addView(createMenuButtons(context, R.color.darkGreen, book_icon, "הספרים שלי"));
            btn.addView(createMenuButtons(context, R.color.darkGray, home_icon, "תפריט ראשי"));
            btn.addView(createMenuButtons(context, R.color.red, logout_icon, "התנתק"));
        }


        imageMenu.setOnClickListener(new View.OnClickListener() {

            {
                popupWindow = new PopupWindow(menu, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, false);
            }

            boolean isShowing = false;

            @Override
            public void onClick(View v) {
                if (isShowing == false){
                    popupWindow.showAsDropDown(toolbar, 0, 0);
                }else{popupWindow.dismiss();}
                isShowing = !isShowing;
            }
        });
        return toolbar;
    }


    private LinearLayout createMenuButtons(Context context, int bgColor, final int imageIcon, String buttonText) {

        LinearLayout.LayoutParams allButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        LinearLayout button = new LinearLayout(context);
        button.setOrientation(LinearLayout.VERTICAL);
        button.setLayoutParams(allButtonParams);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button.setBackgroundColor(context.getResources().getColor(bgColor, null));
        } else {
            button.setBackgroundColor(context.getResources().getColor(bgColor));
        }

        ImageView icon = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            icon.setImageDrawable(context.getResources().getDrawable(imageIcon, null));
        } else {
            icon.setImageDrawable(context.getResources().getDrawable(imageIcon));
        }

        iconParams.setMargins(15, 15, 15, 5);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setLayoutParams(iconParams);
        button.addView(icon);

        TextView text = new TextView(context);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 3);
        textParams.setMargins(0, 0, 0, 15);
        text.setLayoutParams(textParams);
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        text.setText(buttonText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            text.setTextColor(context.getResources().getColor(R.color.white, null));
        } else {
            text.setTextColor(context.getResources().getColor(R.color.white));
        }

        text.setTextSize(14);

        button.addView(text);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAction(imageIcon);
            }
        });


        return button;
    }


    private void buttonAction(int imageIcon) {

        if (imageIcon == public_icon) {

            popupWindow.dismiss();
            Intent intent = new Intent(getApplicationContext(),PublicActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        if (imageIcon == personal_icon) {

            popupWindow.dismiss();
            Intent intent = new Intent(getApplicationContext(),UpdateUser.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        if (imageIcon == book_icon) {

            popupWindow.dismiss();
            Intent intent = new Intent(getApplicationContext(),MyBooksActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        if (imageIcon == chat_icon) {

            popupWindow.dismiss();
            Intent intent = new Intent(getApplicationContext(),ChatMain.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        if (imageIcon == home_icon) {

            popupWindow.dismiss();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }

        if (imageIcon == logout_icon) {

            popupWindow.dismiss();

            settings = getSharedPreferences(getString(R.string.sharedPref),Context.MODE_PRIVATE);
            editor = settings.edit();
            editor.remove(String.valueOf(R.string.sharedPrefUserName));
            editor.remove(String.valueOf(R.string.sharedPrefUserPass));
            editor.remove(String.valueOf(R.string.sharedPrefUserCountyId));
            editor.remove(String.valueOf(R.string.sharedPrefUserCityId));
            editor.remove(String.valueOf(R.string.sharedPrefUserCity));
            editor.remove(String.valueOf(R.string.sharedPrefUserId));
            editor.commit();
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }
    }
}


