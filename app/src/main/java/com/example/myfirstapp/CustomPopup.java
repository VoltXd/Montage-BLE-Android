package com.example.myfirstapp;

import android.app.Activity;
import android.app.Dialog;
import android.widget.Button;
import android.widget.TextView;

public class CustomPopup extends Dialog
{
    // Fields
    private String title;
    private String subTitle;
    private Button yesButton, noButton;
    private TextView titleView, subTitleView;

    // Constructor
    public CustomPopup(Activity activity)
    {
        super(activity, androidx.appcompat.R.style.Theme_AppCompat_DayNight_Dialog);
        setContentView(R.layout.my_popup_template);

        title = "Titre par défaut";
        subTitle = "Sous-titre par défaut";
        yesButton = findViewById(R.id.yesButton);
        noButton = findViewById(R.id.noButton);
        titleView = findViewById(R.id.title);
        subTitleView = findViewById(R.id.subTitle);
    }

    public void setTitle(String title) { this.title = title; }
    public void setSubTitle(String subTitle) { this.subTitle = subTitle; }
    public Button getYesButton() { return yesButton; }
    public Button getNoButton() { return noButton; }

    public void build()
    {
        show();
        titleView.setText(title);
        subTitleView.setText(subTitle);
    }
}
