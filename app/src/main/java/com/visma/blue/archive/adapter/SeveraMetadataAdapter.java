package com.visma.blue.archive.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.visma.blue.R;
import com.visma.blue.network.containers.SeveraCustomData;
import com.visma.blue.provider.MetadataList;
import com.visma.common.util.Util;

public class SeveraMetadataAdapter extends CustomMetadataAdapter {

    public SeveraMetadataAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View rowView = LayoutInflater.from(mContext).inflate(R.layout.blue_list_item_severa_metadata,
                parent, false);
        SeveraItemViewHolder holder = new SeveraItemViewHolder();
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
        SeveraItemViewHolder holder = (SeveraItemViewHolder) view.getTag();
        SeveraCustomData severaCustomData = null;
        if (!cursor.isNull(cursor
                .getColumnIndex(MetadataList.METADATA_SEVERA_CUSTOM_DATA))) {
            String savedJsonData =
                    cursor.getString(cursor
                            .getColumnIndex(MetadataList.METADATA_SEVERA_CUSTOM_DATA));
            severaCustomData = sGson.fromJson(savedJsonData,
                    SeveraCustomData.class);
        }

        String currency = null;
        if (!cursor.isNull(cursor
                .getColumnIndex(MetadataList.METADATA_CURRENCY))) {
            currency = cursor.getString(cursor.getColumnIndex(MetadataList.METADATA_CURRENCY));
        }

        String amountText = getAmountText(currency, cursor.getDouble(
                cursor.getColumnIndex(MetadataList.METADATA_DUE_AMOUNT)));
        if (amountText == null) {
            holder.amountView.setText(null);
            holder.amountView.setVisibility(View.GONE);
        } else {
            holder.amountView.setText(amountText);
            holder.amountView.setVisibility(View.VISIBLE);
        }

        if (severaCustomData != null && severaCustomData.product != null
                && severaCustomData.product.name != null) {
            holder.typeView.setText(severaCustomData.product.name);
        }
    }

    private static class SeveraItemViewHolder extends ItemViewHolder {
        private TextView amountView;
    }

    private String getAmountText(String currency, double amount) {
        if (currency == null || currency.isEmpty()) {
            if (amount > 0) {
                return Util.getFormattedNumberString(amount);
            } else {
                return null;
            }
        }

        return Util.getFormattedNumberString(amount) +  " " + currency;
    }
}
