package com.example.buspassadmin.Home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TicketsFragment extends Fragment {

    private TicketAdapter adapter;
    private List<Ticket> ticketList;
    private ProgressBar progressBar;
    private TextView noTickets;
    private FirebaseUser currentUser;

    public TicketsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tickets, container, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBarTickets);
        noTickets = view.findViewById(R.id.noTicketsAddedTxt);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        ticketList = new ArrayList<>();
        adapter = new TicketAdapter(ticketList, requireContext());
        recyclerView.setAdapter(adapter);
        // Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String adminId = currentUser.getUid();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);


        // Retrieve ticket data from Firestore
        firestore.collection("BusTickets")
                .whereEqualTo("adminId", adminId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ticketList.clear(); // Clear existing list
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Ticket ticket = document.toObject(Ticket.class);
                            ticket.setTicketID(document.getId()); // Set the ticket ID
                            ticketList.add(ticket);
                        }
                        adapter.notifyDataSetChanged();
                        // Hide progress bar after loading tickets
                        progressBar.setVisibility(View.GONE);
                        // Show "no tickets" message if no tickets found
                        if (ticketList.isEmpty()) {
                            noTickets.setVisibility(View.VISIBLE);
                        } else {
                            noTickets.setVisibility(View.GONE);
                        }
                    }
                });

        // Search functionality
        EditText searchEditText = view.findViewById(R.id.searchTicket);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton addTicketBtn = view.findViewById(R.id.AddTicketBtn);
        addTicketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the button click event to start AddTicket activity
                startActivity(new Intent(getActivity(), AddTicket.class));
            }
        });
    }

    private void filter(String searchText) {
        List<Ticket> filteredList = new ArrayList<>();
        for (Ticket ticket : ticketList) {
            if (ticket.getBusLicensePlate().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(ticket);
            } else if (ticket.getDestination().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(ticket);
            } else if (ticket.getStartingLocation().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(ticket);
            } else if (ticket.getDepartureTime().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(ticket);
            }
        }
        adapter.filterList(filteredList);
    }

    // Method to delete ticket from Firestore
    private void deleteTicketFromFirestore(String ticketId) {
        FirebaseFirestore.getInstance().collection("BusTickets")
                .document(ticketId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Ticket successfully deleted from Firestore
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                    }
                });
    }
}
