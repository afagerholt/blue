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

public class SeveraPhaseAdapter extends ArrayAdapter<Severa.Task> implements Filterable {

    private ArrayList<Severa.Task> severaTasks;
    private ArrayList<Severa.Task> storedSeveraTasks;
    private ArrayList<SeveraCustomData.Task> tasksFromServer;

    private ArrayList<Severa.Task> disabledPhases;

    private static class ViewHolder {
        TextView name;
    }

    public SeveraPhaseAdapter(Context context, ArrayList<Severa.Task> tasks,
                              ArrayList<SeveraCustomData.Task> serverTasks) {
        super(context, R.layout.list_item_case_and_phase, tasks);

        severaTasks = tasks;
        storedSeveraTasks = new ArrayList<Severa.Task>(severaTasks);
        tasksFromServer = serverTasks;

        // Create a disabled phases array at the init for better performance when checking if
        // the element is disabled (the isEnabled() method is called multiple times)
        filterDisabledPhases();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Severa.Task severaTask = getItem(position);
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

        viewHolder.name.setText(severaTask.name);
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        if (disabledPhases != null) {
            Severa.Task severaTask = severaTasks.get(position);

            for (Severa.Task t : disabledPhases) {
                if (severaTask.guid.equals(t.guid)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int getCount() {
        if (severaTasks != null) {
            return severaTasks.size();
        } else {
            return 0;
        }
    }

    @Override
    public Severa.Task getItem(int position) {
        if (severaTasks != null) {
            return severaTasks.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public boolean isSaved(int position) {
        if (tasksFromServer != null && tasksFromServer.size() > 0) {
            for (SeveraCustomData.Task task : tasksFromServer) {
                if (task.guid.equals(severaTasks.get(position).guid)) {
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                severaTasks = (ArrayList<Severa.Task>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    // No filter implemented we return all the list
                    results.values = storedSeveraTasks;
                } else {
                    severaTasks = storedSeveraTasks;

                    String constraintString = constraint.toString().toUpperCase(Locale.getDefault());
                    ArrayList<Severa.Task> filteredTasks = new ArrayList<Severa.Task>();

                    for (Severa.Task p : severaTasks) {
                        if (p.name.toUpperCase(Locale.getDefault()).contains(constraintString)) {
                            filteredTasks.add(p);
                        }
                    }

                    results.values = filteredTasks;
                }

                return results;
            }
        };
    }

    private void filterDisabledPhases() {
        disabledPhases = new ArrayList<>();

        for (Severa.Task currentTask : severaTasks) {
            if (currentTask.isLocked && !checkIfEnabled(currentTask)) {
                disabledPhases.add(currentTask);
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
}
