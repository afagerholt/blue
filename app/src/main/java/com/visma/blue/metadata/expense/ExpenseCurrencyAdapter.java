package com.visma.blue.metadata.expense;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.visma.blue.R;
import com.visma.blue.network.requests.customdata.Expense;

import java.util.ArrayList;
import java.util.Locale;

public class ExpenseCurrencyAdapter extends ArrayAdapter<Expense.Currency> implements Filterable {

    private ArrayList<Expense.Currency> mExpenseCurrencies;
    private ArrayList<Expense.Currency> mStoredExpenseCurrencies;
    private Expense.Currency mSelectedCurrency;

    public ExpenseCurrencyAdapter(@NonNull Context context,
                                  @NonNull ArrayList<Expense.Currency> expenseCurrencies,
                                  Expense.Currency defaultCurrency) {
        super(context, R.layout.blue_list_item_expense_currency);

        mExpenseCurrencies = new ArrayList<>();
        mExpenseCurrencies.add(emptyValue(getContext()));
        mExpenseCurrencies.addAll(expenseCurrencies);
        mStoredExpenseCurrencies = new ArrayList<>(mExpenseCurrencies);
        mSelectedCurrency = defaultCurrency;

        if (mSelectedCurrency == null) {
            if (mExpenseCurrencies.size() > 1 && mExpenseCurrencies.get(1).timesUsed > 0) {
                //The most used currency
                mSelectedCurrency = mExpenseCurrencies.get(1);
            } else {
                //Empty currency selection
                mSelectedCurrency = mExpenseCurrencies.get(0);
            }
        }

        scrollToSelectedPosition(getSelectionPosition(), context);
    }

    public static Expense.Currency emptyValue(Context context) {
        return new Expense.Currency(context.getString(R.string
                .visma_blue_spinner_nothing_chosen));
    }

    private static class ViewHolder {
        TextView name;
        ImageView selectionIndicatorImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Expense.Currency expenseCurrency = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.blue_list_item_expense_currency, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.listItemExpenseCurrency);
            viewHolder.selectionIndicatorImage = (ImageView) convertView.findViewById(R.id
                    .currency_selector_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        if (isSelected(position)) {
            viewHolder.selectionIndicatorImage.setVisibility(View.VISIBLE);
        } else {
            viewHolder.selectionIndicatorImage.setVisibility(View.GONE);
        }

        viewHolder.name.setText(expenseCurrency.toString());
        return convertView;
    }

    public boolean isSelected(int position) {
        return mExpenseCurrencies != null && mExpenseCurrencies.size() > 0
                && mSelectedCurrency.guid.equals(mExpenseCurrencies.get(position).guid);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mExpenseCurrencies = (ArrayList<Expense.Currency>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    // No filter implemented we return all the list
                    results.values = mStoredExpenseCurrencies;
                } else {
                    mExpenseCurrencies = mStoredExpenseCurrencies;

                    String constraintString = constraint.toString()
                            .toUpperCase(Locale.getDefault());
                    ArrayList<Expense.Currency> filteredCurrencies = new ArrayList<>();

                    for (Expense.Currency currency : mExpenseCurrencies) {
                        if (currency.toString().toUpperCase(Locale.getDefault())
                                .contains(constraintString)) {
                            filteredCurrencies.add(currency);
                        }
                    }

                    results.values = filteredCurrencies;
                }

                return results;
            }
        };
    }

    @Override
    public int getCount() {
        if (mExpenseCurrencies != null) {
            return mExpenseCurrencies.size();
        } else {
            return 0;
        }
    }

    @Override
    public Expense.Currency getItem(int position) {
        if (mExpenseCurrencies != null) {
            return mExpenseCurrencies.get(position);
        } else {
            return null;
        }
    }

    private int getSelectionPosition() {
        if (mExpenseCurrencies != null && mExpenseCurrencies.size() > 0) {
            int expenseTypeCount = mExpenseCurrencies.size();
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
            final View listView = ((Activity) context).getWindow().getDecorView().findViewById(R.id
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