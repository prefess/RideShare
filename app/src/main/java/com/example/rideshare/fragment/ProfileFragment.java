package com.example.rideshare.fragment;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.rideshare.R;
import com.example.rideshare.activity.DriverProfileActivity;
import com.example.rideshare.activity.DriverRegisterActivity;
import com.example.rideshare.activity.EditProfileActivity;
import com.example.rideshare.activity.Home;
import com.example.rideshare.activity.MainActivity;
import com.example.rideshare.activity.VehicleListActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class ProfileFragment extends Fragment {

    private TextView profileName, manageVehicle, profileEmail;
    private ImageView editProfile, profileImage;
    private LinearLayout logoutIcon;
    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase, mDriverDatabase;
    private String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        profileImage = view.findViewById(R.id.profile_image);
        editProfile = view.findViewById(R.id.edit_profile);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        manageVehicle = view.findViewById(R.id.manage_vehicle_text);
        logoutIcon = view.findViewById(R.id.logout_layout); // Change from `logout_text` to `logout_layout`

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userID = currentUser.getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(userID);
        String userId = mAuth.getCurrentUser().getUid();
        Log.d("UID", userId);
        mDriverDatabase = mDriverDatabase.child("Users").child("Drivers").child(userId);
        getUserInfo();

        LinearLayout driverProfileLayout = view.findViewById(R.id.driver_profile_layout);
        driverProfileLayout.setOnClickListener(v-> checkIfDriver());
        manageVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDriverDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Intent intent = new Intent(getContext(), VehicleListActivity.class);
                            startActivity(intent);
                        } else {
                            showDriverRegisterDialog();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle possible errors.
                    }
                });
            }
        });

        logoutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });

        return view;
    }

    private void getUserInfo() {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                profileName.setText(name);
                profileEmail.setText(email);

                // Load profile image if it exists
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(ProfileFragment.this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_profile) // Placeholder image
                            .circleCrop() // This will make the image circular
                            .into(profileImage);
                } else {
                    // Set default image if no profile image exists
                    profileImage.setImageResource(R.drawable.ic_profile);
                }

                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Log.d(TAG, "User logged out, stopping Firebase and navigating to MainActivity");
        Intent intent = new Intent(getActivity(), Home.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void checkIfDriver() {
        mDriverDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Intent intent = new Intent(getActivity(), DriverProfileActivity.class);
                    startActivity(intent);
                } else {
                    showDriverRegisterDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }

    private void showDriverRegisterDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Driver Register")
                .setMessage("Do you want to become a driver?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getActivity(), DriverRegisterActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", null) // Dismiss the dialog if "No" is clicked
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }
}