package com.visma.blue.metadata.expense;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.visma.blue.network.requests.customdata.Expense;

public class ExpenseCurrencyModelView extends BaseObservable {

    public interface ExpensecurrencyActionListener {
        void onExpenseCurrencyClick(Expense.Currency expenseCurrency);
    }

    private ExpenseCurrencyAdapter mExpenseCurrencyAdapter;
    private ExpensecurrencyActionListener mExpenseActionListener;

    public ExpenseCurrencyModelView(ExpenseCurrencyAdapter expenseCurrencyAdapter,
                                    ExpensecurrencyActionListener currencyActionListener) {
        mExpenseCurrencyAdapter = expenseCurrencyAdapter;
        mExpenseActionListener = currencyActionListener;
    }

    @Bindable
    public BaseAdapter getExpenseCurrencyAdapter() {
        return mExpenseCurrencyAdapter;
    }

    public AdapterView.OnItemClickListener getExpenseCurrencyItemClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mExpenseActionListener.onExpenseCurrencyClick((Expense.Currency) parent
                        .getItemAtPosition(position));
            }
        };
    }

    public void filterCurrencies(String queryText) {
        mExpenseCurrencyAdapter.getFilter().filter(queryText);
    }
}
