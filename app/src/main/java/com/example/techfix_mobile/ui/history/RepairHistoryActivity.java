package com.example.techfix_mobile.ui.history;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techfix_mobile.R;
import com.example.techfix_mobile.DatabaseHelper;
import com.example.techfix_mobile.model.RepairRequest;
import com.example.techfix_mobile.ui.detail.RequestDetailActivity;
import com.example.techfix_mobile.ui.myrequests.MyRequestsAdapter;
import com.example.techfix_mobile.util.BottomNavHelper;

public class RepairHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair_history);

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        MyRequestsAdapter adapter = new MyRequestsAdapter(request -> {
            Intent i = new Intent(this, RequestDetailActivity.class);
            i.putExtra(RequestDetailActivity.EXTRA_REQUEST_ID, request.getRequestId());
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);

        // History = SQLite already synced by MyRequestsActivity; this screen just filters
        // for completed/ready_for_pickup, it does not trigger its own Firestore sync.
        adapter.submitList(dbHelper.getHistoryRequests());

        BottomNavHelper.setup(this, BottomNavHelper.TAB_HISTORY);
    }
}
