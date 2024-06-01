package com.example.buspassadmin.Home.Edit;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.buspassadmin.Home.TicketsFragment;
import com.example.buspassadmin.Home.UpdatesFragment;
import com.example.buspassadmin.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditUpdateFragment extends Fragment {

    private TextView titleUpdateEdit;
    private TextView messageUpdateEdit;
    private TextView startingTimeUpdateEdit;
    private TextView endingTimeUpdateEdit;
    private EditText titleETUpdateEdit;
    private EditText messageETUpdateEdit;
    private EditText startingTimeETUpdateEdit;
    private EditText endingTimeETUpdateEdit;
    private Button submitBtnUpdateEdit;
    private ImageButton backBtnAddUpdateEdit;
    private ProgressBar progressBarUpdateEdit;

    private String updateId;

    public EditUpdateFragment() {
        // Required empty public constructor
    }

    public static EditUpdateFragment newInstance(String updateId) {
        EditUpdateFragment fragment = new EditUpdateFragment();
        Bundle args = new Bundle();
        args.putString("updateId", updateId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            updateId = getArguments().getString("updateId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        titleUpdateEdit = view.findViewById(R.id.titleUpdateEdit);
        messageUpdateEdit = view.findViewById(R.id.messageUpdateEdit);
        startingTimeUpdateEdit = view.findViewById(R.id.startingTimeUpdateEdit);
        endingTimeUpdateEdit = view.findViewById(R.id.endingTimeUpdateEdit);
        titleETUpdateEdit = view.findViewById(R.id.titleETUpdateEdit);
        messageETUpdateEdit = view.findViewById(R.id.messagaeETUpdateEdit);
        startingTimeETUpdateEdit = view.findViewById(R.id.startingTimeETUpdateEdit);
        endingTimeETUpdateEdit = view.findViewById(R.id.endingTimeETUpdateEdit);
        submitBtnUpdateEdit = view.findViewById(R.id.submitBtnUpdateEdit);
        backBtnAddUpdateEdit = view.findViewById(R.id.backBtnAddUpdateEdit);
        progressBarUpdateEdit = view.findViewById(R.id.progressBarUpdateEdit);

        // Show ProgressBar when the fragment is opened
        progressBarUpdateEdit.setVisibility(View.VISIBLE);

        // Fetch update information from Firestore and populate TextViews
        FirebaseFirestore.getInstance().collection("Updates")
                .document(updateId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String title = documentSnapshot.getString("title");
                            String message = documentSnapshot.getString("message");
                            String startTime = documentSnapshot.getString("startTime");
                            String endTime = documentSnapshot.getString("endTime");

                            // Populate TextViews
                            titleUpdateEdit.setText("Title: " + title);
                            messageUpdateEdit.setText("Message: " + message);
                            startingTimeUpdateEdit.setText("Starting Time: " + startTime);
                            endingTimeUpdateEdit.setText("Ending Time: " + endTime);

                            // Hide ProgressBar when data is loaded
                            progressBarUpdateEdit.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(getContext(), "Update not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to retrieve update information", Toast.LENGTH_SHORT).show();
                        // Hide ProgressBar on failure
                        progressBarUpdateEdit.setVisibility(View.GONE);
                    }
                });

        // Set OnClickListener for back button
        backBtnAddUpdateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle back button click
                requireActivity().onBackPressed();
            }
        });

        // Set OnClickListener for submit button
        submitBtnUpdateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show ProgressBar when submitting
                progressBarUpdateEdit.setVisibility(View.VISIBLE);
                // Update update information in Firestore
                updateUpdate();
            }
        });

        // Set OnClickListener for TextViews to make corresponding EditTexts visible
        titleUpdateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTextViewColors();
                titleUpdateEdit.setTextColor(Color.BLUE);
                hideAllEditTexts();
                titleETUpdateEdit.setVisibility(View.VISIBLE);
            }
        });

        messageUpdateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTextViewColors();
                messageUpdateEdit.setTextColor(Color.BLUE);
                hideAllEditTexts();
                messageETUpdateEdit.setVisibility(View.VISIBLE);
            }
        });

        startingTimeUpdateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTextViewColors();
                startingTimeUpdateEdit.setTextColor(Color.BLUE);
                hideAllEditTexts();
                startingTimeETUpdateEdit.setVisibility(View.VISIBLE);
                showTimePicker();
            }
        });

        endingTimeUpdateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTextViewColors();
                endingTimeUpdateEdit.setTextColor(Color.BLUE);
                hideAllEditTexts();
                endingTimeETUpdateEdit.setVisibility(View.VISIBLE);
                showTimePicker();
            }
        });
    }
    private void showTimePicker() {
        // Get current time
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Launch time picker dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Update EditText with selected time
                String time = String.format("%02d:%02d", hourOfDay, minute);
                startingTimeETUpdateEdit.setText(time);
                endingTimeETUpdateEdit.setText(time);
            }
        }, hour, minute, DateFormat.is24HourFormat(requireContext()));

        timePickerDialog.show();
    }

    // Method to update update information in Firestore
    private void updateUpdate() {
        // Initialize a map to store the field name and its corresponding value
        Map<String, Object> updateMap = new HashMap<>();

        // Get updated information from EditTexts based on the clicked TextView
        if (titleETUpdateEdit.getVisibility() == View.VISIBLE) {
            updateMap.put("title", titleETUpdateEdit.getText().toString());
        }
        if (messageETUpdateEdit.getVisibility() == View.VISIBLE) {
            updateMap.put("message", messageETUpdateEdit.getText().toString());
        }
        if (startingTimeETUpdateEdit.getVisibility() == View.VISIBLE) {
            updateMap.put("startTime", startingTimeETUpdateEdit.getText().toString());
        }
        if (endingTimeETUpdateEdit.getVisibility() == View.VISIBLE) {
            updateMap.put("endTime", endingTimeETUpdateEdit.getText().toString());
        }

        // Update the document in Firestore
        FirebaseFirestore.getInstance().collection("Updates")
                .document(updateId)
                .update(updateMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Update successful
                        Toast.makeText(getContext(), "Update successful", Toast.LENGTH_SHORT).show();
                        // Hide ProgressBar after successful update
                        progressBarUpdateEdit.setVisibility(View.GONE);
                        // Reset TextView colors
                        resetTextViewColors();
                        UpdatesFragment updatesFragment = new UpdatesFragment();
                        // Replace the current fragment with TicketsFragment
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, updatesFragment)
                                .commit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Update failed
                        Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                        // Hide ProgressBar on failure
                        progressBarUpdateEdit.setVisibility(View.GONE);
                    }
                });
    }

    // Method to reset TextView colors to default
    private void resetTextViewColors() {
        titleUpdateEdit.setTextColor(Color.BLACK);
        messageUpdateEdit.setTextColor(Color.BLACK);
        startingTimeUpdateEdit.setTextColor(Color.BLACK);
        endingTimeUpdateEdit.setTextColor(Color.BLACK);
    }

    // Method to hide all EditTexts
    private void hideAllEditTexts() {
        titleETUpdateEdit.setVisibility(View.GONE);
        messageETUpdateEdit.setVisibility(View.GONE);
        startingTimeETUpdateEdit.setVisibility(View.GONE);
        endingTimeETUpdateEdit.setVisibility(View.GONE);
    }
}
