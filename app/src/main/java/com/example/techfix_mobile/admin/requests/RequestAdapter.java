package com.example.techfix_mobile.admin.requests;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.techfix_mobile.R;
import com.example.techfix_mobile.model.RepairRequest;
import com.example.techfix_mobile.util.StatusChipBinder;

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RepairRequest r = items.get(position);
        holder.tvDeviceDetails.setText(r.getDeviceDetails() != null ? r.getDeviceDetails() : "Device details N/A");
        holder.tvIssueDesc.setText(r.getIssueDesc() != null ? r.getIssueDesc() : "");

        StatusChipBinder.bind(holder.tvStatusChip, r.getStatus());

        holder.itemView.setOnClickListener(v -> listener.onClick(r));
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