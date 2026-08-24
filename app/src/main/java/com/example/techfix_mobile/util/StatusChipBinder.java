package com.example.techfix_mobile.util;

import android.widget.TextView;
import com.example.techfix_mobile.R;
import com.example.techfix_mobile.model.RepairRequest;

public class StatusChipBinder {

    public static void bind(TextView chip, String status) {
        if (status == null) {
            chip.setText("Unknown");
            chip.setBackgroundResource(R.drawable.bg_chip_pending);
            return;
        }
        switch (status) {
            case RepairRequest.STATUS_PENDING:
                chip.setText("Pending");
                chip.setBackgroundResource(R.drawable.bg_chip_pending);
                break;
            case RepairRequest.STATUS_ASSIGNED:
                chip.setText("Assigned");
                chip.setBackgroundResource(R.drawable.bg_chip_assigned);
                break;
            case RepairRequest.STATUS_IN_PROGRESS:
                chip.setText("In Progress");
                chip.setBackgroundResource(R.drawable.bg_chip_progress);
                break;
            case RepairRequest.STATUS_COMPLETED:
                chip.setText("Completed");
                chip.setBackgroundResource(R.drawable.bg_chip_done);
                break;
            case RepairRequest.STATUS_READY_FOR_PICKUP:
                chip.setText("Ready");
                chip.setBackgroundResource(R.drawable.bg_chip_done);
                break;
            default:
                chip.setText(status);
                chip.setBackgroundResource(R.drawable.bg_chip_pending);
        }
    }
}