package com.example.buspassadmin.Profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.buspassadmin.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class CompanyProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView logoImageView;
    private TextView companyNameTextView;
    private TextView companyEmailTextView;
    private TextView companyPhoneTextView;
    private EditText companyNameEditText;
    private EditText companyEmailEditText;
    private EditText companyPhoneEditText;
    private Button updateButton;
    private ProgressBar progressBar;
    private TextView selectedTextView = null;

    private FirebaseFirestore db;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_company_profile, container, false);

        logoImageView = view.findViewById(R.id.logoImgView);
        companyNameTextView = view.findViewById(R.id.companyNameTxt);
        companyEmailTextView = view.findViewById(R.id.companyEmailTXT);
        companyPhoneTextView = view.findViewById(R.id.companyTelTXT);
        companyNameEditText = view.findViewById(R.id.companyNameET);
        companyEmailEditText = view.findViewById(R.id.comanyEmailET);
        companyPhoneEditText = view.findViewById(R.id.companyPhoneET);
        updateButton = view.findViewById(R.id.companyProfileUpdateBtn);
        progressBar = view.findViewById(R.id.progressBar);
        logoImageView.setImageResource(R.drawable.baseline_add_a_photo_24);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        logoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    updateData(userId);
                } else {
                    Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
                }
            }
        });

        companyNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTextViewClick(companyNameTextView, companyNameEditText, "Enter Company Name");
            }
        });

        companyEmailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTextViewClick(companyEmailTextView, companyEmailEditText, "Enter Company Email");
            }
        });

        companyPhoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTextViewClick(companyPhoneTextView, companyPhoneEditText, "Enter Company Phone");
            }
        });

        // Load company data
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            loadCompanyData(userId);
        }

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            logoImageView.setImageURI(imageUri);
        }
    }

    private void handleTextViewClick(TextView textView, EditText editText, String placeholderText) {
        // Hide all EditText fields except the one that is currently being edited
        if (selectedTextView != null && !selectedTextView.equals(textView)) {
            selectedTextView.setVisibility(View.GONE);
        }

        // Reset text color of all TextViews
        companyNameTextView.setTextColor(Color.BLACK);
        companyEmailTextView.setTextColor(Color.BLACK);
        companyPhoneTextView.setTextColor(Color.BLACK);

        // Show the corresponding EditText field and update text color of the clicked TextView
        editText.setVisibility(View.VISIBLE);
        editText.setHint(placeholderText);
        textView.setTextColor(Color.GREEN);

        // Set selectedTextView to the clicked TextView
        selectedTextView = textView;

        // Show or hide the updateButton based on whether an EditText is visible
        if (editText.getVisibility() == View.VISIBLE) {
            updateButton.setVisibility(View.VISIBLE);
        } else {
            updateButton.setVisibility(View.GONE);
        }
    }


    private void updateData(String userId) {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        if (selectedTextView != null) {
            // Check if any of the EditText fields have been edited
            boolean fieldsEdited = false;
            String companyName = companyNameEditText.getText().toString().trim();
            String companyEmail = companyEmailEditText.getText().toString().trim();
            String companyPhone = companyPhoneEditText.getText().toString().trim();

            if (!companyName.isEmpty() || !companyEmail.isEmpty() || !companyPhone.isEmpty()) {
                fieldsEdited = true;
            }

            if (imageUri != null || fieldsEdited) { // Check if any field is edited or image is selected
                final StorageReference fileRef = storageRef.child("images/" + userId + ".jpg");

                if (imageUri != null) {
                    fileRef.putFile(imageUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            // Update data
                                            updateFirestoreData(userId, uri.toString());
                                            companyEmailEditText.setVisibility(View.GONE);
                                            companyNameEditText.setVisibility(View.GONE);
                                            companyPhoneEditText.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                                    // Hide progress bar
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                } else {
                    // Update data without uploading image
                    updateFirestoreData(userId, null);
                }
            } else {
                Toast.makeText(getActivity(), "No changes made", Toast.LENGTH_SHORT).show();
                // Hide progress bar
                progressBar.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(getActivity(), "No field selected to update", Toast.LENGTH_SHORT).show();
            // Hide progress bar
            progressBar.setVisibility(View.GONE);
        }
    }

    private void updateFirestoreData(String userId, @Nullable String imageUrl) {
        String companyName = companyNameEditText.getText().toString().trim();
        String companyEmail = companyEmailEditText.getText().toString().trim();
        String companyPhone = companyPhoneEditText.getText().toString().trim();

        Map<String, Object> data = new HashMap<>();
        data.put("companyName", companyName);
        data.put("companyEmail", companyEmail);
        data.put("companyPhone", companyPhone);
        if (imageUrl != null) {
            data.put("logoUrl", imageUrl);
        }

        db.collection("Admins").document(userId)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Data uploaded successfully", Toast.LENGTH_SHORT).show();
                        // Reload company data after successful update
                        loadCompanyData(userId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to upload data", Toast.LENGTH_SHORT).show();
                        // Hide progress bar
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void loadCompanyData(String userId) {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Load data from Firestore
        db.collection("Admins").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String logoUrl = documentSnapshot.getString("logoUrl");
                            if (logoUrl != null && !logoUrl.isEmpty()) {
                                // Load logo image using Glide and make it circular
                                Glide.with(requireContext())
                                        .load(logoUrl)
                                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                        .into(logoImageView);
                            } else {
                                // Show placeholder or default company logo
                                logoImageView.setImageResource(R.drawable.baseline_add_a_photo_24);
                            }

                            // Set company name, email, phone from Firestore
                            String companyName = documentSnapshot.getString("companyName");
                            String companyEmail = documentSnapshot.getString("companyEmail");
                            String companyPhone = documentSnapshot.getString("companyPhone");

                            if (companyName != null && !companyName.isEmpty()) {
                                companyNameTextView.setText(companyName);
                            } else {
                                companyNameTextView.setText("Enter Company Name");
                            }

                            if (companyEmail != null && !companyEmail.isEmpty()) {
                                companyEmailTextView.setText(companyEmail);
                            } else {
                                companyEmailTextView.setText("Enter Company Email");
                            }

                            if (companyPhone != null && !companyPhone.isEmpty()) {
                                companyPhoneTextView.setText(companyPhone);
                            } else {
                                companyPhoneTextView.setText("Enter Company Phone");
                            }
                        } else {
                            // Document does not exist
                            Toast.makeText(requireContext(), "Company data not found", Toast.LENGTH_SHORT).show();

                            // Show relevant message in TextViews
                            companyNameTextView.setText("Enter Company Name");
                            companyEmailTextView.setText("Enter Company Email");
                            companyPhoneTextView.setText("Enter Company Phone");
                        }

                        // Hide progress bar
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Failed to load company data", Toast.LENGTH_SHORT).show();

                        // Hide progress bar
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
