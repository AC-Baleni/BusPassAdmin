package com.example.buspassadmin.Home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buspassadmin.Home.Edit.EditTicketFragment;
import com.example.buspassadmin.Home.Edit.EditUpdateFragment;
import com.example.buspassadmin.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UpdatesAdapter extends RecyclerView.Adapter<UpdatesAdapter.ViewHolder> {

    private List<Update> updateList;
    private Context context;

    public UpdatesAdapter(List<Update> updateList, Context context) {
        this.updateList = updateList;
        this.context = context;
    }

    public void filterList(List<Update> filteredList) {
        updateList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.update_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Update update = updateList.get(position);
        holder.bind(update);
    }

    @Override
    public int getItemCount() {
        return updateList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleUpdateTXT;
        TextView messageUpdateTXT;
        TextView timeSpan;
        ImageButton deleteImgBtn;
        ImageButton editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleUpdateTXT = itemView.findViewById(R.id.titleUpdateTXT);
            messageUpdateTXT = itemView.findViewById(R.id.messageUpdateTXT);
            timeSpan = itemView.findViewById(R.id.timeSpanTXT);
            deleteImgBtn = itemView.findViewById(R.id.deleteImgBtnUpdate);
            editButton = itemView.findViewById(R.id.editImgBtnUpdate);

            // Set OnClickListener for delete ImageButton
            deleteImgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog(getAdapterPosition());
                }
            });
            // Set OnClickListener for edit ImageButton
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Redirect to EditTicketFragment
                    Update update = updateList.get(getAdapterPosition());
                    String updateId = update.getUpdateID();

                    EditUpdateFragment editUpdateFragment = new EditUpdateFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("updateId", updateId);
                    editUpdateFragment.setArguments(bundle);

                    FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, editUpdateFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
        }


        // Method to show the delete confirmation dialog
        private void showDeleteConfirmationDialog(final int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Are you sure you want to delete this update?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Call the method to delete the update
                    deleteUpdate(position);
                }
            });
            builder.setNegativeButton("No", null);
            builder.show();
        }

        // Method to delete the update from the list and notify the adapter
        private void deleteUpdate(int position) {
            Update update = updateList.get(position);
            String updateId = update.getUpdateID(); // Assuming getId() returns the document ID in Firestore
            FirebaseFirestore.getInstance().collection("Updates")
                    .document(updateId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Update successfully deleted from Firestore
                            updateList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, updateList.size());
                            Toast.makeText(context, "Update deleted successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failure
                            // You may want to show a Toast or log the error
                            Toast.makeText(context, "Failed to delete update", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Setter method for binding data to views
        public void bind(Update update) {
            titleUpdateTXT.setText(update.getTitle());
            messageUpdateTXT.setText(update.getMessage());
            timeSpan.setText("Update from " + update.getStartTime() + " to " + update.getEndTime());
        }
    }
}
