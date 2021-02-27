package com.college.geteat.fragments;

import android.os.Bundle;
import android.text.Html;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.college.geteat.R;
import com.college.geteat.entity.Client;
import com.college.geteat.entity.Courier;
import com.college.geteat.utils.DB_Keys;
import com.college.geteat.utils.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Fragment_DetailsUpdate extends Fragment {
    private float alpha, translationBottom = 0, translationTop = 0;
    private MaterialButton anim_BTN_clients;
    private TextInputLayout anim_LAY_name;
    private TextInputEditText anim_EDT_name;
    private boolean isNextBTN = false;
    private String name;

    int foodIndex = 0;

    public Fragment_DetailsUpdate() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_details_update, container, false);
        anim_BTN_clients = view.findViewById(R.id.anim_BTN_clients);
        anim_LAY_name = view.findViewById(R.id.anim_LAY_name);
        anim_EDT_name = view.findViewById(R.id.anim_EDT_name);


        anim_BTN_clients.setOnClickListener(v -> {
            if (!isNextBTN) {

                anim_LAY_name.setVisibility(View.VISIBLE);
                anim_BTN_clients.setText(R.string.next);

            } else {
                if ((name = Objects.requireNonNull(anim_EDT_name.getText()).toString()).isEmpty()) {
                    Toast.makeText(requireContext(), "Please Press Back If Don't Want To Update", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference courierRef = FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.USERS
                            , DB_Keys.COURIERS
                            , DB_Keys.COURIERS_PROFILES
                            , FirebaseAuth.getInstance().getUid()));

                    courierRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Courier courier = snapshot.getValue(Courier.class);

                                assert courier != null;
                                courier.setName(name);
                                courierRef.setValue(courier);

                                DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.USERS
                                        , DB_Keys.CLIENTS
                                        , FirebaseAuth.getInstance().getUid()));

                                clientRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {

                                            Client client = snapshot.getValue(Client.class);
                                            assert client != null;
                                            client.setName(name);
                                            clientRef.setValue(client);
                                            Navigation.findNavController(v).navigate(R.id.action_ordersFragment_to_fragment_MainScreen);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            isNextBTN = true;
        });

        initAnimations(view);


        return view;
    }

    public void initAnimations(View view) {

        String logo = "<font color=#000000>Get</font><font color=#DA1A1A>E</font><font color=#000000>at</font>";
        TextView anim_TXT_logo = view.findViewById(R.id.anim_TXT_logo);

        anim_TXT_logo.setText(Html.fromHtml(logo));
//        alphaAnim(init_TXT_logo);


        ImageView anim_IMG_courier = view.findViewById(R.id.anim_IMG_courier);
        deliveryAnim(anim_IMG_courier);

        ImageView anim_IMG_food = view.findViewById(R.id.anim_IMG_food);
        alphaFoodAnim(anim_IMG_food);
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