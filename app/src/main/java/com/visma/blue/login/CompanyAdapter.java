package com.visma.blue.login;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.visma.blue.R;
import com.visma.blue.network.containers.GetCompaniesAnswer;

import java.util.ArrayList;

public class CompanyAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inflater;
    private ArrayList<GetCompaniesAnswer.OnlineCustomer> companies;

    public CompanyAdapter(Context context,
                          ArrayList<GetCompaniesAnswer.OnlineCustomer> onlineCustomers) {
        this.mContext = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.companies = onlineCustomers;
    }

    @Override
    public int getCount() {
        if (companies != null) {
            return companies.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (companies != null) {
            return companies.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = this.inflater.inflate(R.layout.list_item_company, parent, false);
            holder.companyName = (TextView) convertView.findViewById(R.id.list_item_company);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GetCompaniesAnswer.OnlineCustomer company = companies.get(position);
        holder.companyName.setText(company.companyDisplayName);

        return convertView;
    }

    class ViewHolder {
        TextView companyName;
    }
}
