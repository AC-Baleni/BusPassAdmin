package com.example.buspassadmin.Home;

import android.annotation.SuppressLint;
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
import com.example.buspassadmin.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.ViewHolder> {

    private List<Ticket> ticketList;
    private Context context;

    public TicketAdapter(List<Ticket> ticketList, Context context) {
        this.ticketList = ticketList;
        this.context = context;
    }

    public void filterList(List<Ticket> filteredList) {
        ticketList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.ticket_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);
        holder.bind(ticket);
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLicense;
        TextView textViewDepatureTime;
        TextView textViewTrip;
        TextView textViewTripPrice;
        ImageButton deleteImgBtn;
        ImageButton editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLicense = itemView.findViewById(R.id.textViewLicense);
            textViewDepatureTime = itemView.findViewById(R.id.textViewDepTime);
            textViewTrip = itemView.findViewById(R.id.textViewFromTo);
            textViewTripPrice = itemView.findViewById(R.id.textViewTripPrice);
            deleteImgBtn = itemView.findViewById(R.id.deleteImgBtn);
            editButton = itemView.findViewById(R.id.editImgBtn);

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
                    Ticket ticket = ticketList.get(getAdapterPosition());
                    String ticketId = ticket.getTicketID();

                    EditTicketFragment editTicketFragment = new EditTicketFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("ticketId", ticketId);
                    editTicketFragment.setArguments(bundle);

                    FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, editTicketFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
        }

        // Method to show the delete confirmation dialog
        private void showDeleteConfirmationDialog(final int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Are you sure you want to delete this ticket?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Call the method to delete the ticket
                    deleteTicket(position);
                }
            });
            builder.setNegativeButton("No", null);
            builder.show();
        }

        // Method to delete the ticket from the list and notify the adapter
        private void deleteTicket(int position) {
            Ticket ticket = ticketList.get(position);
            String ticketId = ticket.getTicketID(); // Assuming getId() returns the document ID in Firestore
            FirebaseFirestore.getInstance().collection("BusTickets")
                    .document(ticketId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Ticket successfully deleted from Firestore
                            ticketList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, ticketList.size());
                            Toast.makeText(context, "Ticket deleted successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failure
                            // You may want to show a Toast or log the error
                            Toast.makeText(context, "Failed to delete ticket", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Setter method for busName
        @SuppressLint("SetTextI18n")
        public void bind(Ticket ticket) {
            textViewLicense.setText("Bus Number: " + ticket.getBusLicensePlate());
            textViewDepatureTime.setText("Depature Time: " + ticket.getDepartureTime());
            textViewTrip.setText("Bus Trip: from " + ticket.getStartingLocation() + " to " + ticket.getDestination());
            textViewTripPrice.setText("Trip Price: " + ticket.getticketPrice());
            // Bind other ticket information to respective TextViews
        }
    }
}
