package com.example.rideshare.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.rideshare.R;
import com.example.rideshare.activity.LocationPickerActivity;
import com.example.rideshare.activity.RideActivity;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BookARideFragment extends Fragment {

    private static final int ORIGIN_LOCATION_PICKER_REQUEST_CODE = 1;
    private static final int DESTINATION_LOCATION_PICKER_REQUEST_CODE = 2;
    private Button searchButton;
    private EditText editTextSelectDateTime;
    private Spinner spinnerPerson;
    private AutoCompleteTextView autoCompleteLeavingFrom;
    private AutoCompleteTextView autoCompleteGoingTo;
    private Calendar calendar;
    private Dialog loadingDialog;
    private double startPointLat, startPointLng, endPointLat, endPointLng;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_a_ride, container, false);

        initializeLoadingDialog();

        searchButton = view.findViewById(R.id.search_button);
        editTextSelectDateTime = view.findViewById(R.id.editTextSelectDateTime);

        spinnerPerson = view.findViewById(R.id.spinnerPerson);
        autoCompleteLeavingFrom = view.findViewById(R.id.autoCompleteLeavingFrom);

        autoCompleteLeavingFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LocationPickerActivity.class);
                startActivityForResult(intent, ORIGIN_LOCATION_PICKER_REQUEST_CODE);
            }
        });

        autoCompleteGoingTo = view.findViewById(R.id.autoCompleteGoingTo);
        autoCompleteGoingTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LocationPickerActivity.class);
                startActivityForResult(intent, DESTINATION_LOCATION_PICKER_REQUEST_CODE);
            }
        });


        calendar = Calendar.getInstance();

//        setupAutoCompleteTextView(autoCompleteLeavingFrom);
//        setupAutoCompleteTextView(autoCompleteGoingTo);

        editTextSelectDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

//        editTextSelectDateTime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDateTimePicker();
//            }
//        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.person_array_book, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPerson.setAdapter(adapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInputValid()) {
                    navigateToRideActivity();
                }
            }
        });

        return view;
    }

    private void initializeLoadingDialog() {
        if (getContext() != null) {
            loadingDialog = new Dialog(getContext());
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            loadingDialog.setContentView(R.layout.loading_dialog);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            loadingDialog.setCancelable(false); // Prevents the dialog from being dismissed by the user
        }
    }

    private void showLoadingDialog() {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showDateTimePicker() {
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    calendar.set(year1, month1, dayOfMonth);
                    showTimePicker();
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Disable past dates
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minute1) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute1);

                    String dateTime = String.format("%02d-%02d-%04d %02d:%02d",
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE));

                    editTextSelectDateTime.setText(dateTime);
                }, hour, minute, false);

        timePickerDialog.show();
    }

    private boolean isInputValid() {
        boolean isValid = true;
        if (autoCompleteLeavingFrom.getText().toString().trim().isEmpty()) {
            autoCompleteLeavingFrom.setError("Please enter a starting point");
            isValid = false;
        }
        if (autoCompleteGoingTo.getText().toString().trim().isEmpty()) {
            autoCompleteGoingTo.setError("Please enter a destination");
            isValid = false;
        }
        if (editTextSelectDateTime.getText().toString().trim().isEmpty()) {
            editTextSelectDateTime.setError("Please select a date and time");
            isValid = false;
        }
        return isValid;
    }

    private void navigateToRideActivity() {
        Intent intent = new Intent(getActivity(), RideActivity.class);
        String origin = autoCompleteLeavingFrom.getText().toString().trim();
        String  destination =autoCompleteGoingTo.getText().toString().trim();

        double originLat = startPointLat;
        double originLng = startPointLng;
        double destinationLat = endPointLat;
        double destinationLng = endPointLng;

        intent.putExtra("originLat", originLat);
        intent.putExtra("originLng", originLng);
        intent.putExtra("destinationLat", destinationLat);
        intent.putExtra("destinationLng", destinationLng);

        intent.putExtra("origin", origin);
        intent.putExtra("destination", destination);
        intent.putExtra("dateTime", editTextSelectDateTime.getText().toString());
        intent.putExtra("personCount", spinnerPerson.getSelectedItem().toString());
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            String selectedAddress = data.getStringExtra("selectedAddress");
            double latitude = data.getDoubleExtra("latitude", 0.0);
            double longitude = data.getDoubleExtra("longitude", 0.0);

            if (requestCode == ORIGIN_LOCATION_PICKER_REQUEST_CODE) {
                autoCompleteLeavingFrom.setText(selectedAddress);
                startPointLat = latitude;
                startPointLng = longitude;
            } else if (requestCode == DESTINATION_LOCATION_PICKER_REQUEST_CODE) {
                autoCompleteGoingTo.setText(selectedAddress);
                endPointLat = latitude;
                endPointLng = longitude;
            }
        }
    }
}