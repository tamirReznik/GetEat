package com.college.geteat.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.college.geteat.R;
import com.college.geteat.entity.Courier;

public class Utils {

    public static final String PLACES_API_KEY = "AIzaSyBTy9tC0aBM8NZMKzTxodz81ce4UetHLFI";
    public static final String GoogleApiKey = "AIzaSyBwanXgSOXbeMpeNmnwAUfqqm9ocxw3LyI";
    public static final String ISRAEL_CODE = "" + "IL";
    public static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    public static final int SELECT_PICTURE_REQUEST_CODE = 10;
    public static final int RESULT_OK = -1;
    public static final int RESULT_CANCELED = 0;

    public static int[] resArray = {R.drawable.cake, R.drawable.salad, R.drawable.fast_food, R.drawable.pizza, R.drawable.pasta, R.drawable.hot_dog, R.drawable.diet};
    public static String uid;
    public static Courier currentCourier;


    // use on any phase of app lifecycle


    public static void hideNavigationBar(Window window) {
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    public static String generateFireBaseDBPath(String... args) {

        String path = "";
        for (String arg : args) {
            path = path.concat(arg).concat(DB_Keys.COMMA);
        }

        return path.substring(0, path.length() - DB_Keys.COMMA.length());

    }

    //return true if permission request pop up showed (no permissions before pop up)
    public static boolean checkLocationPermission(Activity activity, int code) {
        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, code);
            return true;
        }
        //return false if permissions already granted
        return false;
    }

    //return true if permission granted
    public static boolean isLocationPermGranted(Activity activity) {
        return ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static <T extends AppCompatActivity> void startNewActivity(Activity fromActivity, Class<T> toActivity) {
        Intent mainIntent = new Intent(fromActivity, toActivity);
        fromActivity.startActivity(mainIntent);
        fromActivity.finish();
    }

    public static void fullScreen(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = window.getInsetsController();

            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
                controller.hide(WindowInsets.Type.navigationBars());
            }
        } else {
            //noinspection deprecation
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                            View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }
    }


}
