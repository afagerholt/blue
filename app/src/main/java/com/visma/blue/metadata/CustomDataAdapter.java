package com.visma.blue.metadata;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.visma.blue.R;
import com.visma.blue.network.requests.customdata.Netvisor;

public class CustomDataAdapter extends ArrayAdapter<Netvisor.Value> {

    public CustomDataAdapter(Context context) {
        super(context, R.layout.blue_spinner_item);

        // Create and add an empty item
        String valueId = null;
        String valueTitle = context.getString(R.string.visma_blue_spinner_nothing_chosen);
        Netvisor.Value emptyValue = new Netvisor.Value(valueId, valueTitle, null, true);

        this.add(emptyValue);

        this.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
}
