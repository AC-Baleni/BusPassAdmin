package com.example.buspassadmin;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.buspassadmin.Home.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddUpdates extends AppCompatActivity {
    EditText titleUpdatesET, messageUpdatesET, startUpdateTime, timeUpdateEndsET;
    TextView textView2;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private String userId;
    ImageButton backBtn;
    private FirebaseUser currentUser;
    private static final String TAG = "AddUpdatesActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_updates);

        titleUpdatesET = findViewById(R.id.titleUpdatesET);
        messageUpdatesET = findViewById(R.id.messageUpdatesET);
        startUpdateTime = findViewById(R.id.startUpdateTime);
        timeUpdateEndsET = findViewById(R.id.timeUpdateEndsET);
        textView2 = findViewById(R.id.textView2);
        progressBar = findViewById(R.id.UpdatesProgressBar);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        backBtn = findViewById(R.id.backBtnImgUpdate);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to UpdatesFragment
                Intent intent = new Intent(AddUpdates.this, MainActivity.class);
                intent.putExtra("fragmentToLoad", "UpdatesFragment");
                startActivity(intent);
                finish(); // Optional: Finish the activity to remove it from back stack
            }
        });

        String adminId = currentUser.getUid();

        // Set onClickListener for start time EditText
        startUpdateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(startUpdateTime);
            }
        });

        // Set onClickListener for end time EditText
        timeUpdateEndsET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(timeUpdateEndsET);
            }
        });

        // Submit button click listener
        findViewById(R.id.submitBtnUpdates).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get FCM token
                getFCMTokenAndAddUpdate(adminId);
            }
        });
    }
    // Method to show TimePickerDialog
    private void showTimePickerDialog(final EditText editText)
    {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String time = hourOfDay + ":" + minute;
                        editText.setText(time);
                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }
    // Method to get FCM token and add update to Firestore
    private void getFCMTokenAndAddUpdate(String adminId) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String fcmToken = task.getResult();

                        addUpdateToFirestore(adminId, fcmToken);
                    } else {
                        Log.w(TAG, "Fetching FCM token failed", task.getException());
                        Toast.makeText(AddUpdates.this, "Failed to fetch FCM token", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // Method to add update to Firestore
    private void addUpdateToFirestore(String adminId, String fcmToken) {
        String title = titleUpdatesET.getText().toString();
        String message = messageUpdatesET.getText().toString();
        String startTime = startUpdateTime.getText().toString();
        String endTime = timeUpdateEndsET.getText().toString();

        // Check if any field is empty
        if (title.isEmpty() || message.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Admins").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String companyName = documentSnapshot.getString("companyName");
                            String logoUrl = documentSnapshot.getString("logoUrl");

                            // Add ticket details to Firestore with companyName and logoUrl
                            addLogo(companyName, logoUrl, adminId, title, message, startTime, endTime);

                            // Save FCM token in BusTickets collection

                        } else {
                            // Document for the current user doesn't exist in the Admins collection
                            Toast.makeText(AddUpdates.this, "Error, please create a company profile", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE); // Hide progress bar
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to fetch user document from Firestore
                        Toast.makeText(AddUpdates.this, "Failed to fetch user document", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE); // Hide progress bar
                    }
                });
    }
    private void addLogo(String companyName, String logoUrl, String adminId, String title, String message, String startTime, String endTime) {

        // Create a Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Create a new map to store the update data
        Map<String, Object> update = new HashMap<>();
        update.put("adminId", adminId);
        update.put("title", title);
        update.put("message", message);
        update.put("startTime", startTime);
        update.put("endTime", endTime);
        update.put("companyName", companyName);
        update.put("logoUrl", logoUrl);

        // Add the update to the "Updates" collection
        db.collection("Updates")
                .add(update)
                .addOnSuccessListener(documentReference -> {
                    // Hide progress bar
                    sendNotificationToUsers(title, message);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddUpdates.this, "Update added successfully", Toast.LENGTH_SHORT).show();
                    // Clear all EditText fields after successful addition
                    Intent intent = new Intent(AddUpdates.this, MainActivity.class);
                    intent.putExtra("fragmentToLoad", "UpdatesFragment");
                    startActivity(intent);
                    finish(); // Optional: Finish the activity to remove it from back stack
                })
                .addOnFailureListener(e -> {
                    // Hide progress bar
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddUpdates.this, "Error adding update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    // Method to save FCM token in BusTickets collection

    // Method to send notification to users
    // Method to send notification to users
    private void sendNotificationToUsers(String title, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query all documents in the Users collection
        db.collection("Users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String fcmToken = document.getString("token");
                            if (fcmToken != null && !fcmToken.isEmpty()) {
                                // Create notification payload
                                Map<String, String> data = new HashMap<>();
                                data.put("title", title);
                                data.put("message", message);

                                // Create notification message
                                RemoteMessage.Builder builder = new RemoteMessage.Builder(fcmToken);
                                builder.setMessageId(Integer.toString(new Random().nextInt()));
                                builder.setData(data);
                                RemoteMessage notification = builder.build();

                                // Send the notification
                                FirebaseMessaging.getInstance().send(notification);
                            } else {
                                Log.e(TAG, "FCM token is null or empty for user: " + document.getId());
                            }
                        }
                    } else {
                        Log.e(TAG, "Failed to retrieve users from Firestore: ", task.getException());
                    }
                });
    }

}
