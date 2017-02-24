package com.visma.blue.metadata.expense;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.drew.lang.annotations.Nullable;
import com.visma.blue.R;
import com.visma.blue.network.requests.customdata.Expense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class ExpenseTypeAdapter extends ArrayAdapter<Expense.ExpenseType> implements Filterable {

    private ArrayList<Expense.ExpenseType> mExpenseTypes;
    private ArrayList<Expense.ExpenseType> storedExpenseTypes;
    private Expense.ExpenseType mSelectedExpenseType;
    private Date mFilterDate;

    public ExpenseTypeAdapter(@NonNull Context context, @NonNull Date filterDate,
                              @NonNull ArrayList<Expense.ExpenseType> expenseTypes,
                              @Nullable Expense.ExpenseType expenseCustomData) {
        super(context, R.layout.blue_list_item_expense_type);

        mFilterDate = filterDate;
        mExpenseTypes = new ArrayList<>();
        mExpenseTypes.add(emptyValue());
        mExpenseTypes.addAll(expenseTypes);
        performFiltering(mFilterDate);
        storedExpenseTypes = new ArrayList<>(mExpenseTypes);
        mSelectedExpenseType = expenseCustomData;

        if (mSelectedExpenseType == null) {
            mSelectedExpenseType = mExpenseTypes.get(0);
        }

        scrollToSelectedPosition(getSelectionPosition(), context);
    }

    /**
     * Filter the adapter so that only expense types valid for a certain date are displayed.
     *
     * @param date - date for which filtering is applied.
     */
    public void performFiltering(@NonNull Date date) {
        ArrayList<Expense.ExpenseType> filteredExpenseTypes = new ArrayList<>(mExpenseTypes);

        Iterator<Expense.ExpenseType> iterator = filteredExpenseTypes.iterator();
        while (iterator.hasNext()) {
            Expense.ExpenseType expenseType = iterator.next();
            if (!expenseType.isValid(date)) {
                iterator.remove();
            }
        }

        mExpenseTypes = filteredExpenseTypes;

    }

    private Expense.ExpenseType emptyValue() {
        return new Expense.ExpenseType(getContext().getString(R.string
                .visma_blue_spinner_nothing_chosen));
    }

    private static class ViewHolder {
        TextView name;
        ImageView selectionIndicatorImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Expense.ExpenseType expenseType = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.blue_list_item_expense_type, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.listItemExpenseType);
            viewHolder.selectionIndicatorImage = (ImageView) convertView.findViewById(R.id
                    .type_selector_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        if (isSelected(position)) {
           viewHolder.selectionIndicatorImage.setVisibility(View.VISIBLE);
        } else {
            viewHolder.selectionIndicatorImage.setVisibility(View.GONE);
        }

        viewHolder.name.setText(expenseType.name);
        return convertView;
    }

    public boolean isSelected(int position) {
        return mExpenseTypes != null && mExpenseTypes.size() > 0 && mSelectedExpenseType.code
                .equals(mExpenseTypes.get(position).code);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mExpenseTypes = (ArrayList<Expense.ExpenseType>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    // No filter implemented we return all the list
                    results.values = storedExpenseTypes;
                } else {
                    mExpenseTypes = storedExpenseTypes;

                    String constraintString = constraint.toString()
                            .toUpperCase(Locale.getDefault());
                    ArrayList<Expense.ExpenseType> filteredTasks = new ArrayList<>();

                    for (Expense.ExpenseType type : mExpenseTypes) {
                        if (type.name.toUpperCase(Locale.getDefault()).contains(constraintString)) {
                            filteredTasks.add(type);
                        }
                    }

                    results.values = filteredTasks;
                }

                return results;
            }
        };
    }

    @Override
    public int getCount() {
        if (mExpenseTypes != null) {
            return mExpenseTypes.size();
        } else {
            return 0;
        }
    }

    @Override
    public Expense.ExpenseType getItem(int position) {
        if (mExpenseTypes != null) {
            return mExpenseTypes.get(position);
        } else {
            return null;
        }
    }

    private int getSelectionPosition() {
        if (mExpenseTypes != null && mExpenseTypes.size() > 0) {
            int expenseTypeCount =  mExpenseTypes.size();
            for (int i = 0; i < expenseTypeCount; i++) {
                if (isSelected(i)) {
                    return i;
                }
            }
        }

        return 0;
    }

    private void scrollToSelectedPosition(final int position, Context context) {
        if (context instanceof Activity) {
            final View listView = ((Activity)context).getWindow().getDecorView().findViewById(R.id
                    .listViewExpenseType);
            if (listView != null && listView instanceof ListView) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        ((ListView) listView).setSelection(position);
                    }
                });

            }
        }
    }
}
