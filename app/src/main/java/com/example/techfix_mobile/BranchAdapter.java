package com.example.techfix_mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.techfix_mobile.model.Branch;
import java.util.List;

public class BranchAdapter extends RecyclerView.Adapter<BranchAdapter.BranchViewHolder> {

    private List<Branch> branchList;
    private OnBranchClickListener listener;

    public interface OnBranchClickListener {
        void onBranchClick(Branch branch);
    }

    public BranchAdapter(List<Branch> branchList, OnBranchClickListener listener) {
        this.branchList = branchList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BranchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_branch, parent, false);
        return new BranchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchViewHolder holder, int position) {
        Branch branch = branchList.get(position);
        holder.name.setText(branch.getName());
        holder.address.setText(branch.getAddress());
        holder.phone.setText(branch.getContactNumber());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBranchClick(branch);
            }
        });
    }

    @Override
    public int getItemCount() {
        return branchList.size();
    }

    public static class BranchViewHolder extends RecyclerView.ViewHolder {
        TextView name, address, phone;

        public BranchViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.branchName);
            address = itemView.findViewById(R.id.branchAddress);
            phone = itemView.findViewById(R.id.branchPhone);
        }
    }
}