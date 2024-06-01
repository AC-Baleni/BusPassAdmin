package com.example.buspassadmin.Home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buspassadmin.R;

import java.util.List;

public class HomeUpdatesCarouselAdapter extends RecyclerView.Adapter<HomeUpdatesCarouselAdapter.ViewHolder> {

    private List<Update> updateList;
    private Context context;

    public HomeUpdatesCarouselAdapter(List<Update> updateList, Context context) {
        this.updateList = updateList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_update_carousel, parent, false);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleUpdateTXT = itemView.findViewById(R.id.titleTextViewCarousel);
            messageUpdateTXT = itemView.findViewById(R.id.messageTextViewCarousel);
            timeSpan = itemView.findViewById(R.id.timeSpanHome);
        }

        // Setter method for binding data to views
        public void bind(Update update) {
            titleUpdateTXT.setText(update.getTitle());
            messageUpdateTXT.setText(update.getMessage());
            timeSpan.setText("Update from " + update.getStartTime() + " to " + update.getEndTime());
        }
    }
}
