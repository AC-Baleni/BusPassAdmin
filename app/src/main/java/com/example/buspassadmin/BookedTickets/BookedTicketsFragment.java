package com.example.buspassadmin.BookedTickets;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.buspassadmin.AddTicket;
import com.example.buspassadmin.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookedTicketsFragment extends Fragment {


    private TextView noTicketsTxt;

    private BookedAdapter adapter;
    private List<Booked> ticketList;
    private ProgressBar progressBar;
    private FirebaseUser currentUser;

    public BookedTicketsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_booked_tickets, container, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.bookedTicketsRview);
        progressBar = view.findViewById(R.id.progressBarBT);

        noTicketsTxt = view.findViewById(R.id.noBookedTicketsBT);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();



        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ticketList = new ArrayList<>();
        adapter = new BookedAdapter(ticketList, requireContext());
        recyclerView.setAdapter(adapter);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        // Fetch company name and logo URL from Firestore based on the current user's ID
        firestore.collection("Admins").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String companyName = documentSnapshot.getString("companyName");
                            // Add ticket details to Firestore with companyName and logoUrl
                            // Retrieve ticket data from Firestore
                            // Retrieve ticket data from Firestore
                            firestore.collection("BookedTickets")
                                    .whereEqualTo("companyName", companyName)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            ticketList.clear(); // Clear existing list
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Booked bookedTicket = document.toObject(Booked.class);
                                                bookedTicket.setTicketID(document.getId()); // Set the ticket ID
                                                ticketList.add(bookedTicket);
                                            }
                                            adapter.notifyDataSetChanged();
                                            // Hide progress bar after loading tickets
                                            progressBar.setVisibility(View.GONE);
                                            // Show "no tickets" message if no tickets found
                                            if (ticketList.isEmpty()) {
                                                noTicketsTxt.setVisibility(View.VISIBLE);
                                            } else {
                                                noTicketsTxt.setVisibility(View.GONE);
                                            }
                                        } else {
                                            // Handle query failure
                                            progressBar.setVisibility(View.GONE);
                                            Log.e(TAG, "Error getting booked tickets: ", task.getException());
                                            // Check if the error indicates that the collection doesn't exist
                                            if (task.getException() instanceof FirebaseFirestoreException && ((FirebaseFirestoreException) task.getException()).getCode() == FirebaseFirestoreException.Code.NOT_FOUND) {
                                                // Handle the case where the collection doesn't exist
                                                Toast.makeText(requireContext(), "BookedTickets collection does not exist", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Show a generic error message to the user
                                                Toast.makeText(requireContext(), "Failed to retrieve booked tickets", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        } else {
                            // Document for the current user doesn't exist in the Admins collection
                             progressBar.setVisibility(View.GONE); // Hide progress bar
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to fetch user document from Firestore
                       progressBar.setVisibility(View.GONE); // Hide progress bar
                    }
                });

    }

    // Method to redirect to SearchTicketFragment

}