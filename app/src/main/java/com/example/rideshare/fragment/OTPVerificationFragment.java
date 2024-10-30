package com.example.rideshare.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.rideshare.R;
import com.example.rideshare.activity.Home;
import com.example.rideshare.entity.User;
import com.example.rideshare.utils.AndroidUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OTPVerificationFragment extends Fragment {

    private EditText otpInput1, otpInput2, otpInput3, otpInput4, otpInput5, otpInput6;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextView phoneVerify;
    private Button verifyButton;
    private String verificationId;
    private String phoneNumber, name, email, password;
    private Dialog dialog;

    public OTPVerificationFragment() {

    }

    public OTPVerificationFragment(String verificationId, String phoneNumber) {
        this.verificationId = verificationId;
        this.phoneNumber = phoneNumber;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_otp_verification, container, false);

        otpInput1 = view.findViewById(R.id.otp_input_1);
        otpInput2 = view.findViewById(R.id.otp_input_2);
        otpInput3 = view.findViewById(R.id.otp_input_3);
        otpInput4 = view.findViewById(R.id.otp_input_4);
        otpInput5 = view.findViewById(R.id.otp_input_5);
        otpInput6 = view.findViewById(R.id.otp_input_6);
        verifyButton = view.findViewById(R.id.verify_button);
        phoneVerify = view.findViewById(R.id.text_phone_receive);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Retrieve the data from arguments
        Bundle args = getArguments();
        if (args != null) {
            name = args.getString("name");
            email = args.getString("email");
            password = args.getString("password");
            verificationId = args.getString("verificationId");
            phoneNumber = args.getString("phoneNumber");
        }
        phoneVerify.setText(String.format("Confirmation code has been sent to your mobile phone number %s", phoneNumber));

        otpInput1.addTextChangedListener(new OTPTextWatcher(otpInput1, otpInput2, null));
        otpInput2.addTextChangedListener(new OTPTextWatcher(otpInput2, otpInput3, otpInput1));
        otpInput3.addTextChangedListener(new OTPTextWatcher(otpInput3, otpInput4, otpInput2));
        otpInput4.addTextChangedListener(new OTPTextWatcher(otpInput4, otpInput5, otpInput3));
        otpInput5.addTextChangedListener(new OTPTextWatcher(otpInput5, otpInput6, otpInput4));
        otpInput6.addTextChangedListener(new OTPTextWatcher(otpInput6, null, otpInput5));

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.loading_dialog);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.primary_text_dark_nodisable);
                dialog.show();
                // Concatenate all the OTP input fields into a single string
                String code = otpInput1.getText().toString().trim() +
                        otpInput2.getText().toString().trim() +
                        otpInput3.getText().toString().trim() +
                        otpInput4.getText().toString().trim() +
                        otpInput5.getText().toString().trim() +
                        otpInput6.getText().toString().trim();

                // Check if the code is complete and the verification ID is not null
                if (code.length() == 6 && verificationId != null) {
                    verifyCode(code);  // Pass the complete OTP code for verification
                } else {
                    // Handle the case where the code is incomplete or verificationId is null
                    AndroidUtil.showToast(getContext(), "Please enter a valid 6-digit OTP code.");
                }
            }
        });

        return view;
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AndroidUtil.showToast(getContext(), "OTP verification successfully");

                        // Retrieve the data passed from RegisterFragment
//                        String name = ((Home) getActivity()).getUserName();  // Assuming you have a method to get this data from Home activity
//                        String email = ((Home) getActivity()).getUserEmail();
//                        String password = ((Home) getActivity()).getUserPassword();

                        // Store user data with the phone UID
                        if (name!=null && email !=null && phoneNumber!=null && password!=null) {
                            storeUserDataWithPhoneUid(name, email, phoneNumber, password);
                        }

                        dialog.dismiss();
                        ((Home) getActivity()).goToMainActivity();
                    } else {
                        AndroidUtil.showToast(getContext(), "Wrong OTP code");
                        // Handle error
                    }
                });
    }

    private void storeUserDataWithPhoneUid(String name, String email, String phoneNumber, String password) {
        // Generate a unique key for the user under the "Customer" node using the phone number UID
        String phoneUid = mAuth.getCurrentUser().getUid();  // This will return the user's UID linked to the phone number

        if (phoneUid == null) {
            return;
        }

        // Create a new User object with the provided details
        User user = new User(name, email, phoneNumber, password, null);

        // Store the user data in the "Users/Customer" node
        mDatabase.child("Users").child("Customer").child(phoneUid).setValue(user)
                .addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {

                    } else {

                    }
                });
    }

    private class OTPTextWatcher implements TextWatcher {

        private View currentView;
        private View nextView;
        private View previousView;

        public OTPTextWatcher(View currentView, View nextView, View previousView) {
            this.currentView = currentView;
            this.nextView = nextView;
            this.previousView = previousView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            } else if (s.length() == 0 && previousView != null) {
                previousView.requestFocus();
            }

            checkAndEnableButton();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    private void checkAndEnableButton() {
        if (!otpInput1.getText().toString().trim().isEmpty() &&
                !otpInput2.getText().toString().trim().isEmpty() &&
                !otpInput3.getText().toString().trim().isEmpty() &&
                !otpInput4.getText().toString().trim().isEmpty() &&
                !otpInput5.getText().toString().trim().isEmpty() &&
                !otpInput6.getText().toString().trim().isEmpty()) {
            verifyButton.setEnabled(true);
        } else {
            verifyButton.setEnabled(false);
        }
    }

}