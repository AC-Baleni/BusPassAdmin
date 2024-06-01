package com.example.buspassadmin.Home.Edit;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
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
import androidx.fragment.app.FragmentTransaction;

import com.example.buspassadmin.Home.Ticket;
import com.example.buspassadmin.Home.TicketsFragment;
import com.example.buspassadmin.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditTicketFragment extends Fragment {

    private TextView busNumberEdit;
    private TextView startingLocationEdit;
    private TextView destinationEdit;
    private TextView pricePerKmEdit;
    private TextView depTimeEdit;
    private EditText licensePlateETEdit;
    private EditText fromETEdit;
    private EditText destinationETEdit;
    private EditText priceETEdit;
    private EditText depTimeETEdit;
    private Button submitBtnEdit;
    private ImageButton backBtn;
    private ProgressBar progressBarABTEdit;
    private int AUTOCOMPLETE_REQUEST_CODE_STARTING_LOCATION = 1;
    private int AUTOCOMPLETE_REQUEST_CODE_DESTINATION = 2;
    private double startLatitude, startLongitude, destLatitude, destLongitude;

    private String ticketId;

    public EditTicketFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ticketId = getArguments().getString("ticketId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_ticket, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        busNumberEdit = view.findViewById(R.id.busNumberEdit);
        backBtn = view.findViewById(R.id.backBtnAddTicketEdit);
        startingLocationEdit = view.findViewById(R.id.startingLocationEdit);
        destinationEdit = view.findViewById(R.id.destinationEdit);
        pricePerKmEdit = view.findViewById(R.id.pricePerKmEdit);
        depTimeEdit = view.findViewById(R.id.DepartureTimeEdit);
        licensePlateETEdit = view.findViewById(R.id.licensePlateETEdit);
        fromETEdit = view.findViewById(R.id.fromETEdit);
        destinationETEdit = view.findViewById(R.id.destinationETEdit);
        priceETEdit = view.findViewById(R.id.priceETEdit);
        depTimeETEdit = view.findViewById(R.id.depTimeETEdit);
        submitBtnEdit = view.findViewById(R.id.submitBtnEdit);
        progressBarABTEdit = view.findViewById(R.id.progressBarABTEdit);

        Places.initialize(requireContext(), getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(requireContext());

        // Show ProgressBar when the fragment is opened
        progressBarABTEdit.setVisibility(View.VISIBLE);

        // Fetch ticket information from Firestore and populate TextViews
        FirebaseFirestore.getInstance().collection("BusTickets")
                .document(ticketId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Ticket ticket = documentSnapshot.toObject(Ticket.class);
                            if (ticket != null) {
                                busNumberEdit.setText("Bus Number: " + ticket.getBusLicensePlate());
                                startingLocationEdit.setText("Starting Location: " + ticket.getStartingLocation());
                                destinationEdit.setText("Destination: " + ticket.getDestination());
                                pricePerKmEdit.setText("Trip Price = " + ticket.getticketPrice());
                                depTimeEdit.setText("Departure Time: " + ticket.getDepartureTime());

                                // Hide ProgressBar when data is loaded
                                progressBarABTEdit.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(getContext(), "Ticket not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to retrieve ticket information", Toast.LENGTH_SHORT).show();
                        // Hide ProgressBar on failure
                        progressBarABTEdit.setVisibility(View.GONE);
                    }
                });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TicketsFragment ticketsFragment = new TicketsFragment();
                // Replace the current fragment with TicketsFragment
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, ticketsFragment)
                        .commit();
            }
        });

        // Set OnClickListener for submit button
        submitBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show ProgressBar when submitting
                progressBarABTEdit.setVisibility(View.VISIBLE);
                // Update ticket information in Firestore
                updateTicket();
            }
        });

        // Set OnClickListener for TextViews to make corresponding EditTexts visible
        busNumberEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTextViewColors();
                busNumberEdit.setTextColor(Color.GREEN);
                hideAllEditTexts();
                licensePlateETEdit.setVisibility(View.VISIBLE);
            }
        });

        startingLocationEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTextViewColors();
                startingLocationEdit.setTextColor(Color.GREEN);
                hideAllEditTexts();
                startAutocompleteActivity(AUTOCOMPLETE_REQUEST_CODE_STARTING_LOCATION);
                fromETEdit.setVisibility(View.VISIBLE);
            }
        });

        destinationEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTextViewColors();
                destinationEdit.setTextColor(Color.GREEN);
                hideAllEditTexts();
                destinationETEdit.setVisibility(View.VISIBLE);
                startAutocompleteActivity(AUTOCOMPLETE_REQUEST_CODE_DESTINATION);
            }
        });

        pricePerKmEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTextViewColors();
                pricePerKmEdit.setTextColor(Color.GREEN);
                hideAllEditTexts();
                priceETEdit.setVisibility(View.VISIBLE);
            }
        });

        depTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTextViewColors();
                depTimeEdit.setTextColor(Color.GREEN);
                hideAllEditTexts();
                depTimeETEdit.setVisibility(View.VISIBLE);
                // Show time picker
                showTimePicker();
            }
        });
    }

    // Method to show time picker
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
                depTimeETEdit.setText(time);
            }
        }, hour, minute, DateFormat.is24HourFormat(requireContext()));

        timePickerDialog.show();
    }

    private void startAutocompleteActivity(int requestCode) {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(requireContext());
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            if (requestCode == AUTOCOMPLETE_REQUEST_CODE_STARTING_LOCATION) {
                startingLocationEdit.setText(place.getName());
                startLatitude = place.getLatLng().latitude;
                startLongitude = place.getLatLng().longitude;
            } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE_DESTINATION) {
                destinationEdit.setText(place.getName());
                destLatitude = place.getLatLng().latitude;
                destLongitude = place.getLatLng().longitude;
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(requireContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Method to update ticket information in Firestore
    private void updateTicket() {
        // Initialize a map to store the field name and its corresponding value
        Map<String, Object> updateMap = new HashMap<>();

        // Get updated information from EditTexts based on the clicked TextView
        if (licensePlateETEdit.getVisibility() == View.VISIBLE) {
            updateMap.put("busLicensePlate", licensePlateETEdit.getText().toString());
        }
        if (fromETEdit.getVisibility() == View.VISIBLE) {
            updateMap.put("startingLocation", fromETEdit.getText().toString());
        }
        if (destinationETEdit.getVisibility() == View.VISIBLE) {
            updateMap.put("destination", destinationETEdit.getText().toString());
        }
        if (priceETEdit.getVisibility() == View.VISIBLE) {
            updateMap.put("ticketPrice", "R "+priceETEdit.getText().toString());
        }
        if (depTimeETEdit.getVisibility() == View.VISIBLE) {
            updateMap.put("departureTime", depTimeETEdit.getText().toString());
        }

        // Update ticket document in Firestore with the fields that are updated
        FirebaseFirestore.getInstance().collection("BusTickets")
                .document(ticketId)
                .update(updateMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Ticket updated successfully", Toast.LENGTH_SHORT).show();
                        // Hide ProgressBar on success
                        progressBarABTEdit.setVisibility(View.GONE);
                        TicketsFragment ticketsFragment = new TicketsFragment();
                        // Replace the current fragment with TicketsFragment
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, ticketsFragment)
                                .commit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to update ticket", Toast.LENGTH_SHORT).show();
                        // Hide ProgressBar on failure
                        progressBarABTEdit.setVisibility(View.GONE);
                    }
                });
    }

    private void resetTextViewColors() {
        busNumberEdit.setTextColor(Color.BLACK);
        startingLocationEdit.setTextColor(Color.BLACK);
        destinationEdit.setTextColor(Color.BLACK);
        pricePerKmEdit.setTextColor(Color.BLACK);
        depTimeEdit.setTextColor(Color.BLACK);
    }

    private void hideAllEditTexts() {
        licensePlateETEdit.setVisibility(View.GONE);
        fromETEdit.setVisibility(View.GONE);
        destinationETEdit.setVisibility(View.GONE);
        priceETEdit.setVisibility(View.GONE);
        depTimeETEdit.setVisibility(View.GONE);
    }
}
