package com.college.geteat.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.college.geteat.R;
import com.college.geteat.activities.MainScreenActivity;
import com.college.geteat.entity.Client;
import com.college.geteat.utils.DB_Keys;
import com.college.geteat.utils.Utils;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.Objects;

public class Fragment_Login extends Fragment {


    private float alpha, translationBottom = 0, translationTop = 0;
    int foodIndex = 0;
    private static final int RC_SIGN_IN = 1111;
    private FirebaseUser user;


    public Fragment_Login() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Log.i("pttt", "onCreate: entered login");
        initAnimations(view);


        if (user != null) {
            Utils.uid = user.getUid();
            openApp();
        } else {
            startLogin();
        }

        return view;
    }

    public void initAnimations(View view) {

        String logo = "<font color=#000000>Get</font><font color=#DA1A1A>E</font><font color=#000000>at</font>";
        TextView login_TXT_logo = view.findViewById(R.id.login_TXT_logo);

        login_TXT_logo.setText(Html.fromHtml(logo));
//        alphaAnim(init_TXT_logo);


        ImageView login_IMG_courier = view.findViewById(R.id.login_IMG_courier);
        deliveryAnim(login_IMG_courier);

        ImageView login_IMG_food = view.findViewById(R.id.login_IMG_food);
        alphaFoodAnim(login_IMG_food);
    }

    private void startLogin() {

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder().build()))
                        .build(), RC_SIGN_IN);
    }

    private void openApp() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        Utils.uid = user.getUid();
        FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.USERS, DB_Keys.CLIENTS, Utils.uid))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i("pttt", "onDataChange: " + snapshot);
                        if (snapshot.exists()) {
                            if (snapshot.getValue(Client.class) != null) {
                                startMainScreenActivity();
                                return;
                            }
                        }
                        openDetailsFrag();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.i("pttt", "DatabaseError->: " + error.getDetails());
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {

                openApp();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.no_internet_connection);
                    return;
                }

                showSnackbar(R.string.unknown_error);
                Log.i("pttt", "Sign-in error: ", response.getError());
            }
        }
    }

    public void showSnackbar(int id) {
        Toast.makeText(requireContext(), getResources().getText(id), Toast.LENGTH_SHORT).show();
    }


    private void startMainScreenActivity() {
        Utils.startNewActivity(requireActivity(), MainScreenActivity.class);
    }


    public void openDetailsFrag() {

        Fragment_LoginDirections.ActionFragmentLogin2ToFragmentDetailsForm action = Fragment_LoginDirections
                .actionFragmentLogin2ToFragmentDetailsForm(Utils.uid);

        NavHostFragment.findNavController(this).navigate(action);

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


}