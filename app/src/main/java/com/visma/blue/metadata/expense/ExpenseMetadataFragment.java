package com.visma.blue.metadata.expense;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.visma.blue.R;
import com.visma.blue.databinding.BlueFragmentMetadataExpenseBinding;
import com.visma.blue.metadata.BaseMetadataFragment;
import com.visma.blue.metadata.MetadataFragment;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.containers.ExpenseCustomData;
import com.visma.blue.network.requests.customdata.Expense;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.ExpenseCurrencies;
import com.visma.blue.provider.ExpenseExpenseTypes;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class ExpenseMetadataFragment extends BaseMetadataFragment implements LoaderManager
        .LoaderCallbacks<Cursor>, MetadataViewModel.ExpenseActionListener {

    private static final int EXPENSE_TYPE_REQUEST_CODE = 5674;
    private static final int EXPENSE_CURRENCY_REQUEST_CODE = 5675;

    private static final int LOADER_ID_EXPENSE_TYPES = 1;
    private static final int LOADER_ID_CURRENCIES = 2;

    private static final int MOST_USED_EXPENSE_TYPES_COUNT = 5;
    private static final int MOST_USED_EXPENSE_CURRENCIES_COUNT = 5;

    private MetadataViewModel mMetadataViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle;
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            bundle = savedInstanceState;
        } else {
            bundle = getArguments();
        }

        if (bundle != null) {
            if (bundle.containsKey(MetadataFragment.EXTRA_DATA_METADATA_DOCUMENT_CREATION_DATE)) {
                mOnlineMetaData.date = new Date(bundle.getLong(MetadataFragment
                        .EXTRA_DATA_METADATA_DOCUMENT_CREATION_DATE));
            }

            if (mOnlineMetaData.expenseCustomData == null) {
                mOnlineMetaData.expenseCustomData = new ExpenseCustomData();
            }
        }

        getLoaderManager().initLoader(LOADER_ID_EXPENSE_TYPES, null, this);
        getLoaderManager().initLoader(LOADER_ID_CURRENCIES, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        BlueFragmentMetadataExpenseBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.blue_fragment_metadata_expense,
                        container, false);
        mMetadataViewModel = new MetadataViewModel(inflater.getContext(), getActivity()
                .getFragmentManager(), mOnlineMetaData, this);
        binding.setMetadataViewModel(mMetadataViewModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupImageLayout(view, R.id.fragment_metadata_layout_image, R.id
                .fragment_metadata_image_filename);

        /*
        //Add some mocked data to the spinners
        Expense mockedData = getMockedData();
        mMetadataViewModel.updateCustomData(mockedData.expenseTypes, mockedData.currencies);
        */
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.findItem(R.id.blue_fragment_metadata_menu_qr_code).setVisible(false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID_EXPENSE_TYPES) {
            return new CursorLoader(getActivity(), BlueContentProvider
                    .CONTENT_URI_EXPENSE_EXPENSE_TYPES, new String[]{
                    ExpenseExpenseTypes._ID,
                    ExpenseExpenseTypes.NAME,
                    ExpenseExpenseTypes.CODE,
                    ExpenseExpenseTypes.VALID_FROM,
                    ExpenseExpenseTypes.VALID_UNTIL,
                    ExpenseExpenseTypes.TIMES_USED},
                    null, null, ExpenseExpenseTypes.NAME + " COLLATE LOCALIZED ASC");
        } else if (id == LOADER_ID_CURRENCIES) {
            return new CursorLoader(getActivity(), BlueContentProvider
                    .CONTENT_URI_EXPENSE_CURRENCIES, new String[]{
                    ExpenseCurrencies._ID,
                    ExpenseCurrencies.COUNTRY_NAME,
                    ExpenseCurrencies.CURRENCY_CODE,
                    ExpenseCurrencies.TIMES_USED,
                    ExpenseCurrencies.GUID},
                    null, null, ExpenseCurrencies.COUNTRY_NAME + " COLLATE LOCALIZED ASC");
        } else {
            throw new UnsupportedOperationException("Trying to use a loader that is not "
                    + "implemented.");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        final int loaderId = loader.getId();

        if (loaderId == LOADER_ID_EXPENSE_TYPES) {
            if (data != null) {
                ArrayList<Expense.ExpenseType> expenseTypes = new ArrayList<>(data.getCount());
                boolean rowsLeft = data.moveToFirst();
                while (rowsLeft) {
                    expenseTypes.add(new Expense.ExpenseType(data));
                    rowsLeft = data.moveToNext();
                }

                ArrayList<Expense.ExpenseType> mostCommonExpenseTypes =
                        new ArrayList<>(expenseTypes);
                Collections.sort(mostCommonExpenseTypes, new Comparator<Expense.ExpenseType>() {
                    @Override
                    public int compare(Expense.ExpenseType lhs, Expense.ExpenseType rhs) {
                        int result = (int) (rhs.timesUsed - lhs.timesUsed);
                        if (result != 0) {
                            return result;
                        } else {
                            return Collator.getInstance().compare(lhs.name, rhs.name);
                        }
                    }
                });

                for (int i = 0; i < mostCommonExpenseTypes.size() && i
                        < MOST_USED_EXPENSE_TYPES_COUNT; i++) {
                    Expense.ExpenseType expenseType = mostCommonExpenseTypes.get(i);
                    expenseTypes.remove(expenseType);
                    expenseTypes.add(i, expenseType);
                }

                mMetadataViewModel.updateExpenseTypes(expenseTypes);
            } else {
                mMetadataViewModel.updateExpenseTypes(new ArrayList<Expense.ExpenseType>());
            }
        } else if (loaderId == LOADER_ID_CURRENCIES) {
            if (data != null) {
                ArrayList<Expense.Currency> currencies = new ArrayList<>(data.getCount());
                boolean rowsLeft = data.moveToFirst();
                while (rowsLeft) {
                    currencies.add(new Expense.Currency(data));
                    rowsLeft = data.moveToNext();
                }

                ArrayList<Expense.Currency> mostCommonCurrencies = new ArrayList<>(currencies);
                Collections.sort(mostCommonCurrencies, new Comparator<Expense.Currency>() {
                    @Override
                    public int compare(Expense.Currency lhs, Expense.Currency rhs) {
                        int result = (int) (rhs.timesUsed - lhs.timesUsed);
                        if (result != 0) {
                            return result;
                        } else {
                            return Collator.getInstance().compare(lhs.countryName, rhs.countryName);
                        }
                    }
                });

                for (int i = 0; i < mostCommonCurrencies.size() && i
                        < MOST_USED_EXPENSE_CURRENCIES_COUNT; i++) {
                    Expense.Currency currency = mostCommonCurrencies.get(i);
                    currencies.remove(currency);
                    currencies.add(i, currency);
                }

                mMetadataViewModel.updateCurrencies(currencies);
                if (mMetadataViewModel.shouldSetDefaultCurrency()) {
                    mMetadataViewModel.setDefaultCurrency(VismaUtils
                            .getDefaultCurrency(getContext()));
                }

            }
        } else {
            throw new UnsupportedOperationException("Trying to use a loader that is not "
                    + "implemented.");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void openExpenseTypeActivity() {
        final Intent intent = new Intent(getActivity(), ExpenseTypeActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ExpenseTypeActivity.EXTRA_EXPENSE_TYPES, mMetadataViewModel
                .getExpenseTypes());
        bundle.putLong(ExpenseTypeActivity.EXTRA_FILTER_DATE, mMetadataViewModel
                .getExpenseFilterTime());
        bundle.putParcelable(ExpenseTypeActivity.EXTRA_SELECTED_EXPENSE_TYPE, mMetadataViewModel
                .getSelectedExpenseType());
        intent.putExtras(bundle);

        startActivityForResult(intent, EXPENSE_TYPE_REQUEST_CODE);
    }

    private void openExpenseCurrencyActivity() {
        final Intent intent = new Intent(getActivity(), ExpenseCurrencyActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ExpenseCurrencyActivity.EXTRA_EXPENSE_CURRENCIES,
                mMetadataViewModel
                .getExpenseCurrencies());
        bundle.putParcelable(ExpenseCurrencyActivity.EXTRA_SELECTED_EXPENSE_CURRENCY,
                mMetadataViewModel
                .getSelectedExpenseCurrency());
        intent.putExtras(bundle);

        startActivityForResult(intent, EXPENSE_CURRENCY_REQUEST_CODE);
    }

    @Override
    public void onOpenExpenseTypeList() {
        openExpenseTypeActivity();
    }

    @Override
    public void onOpenExpenseCurrencyList() {
        openExpenseCurrencyActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        Bundle receivedBundle = data.getExtras();

        if (requestCode == EXPENSE_TYPE_REQUEST_CODE && resultCode == Activity.RESULT_OK
                && receivedBundle != null) {
            Expense.ExpenseType selectedExpenseType = receivedBundle
                    .getParcelable(ExpenseTypeActivity.EXTRA_SELECTED_EXPENSE_TYPE);
            mMetadataViewModel.updateExpenseTypeSelection(selectedExpenseType);
        } else if (requestCode == EXPENSE_CURRENCY_REQUEST_CODE && resultCode == Activity.RESULT_OK
                && receivedBundle != null) {
            Expense.Currency selectedExpenseCurrency = receivedBundle
                    .getParcelable(ExpenseCurrencyActivity.EXTRA_SELECTED_EXPENSE_CURRENCY);
            mMetadataViewModel.updateExpenseCurrencySelection(selectedExpenseCurrency);
        }
    }

}