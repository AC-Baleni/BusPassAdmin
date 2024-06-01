package com.example.buspassadmin.BookedTickets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.buspassadmin.R;

import java.util.List;

public class BookedAdapter extends RecyclerView.Adapter<BookedAdapter.ViewHolder> {

    private List<Booked> ticketList;
    private Context context;

    public BookedAdapter(List<Booked> ticketList, Context context) {
        this.ticketList = ticketList;
        this.context = context;
    }

    public void filterList(List<Booked> filteredList) {
        ticketList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.booked_ticket_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booked ticket = ticketList.get(position);
        holder.bind(ticket);
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView statusTxt;
        TextView textViewTrip;
        TextView busNum;
        TextView ticketDate;
        TextView seatNum;




        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            busNum = itemView.findViewById(R.id.busNumBTI);
            ticketDate = itemView.findViewById(R.id.dateBTI);
            seatNum = itemView.findViewById(R.id.seatBTI);
            textViewTrip = itemView.findViewById(R.id.destinationTXTBT);
            statusTxt = itemView.findViewById(R.id.statusBTI);


            // Set click listener for trackTicket button

        }



        @Override
        public void onClick(View v) {
            // Handle click event
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Booked clickedTicket = ticketList.get(position);


            }
        }

        // Setter method for busName
        @SuppressLint("SetTextI18n")
        public void bind(Booked ticket) {
            textViewTrip.setText("Bus Trip: From " + ticket.getStartingLocation() + " to " + ticket.getDestination());
            seatNum.setText("Seat Number: "+ticket.getSelectedSeat());
            busNum.setText(ticket.getBusLicensePlate());
            ticketDate.setText("Ticket date: "+ticket.getSelectedDate());
            statusTxt.setText(ticket.getTicketStatus());

        }
    }
}