package com.techfix.app.admin.resources;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class GenericFieldViews {

    // Wraps whatever input view got created for a field, so we can read its value later
    public static class FieldViewHolder {
        public final ResourceField field;
        public final View inputView;
        public List<Map<String, Object>> dropdownSource; // for BRANCH_DROPDOWN / CATEGORY_DROPDOWN
        public List<String> dropdownIds;

        public FieldViewHolder(ResourceField field, View inputView) {
            this.field = field;
            this.inputView = inputView;
        }

        public Object readValue() {
            switch (field.type) {
                case TEXT:
                    return ((EditText) inputView).getText().toString().trim();
                case NUMBER_INT: {
                    String s = ((EditText) inputView).getText().toString().trim();
                    return s.isEmpty() ? 0 : Integer.parseInt(s);
                }
                case NUMBER_DECIMAL: {
                    String s = ((EditText) inputView).getText().toString().trim();
                    return s.isEmpty() ? 0.0 : Double.parseDouble(s);
                }
                case BOOLEAN:
                    return ((Switch) inputView).isChecked();
                case BRANCH_DROPDOWN:
                case CATEGORY_DROPDOWN: {
                    Spinner spinner = (Spinner) inputView;
                    int pos = spinner.getSelectedItemPosition();
                    if (dropdownIds == null || pos < 0 || pos >= dropdownIds.size()) return null;
                    return dropdownIds.get(pos);
                }
                default:
                    return null;
            }
        }

        public void setValue(Object value) {
            if (value == null) return;
            switch (field.type) {
                case TEXT:
                case NUMBER_INT:
                case NUMBER_DECIMAL:
                    ((EditText) inputView).setText(String.valueOf(value));
                    break;
                case BOOLEAN:
                    ((Switch) inputView).setChecked(Boolean.TRUE.equals(value) || "true".equals(String.valueOf(value)));
                    break;
                case BRANCH_DROPDOWN:
                case CATEGORY_DROPDOWN:
                    if (dropdownIds != null) {
                        int idx = dropdownIds.indexOf(String.valueOf(value));
                        if (idx >= 0) ((Spinner) inputView).setSelection(idx);
                    }
                    break;
            }
        }
    }

    public static View createLabel(Context ctx, String text) {
        TextView label = new TextView(ctx);
        label.setText(text);
        label.setTextColor(0xFFC8C8B0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 24;
        label.setLayoutParams(params);
        return label;
    }

    public static View createInput(Context ctx, ResourceField field) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        switch (field.type) {
            case NUMBER_INT:
            case NUMBER_DECIMAL: {
                EditText et = new EditText(ctx);
                et.setInputType(field.type == FieldType.NUMBER_INT
                        ? android.text.InputType.TYPE_CLASS_NUMBER
                        : android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                et.setTextColor(0xFFF0F0E0);
                et.setLayoutParams(params);
                return et;
            }
            case BOOLEAN: {
                Switch sw = new Switch(ctx);
                sw.setLayoutParams(params);
                return sw;
            }
            case BRANCH_DROPDOWN:
            case CATEGORY_DROPDOWN: {
                Spinner spinner = new Spinner(ctx);
                spinner.setLayoutParams(params);
                return spinner;
            }
            case TEXT:
            default: {
                EditText et = new EditText(ctx);
                et.setTextColor(0xFFF0F0E0);
                et.setLayoutParams(params);
                return et;
            }
        }
    }

    public static void bindDropdown(Context ctx, FieldViewHolder holder,
                                    List<Map<String, Object>> source, String labelKey, String idKey) {
        holder.dropdownSource = source;
        holder.dropdownIds = new java.util.ArrayList<>();
        List<String> labels = new java.util.ArrayList<>();
        for (Map<String, Object> item : source) {
            holder.dropdownIds.add(String.valueOf(item.get(idKey)));
            labels.add(String.valueOf(item.get(labelKey)));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) holder.inputView).setAdapter(adapter);
    }
}