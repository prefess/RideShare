package com.example.rideshare.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.example.rideshare.R;
import com.example.rideshare.activity.Home;
import com.example.rideshare.entity.User;
import com.example.rideshare.utils.AndroidUtil;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.concurrent.TimeUnit;


public class RegisterFragment extends Fragment {

    private EditText nameInput, emailInput, passwordInput;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String phoneNumber;
    private Button registerButton;
    private Dialog dialog;
    private static final String TAG = "RegisterFragment";

    public RegisterFragment(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        nameInput = view.findViewById(R.id.name_input);
        emailInput = view.findViewById(R.id.email_input);
        passwordInput = view.findViewById(R.id.password_input);
        registerButton = view.findViewById(R.id.register_button);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        registerButton.setOnClickListener(v -> {
            dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.loading_dialog);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.primary_text_dark_nodisable);
            dialog.show();

            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            registerButton.setActivated(false);

            if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                registerUser(name, email, password);
            } else {
                Log.d(TAG, "Fields are empty");
                AndroidUtil.showToast(getContext(), "Fields are empty");
                dialog.dismiss();
                registerButton.setActivated(true);
            }
        });

        return view;
    }

    private void registerUser(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User registered successfully");
                        FirebaseUser user = mAuth.getCurrentUser();
                        AndroidUtil.showToast(getContext(), "Registered, OTP sent successfully");
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Log.d(TAG, "User profile updated");
//                                            MyApplication.setUserId(user.getUid());
//                                            storeUserData(name, email, phoneNumber, password);
                                            sendVerificationCode(phoneNumber, name, email, password);
                                        } else {
                                            Log.e(TAG, "Failed to update user profile", profileTask.getException());
                                            registerButton.setActivated(true);
                                        }
                                    });
                        }
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Log.e(TAG, "User already exists", task.getException());
                        } else {
                            Log.e(TAG, "Registration failed", task.getException());
                        }
                    }
                });
    }

    private void storeUserData(String name, String email, String phoneNumber, String password) {
        // Generate a unique key for the user under the "Customer" node
        String userId = mAuth.getCurrentUser().getUid();

        if (userId == null) {
            Log.e(TAG, "Failed to generate user ID");
            return;
        }

        // Create a new User object with the provided details
        User user = new User(name, email, phoneNumber, password, null);

        mDatabase.child("Users").child("Customer").child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User data stored successfully");
                    } else {
                        Log.e(TAG, "Failed to store user data", task.getException());
                    }
                });
    }
    private void sendVerificationCode(String phoneNumber, String name, String email, String password) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(getActivity())
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                Log.d(TAG, "Verification completed");
                                dialog.dismiss();
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Log.e(TAG, "Verification failed", e);
                                dialog.dismiss();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                Log.d(TAG, "Code sent: " + verificationId);

                                // Create a new instance of OTPVerificationFragment
                                OTPVerificationFragment otpFragment = new OTPVerificationFragment();

                                // Create a Bundle to pass the data
                                Bundle args = new Bundle();
                                args.putString("name", name);
                                args.putString("email", email);
                                args.putString("password", password);
                                args.putString("verificationId", verificationId);
                                args.putString("phoneNumber", phoneNumber);

                                // Attach the arguments to the fragment
                                otpFragment.setArguments(args);

                                dialog.dismiss();
                                // Transition to the OTPVerificationFragment
                                ((Home) getActivity()).goToOTPVerificationFragment(otpFragment);
//                                storeUserDataWithPhoneUid(name, email, phoneNumber, password);
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

//    private void storeUserDataWithPhoneUid(String name, String email, String phoneNumber, String password) {
//        // Sign in with the credential to get the phone number UID
//        mAuth.getCurrentUser().linkWithCredential(PhoneAuthProvider.getCredential(phoneNumber, ""))
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        String phoneUid = task.getResult().getUser().getUid();
//                        User user = new User(name, email, phoneNumber, password, null);
//
//                        mDatabase.child("Users").child("Customer").child(phoneUid).setValue(user)
//                                .addOnCompleteListener(dbTask -> {
//                                    if (dbTask.isSuccessful()) {
//                                        Log.d(TAG, "User data stored successfully with phone UID");
//                                    } else {
//                                        Log.e(TAG, "Failed to store user data", dbTask.getException());
//                                    }
//                                });
//                    } else {
//                        Log.e(TAG, "Failed to link phone credential", task.getException());
//                    }
//                });
//    }
}