package com.visma.blue.login.integrations;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.visma.blue.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class IntegrationsAdapter extends BaseAdapter {

    private ArrayList<IntegrationData> mIntegrations;
    private Context mContext;

    public IntegrationsAdapter(Context context, ArrayList<IntegrationData> integrations) {
        mContext = context;
        mIntegrations = integrations;
        sortIntegrationsByName();
    }

    private void sortIntegrationsByName() {
        if (mIntegrations != null) {
            Collections.sort(mIntegrations, new IntegrationsComparator());
        }
    }

    @Override
    public int getCount() {
        if (mIntegrations == null) {
            return 0;
        } else {
            return mIntegrations.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (mIntegrations != null) {
            return mIntegrations.get(position);
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
        IntegrationData data = mIntegrations.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.blue_integration_item,
                    parent, false);
            holder.integrationName = (TextView) convertView.findViewById(R.id.integration_name);
            holder.integrationDescription = (TextView) convertView.findViewById(R.id
                    .integration_description);
            holder.integrationName = (TextView) convertView.findViewById(R.id.integration_name);
            holder.integrationIcon = (ImageView) convertView.findViewById(R.id.integration_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        updateIntegrationData(holder, data);

        return convertView;
    }

    private void updateIntegrationData(ViewHolder holder, IntegrationData data) {
        holder.integrationName.setText(data.getName());
        holder.integrationDescription.setText(data.getDescription());
        holder.integrationIcon.setImageResource(data.getIconResId());
    }

    class ViewHolder {
        TextView integrationName;
        TextView integrationDescription;
        ImageView integrationIcon;
    }

    class IntegrationsComparator implements Comparator<IntegrationData> {

        @Override
        public int compare(IntegrationData integrationDataA, IntegrationData integrationDataB) {
            return integrationDataA.getName().compareToIgnoreCase(integrationDataB.getName());
        }
    }
}
