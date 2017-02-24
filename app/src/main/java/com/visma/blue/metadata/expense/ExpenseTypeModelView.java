package com.visma.blue.metadata.expense;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.visma.blue.network.requests.customdata.Expense;

public class ExpenseTypeModelView extends BaseObservable {

    public interface ExpenseTypeActionListener {
        void onExpenseTypeClick(Expense.ExpenseType expenseType);
    }

    private ExpenseTypeAdapter mExpenseTypeAdapter;
    private ExpenseTypeActionListener mExpenseActionListener;

    public ExpenseTypeModelView(ExpenseTypeAdapter expenseTypeAdapter,
                                ExpenseTypeActionListener typeActionListener) {
        mExpenseTypeAdapter = expenseTypeAdapter;
        mExpenseActionListener = typeActionListener;
    }

    @Bindable
    public BaseAdapter getExpenseTypeAdapter() {
        return mExpenseTypeAdapter;
    }

    public AdapterView.OnItemClickListener getExpenseTypeItemClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mExpenseActionListener.onExpenseTypeClick((Expense.ExpenseType) parent
                        .getItemAtPosition(position));
            }
        };
    }

    public void filterTypes(String queryText) {
        mExpenseTypeAdapter.getFilter().filter(queryText);
    }
}
