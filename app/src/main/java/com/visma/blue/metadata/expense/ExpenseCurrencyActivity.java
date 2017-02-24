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
import com.visma.blue.databinding.BlueActivityExpenseCurrencyBinding;
import com.visma.blue.misc.Logger;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.requests.customdata.Expense;

import java.util.ArrayList;

public class ExpenseCurrencyActivity extends AppCompatActivity implements ExpenseCurrencyModelView
        .ExpensecurrencyActionListener {

    public static final String EXTRA_EXPENSE_CURRENCIES = "EXTRA_EXPENSE_CURRENCIES";
    public static final String EXTRA_SELECTED_EXPENSE_CURRENCY = "EXTRA_SELECTED_EXPENSE_CURRENCY";

    private SearchView mSearchView;

    private ExpenseCurrencyModelView mExpenseCurrencyModelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blue_activity_expense_currency);
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
        ArrayList<Expense.Currency> expenseCurrencies = new ArrayList<>();
        Expense.Currency selectedExpenseCurrency = null;

        if (bundle != null) {
            expenseCurrencies = bundle.getParcelableArrayList(EXTRA_EXPENSE_CURRENCIES);
            selectedExpenseCurrency = bundle.getParcelable(EXTRA_SELECTED_EXPENSE_CURRENCY);
        }

        bindData(expenseCurrencies, selectedExpenseCurrency);
    }

    private void bindData(ArrayList<Expense.Currency> expenseCurrencies, Expense.Currency
            selectedExpenseCurrency) {

        BlueActivityExpenseCurrencyBinding expenseCurrencyBinder = DataBindingUtil
                .setContentView(this, R.layout.blue_activity_expense_currency);

        mExpenseCurrencyModelView = new ExpenseCurrencyModelView(
                getCurrencyAdapter(expenseCurrencies, selectedExpenseCurrency), this);
        expenseCurrencyBinder.setExpenseViewModel(mExpenseCurrencyModelView);
    }


    private ExpenseCurrencyAdapter getCurrencyAdapter(ArrayList<Expense.Currency> expenseCurrencies,
                                                      Expense.Currency selectedExpenseCurrency) {

        return new ExpenseCurrencyAdapter(ExpenseCurrencyActivity.this, expenseCurrencies,
                selectedExpenseCurrency);
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
                mExpenseCurrencyModelView.filterCurrencies(newText);
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

    @Override
    public void onExpenseCurrencyClick(Expense.Currency expenseCurrency) {
        returnSelectedExpenseCurrency(expenseCurrency);
    }

    private void returnSelectedExpenseCurrency(Expense.Currency expenseCurrency) {
        Intent resultIntent = new Intent();
        Bundle savedBundle = new Bundle();

        savedBundle.putParcelable(EXTRA_SELECTED_EXPENSE_CURRENCY, expenseCurrency);
        resultIntent.putExtras(savedBundle);

        setResult(Activity.RESULT_OK, resultIntent);
        VismaUtils.forceKeyboardClose(ExpenseCurrencyActivity.this);
        finish();
    }
}