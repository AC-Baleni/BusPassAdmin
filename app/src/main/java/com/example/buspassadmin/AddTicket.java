package com.example.buspassadmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.buspassadmin.Home.MainActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import android.app.TimePickerDialog;
import android.widget.TimePicker;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTicket extends AppCompatActivity {

    private EditText editTextBusLicensePlate, editTextDepartureTime,
            editTextStartingLocation, editTextDestination, editTextPricePerKm;
    private ImageButton backBtn;
    private Button buttonSubmit;
    private ProgressBar progressBar;
    private FirebaseFirestore firestore;
    private int AUTOCOMPLETE_REQUEST_CODE_STARTING_LOCATION = 1;
    private int AUTOCOMPLETE_REQUEST_CODE_DESTINATION = 2;
    private double startLatitude, startLongitude, destLatitude, destLongitude;
    private FirebaseAuth mAuth;
    private String userId;
    private FirebaseUser currentUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ticket);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        // Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        editTextBusLicensePlate = findViewById(R.id.licensePlateET);
        editTextDepartureTime = findViewById(R.id.depTimeET);
        editTextStartingLocation = findViewById(R.id.fromET);
        editTextDestination = findViewById(R.id.destinationET);
        editTextPricePerKm = findViewById(R.id.priceET);
        buttonSubmit = findViewById(R.id.submitBtn);
        progressBar = findViewById(R.id.progressBarABT);
        backBtn = findViewById(R.id.backBtnAddTicket);

        firestore = FirebaseFirestore.getInstance();

        // Initialize the Places SDK
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(this);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        editTextStartingLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAutocompleteActivity(AUTOCOMPLETE_REQUEST_CODE_STARTING_LOCATION);
            }
        });

        editTextDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAutocompleteActivity(AUTOCOMPLETE_REQUEST_CODE_DESTINATION);
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE); // Show progress bar
                addTicketToFirestore(userId);
            }
        });
        editTextDepartureTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
    }

    private void showTimePickerDialog() {
        // Get current time
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Display selected time in EditText in 12-hour format with AM/PM
                        String amPm;
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        } else {
                            amPm = "AM";
                        }
                        @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d %s", hourOfDay % 12, minute, amPm);
                        editTextDepartureTime.setText(time);
                    }
                }, hour, minute, false); // false indicates 24-hour time format

        // Show the TimePickerDialog
        timePickerDialog.show();
    }

    private void startAutocompleteActivity(int requestCode) {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            if (requestCode == AUTOCOMPLETE_REQUEST_CODE_STARTING_LOCATION) {
                editTextStartingLocation.setText(place.getName());
                startLatitude = place.getLatLng().latitude;
                startLongitude = place.getLatLng().longitude;
            } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE_DESTINATION) {
                editTextDestination.setText(place.getName());
                destLatitude = place.getLatLng().latitude;
                destLongitude = place.getLatLng().longitude;
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addTicketToFirestore(String adminID)
    {
        String busLicensePlate = editTextBusLicensePlate.getText().toString().trim();
        String departureTime = editTextDepartureTime.getText().toString().trim();
        String pricePerKm = editTextPricePerKm.getText().toString().trim();

        if (busLicensePlate.isEmpty() || departureTime.isEmpty() ||
                startLatitude == 0 || startLongitude == 0 || destLatitude == 0 || destLongitude == 0 || pricePerKm.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE); // Hide progress bar
            return;
        }

        double distance = calculateDistance();
        if (distance == -1) {
            progressBar.setVisibility(View.GONE); // Hide progress bar
            return;
        }

        // Fetch company name and logo URL from Firestore based on the current user's ID
        firestore.collection("Admins").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String companyName = documentSnapshot.getString("companyName");
                            String logoUrl = documentSnapshot.getString("logoUrl");

                            // Add ticket details to Firestore with companyName and logoUrl
                            addTicketToFirestoreWithCompanyDetails(busLicensePlate, departureTime, pricePerKm, distance, companyName, logoUrl,adminID);
                        } else {
                            // Document for the current user doesn't exist in the Admins collection
                            Toast.makeText(AddTicket.this, "Error, please create a company profile", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE); // Hide progress bar
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to fetch user document from Firestore
                        Toast.makeText(AddTicket.this, "Failed to fetch user document", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE); // Hide progress bar
                    }
                });
    }

    private void addTicketToFirestoreWithCompanyDetails(String busLicensePlate, String departureTime,
                                                        String pricePerKm, double distance,
                                                        String companyName, String logoUrl,String adminID) {
        // Calculate ticket price
        double ticketPrice = Double.parseDouble(pricePerKm) * distance;

        // Round ticket price to two decimal places
        DecimalFormat df = new DecimalFormat("#.##");
        ticketPrice = Double.parseDouble(df.format(ticketPrice));




        // Check if the bus already exists
        double finalTicketPrice = ticketPrice;
        firestore.collection("Buses")
                .whereEqualTo("busNumber", busLicensePlate)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Bus already exists, use its document ID
                            DocumentSnapshot busDocumentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            String busId = busDocumentSnapshot.getId();

                            // Create a Map to store ticket data
                            Map<String, Object> ticket = new HashMap<>();
                            ticket.put("busId", busId); // Save the BusID
                            ticket.put("adminId",adminID);
                            ticket.put("busLicensePlate", busLicensePlate);
                            ticket.put("departureTime", departureTime);
                            ticket.put("startingLocation", editTextStartingLocation.getText().toString().trim());
                            ticket.put("destination", editTextDestination.getText().toString().trim());
                            ticket.put("pricePerKm", pricePerKm);
                            ticket.put("distance", distance); // Save the distance
                            ticket.put("ticketPrice", "R" + finalTicketPrice); // Save the ticket price
                            ticket.put("qrCodeUrl", ""); // Save the download URL of the QR code image
                            ticket.put("companyName", companyName); // Save the company name
                            ticket.put("logoUrl", logoUrl); // Save the logo URL

                            // Add the ticket to Firestore
                            firestore.collection("BusTickets")
                                    .add(ticket)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(AddTicket.this, "Ticket added successfully", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE); // Hide progress bar
                                            // Redirect to TicketsFragment
                                            Intent intent = new Intent(AddTicket.this, MainActivity.class);
                                            intent.putExtra("fragmentToLoad", "TicketsFragment");
                                            startActivity(intent);
                                            finish(); // Finish current activity to prevent going back to it with back button
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AddTicket.this, "Failed to add ticket", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE); // Hide progress bar
                                        }
                                    });
                        } else {
                            // Bus does not exist, add it to the "Buses" collection
                            Map<String, Object> bus = new HashMap<>();
                            bus.put("companyName", companyName);
                            bus.put("busNumber", busLicensePlate);
                            bus.put("startingLocation", editTextStartingLocation.getText().toString().trim());
                            bus.put("destination",editTextDestination.getText().toString().trim());


                            firestore.collection("Buses")
                                    .add(bus)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference busDocumentReference) {
                                            // Document ID of the newly added bus
                                            String busId = busDocumentReference.getId();

                                            // Create a Map to store ticket data
                                            Map<String, Object> ticket = new HashMap<>();
                                            ticket.put("busId", busId); // Save the BusID
                                            ticket.put("busLicensePlate", busLicensePlate);
                                            ticket.put("adminId",adminID);
                                            ticket.put("departureTime", departureTime);
                                            ticket.put("startingLocation", editTextStartingLocation.getText().toString().trim());
                                            ticket.put("destination", editTextDestination.getText().toString().trim());
                                            ticket.put("pricePerKm", pricePerKm);
                                            ticket.put("distance", distance); // Save the distance
                                            ticket.put("ticketPrice", "R" + finalTicketPrice); // Save the ticket price
                                            ticket.put("companyName", companyName); // Save the company name
                                            ticket.put("logoUrl", logoUrl); // Save the logo URL

                                            // Add the ticket to Firestore
                                            firestore.collection("BusTickets")
                                                    .add(ticket)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Toast.makeText(AddTicket.this, "Ticket added successfully", Toast.LENGTH_SHORT).show();
                                                            progressBar.setVisibility(View.GONE); // Hide progress bar
                                                            // Redirect to TicketsFragment
                                                            Intent intent = new Intent(AddTicket.this, MainActivity.class);
                                                            intent.putExtra("fragmentToLoad", "TicketsFragment");
                                                            startActivity(intent);
                                                            finish(); // Finish current activity to prevent going back to it with back button
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(AddTicket.this, "Failed to add ticket", Toast.LENGTH_SHORT).show();
                                                            progressBar.setVisibility(View.GONE); // Hide progress bar
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AddTicket.this, "Failed to add bus", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE); // Hide progress bar
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddTicket.this, "Failed to check bus existence", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE); // Hide progress bar
                    }
                });
    }


    private double calculateDistance() {
        // Check if latitude and longitude of starting location and destination are available
        if (startLatitude == 0 || startLongitude == 0 || destLatitude == 0 || destLongitude == 0) {
            Toast.makeText(this, "Please select starting location and destination", Toast.LENGTH_SHORT).show();
            return -1; // Return a negative value to indicate error
        }

        // Calculate distance using Haversine formula
        final int R = 6371; // Radius of the Earth in km

        double latDistance = Math.toRadians(destLatitude - startLatitude);
        double lonDistance = Math.toRadians(destLongitude - startLongitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(startLatitude)) * Math.cos(Math.toRadians(destLatitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance;
    }
}