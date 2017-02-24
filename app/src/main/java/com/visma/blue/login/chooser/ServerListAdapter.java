package com.visma.blue.login.chooser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.visma.blue.R;
import com.visma.blue.network.containers.ServerData;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import java.util.ArrayList;

public class ServerListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private ArrayList<ServerData> mServerListData;
    private ServerData mSelectedServerData;
    private Context mContext;

    public ServerListAdapter(Context context, ArrayList<ServerData> serverList,
                             ServerData selectedServerData) {
        mContext = context;
        mServerListData = serverList;
        mSelectedServerData = selectedServerData;
    }

    @Override
    public int getCount() {
        if (mServerListData == null) {
            return 0;
        } else {
            return mServerListData.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (mServerListData != null) {
            return mServerListData.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ServerData data = mServerListData.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.blue_list_item_server,
                    parent, false);
            holder.integrationName = (TextView) convertView.findViewById(R.id.server_name);
            holder.integrationIcon = (ImageView) convertView.findViewById(R.id
                    .server_selector_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        updateServerItem(holder, data);

        return convertView;
    }

    private void updateServerItem(ViewHolder holder, ServerData data) {
        holder.integrationName.setText(data.name);
        if (data.url.equals(mSelectedServerData.url)) {
            holder.integrationIcon.setVisibility(View.VISIBLE);
        } else {
            holder.integrationIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout
                    .blue_list_header_server, parent, false);
            holder.serverName = (TextView) convertView.findViewById(R.id
                    .blue_row_sectioned_server_environment_title);
            holder.bottomSeparator = convertView.findViewById(R.id
                    .blue_row_sectioned_server_header_bottom_separator);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        ServerData serverData = mServerListData.get(position);
        if (position == 0) {
            holder.serverName.setText(R.string.visma_blue_dev_server_list_header_live);
        } else if (!serverData.developer) {
            holder.serverName.setText(R.string.visma_blue_dev_server_list_header_test);
        } else {
            holder.serverName.setText(R.string.visma_blue_dev_server_list_header_dev);
        }

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        ServerData serverData = mServerListData.get(position);
        if (position == 0) {
            return 0;
        } else if (!serverData.developer) {
            return 1;
        } else {
            return 2;
        }
    }

    private static class HeaderViewHolder {
        TextView serverName;
        View bottomSeparator;
    }

    private static class ViewHolder {
        TextView integrationName;
        ImageView integrationIcon;
    }

    public void update(ArrayList<ServerData> serverList) {
        mServerListData.clear();
        mServerListData.addAll(serverList);
        notifyDataSetChanged();
    }
}