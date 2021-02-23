package com.college.geteat.activities;

import android.os.Bundle;
import android.text.Html;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.college.geteat.R;
import com.college.geteat.utils.Utils;

public class InitActivity extends AppCompatActivity {

    private TextView init_TXT_logo;
    private ImageView init_IMG_courier, init_IMG_food;
    private String logo;
    private float alpha, translationBottom = 0, translationTop = 0;

    int foodIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        Utils.fullScreen(getWindow());
        initAnimations();


    }


    public void initAnimations() {

        logo = "<font color=#000000>Get</font><font color=#DA1A1A>E</font><font color=#000000>at</font>";
        init_TXT_logo = findViewById(R.id.init_TXT_logo);

        init_TXT_logo.setText(Html.fromHtml(logo));
//        alphaAnim(init_TXT_logo);


        init_IMG_courier = findViewById(R.id.init_IMG_courier);
        deliveryAnim(init_IMG_courier);

        init_IMG_food = findViewById(R.id.init_IMG_food);
        alphaFoodAnim(init_IMG_food);
    }


    private void alphaFoodAnim(final ImageView food) {


        AlphaAnimation animation1 = new AlphaAnimation(alpha, 1 - alpha);
        animation1.setDuration(1500);
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationEnd(Animation anim) {
                alpha = 1 - alpha;
                if (alpha == 0) {
                    food.setImageResource(Utils.resArray[foodIndex]);
                    if (foodIndex >= Utils.resArray.length - 1) {
                        foodIndex = 0;
                    } else {
                        foodIndex++;
                    }
                }
                alphaFoodAnim(food);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });


        food.startAnimation(animation1);
    }


    private void deliveryAnim(final ImageView img) {


        TranslateAnimation animation1 = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0,
                Animation.RELATIVE_TO_PARENT, 1.0f,
                Animation.RELATIVE_TO_PARENT, translationBottom,
                Animation.RELATIVE_TO_PARENT, translationTop);
        animation1.setDuration(3500);
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationEnd(Animation anim) {
                translationBottom = (float) Math.random();
                translationTop = (float) Math.random();
                deliveryAnim(img);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });


        img.startAnimation(animation1);
    }


    private void alphaAnim(final TextView tv) {

        tv.setText(Html.fromHtml(logo));
        AlphaAnimation animation1 = new AlphaAnimation(alpha, 1 - alpha);
        animation1.setDuration(1500);
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationEnd(Animation anim) {
                alpha = 1 - alpha;
                alphaAnim(tv);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });


        tv.startAnimation(animation1);
    }


}