package com.visma.blue.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.visma.blue.BuildConfig;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.requests.customdata.Expense;
import com.visma.blue.network.requests.customdata.GetCustomDataAnswer;
import com.visma.blue.network.requests.customdata.GetCustomDataRequest;
import com.visma.blue.network.requests.customdata.Netvisor;
import com.visma.blue.network.requests.customdata.Severa;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.ExpenseCurrencies;
import com.visma.blue.provider.ExpenseExpenseTypes;
import com.visma.blue.provider.LastSyncTimestamps;
import com.visma.blue.provider.NetvisorPayloads;
import com.visma.blue.provider.SeveraCases;
import com.visma.blue.provider.SeveraProducts;
import com.visma.blue.provider.SeveraTaxes;
import com.visma.common.util.Util;

import java.util.ArrayList;

public class CustomDataDownloadService extends IntentService {

    public static final String CUSTOM_DATA_UPDATE_TYPE = "CUSTOM_DATA_UPDATE_TYPE";

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public CustomDataDownloadService() {
        super(CustomDataDownloadService.class.toString());
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns,
     * IntentService stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        final int type;
        if (!intent.hasExtra(CUSTOM_DATA_UPDATE_TYPE)) {
            return;
        }
        type = intent.getIntExtra(CUSTOM_DATA_UPDATE_TYPE, -1);

        if (!Util.isConnectedOrConnecting(this)) {
            return;
        }

        final String token = VismaUtils.getToken();
        if (TextUtils.isEmpty(token)) {
            return;
        }

        // Formulate the request and handle the response.
        GetCustomDataRequest request = new GetCustomDataRequest<>(this,
                token,
                GetCustomDataAnswer.class,
                new Response.Listener<GetCustomDataAnswer>() {
                    @Override
                    public void onResponse(final GetCustomDataAnswer response) {
                        new UpdateDatabaseTask(System.currentTimeMillis(), type).execute(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (BuildConfig.DEBUG) {
                            GetCustomDataAnswer answer = new GetCustomDataAnswer();
                            answer.expense = getMockedData();
                            // Make the data 14 minutes old as it is refreshed every 15 minutes
                            new UpdateDatabaseTask(System.currentTimeMillis() - DateUtils
                                    .MINUTE_IN_MILLIS * 14, type).execute(answer);
                        }
                    }
                }
        );

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance().addToRequestQueue(request);
    }

    private Expense getMockedData() {
        Expense mockedData = new Expense();
        mockedData.currencies = new ArrayList<>();
        mockedData.expenseTypes = new ArrayList<>();

        mockedData.currencies.add(Expense.Currency.mockedData());
        mockedData.currencies.add(Expense.Currency.mockedData());
        mockedData.currencies.add(Expense.Currency.mockedData());

        mockedData.expenseTypes.add(Expense.ExpenseType.mockedData());
        mockedData.expenseTypes.add(Expense.ExpenseType.mockedData());

        return mockedData;
    }

    private class UpdateDatabaseTask extends AsyncTask<GetCustomDataAnswer, Void, Void> {
        private final long mTimestamp;
        private final int mType;

        public UpdateDatabaseTask(long timestamp, int type) {
            this.mTimestamp = timestamp;
            this.mType = type;
        }

        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected Void doInBackground(GetCustomDataAnswer... getCustomDataAnswers) {
            // Update the timestamp before we insert.
            // The delete/insert will trigger any active loaders to reload the data and probably
            // trigger a new
            // download if the time stamp is not updated
            updateCustomDataTimestamps();

            //Delete the old data
            deleteOldCustomData();

            //Update with new custom data
            updateCustomData(getCustomDataAnswers[0]);

            return null;
        }

        private void updateCustomDataTimestamps() {
            ContentValues timestampValues = new ContentValues();
            if (mType == LastSyncTimestamps.Type.EXPENSE_CUSTOM_DATA) {
                timestampValues.put(LastSyncTimestamps.TYPE,
                        LastSyncTimestamps.Type.EXPENSE_CUSTOM_DATA);
                timestampValues.put(LastSyncTimestamps.TIMESTAMP, this.mTimestamp);
            } else if (mType == LastSyncTimestamps.Type.SEVERA_CUSTOM_DATA) {
                timestampValues.put(LastSyncTimestamps.TYPE,
                        LastSyncTimestamps.Type.SEVERA_CUSTOM_DATA);
                timestampValues.put(LastSyncTimestamps.TIMESTAMP, this.mTimestamp);
            } else if (mType == LastSyncTimestamps.Type.NETVISOR_CUSTOM_DATA) {
                timestampValues.put(LastSyncTimestamps.TYPE,
                        LastSyncTimestamps.Type.NETVISOR_CUSTOM_DATA);
                timestampValues.put(LastSyncTimestamps.TIMESTAMP, this.mTimestamp);
            }

            if (timestampValues.size() > 0) {
                getContentResolver().insert(BlueContentProvider.CONTENT_URI_LAST_SYNC_TIMESTAMPS,
                        timestampValues);
            }

        }

        private void deleteOldCustomData() {
            if (mType == LastSyncTimestamps.Type.EXPENSE_CUSTOM_DATA) {
                getContentResolver().delete(BlueContentProvider
                        .CONTENT_URI_EXPENSE_EXPENSE_TYPES, null, null);
                getContentResolver().delete(BlueContentProvider.CONTENT_URI_EXPENSE_CURRENCIES,
                        null, null);
            } else if (mType == LastSyncTimestamps.Type.SEVERA_CUSTOM_DATA) {
                getContentResolver().delete(BlueContentProvider.CONTENT_URI_SEVERA_CASES, null,
                        null);
                getContentResolver().delete(BlueContentProvider.CONTENT_URI_SEVERA_PRODUCTS,
                        null, null);
                getContentResolver().delete(BlueContentProvider.CONTENT_URI_SEVERA_TAXES, null,
                        null);
            } else if (mType == LastSyncTimestamps.Type.NETVISOR_CUSTOM_DATA) {
                getContentResolver().delete(BlueContentProvider.CONTENT_URI_NETVISOR_PAYLOADS, null,
                        null);
            }
        }

        private void updateCustomData(GetCustomDataAnswer getCustomDataAnswer) {
            if (mType == LastSyncTimestamps.Type.EXPENSE_CUSTOM_DATA) {
                updateExpenseCustomData(getCustomDataAnswer);
            } else if (mType == LastSyncTimestamps.Type.SEVERA_CUSTOM_DATA) {
                updateSeveraCustomData(getCustomDataAnswer);
            } else if (mType == LastSyncTimestamps.Type.NETVISOR_CUSTOM_DATA) {
                updateNetvisorCustomData(getCustomDataAnswer);
            }
        }

        private void updateExpenseCustomData(GetCustomDataAnswer getCustomDataAnswer) {
            if (getCustomDataAnswer.expense != null) {
                Expense expenseCustomData = getCustomDataAnswer.expense;
                insertExpenseTypes(expenseCustomData.expenseTypes);
                insertExpenseCurrencies(expenseCustomData.currencies);
            }
        }

        private void updateSeveraCustomData(GetCustomDataAnswer getCustomDataAnswer) {
            if (getCustomDataAnswer.severa != null) {
                Severa severaCustomData = getCustomDataAnswer.severa;
                insertSeveraCases(severaCustomData.cases);
                insertSeveraProducts(severaCustomData.products);
                insertSeveraTaxes(severaCustomData.taxes);
            }
        }

        // Add the severa cases
        private void insertSeveraCases(ArrayList<Severa.Case> cases) {
            if (cases != null) {
                Gson gson = new GsonBuilder().create();
                ArrayList<ContentValues> bulkValues = new ArrayList<>(cases.size());

                for (Severa.Case severaCase : cases) {
                    ContentValues values = new ContentValues();
                    values.put(SeveraCases.GUID, severaCase.guid);
                    values.put(SeveraCases.CASE_NAME, severaCase.name);
                    values.put(SeveraCases.TASK, gson.toJson(severaCase.task));
                    bulkValues.add(values);
                }

                if (!bulkValues.isEmpty()) {
                    getContentResolver().bulkInsert(BlueContentProvider.CONTENT_URI_SEVERA_CASES,
                            bulkValues.toArray(new ContentValues[bulkValues.size()]));
                }
            }
        }

        // Add the severa products
        private void insertSeveraProducts(ArrayList<Severa.Product> products) {
            if (products != null) {
                ArrayList<ContentValues> bulkValues = new ArrayList<>(products.size());

                for (Severa.Product product : products) {
                    ContentValues values = new ContentValues();
                    values.put(SeveraProducts.GUID, product.guid);
                    values.put(SeveraProducts.PRODUCT_NAME, product.name);
                    values.put(SeveraProducts.USE_START_AND_END_TIME,
                            product.useStartAndEndTime ? 1 : 0);
                    values.put(SeveraProducts.PRICE, product.price);
                    values.put(SeveraProducts.CURRENCY_CODE, product.currencyCode);
                    values.put(SeveraProducts.VAT_PERCENTAGE, product.vatPercentage);
                    bulkValues.add(values);
                }

                if (!bulkValues.isEmpty()) {
                    getContentResolver().bulkInsert(BlueContentProvider.CONTENT_URI_SEVERA_PRODUCTS,
                            bulkValues.toArray(new ContentValues[bulkValues.size()]));
                }
            }
        }

        // Add the severa taxes
        private void insertSeveraTaxes(ArrayList<Severa.Tax> taxes) {
            if (taxes != null) {
                ArrayList<ContentValues> bulkValues = new ArrayList<>(taxes.size());

                for (Severa.Tax severaTax : taxes) {
                    ContentValues values = new ContentValues();
                    values.put(SeveraTaxes.GUID, severaTax.guid);
                    values.put(SeveraTaxes.IS_DEFAULT, severaTax.isDefault ? 1 : 0);
                    values.put(SeveraTaxes.PERCENTAGE, severaTax.percentage);
                    bulkValues.add(values);
                }

                if (!bulkValues.isEmpty()) {
                    getContentResolver().bulkInsert(BlueContentProvider.CONTENT_URI_SEVERA_TAXES,
                            bulkValues.toArray(new ContentValues[bulkValues.size()]));
                }
            }
        }

        // Add the expense types
        private void insertExpenseTypes(ArrayList<Expense.ExpenseType> expenseTypes) {
            if (expenseTypes != null) {
                ArrayList<ContentValues> bulkValues = new ArrayList<>(expenseTypes.size());

                for (Expense.ExpenseType expenseType : expenseTypes) {
                    ContentValues values = new ContentValues();
                    values.put(ExpenseExpenseTypes.NAME, expenseType.name);
                    values.put(ExpenseExpenseTypes.CODE, expenseType.code);
                    values.put(ExpenseExpenseTypes.VALID_FROM, expenseType.validFrom.getTime());
                    values.put(ExpenseExpenseTypes.VALID_UNTIL,
                            expenseType.validUntil != null ? expenseType.validUntil.getTime() :
                                    null);
                    values.put(ExpenseExpenseTypes.TIMES_USED, expenseType.timesUsed);

                    bulkValues.add(values);
                }

                if (!bulkValues.isEmpty()) {
                    getContentResolver().bulkInsert(BlueContentProvider
                                    .CONTENT_URI_EXPENSE_EXPENSE_TYPES,
                            bulkValues.toArray(new ContentValues[bulkValues.size()]));
                }
            }
        }

        // Add the currencies
        private void insertExpenseCurrencies(ArrayList<Expense.Currency> currencies) {
            if (currencies != null) {
                ArrayList<ContentValues> bulkValues = new ArrayList<ContentValues>(currencies
                        .size());

                for (Expense.Currency currency : currencies) {
                    ContentValues values = new ContentValues();
                    values.put(ExpenseCurrencies.COUNTRY_NAME, currency.countryName);
                    values.put(ExpenseCurrencies.CURRENCY_CODE, currency.currencyCode);
                    values.put(ExpenseCurrencies.TIMES_USED, currency.timesUsed);
                    values.put(ExpenseCurrencies.GUID, currency.guid);

                    bulkValues.add(values);
                }

                if (!bulkValues.isEmpty()) {
                    getContentResolver().bulkInsert(BlueContentProvider
                                    .CONTENT_URI_EXPENSE_CURRENCIES,
                            bulkValues.toArray(new ContentValues[bulkValues.size()]));
                }
            }
        }

        private void updateNetvisorCustomData(GetCustomDataAnswer getCustomDataAnswer) {
            if (getCustomDataAnswer.netvisor != null) {
                insertNetvisorPayloads(getCustomDataAnswer.netvisor.dropDowns);
            }
        }

        // Add Netvisor payloads
        private void insertNetvisorPayloads(ArrayList<Netvisor.DropDown> netvisorPayloads) {
            if (netvisorPayloads != null) {
                ArrayList<ContentValues> bulkValues = new ArrayList<>(netvisorPayloads.size());
                Gson gson = new GsonBuilder().create();
                for (Netvisor.DropDown netvisorPayload : netvisorPayloads) {
                    ContentValues values = new ContentValues();
                    values.put(NetvisorPayloads.PAYLOAD_ID, netvisorPayload.id);
                    values.put(NetvisorPayloads.TYPE, netvisorPayload.type);
                    values.put(NetvisorPayloads.TITLE, netvisorPayload.title);
                    values.put(NetvisorPayloads.VALUES,gson.toJson(netvisorPayload.values));
                    bulkValues.add(values);
                }

                if (!bulkValues.isEmpty()) {
                    getContentResolver().bulkInsert(BlueContentProvider
                                    .CONTENT_URI_NETVISOR_PAYLOADS,
                            bulkValues.toArray(new ContentValues[bulkValues.size()]));
                }
            }
        }

    }
}