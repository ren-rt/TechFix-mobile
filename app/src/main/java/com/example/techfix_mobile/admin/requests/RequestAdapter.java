package com.example.techfix_mobile.admin.requests;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.techfix_mobile.R;
import com.example.techfix_mobile.models.RepairRequest;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    public interface Listener {
        void onClick(RepairRequest request);
    }

    private final List<RepairRequest> items;
    private final Listener listener;

    public RequestAdapter(List<RepairRequest> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RepairRequest r = items.get(position);
        holder.tvDeviceDetails.setText(r.getDeviceDetails() != null ? r.getDeviceDetails() : "Device details N/A");
        holder.tvIssueDesc.setText(r.getIssueDesc() != null ? r.getIssueDesc() : "");

        String status = r.getStatus() != null ? r.getStatus() : "pending";
        holder.tvStatusChip.setText(status.replace("_", " "));
        holder.tvStatusChip.setBackgroundColor(colorForStatus(status));

        holder.itemView.setOnClickListener(v -> listener.onClick(r));
    }

    private int colorForStatus(String status) {
        switch (status) {
            case "pending": return Color.parseColor("#EEEE30");        // bright yellow
            case "assigned": return Color.parseColor("#A0A000");       // mustard
            case "in_progress": return Color.parseColor("#D8D800");    // gold-yellow
            case "completed": return Color.parseColor("#C8C8B0");      // light khaki
            case "ready_for_pickup": return Color.parseColor("#F8FFD0"); // pale yellow
            default: return Color.parseColor("#787868");               // muted olive fallback
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceDetails, tvIssueDesc, tvStatusChip;
        ViewHolder(View itemView) {
            super(itemView);
            tvDeviceDetails = itemView.findViewById(R.id.tvDeviceDetails);
            tvIssueDesc = itemView.findViewById(R.id.tvIssueDesc);
            tvStatusChip = itemView.findViewById(R.id.tvStatusChip);
        }
    }
}