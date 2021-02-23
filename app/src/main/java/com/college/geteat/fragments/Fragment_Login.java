package com.college.geteat.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.college.geteat.R;
import com.college.geteat.activities.MainScreenActivity;
import com.college.geteat.entity.Courier;
import com.college.geteat.utils.DB_Keys;
import com.college.geteat.utils.SharedPreferencesManager;
import com.college.geteat.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Fragment_Login extends Fragment implements View.OnTouchListener {


    private enum AUTH_STATE {
        PHONE,
        CODE
    }

    private int windowWidth, windowHeight;
    int _xDelta;
    int _yDelta;
    private boolean isOutReported = false;

    private AUTH_STATE state = AUTH_STATE.PHONE;
    private String phoneInput;
    private ImageView login_IMG_back;
    private ViewGroup viewGroup;
    private TextInputLayout login_EDT_input;
    private MaterialButton login_BTN_next;
    private FirebaseUser user;
    private View mainView;
    private ValueEventListener fetchClientEventListener;
    private ValueEventListener fetchCourierEventListener;


    public Fragment_Login() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetchClientEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("pttt", "onDataChange: " + snapshot);
                if (snapshot.getValue() != null) {
                    SharedPreferencesManager.write(DB_Keys.DB_STATE, DB_Keys.STATE.FULL_REGISTERED.name());
                    FirebaseDatabase.getInstance()
                            .getReference(Utils.generateFireBaseDBPath(DB_Keys.USERS, DB_Keys.COURIERS, DB_Keys.COURIERS_PROFILES, user.getUid()))
                            .addListenerForSingleValueEvent(fetchCourierEventListener);
//                    Utils.startNewActivity(requireActivity(), MainScreenActivity.class);
                } else {

                    openDetailsFrag(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("pttt", "DatabaseError->: " + error.getDetails());
            }
        };

        fetchCourierEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Utils.currentCourier = null;
                if (snapshot.getValue() != null) {
                    Utils.currentCourier = snapshot.getValue(Courier.class);
                }
                startMainScreenActivity();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Log.i("pttt", "onCreate: entered login");

        user = FirebaseAuth.getInstance().getCurrentUser();


        if (user != null) {
            Utils.uid = user.getUid();
            directUser(view);
        } else { // No user is signed in
            initFragment(view);
        }


        return view;
    }


    public void initFragment(View view) {


        initUI(view);
        updateUI();
    }


    public void checkUserRecord() {
        FirebaseDatabase.getInstance().getReference(Utils.generateFireBaseDBPath(DB_Keys.USERS, DB_Keys.CLIENTS, user.getUid(), DB_Keys.CLIENT))
                .addListenerForSingleValueEvent(fetchClientEventListener);
    }

    public void directUser(View view) {
        String state = SharedPreferencesManager.read(DB_Keys.DB_STATE, "invalid");

        if (state.equals(DB_Keys.STATE.AUTH_REGISTERED.name())) {
            checkUserRecord();
        }
        if (state.equals(DB_Keys.STATE.FULL_REGISTERED.name())) {
            startMainScreenActivity();
        }
        if (state.equals("invalid")) {
            if (!user.getUid().isEmpty()) {
                SharedPreferencesManager.write(DB_Keys.DB_STATE, DB_Keys.STATE.AUTH_REGISTERED.name());
            } else {
                user.delete();
                FirebaseAuth.getInstance().signOut();
                initFragment(view);
            }
        }
    }

    private void startMainScreenActivity() {
//        SharedPreferencesManager.write(DB_Keys.UID, user.getUid());
        Utils.startNewActivity(requireActivity(), MainScreenActivity.class);
    }


    private void findViews() {
        login_BTN_next = mainView.findViewById(R.id.login_BTN_next);
        login_EDT_input = mainView.findViewById(R.id.login_EDT_input);
        login_IMG_back = mainView.findViewById(R.id.login_IMG_back);

    }

    public void openDetailsFrag(FirebaseUser user) {

        Fragment_LoginDirections.ActionFragmentLogin2ToFragmentDetailsForm action = Fragment_LoginDirections.actionFragmentLogin2ToFragmentDetailsForm(user.getUid());
        NavHostFragment.findNavController(this).navigate(action);

    }

    private void initUI(View view) {
        mainView = view;
//        Utils.hideNavigationBar(getActivity().getWindow());
        findViews();
        Glide
                .with(this)
                .load(R.drawable.img_background_portrait)
                .circleCrop()
                .into(login_IMG_back);

        login_BTN_next.setOnClickListener(v -> nextClicked());

        movingBackground(login_IMG_back);
    }

    private void nextClicked() {
        if (state == AUTH_STATE.PHONE) {
            login_BTN_next.setEnabled(false);
            phoneInput = Objects.requireNonNull(login_EDT_input.getEditText()).getText().toString();
            Log.d("pttt", "phoneInput= " + phoneInput);

            if (phoneInput.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your phone number", Toast.LENGTH_LONG).show();
                updateUI();
                return;
            }
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneInput)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(requireActivity())
                    .setCallbacks(onVerificationStateChangedCallbacks)
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(options);


        } else if (state == AUTH_STATE.CODE) {
            login_BTN_next.setEnabled(false);
            String codeInput = Objects.requireNonNull(login_EDT_input.getEditText()).getText().toString();
            Log.i("pttt", "nextClicked: " + codeInput);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseAuthSettings firebaseAuthSettings = firebaseAuth.getFirebaseAuthSettings();
            firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneInput, codeInput);

            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneInput)
                    .setTimeout(30L, TimeUnit.SECONDS)
                    .setActivity(requireActivity())
                    .setCallbacks(onVerificationStateChangedCallbacks)
                    .build();

            PhoneAuthProvider.verifyPhoneNumber(options);

        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("pttt", "signInWithCredential:success");
                            SharedPreferencesManager.write(DB_Keys.DB_STATE, DB_Keys.STATE.AUTH_REGISTERED.name());
                            user = task.getResult().getUser();
                            checkUserRecord();

                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("pttt", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getContext(), "Wrong Code", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks onVerificationStateChangedCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            Log.d("pttt", "onCodeSent");
            state = AUTH_STATE.CODE;
            updateUI();
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            Log.d("pttt", "onCodeAutoRetrievalTimeOut ");
            super.onCodeAutoRetrievalTimeOut(s);

            state = AUTH_STATE.PHONE;
            updateUI();
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            Log.d("pttt", "PhoneAuthCredential");
            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.d("pttt", "FirebaseException " + e.getMessage());
            updateUI();
        }
    };


    private void updateUI() {
        if (state == AUTH_STATE.PHONE) {
            login_BTN_next.setEnabled(true);
            login_EDT_input.setPlaceholderText("055 1234567");
            Objects.requireNonNull(login_EDT_input.getEditText()).setText("");
            login_EDT_input.setHint(getString(R.string.phone_number));
            login_BTN_next.setText(getString(R.string.next));
        } else if (state == AUTH_STATE.CODE) {
            login_BTN_next.setEnabled(true);
            login_EDT_input.setPlaceholderText("*** ***");
            Objects.requireNonNull(login_EDT_input.getEditText()).setText("");
            login_EDT_input.setHint(getString(R.string.verification_code));
            login_BTN_next.setText(getString(R.string.ok));
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {


        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();

        // Check if the image view is out of the parent view and report it if it is.
        // Only report once the image goes out and don't stack toasts.
        if (isOut(view)) {
            if (!isOutReported) {
                isOutReported = true;
                Toast.makeText(getContext(), "OUT", Toast.LENGTH_SHORT).show();
            }
        } else {
            isOutReported = false;
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // _xDelta and _yDelta record how far inside the view we have touched. These
                // values are used to compute new margins when the view is moved.
                _xDelta = X - view.getLeft();
                _yDelta = Y - view.getTop();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                // Do nothing
                break;
            case MotionEvent.ACTION_MOVE:
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view
                        .getLayoutParams();
                // Image is centered to start, but we need to unhitch it to move it around.
                lp.removeRule(RelativeLayout.CENTER_HORIZONTAL);
                lp.removeRule(RelativeLayout.CENTER_VERTICAL);
                lp.leftMargin = X - _xDelta;
                lp.topMargin = Y - _yDelta;
                // Negative margins here ensure that we can move off the screen to the right
                // and on the bottom. Comment these lines out and you will see that
                // the image will be hemmed in on the right and bottom and will actually shrink.
                lp.rightMargin = view.getWidth() - lp.leftMargin - windowWidth;
                lp.bottomMargin = view.getHeight() - lp.topMargin - windowHeight;
                view.setLayoutParams(lp);
                break;
        }
        // invalidate is redundant if layout params are set or not needed if they are not set.
//        mRrootLayout.invalidate();
        return true;
    }

    private boolean isOut(View view) {
        // Check to see if the view is out of bounds by calculating how many pixels
        // of the view must be out of bounds to and checking that at least that many
        // pixels are out.
        float percentageOut = 0.50f;
        int viewPctWidth = (int) (view.getWidth() * percentageOut);
        int viewPctHeight = (int) (view.getHeight() * percentageOut);

        return ((-view.getLeft() >= viewPctWidth) ||
                (view.getRight() - windowWidth) > viewPctWidth ||
                (-view.getTop() >= viewPctHeight) ||
                (view.getBottom() - windowHeight) > viewPctHeight);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void movingBackground(View motionView) {


        // We are interested when the image view leaves its parent RelativeLayout
        // container and not the screen, so the following code is not needed.
//        DisplayMetrics displaymetrics = new DisplayMetrics();
//        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//        windowwidth = displaymetrics.widthPixels;
//        windowheight = displaymetrics.heightPixels;

        viewGroup = mainView.findViewById(R.id.login_FRAG_lay);

        // These these following 2 lines that address layoutparams set the width
        // and height of the ImageView to 150 pixels and, as a side effect, clear any
        // params that will interfere with movement of the ImageView.
        // We will rely on the XML to define the size and avoid anything that will
        // interfere, so we will comment these lines out. (You can test out how a layout parameter
        // can interfere by setting android:layout_centerInParent="true" in the ImageView.
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(150, 150);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(150, 150);
//        mImageView.setLayoutParams(layoutParams);
        motionView.setOnTouchListener(this);

        // Capture the width of the RelativeLayout once it is laid out.
        viewGroup.post(() -> {
            windowWidth = viewGroup.getWidth();
            windowHeight = viewGroup.getHeight();
        });

    }
}