package com.example.techfix_mobile.ui.myrequests;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techfix_mobile.R;
import com.example.techfix_mobile.model.RepairRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyRequestsAdapter extends RecyclerView.Adapter<MyRequestsAdapter.VH> {

    public interface OnRequestClick {
        void onClick(RepairRequest request);
    }

    private final List<RepairRequest> items = new ArrayList<>();
    private final OnRequestClick listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public MyRequestsAdapter(OnRequestClick listener) {
        this.listener = listener;
    }

    public void submitList(List<RepairRequest> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        RepairRequest r = items.get(position);
        holder.title.setText(r.getDeviceDetails() != null ? r.getDeviceDetails() : r.getRequestId());
        com.example.techfix_mobile.util.StatusChipBinder.bind(holder.status, r.getStatus());
        holder.date.setText(r.getRequestedAt() > 0 ? dateFormat.format(r.getRequestedAt()) : "");
        holder.itemView.setOnClickListener(v -> listener.onClick(r));
    }

    private String formatStatus(String status) {
        if (status == null) return "";
        return status.replace("_", " ").toUpperCase(Locale.getDefault());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, status, date;
        VH(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtRequestTitle);
            status = itemView.findViewById(R.id.txtRequestStatus);
            date = itemView.findViewById(R.id.txtRequestDate);
        }
    }
}
