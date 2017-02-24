package com.visma.blue.metadata.severa;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.visma.blue.R;
import com.visma.blue.network.containers.SeveraCustomData;
import com.visma.blue.network.requests.customdata.Severa;

import java.util.ArrayList;
import java.util.Locale;

public class SeveraCaseAdapter extends ArrayAdapter<Severa.Case> implements Filterable {

    private ArrayList<Severa.Case> severaCases;
    private ArrayList<Severa.Case> storedSeveraCases;
    private SeveraCustomData.Case caseFromServer;

    private ArrayList<Severa.Case> disabledCases;

    private static class ViewHolder {
        TextView name;
    }

    public SeveraCaseAdapter(Context context, ArrayList<Severa.Case> cases, SeveraCustomData.Case serverCase) {
        super(context, R.layout.list_item_case_and_phase, cases);
        cases.add(0, getEmptyCase());
        severaCases = cases;
        storedSeveraCases = new ArrayList<Severa.Case>(severaCases);
        caseFromServer = serverCase;

        // Create a disabled cases array at the init for better performance when checking if
        // the element is disabled (the isEnabled() method is called multiple times)
        filterDisabledCases();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Severa.Case severaCase = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_case_and_phase, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.listItemCaseAndPhase);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Reset the text color every time, because the views position changes during the filtering,
        // and that affects cached views

        if (!isEnabled(position)) {
            viewHolder.name.setTextColor(ContextCompat.getColor(getContext(), R.color.bright_foreground_disabled_nc_light));
        } else if (isSaved(position)) {
            viewHolder.name.setTextColor(ContextCompat.getColor(getContext(), R.color.nc_blue));
        } else {
            viewHolder.name.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_text_nc_light));
        }

        viewHolder.name.setText(severaCase.name);

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        if (disabledCases != null) {
            Severa.Case severaCase = severaCases.get(position);

            for (Severa.Case c : disabledCases) {
                if (severaCase.guid.equals(c.guid)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isSaved(int position) {
        return (caseFromServer == null && position == 0) || (caseFromServer != null
                && caseFromServer.guid.equals(severaCases.get(position).guid));
    }

    @Override
    public int getCount() {
        if (severaCases != null) {
            return severaCases.size();
        } else {
            return 0;
        }
    }

    @Override
    public Severa.Case getItem(int position) {
        if (severaCases != null) {
            return severaCases.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                severaCases = (ArrayList<Severa.Case>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    // No filter implemented we return all the list
                    results.values = storedSeveraCases;
                } else {
                    severaCases = storedSeveraCases;

                    String constraintString = constraint.toString().toUpperCase(Locale.getDefault());
                    ArrayList<Severa.Case> filteredCases = new ArrayList<Severa.Case>();

                    for (Severa.Case p : severaCases) {
                        if (p.name.toUpperCase(Locale.getDefault()).contains(constraintString)) {
                            filteredCases.add(p);
                        }
                    }

                    results.values = filteredCases;
                }

                return results;
            }
        };
    }

    private void filterDisabledCases() {
        disabledCases = new ArrayList<>();

        for (Severa.Case currentCase : severaCases) {
            Severa.Task task = currentCase.task;

            if (task.isLocked && !checkIfEnabled(task)) {
                disabledCases.add(currentCase);
            }
        }
    }

    public boolean checkIfEnabled(Severa.Task severaTask) {
        boolean result = false;

        if (severaTask.tasks != null && severaTask.tasks.size() > 0) {
            for (Severa.Task task : severaTask.tasks) {
                if (task.isLocked) {
                    result = checkIfEnabled(task);

                    // Break the loop if clickable element is found at the bottom and start exiting the recursion
                    if (result) {
                        break;
                    }
                } else {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    private Severa.Case getEmptyCase() {
        return new Severa.Case(getContext().getString(R.string.visma_blue_spinner_nothing_chosen));
    }
}
