package com.example.buspassadmin.Home;

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

import com.example.buspassadmin.AddUpdates;
import com.example.buspassadmin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UpdatesFragment extends Fragment {

    private UpdatesAdapter adapter;
    private List<Update> updateList;
    private ProgressBar progressBar;
    private TextView noUpdates;
    private FirebaseUser currentUser;

    public UpdatesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_updates, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewUpdates);
        progressBar = view.findViewById(R.id.progressBarLoadUpdate);
        noUpdates = view.findViewById(R.id.noUpdatesAddedTxt);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        updateList = new ArrayList<>();
        adapter = new UpdatesAdapter(updateList, requireContext());
        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.VISIBLE);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String adminId = currentUser.getUid();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Retrieve update data from Firestore
        firestore.collection("Updates")
                .whereEqualTo("adminId",adminId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateList.clear(); // Clear existing list
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Update update = document.toObject(Update.class);
                            update.setUpdateID(document.getId()); // Set the update ID
                            updateList.add(update);
                        }
                        adapter.notifyDataSetChanged();
                        // Hide progress bar after loading updates
                        progressBar.setVisibility(View.GONE);
                        // Show "no updates" message if no updates found
                        if (updateList.isEmpty()) {
                            noUpdates.setVisibility(View.VISIBLE);
                        } else {
                            noUpdates.setVisibility(View.GONE);
                        }
                    }
                });

        // Search functionality
        EditText searchEditText = view.findViewById(R.id.searchTicketUpdate);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUpdates(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ImageButton addUpdatesBtn = view.findViewById(R.id.AddUpdatesBtn);
        addUpdatesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddUpdates.class));
            }
        });
    }

    private void filterUpdates(String searchText) {
        List<Update> filteredList = new ArrayList<>();
        for (Update update : updateList) {
            if (update.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(update);
            }
        }
        adapter.filterList(filteredList);
    }
}
