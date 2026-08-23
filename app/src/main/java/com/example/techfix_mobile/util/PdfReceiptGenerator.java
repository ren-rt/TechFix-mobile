package com.example.techfix_mobile.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.example.techfix_mobile.model.Payment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Generates a simple one-page PDF receipt using Android's built-in PdfDocument
 * (no external library, per team decision).
 */
public class PdfReceiptGenerator {

    private static final int PAGE_WIDTH = 595;  // A4 at 72dpi
    private static final int PAGE_HEIGHT = 842;

    /** Returns the generated file, or null on failure. */
    public static File generate(Context context, Payment payment, String deviceDetails) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint title = new Paint();
        title.setTextSize(20);
        title.setFakeBoldText(true);

        Paint label = new Paint();
        label.setTextSize(12);

        Paint value = new Paint();
        value.setTextSize(12);
        value.setFakeBoldText(true);

        int x = 40;
        int y = 60;
        int lineHeight = 26;

        canvas.drawText("TechFix — Payment Receipt", x, y, title);
        y += lineHeight * 2;

        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        String paidAt = payment.getPaidAt() > 0 ? fmt.format(new Date(payment.getPaidAt())) : "-";

        y = drawRow(canvas, x, y, lineHeight, label, value, "Receipt No.", payment.getPaymentId());
        y = drawRow(canvas, x, y, lineHeight, label, value, "Device", deviceDetails != null ? deviceDetails : "-");
        y = drawRow(canvas, x, y, lineHeight, label, value, "Amount",
                String.format(Locale.getDefault(), "%s %.2f", payment.getCurrency(), payment.getAmount()));
        y = drawRow(canvas, x, y, lineHeight, label, value, "Status", payment.getStatus());
        y = drawRow(canvas, x, y, lineHeight, label, value, "PayHere Payment ID",
                payment.getPayherePaymentId() != null ? payment.getPayherePaymentId() : "-");
        y = drawRow(canvas, x, y, lineHeight, label, value, "Method", payment.getMethod() != null ? payment.getMethod() : "-");
        drawRow(canvas, x, y, lineHeight, label, value, "Paid At", paidAt);

        document.finishPage(page);

        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir != null && !dir.exists()) dir.mkdirs();
        File file = new File(dir, "receipt_" + payment.getPaymentId() + ".pdf");

        try (FileOutputStream out = new FileOutputStream(file)) {
            document.writeTo(out);
            return file;
        } catch (IOException e) {
            return null;
        } finally {
            document.close();
        }
    }

    private static int drawRow(Canvas canvas, int x, int y, int lineHeight,
                                Paint label, Paint value, String labelText, String valueText) {
        canvas.drawText(labelText + ":", x, y, label);
        canvas.drawText(valueText, x + 180, y, value);
        return y + lineHeight;
    }
}
