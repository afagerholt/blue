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

public class SeveraProductAdapter extends ArrayAdapter<Severa.Product> {

    private int mSelectedItemPos = 0;
    private String mSelectedGuid;
    private ArrayList<Severa.Product> mSeveraProducts;

    public SeveraProductAdapter(Context context) {
        super(context, R.layout.blue_spinner_item);
        mSeveraProducts = new ArrayList<>();

        // Add the empty element
        mSeveraProducts.add(getEmptyValue(context));
        this.addAll(mSeveraProducts);

        this.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View view = super.getDropDownView(position, null, parent);

        TextView textView = (TextView) view;

        if (mSelectedItemPos == position) {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.nc_blue));
        } else {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color
                    .primary_text_nc_light));
        }


        return view;
    }

    private Severa.Product getEmptyValue(Context context) {
        double emptyDouble = -1;
        String emptyString = context.getString(R.string.visma_blue_spinner_nothing_chosen);
        return new Severa.Product(null, emptyString, false, emptyDouble, "", emptyDouble);
    }

    public void highlightText(int position) {
        mSelectedItemPos = position;
        notifyDataSetChanged();
    }

    public int getPositionByProductGuid(String productGuid) {
        for (int i = 0; i < mSeveraProducts.size(); i++) {
            if (mSeveraProducts.get(i).guid == null) {
                continue;
            }

            if (mSeveraProducts.get(i).guid.equals(productGuid)) {
                mSelectedGuid = mSeveraProducts.get(i).guid;
                return i;
            }
        }
        mSelectedGuid = null;
        return -1;
    }

    @Override
    public void add(Severa.Product product) {
        mSeveraProducts.add(product);
        super.add(product);
    }

    public void updateProducts(Context context, Collection<? extends Severa.Product> products) {
        mSeveraProducts.clear();
        mSeveraProducts.add(getEmptyValue(context));
        mSeveraProducts.addAll(products);
        this.clear();
        this.addAll(mSeveraProducts);
    }

    public String getSelectedGuid() {
        return mSelectedGuid;
    }
}
