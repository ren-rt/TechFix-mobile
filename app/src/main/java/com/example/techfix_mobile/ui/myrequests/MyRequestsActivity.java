package com.example.techfix_mobile.ui.myrequests;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.techfix_mobile.R;
//import com.example.techfix_mobile.data.RequestSyncManager;
import com.example.techfix_mobile.db.DBHelper;
import com.example.techfix_mobile.model.RepairRequest;
import com.example.techfix_mobile.ui.detail.RequestDetailActivity;
//import com.google.firebase.auth.FirebaseAuth;


import java.util.List;

public class MyRequestsActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    // private RequestSyncManager syncManager;  // TODO: uncomment once Firebase is added
    private MyRequestsAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        dbHelper = new DBHelper(this);

        // TEMPORARY test data
        RepairRequest fake1 = new RepairRequest();
        fake1.setRequestId("test1");
        fake1.setDeviceDetails("Samsung A125F - cracked screen");
        fake1.setIssueDesc("Screen cracked after drop");
        fake1.setStatus(RepairRequest.STATUS_PENDING);
        fake1.setRequestedAt(System.currentTimeMillis());
        dbHelper.upsertRequest(fake1);

        RepairRequest fake2 = new RepairRequest();
        fake2.setRequestId("test2");
        fake2.setDeviceDetails("Dell Laptop - won't boot");
        fake2.setIssueDesc("Blue screen on startup");
        fake2.setStatus(RepairRequest.STATUS_IN_PROGRESS);
        fake2.setRequestedAt(System.currentTimeMillis());
        dbHelper.upsertRequest(fake2);

        RepairRequest fake3 = new RepairRequest();
        fake3.setRequestId("test3");
        fake3.setDeviceDetails("iPhone 12 - battery issue");
        fake3.setIssueDesc("Battery drains fast");
        fake3.setStatus(RepairRequest.STATUS_COMPLETED);
        fake3.setRequestedAt(System.currentTimeMillis());
        dbHelper.upsertRequest(fake3);

        // syncManager = new RequestSyncManager(dbHelper);
        //TODO: uncomment once Firebase is added

        RecyclerView recyclerView = findViewById(R.id.recyclerRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRequestsAdapter(request -> {
            Intent i = new Intent(this, RequestDetailActivity.class);
            i.putExtra("requestId", request.getRequestId());
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> {
            swipeRefresh.setRefreshing(false); // TODO: call syncThenLoad() once Firebase is added
        });

        loadFromLocal(); // show cached data immediately
        // syncThenLoad();  // TODO: uncomment once Firebase is added
    }

    // TODO: uncomment once Firebase is added
    // private void syncThenLoad() {
    //     String uid = FirebaseAuth.getInstance().getUid();
    //     if (uid == null) {
    //         swipeRefresh.setRefreshing(false);
    //         return;
    //     }
    //     syncManager.sync(uid, success -> runOnUiThread(() -> {
    //         swipeRefresh.setRefreshing(false);
    //         loadFromLocal();
    //     }));
    // }

    private void loadFromLocal() {
        List<RepairRequest> requests = dbHelper.getAllRequests();
        adapter.submitList(requests);
    }
}