package com.visma.blue.metadata.expense;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.visma.blue.R;
import com.visma.blue.databinding.BlueActivityExpenseTypeBinding;
import com.visma.blue.misc.Logger;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.requests.customdata.Expense;

import java.util.ArrayList;
import java.util.Date;

public class ExpenseTypeActivity extends AppCompatActivity implements ExpenseTypeModelView
        .ExpenseTypeActionListener {

    public static final String EXTRA_EXPENSE_TYPES = "EXTRA_EXPENSE_TYPES";
    public static final String EXTRA_SELECTED_EXPENSE_TYPE = "EXTRA_SELECTECTED_EXPENSE_TYPE";
    public static final String EXTRA_FILTER_DATE = "EXTRA_FILTER_DATE";

    private SearchView mSearchView;

    private ExpenseTypeModelView mExpenseTypeModelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blue_activity_expense_type);
        enableTopBackButton();
        parseExtraData();
    }

    private void enableTopBackButton() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setHomeButtonEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void parseExtraData() {
        Bundle bundle = getIntent().getExtras();
        ArrayList<Expense.ExpenseType> expenseTypes = null;
        Expense.ExpenseType selectedExpenseType = null;
        long filterDate = 1;

        if (bundle != null) {
            expenseTypes = bundle.getParcelableArrayList(EXTRA_EXPENSE_TYPES);
            selectedExpenseType = bundle.getParcelable(EXTRA_SELECTED_EXPENSE_TYPE);
            filterDate = bundle.getLong(EXTRA_FILTER_DATE);
        }

        bindData(expenseTypes, selectedExpenseType, filterDate);
    }

    private void bindData(ArrayList<Expense.ExpenseType> expenseTypes, Expense.ExpenseType
            selectedExpenseType, long filterDate) {

        BlueActivityExpenseTypeBinding expenseTypeBinding = DataBindingUtil.setContentView(this,
                R.layout.blue_activity_expense_type);

        mExpenseTypeModelView = new ExpenseTypeModelView(getTypeAdapter(expenseTypes,
                new Date(filterDate), selectedExpenseType), this);
        expenseTypeBinding.setExpenseViewModel(mExpenseTypeModelView);
    }


    private ExpenseTypeAdapter getTypeAdapter(ArrayList<Expense.ExpenseType> expenseTypes,
                                              Date date, Expense.ExpenseType
                                                      selectedExpenseType) {

        return new ExpenseTypeAdapter(ExpenseTypeActivity.this, date, expenseTypes,
                selectedExpenseType);
    }

    @Override
    public void onExpenseTypeClick(Expense.ExpenseType expenseType) {
        returnSelectedExpenseType(expenseType);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.blue_activity_severa_search, menu);

        MenuItem searchViewMenuItem = menu.findItem(R.id.blue_activity_severa_search);
        mSearchView = (SearchView) searchViewMenuItem.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mExpenseTypeModelView.filterTypes(newText);
                return true;
            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                    mSearchView.setQuery(null, true);
                }
                return true;
            }
        });

        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Logger.logAction(Logger.ACTION_SEARCH);
                }
            }
        });

        return true;
    }

    private void returnSelectedExpenseType(Expense.ExpenseType expenseType) {
        Intent resultIntent = new Intent();
        Bundle savedBundle = new Bundle();

        savedBundle.putParcelable(EXTRA_SELECTED_EXPENSE_TYPE, expenseType);
        resultIntent.putExtras(savedBundle);

        setResult(Activity.RESULT_OK, resultIntent);
        VismaUtils.forceKeyboardClose(ExpenseTypeActivity.this);
        finish();
    }
}
