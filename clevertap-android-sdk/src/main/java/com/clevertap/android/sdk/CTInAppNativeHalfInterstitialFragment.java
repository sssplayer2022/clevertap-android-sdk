package com.clevertap.android.sdk;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class CTInAppNativeHalfInterstitialFragment extends CTInAppBaseFullNativeFragment {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        ArrayList<Button> inAppButtons = new ArrayList<>();
        View inAppView = inflater.inflate(R.layout.inapp_half_interstitial, container, false);

        FrameLayout fl  = inAppView.findViewById(R.id.inapp_half_interstitial_frame_layout);

        RelativeLayout relativeLayout = fl.findViewById(R.id.half_interstitial_relative_layout);
        relativeLayout.setBackgroundColor(Color.parseColor(inAppNotification.getBackgroundColor()));
        LinearLayout linearLayout = relativeLayout.findViewById(R.id.half_interstitial_linear_layout);
        Button mainButton = linearLayout.findViewById(R.id.half_interstitial_button1);
        inAppButtons.add(mainButton);
        Button secondaryButton = linearLayout.findViewById(R.id.half_interstitial_button2);
        inAppButtons.add(secondaryButton);


        Bitmap image = inAppNotification.getImage();
        if (image != null) {
            ImageView imageView = relativeLayout.findViewById(R.id.backgroundImage);
            imageView.setImageBitmap(inAppNotification.getImage());
        }


        TextView textView1 = relativeLayout.findViewById(R.id.half_interstitial_title);
        textView1.setText(inAppNotification.getTitle());
        textView1.setTextColor(Color.parseColor(inAppNotification.getTitleColor()));

        TextView textView2 = relativeLayout.findViewById(R.id.half_interstitial_message);
        textView2.setText(inAppNotification.getMessage());
        textView2.setTextColor(Color.parseColor(inAppNotification.getMessageColor()));

        ArrayList<CTInAppNotificationButton> buttons = inAppNotification.getButtons();
        if (buttons != null && !buttons.isEmpty()) {
            for(int i=0; i < buttons.size(); i++) {
                if (i >= 2) continue; // only show 2 buttons
                CTInAppNotificationButton inAppNotificationButton = buttons.get(i);
                Button button = inAppButtons.get(i);
                setupInAppButton(button,inAppNotificationButton,inAppNotification,i);
            }
        }

        if(inAppNotification.getButtonCount()==1){
            secondaryButton.setVisibility(View.GONE);

        }

        fl.setBackgroundDrawable(new ColorDrawable(0xBB000000));

        CloseImageView closeImageView = fl.findViewById(199272);

        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didDismiss(null);
                getActivity().finish();
            }
        });

        if(!inAppNotification.isHideCloseButton()) {
            closeImageView.setVisibility(View.GONE);
        }
        else {
            closeImageView.setVisibility(View.VISIBLE);
        }

        return inAppView;
    }
}
