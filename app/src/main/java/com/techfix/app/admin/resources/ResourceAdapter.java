package com.techfix.app.admin.resources;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.techfix.app.R;

import java.util.List;
import java.util.Map;

public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(Map<String, Object> item);
        void onDelete(Map<String, Object> item);
    }

    private final List<Map<String, Object>> items;
    private final ResourceType type;
    private final Listener listener;

    public ResourceAdapter(List<Map<String, Object>> items, ResourceType type, Listener listener) {
        this.items = items;
        this.type = type;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resource, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = items.get(position);
        String title = String.valueOf(item.get(type.getTitleFieldKey()));
        holder.tvTitle.setText(title);

        StringBuilder subtitle = new StringBuilder();
        for (ResourceField f : type.fields) {
            if (f.key.equals(type.getTitleFieldKey()) || f.type == FieldType.LATLNG) continue;
            Object val = item.get(f.key);
            if (val != null) {
                if (subtitle.length() > 0) subtitle.append("  •  ");
                subtitle.append(f.label).append(": ").append(val);
            }
        }
        holder.tvSubtitle.setText(subtitle.toString());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageButton btnEdit, btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
