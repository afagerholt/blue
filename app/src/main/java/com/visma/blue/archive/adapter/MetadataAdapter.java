package com.visma.blue.archive.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.TextView;

import com.visma.blue.R;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.OnlinePhotoType;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.MetadataList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class MetadataAdapter extends CursorAdapter implements StickyListHeadersAdapter, Filterable {

    protected Context mContext;
    private GregorianCalendar mCalendar;
    private Cursor mCursor;

    private int mNordicCoolBlue;

    public MetadataAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        this.mCursor = cursor;
        this.mContext = context;
        mNordicCoolBlue = ContextCompat.getColor(mContext, R.color.nc_blue);
        mCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        setFilterQueryProvider(mFilterProvider);
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout
                    .blue_list_header_metadata, parent, false);
            holder.dateTextView = (TextView) convertView.findViewById(R.id
                    .blue_row_sectioned_metadata_header_title);
            holder.bottomSeparator = convertView.findViewById(R.id
                    .blue_row_sectioned_metadata_header_bottom_separator);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
            //When make search and  then scroll the list and make another search date headers are
            // set to View.INVISIBLE. This check makes header views visible.
            if (convertView.getVisibility() == View.INVISIBLE) {
                convertView.setVisibility(View.VISIBLE);
            }
        }

        if (!mCursor.moveToPosition(position)) {
            return convertView;
        }

        if (OnlinePhotoType.UNKNOWN.getValue() == mCursor.getInt(mCursor
                .getColumnIndex(MetadataList.METADATA_TYPE))) {
            holder.dateTextView.setText(R.string.visma_blue_document_type_unknown);
        } else if (mCursor.getInt(mCursor
                .getColumnIndex(MetadataList.METADATA_NOT_SYNCED_DUE_TO_ERROR)) == 1) {
            holder.dateTextView.setText(R.string.visma_blue_upload_photo_fail_header_title);
        } else {
            DateFormat dateFormat = SimpleDateFormat.getDateInstance();
            holder.dateTextView.setText(dateFormat.format(new Date(mCursor.getLong(mCursor
                    .getColumnIndex(MetadataList.METADATA_DATE)))));
        }

        holder.dateTextView.setTextColor(mNordicCoolBlue);
        holder.bottomSeparator.setBackgroundColor(mNordicCoolBlue);

        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        if (!mCursor.moveToPosition(i)) {
            return -1;
        }

        if (OnlinePhotoType.UNKNOWN.getValue() == mCursor.getInt(mCursor
                .getColumnIndex(MetadataList.METADATA_TYPE))) {
            return 1;
        } else if (mCursor.getInt(mCursor
                .getColumnIndex(MetadataList.METADATA_NOT_SYNCED_DUE_TO_ERROR)) == 1) {
            return 2;
        }

        return getNotSyncedItemDate(mCursor.getLong(mCursor.getColumnIndex(MetadataList
                .METADATA_DATE)));
    }

    private static class HeaderViewHolder {
        TextView dateTextView;
        View bottomSeparator;
    }

    protected static class ItemViewHolder {
        public TextView typeView;
        public TextView commentView;
        public View lockedIndicator;
        public View notSyncedIndicator;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View rowView = LayoutInflater.from(mContext).inflate(R.layout.blue_list_item_metadata,
                parent, false);
        ItemViewHolder holder = new ItemViewHolder();
        holder.commentView = (TextView) rowView.findViewById(R.id
                .blue_row_sectioned_metadata_photo_comment);
        holder.typeView = (TextView) rowView.findViewById(R.id
                .blue_row_sectioned_metadata_photo_type);
        holder.lockedIndicator = rowView.findViewById(R.id
                .blue_row_sectioned_metadata_photo_locked);
        holder.notSyncedIndicator = rowView.findViewById(R.id.blue_row_not_sync_indicator);

        rowView.setTag(holder);
        return rowView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ItemViewHolder holder = (ItemViewHolder) view.getTag();
        holder.typeView.setText(VismaUtils.getTypeTextId(
                cursor.getInt(cursor.getColumnIndex(MetadataList.METADATA_TYPE))));
        String comment = cursor.getString(cursor.getColumnIndex(MetadataList.METADATA_COMMENT));
        if (comment != null && !comment.isEmpty()) {
            holder.commentView.setText(comment);
            holder.commentView.setVisibility(View.VISIBLE);
        } else {
            holder.commentView.setText(null);
            holder.commentView.setVisibility(View.GONE);
        }

        if (!cursor.isNull(cursor.getColumnIndex(MetadataList.METADATA_VERIFIED)) && cursor.getInt(
                cursor.getColumnIndex(MetadataList.METADATA_VERIFIED)) == 1) {
            holder.notSyncedIndicator.setVisibility(View.GONE);
        } else {
            holder.notSyncedIndicator.setVisibility(View.VISIBLE);
        }

        holder.lockedIndicator.setVisibility(cursor.getInt(cursor.getColumnIndex(MetadataList
                .METADATA_CAN_DELETE)) == 1 ? View.GONE : View.VISIBLE);

    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        return super.swapCursor(newCursor);
    }

    private long getNotSyncedItemDate(long dateInMillis) {
        mCalendar.setTimeInMillis(dateInMillis);
        mCalendar.set(GregorianCalendar.HOUR, 0);
        mCalendar.set(GregorianCalendar.MINUTE, 0);
        mCalendar.set(GregorianCalendar.SECOND, 0);
        mCalendar.set(GregorianCalendar.MILLISECOND, 0);
        return mCalendar.getTimeInMillis();
    }

    private FilterQueryProvider mFilterProvider = new FilterQueryProvider() {

        @Override
        public Cursor runQuery(CharSequence charSequence) {
            if (charSequence == null || charSequence.length() == 0) {
                return mContext.getContentResolver().query(BlueContentProvider
                                .CONTENT_URI_FILTERED_METADATA, MetadataList
                                .getDatabaseColumnNames(),
                        null, null, MetadataList.METADATA_NOT_SYNCED_DUE_TO_ERROR + " DESC, "
                                + MetadataList.METADATA_DATE + " DESC");
            } else {
                String projection = MetadataList.METADATA_COMMENT + " LIKE '%' || ? || '%' OR "
                        + MetadataList.METADATA_NAME + " LIKE '%' || ? || '%' OR "
                        + MetadataList.METADATA_ORGANISATION_NUMBER + " LIKE '%' || ? || '%' OR "
                        + MetadataList.METADATA_REFERENCE_NUMBER + " LIKE '%' || ? || '%'";
                String filterValue = charSequence.toString();
                return mContext.getContentResolver().query(BlueContentProvider
                                .CONTENT_URI_FILTERED_METADATA, MetadataList
                                .getDatabaseColumnNames(),
                        projection, new String[]{filterValue, filterValue, filterValue,
                                filterValue}, MetadataList.METADATA_NOT_SYNCED_DUE_TO_ERROR + " DESC, "
                                + MetadataList.METADATA_DATE + " DESC");
            }

        }
    };

    @Override
    public void changeCursor(Cursor cursor) {
        swapCursor(cursor);
    }
}