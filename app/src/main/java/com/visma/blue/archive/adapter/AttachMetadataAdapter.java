package com.visma.blue.archive.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.visma.blue.R;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.DateTypeDeserializer;
import com.visma.blue.network.containers.ExpenseCustomData;
import com.visma.blue.provider.MetadataList;
import com.visma.common.util.Util;

import java.util.Date;

public class AttachMetadataAdapter  extends CustomMetadataAdapter {

    public AttachMetadataAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View rowView = LayoutInflater.from(mContext).inflate(R.layout.blue_list_item_attach_metadata,
                parent, false);
        AttachItemViewHolder holder = new AttachItemViewHolder();
        holder.commentView = (TextView) rowView.findViewById(R.id
                .blue_row_sectioned_metadata_photo_comment);
        holder.typeView = (TextView) rowView.findViewById(R.id
                .blue_row_sectioned_metadata_photo_type);
        holder.lockedIndicator = rowView.findViewById(R.id
                .blue_row_sectioned_metadata_photo_locked);
        holder.notSyncedIndicator = rowView.findViewById(R.id.blue_row_not_sync_indicator);
        holder.amountView =  (TextView) rowView.findViewById(R.id
                .blue_row_sectioned_metadata_amount);
        rowView.setTag(holder);
        return rowView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        AttachItemViewHolder holder = (AttachItemViewHolder) view.getTag();
        ExpenseCustomData expenseCustomData = null;
        if (!cursor.isNull(cursor
                .getColumnIndex(MetadataList.METADATA_EXPENSE_CUSTOM_DATA))) {
            String savedJsonData =
                    cursor.getString(cursor
                            .getColumnIndex(MetadataList.METADATA_EXPENSE_CUSTOM_DATA));
            expenseCustomData = sGson.fromJson(savedJsonData,
                    ExpenseCustomData.class);
        }

        String amountText = getAmountText(expenseCustomData, cursor.getDouble(
                cursor.getColumnIndex(MetadataList.METADATA_DUE_AMOUNT)));
        if (amountText == null) {
            holder.amountView.setText(null);
            holder.amountView.setVisibility(View.GONE);
        } else {
            holder.amountView.setText(amountText);
            holder.amountView.setVisibility(View.VISIBLE);
        }

        if (expenseCustomData != null && expenseCustomData.expenseType != null
                && expenseCustomData.expenseType.name != null) {
            holder.typeView.setText(expenseCustomData.expenseType.name);
        }
    }

    private static class AttachItemViewHolder extends ItemViewHolder {
        private TextView amountView;
    }

    private String getAmountText(ExpenseCustomData expenseCustomData, double amount) {
        if (expenseCustomData == null || expenseCustomData.currency == null || expenseCustomData
                .currency.currencyCode == null) {
            if (amount > 0) {
                return Util.getFormattedNumberString(amount);
            } else {
                return null;
            }
        }

        return Util.getFormattedNumberString(amount) +  " " + expenseCustomData.currency.currencyCode;
    }
}
