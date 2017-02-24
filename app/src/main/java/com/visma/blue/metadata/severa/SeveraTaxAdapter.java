package com.visma.blue.metadata.severa;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.visma.blue.R;
import com.visma.blue.network.requests.customdata.Severa;

import java.util.ArrayList;
import java.util.Collection;

public class SeveraTaxAdapter extends ArrayAdapter<Severa.Tax> {

    private ArrayList<Severa.Tax> mTaxes;
    private int selectedItemPos = 0;

    public SeveraTaxAdapter(Context context) {
        super(context, R.layout.blue_spinner_item);
        mTaxes = new ArrayList<>();

        // Add the empty element
        mTaxes.add(getEmptyValue());
        this.addAll(mTaxes);

        this.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View view = super.getDropDownView(position, null, parent);

        TextView textView = (TextView) view;

        if (selectedItemPos == position) {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.nc_blue));
        } else {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color
                    .primary_text_nc_light));
        }

        return view;
    }

    private Severa.Tax getEmptyValue() {
        double emptyDouble = -1;
        return new Severa.Tax(emptyDouble, getContext().getString(R.string
                .visma_blue_spinner_nothing_chosen));
    }

    public void highlightText(int position) {
        selectedItemPos = position;
        notifyDataSetChanged();
    }

    public int getPositionByTaxPercentage(double percentage) {
        for (int i = 0; i < mTaxes.size(); i++) {
            if (mTaxes.get(i).percentage == percentage) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void add(Severa.Tax tax) {
        mTaxes.add(tax);
        super.add(tax);
    }

    public void updateTaxes(Collection<? extends Severa.Tax> taxes) {
        mTaxes.clear();
        mTaxes.add(getEmptyValue());
        mTaxes.addAll(taxes);
        this.clear();
        this.addAll(mTaxes);
    }
}
