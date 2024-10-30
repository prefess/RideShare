package com.example.rideshare.fragment;

import android.app.Dialog;
import android.content.Intent;
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
import android.widget.Spinner;

import com.example.rideshare.R;
import com.example.rideshare.activity.AdminActivity;
import com.example.rideshare.activity.Home;

import com.example.rideshare.utils.AndroidUtil;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;


public class PhoneInputFragment extends Fragment {

    private Spinner countryCodeSpinner;
    private EditText phoneInput;
    private FirebaseAuth mAuth;
    private Dialog dialog;

    private DatabaseReference mDatabase;
    private static final String TAG = "PhoneInputFragment";
    private static final String ADMIN = "999999999";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone_input, container, false);

        countryCodeSpinner = view.findViewById(R.id.country_code_spinner);
        phoneInput = view.findViewById(R.id.phone_input);
        Button nextButton = view.findViewById(R.id.next_button);
        mAuth = FirebaseAuth.getInstance();


        mDatabase = FirebaseDatabase.getInstance().getReference();


        nextButton.setOnClickListener(v -> {
            dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.loading_dialog);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.primary_text_dark_nodisable);
            dialog.show();
            String phoneNumber = phoneInput.getText().toString().trim();
            String countryCode = countryCodeSpinner.getSelectedItem().toString();
            String fullPhoneNumber = countryCode + phoneNumber;

            if (phoneNumber.isEmpty()) {
                Log.d(TAG, "Phone number is empty");
                AndroidUtil.showToast(getContext(), "Phone number cannot be empty");
                dialog.dismiss();
            } else if (phoneNumber.length() != 9) {
                Log.d(TAG, "Phone number length is not 9 digits");
                AndroidUtil.showToast(getContext(), "Phone number must be exactly 9 digits");
                dialog.dismiss();
            } else {
                if (phoneNumber.equalsIgnoreCase(ADMIN)) {
                    Intent intent = new Intent(getContext(), AdminActivity.class);
                    startActivity(intent);
                } else {
                    checkPhoneNumber(fullPhoneNumber);
                }
            }
        });

        return view;
    }

    private void checkPhoneNumber(String phoneNumber) {
        mDatabase.child("Users").child("Customer").orderByChild("phoneNumber").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "Phone number exists, navigating to OTPVerificationFragment");
                    sendVerificationCode(phoneNumber);
                } else {
                    Log.d(TAG, "Phone number does not exist, navigating to RegisterFragment");
                    // Phone number does not exist, go to RegisterFragment
                    dialog.dismiss();
                    ((Home) getActivity()).goToRegisterFragment(phoneNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(getActivity())
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                // Auto-retrieval or instant verification succeeded
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                AndroidUtil.showToast(getContext(), "OTP verification failed");
                                Log.e("PhoneInputFragment", "Verification failed", e);
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                AndroidUtil.showToast(getContext(), "OTP sent successfully");
                                dialog.dismiss();
                                ((Home) getActivity()).goToOTPVerificationFragment(verificationId, phoneNumber);
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}