package com.visma.blue.provider;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.visma.blue.BuildConfig;
import com.visma.blue.events.MetadataEvent;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.services.CustomDataDownloadService;
import com.visma.blue.services.MetadataDownloadService;

import org.greenrobot.eventbus.EventBus;

public class BlueContentProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    public static final Uri CONTENT_URI_LAST_SYNC_TIMESTAMPS =
            Uri.parse("content://" + AUTHORITY + "/lastsynctimestamps");
    public static final Uri CONTENT_URI_EXPENSE_EXPENSE_TYPES =
            Uri.parse("content://" + AUTHORITY + "/expense/expensetypes");
    public static final Uri CONTENT_URI_EXPENSE_CURRENCIES =
            Uri.parse("content://" + AUTHORITY + "/expense/currencies");
    public static final Uri CONTENT_URI_METADATA_LIST =
            Uri.parse("content://" + AUTHORITY + "/metadata/storedImages");
    public static final Uri CONTENT_URI_METADATA_TEMP_BITMAP =
            Uri.parse("content://" + AUTHORITY + "/metadata/tempBitmap");
    public static final Uri CONTENT_URI_SEVERA_CASES =
            Uri.parse("content://" + AUTHORITY + "/severa/cases");
    public static final Uri CONTENT_URI_SEVERA_PRODUCTS =
            Uri.parse("content://" + AUTHORITY + "/severa/products");
    public static final Uri CONTENT_URI_SEVERA_TAXES =
            Uri.parse("content://" + AUTHORITY + "/severa/taxes");
    public static final Uri CONTENT_URI_NETVISOR_PAYLOADS =
            Uri.parse("content://" + AUTHORITY + "/netvisor/payloads");
    public static final Uri CONTENT_URI_METADATA_LIST_TEMP =
            Uri.parse("content://" + AUTHORITY + "/metadata/temporary");
    public static final Uri CONTENT_URI_METADATA_NOT_SYNC =
            Uri.parse("content://" + AUTHORITY + "/metadata/notSynchronized");
    public static final Uri CONTENT_URI_METADATA_NOT_SYNC_IMAGE =
            Uri.parse("content://" + AUTHORITY + "/metadata/notSynchronizedImage");
    public static final Uri CONTENT_URI_FILTERED_METADATA =
            Uri.parse("content://" + AUTHORITY + "/metadata/filtered");


    // Don't use the same name as the old database as we don't want to create any conflicts
    private static final String DATABASE_NAME = "blue";

    private static final String DATABASE_TABLE_LAST_SYNC_TIMESTAMPS = "lastSyncTimestamps";
    private static final String DATABASE_TABLE_EXPENSE_EXPENSE_TYPES = "expenseExpenseTypes";
    private static final String DATABASE_TABLE_EXPENSE_CURRENCIES = "expenseCurrencies";
    private static final String DATABASE_TABLE_METADATA = "storedImages";
    private static final String DATABASE_TABLE_TEMP_BITMAP = "tempBitmap";
    private static final String DATABASE_TABLE_SEVERA_CASES = "severaCases";
    private static final String DATABASE_TABLE_SEVERA_PRODUCTS = "severaProducts";
    private static final String DATABASE_TABLE_SEVERA_TAXES = "severaTaxes";
    private static final String DATABASE_TABLE_NETVISOR_PAYLOADS = "netvisorPayloads";


    private static final int LAST_SYNC_TIMESTAMPS = 1;
    private static final int EXPENSE_EXPENSE_TYPES = 2;
    private static final int EXPENSE_CURRENCIES = 3;
    private static final int METADATA_STORED_IMAGES = 4;
    private static final int METADATA_TEMP_BITMAP = 5;
    private static final int METADATA_FROM_BACKEND = 6;
    private static final int SEVERA_CASES = 7;
    private static final int SEVERA_PRODUCTS = 8;
    private static final int SEVERA_TAXES = 9;
    private static final int NETVISOR_PAYLOADS = 10;
    private static final int METADATA_TEMPORARY_IMAGES = 11;
    private static final int METADATA_NOT_SYNC = 12;
    private static final int METADATA_NOT_SYNC_IMAGE = 13;
    private static final int METADATA_FILTERED = 14;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "lastsynctimestamps", LAST_SYNC_TIMESTAMPS);
        sUriMatcher.addURI(AUTHORITY, "expense/expensetypes", EXPENSE_EXPENSE_TYPES);
        sUriMatcher.addURI(AUTHORITY, "expense/currencies", EXPENSE_CURRENCIES);
        sUriMatcher.addURI(AUTHORITY, "metadata/storedImages", METADATA_STORED_IMAGES);
        sUriMatcher.addURI(AUTHORITY, "metadata/tempBitmap", METADATA_TEMP_BITMAP);
        sUriMatcher.addURI(AUTHORITY, "metadata/fromBackend", METADATA_FROM_BACKEND);
        sUriMatcher.addURI(AUTHORITY, "severa/cases", SEVERA_CASES);
        sUriMatcher.addURI(AUTHORITY, "severa/products", SEVERA_PRODUCTS);
        sUriMatcher.addURI(AUTHORITY, "severa/taxes", SEVERA_TAXES);
        sUriMatcher.addURI(AUTHORITY, "netvisor/payloads", NETVISOR_PAYLOADS);
        sUriMatcher.addURI(AUTHORITY, "/metadata/temporary", METADATA_TEMPORARY_IMAGES);
        sUriMatcher.addURI(AUTHORITY, "/metadata/notSynchronized", METADATA_NOT_SYNC);
        sUriMatcher.addURI(AUTHORITY, "/metadata/notSynchronizedImage", METADATA_NOT_SYNC_IMAGE);
        sUriMatcher.addURI(AUTHORITY, "/metadata/filtered", METADATA_FILTERED);
    }

    private DatabaseHelper mOpenHelper;
    private SQLiteDatabase db;
    private MetadataList mMetadaList;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        this.db = mOpenHelper.getWritableDatabase();
        mMetadaList = new MetadataList();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        this.db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        /**
         * Choose the projection and adjust the "where" clause based on URI pattern-matching.
         */
        switch (sUriMatcher.match(uri)) {
            case LAST_SYNC_TIMESTAMPS:
                setUserIdAndCompanyInWhereClause(qb);
                qb.setTables(DATABASE_TABLE_LAST_SYNC_TIMESTAMPS);
                break;
            case EXPENSE_EXPENSE_TYPES:
                setUserIdAndCompanyInWhereClause(qb);
                qb.setTables(DATABASE_TABLE_EXPENSE_EXPENSE_TYPES);
                break;
            case EXPENSE_CURRENCIES:
                setUserIdAndCompanyInWhereClause(qb);
                qb.setTables(DATABASE_TABLE_EXPENSE_CURRENCIES);
                break;
            case METADATA_STORED_IMAGES:
                setUserIdAndCompanyInWhereClause(qb);
                qb.setTables(DATABASE_TABLE_METADATA);
                break;
            case METADATA_TEMP_BITMAP:
                qb.setTables(DATABASE_TABLE_TEMP_BITMAP);
                break;
            case SEVERA_CASES:
                setUserIdAndCompanyInWhereClause(qb);
                qb.setTables(DATABASE_TABLE_SEVERA_CASES);
                break;
            case SEVERA_PRODUCTS:
                setUserIdAndCompanyInWhereClause(qb);
                qb.setTables(DATABASE_TABLE_SEVERA_PRODUCTS);
                break;
            case SEVERA_TAXES:
                setUserIdAndCompanyInWhereClause(qb);
                qb.setTables(DATABASE_TABLE_SEVERA_TAXES);
                break;
            case NETVISOR_PAYLOADS:
                setUserIdAndCompanyInWhereClause(qb);
                qb.setTables(DATABASE_TABLE_NETVISOR_PAYLOADS);
                break;
            case METADATA_NOT_SYNC:
                setUserIdAndCompanyInWhereClause(qb);
                qb.setTables(DATABASE_TABLE_METADATA);
                break;
            case METADATA_NOT_SYNC_IMAGE:
                setUserInWhereClause(qb);
                qb.setTables(DATABASE_TABLE_METADATA);
                break;
            case METADATA_FILTERED:
                setUserInWhereClause(qb);
                qb.setTables(DATABASE_TABLE_METADATA);
                break;
            default:
                // If the URI doesn't match any of the known patterns, throw an exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*
         * Performs the query. If no problems occur trying to read the database,
         * then a Cursor object is returned; otherwise, the cursor variable
         * contains null. If no records were selected, then the Cursor object is
         * empty, and Cursor.getCount() returns 0.
         */
        Cursor cursor = qb.query(db, // The database to query
                projection, // The columns to return from the query
                selection, // The columns for the where clause
                selectionArgs, // The values for the where clause
                null, // don't group the rows
                null, // don't filter by row groups
                sortOrder // The sort order
        );

        // Tells the Cursor what URI to watch, so it knows when its source data
        // changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        refreshDataIfNeeded(sUriMatcher.match(uri));
        return getMergedCursor(cursor, sUriMatcher.match(uri), selectionArgs);
    }

    private Cursor getMergedCursor(Cursor localDataCursor, int uri, String[] selectionArgs) {
        if (uri == METADATA_STORED_IMAGES) {
            MatrixCursor mergedCursor = mMetadaList
                    .getMergedMetaData(MetadataList.getMetaData(localDataCursor), null);
            mergedCursor.setNotificationUri(getContext().getContentResolver(), BlueContentProvider
                    .CONTENT_URI_METADATA_LIST);
            return mergedCursor;
        } else if (uri == METADATA_FILTERED) {
            MatrixCursor mergedCursor = mMetadaList
                    .getMergedMetaData(MetadataList.getMetaData(localDataCursor), selectionArgs);
            mergedCursor.setNotificationUri(getContext().getContentResolver(), BlueContentProvider
                    .CONTENT_URI_METADATA_LIST);
            return mergedCursor;
        } else {
            return localDataCursor;
        }
    }

    private void setUserIdAndCompanyInWhereClause(SQLiteQueryBuilder qb) {
        final String currentUserId = VismaUtils.getCurrentUserId();
        final String currentCompanyGuid = VismaUtils.getCurrentCompanyId();

        qb.appendWhere(BaseColumns.OWNER_USER_ID + " = ");
        qb.appendWhereEscapeString(currentUserId);
        qb.appendWhere(" AND " + BaseColumns.OWNER_COMPANY_ID + " = ");
        qb.appendWhereEscapeString(currentCompanyGuid);
    }

    private void setUserInWhereClause(SQLiteQueryBuilder qb) {
        final String user = VismaUtils.getCurrentEmail(getContext());
        qb.appendWhere(MetadataList.METADATA_USER + " = ");
        qb.appendWhereEscapeString(user);
    }

    private void refreshDataIfNeeded(int dataType) {
        // Prevent refreshing of all tables as multiple tables are downloaded in the same calls
        if (dataType == EXPENSE_EXPENSE_TYPES) {
            refreshCustomData(LastSyncTimestamps.Type.EXPENSE_CUSTOM_DATA);
        } else if (dataType == SEVERA_CASES) {
            refreshCustomData(LastSyncTimestamps.Type.SEVERA_CUSTOM_DATA);
        } else if (dataType == NETVISOR_PAYLOADS) {
            refreshCustomData(LastSyncTimestamps.Type.NETVISOR_CUSTOM_DATA);
        } else if (dataType == METADATA_STORED_IMAGES) {
            refreshMetadata();
        }
    }

    private void refreshMetadata() {
        long currentTime = System.currentTimeMillis();
        if (currentTime > mMetadaList.getLastUpdateTime() + MetadataList.UPDATE_DELAY_TIME) {
            Context context = getContext();
            Intent intent = new Intent(context, MetadataDownloadService.class);
            context.startService(intent);
        } else {
            EventBus.getDefault().post(new MetadataEvent(MetadataEvent.UpdateStatus.FINISHED_UPDATE
                    .getValue()));
        }
    }

    private void refreshCustomData(int type) {
        final String selection = BaseColumns.OWNER_USER_ID + " = ? AND " + BaseColumns
                .OWNER_COMPANY_ID + " = ? "
                + "AND " + LastSyncTimestamps.TYPE + " = ?";
        final String[] selectionArgs =
                new String[]{VismaUtils.getCurrentUserId(), VismaUtils.getCurrentCompanyId(),
                        Integer.toString(type)};

        Cursor timestampCursor = db.query(
                DATABASE_TABLE_LAST_SYNC_TIMESTAMPS,
                new String[]{LastSyncTimestamps.TIMESTAMP},
                selection,
                selectionArgs,
                null,
                null,
                null);

        long now = System.currentTimeMillis();
        long timestamp = 0;
        if (timestampCursor != null && timestampCursor.getCount() != 0) {
            timestampCursor.moveToFirst();
            timestamp =
                    timestampCursor.getLong(timestampCursor
                            .getColumnIndexOrThrow(LastSyncTimestamps.TIMESTAMP));
            timestampCursor.close();
        }

        final long dataAge = now - timestamp;
        if (dataAge > (DateUtils.MINUTE_IN_MILLIS * 15)) {
            Context context = getContext();
            Intent intent = new Intent(context, CustomDataDownloadService.class);
            intent.putExtra(CustomDataDownloadService.CUSTOM_DATA_UPDATE_TYPE, type);
            context.startService(intent);
        }
    }

    private int updateTempBitmap(ContentValues values) {
        final String currentUserId = VismaUtils.getCurrentUserId();
        final String currentCompanyGuid = VismaUtils.getCurrentCompanyId();

        // To be able to filter out the current user later on
        if (!values.containsKey(BaseColumns.OWNER_USER_ID)) {
            values.put(BaseColumns.OWNER_USER_ID, currentUserId);
        }
        if (!values.containsKey(BaseColumns.OWNER_COMPANY_ID)) {
            values.put(BaseColumns.OWNER_COMPANY_ID, currentCompanyGuid);
        }

        int count = db.update(DATABASE_TABLE_TEMP_BITMAP, values, null, null);
        if (count == 0) {
            db.insert(DATABASE_TABLE_TEMP_BITMAP, null, values);
        }
        return count;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        this.db = mOpenHelper.getWritableDatabase();

        final String currentUserId = VismaUtils.getCurrentUserId();
        final String currentCompanyGuid = VismaUtils.getCurrentCompanyId();

        // To be able to filter out the current user later on
        if (!values.containsKey(BaseColumns.OWNER_USER_ID)) {
            values.put(BaseColumns.OWNER_USER_ID, currentUserId);
        }
        if (!values.containsKey(BaseColumns.OWNER_COMPANY_ID)) {
            values.put(BaseColumns.OWNER_COMPANY_ID, currentCompanyGuid);
        }

        long rowId;
        switch (sUriMatcher.match(uri)) {
            case LAST_SYNC_TIMESTAMPS:
                rowId = db.insert(DATABASE_TABLE_LAST_SYNC_TIMESTAMPS, null, values);
                break;
            case EXPENSE_EXPENSE_TYPES:
                rowId = db.insert(DATABASE_TABLE_EXPENSE_EXPENSE_TYPES, null, values);
                break;
            case EXPENSE_CURRENCIES:
                rowId = db.insert(DATABASE_TABLE_EXPENSE_CURRENCIES, null, values);
                break;
            case METADATA_STORED_IMAGES:
                rowId = db.insert(DATABASE_TABLE_METADATA, null, values);
                break;
            case SEVERA_CASES:
                rowId = db.insert(DATABASE_TABLE_SEVERA_CASES, null, values);
                break;
            case SEVERA_PRODUCTS:
                rowId = db.insert(DATABASE_TABLE_SEVERA_PRODUCTS, null, values);
                break;
            case SEVERA_TAXES:
                rowId = db.insert(DATABASE_TABLE_SEVERA_TAXES, null, values);
                break;
            case NETVISOR_PAYLOADS:
                rowId = db.insert(DATABASE_TABLE_NETVISOR_PAYLOADS, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If the insert succeeded, the rowID has a value larger than 0.
        if (rowId > 0) {
            // Creates a URI with the note ID pattern and the new row ID appended to it.
            Uri insertedUri = ContentUris.withAppendedId(uri, rowId);

            // Notifies observers registered against this provider that the data changed.
            getContext().getContentResolver().notifyChange(insertedUri, null);
            return insertedUri;
        } else {
            // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
            throw new SQLException("Failed to insert row into " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        this.db = mOpenHelper.getWritableDatabase();

        final String finalSelection = getDeleteRequestSelection(selection, sUriMatcher.match(uri));
        int count;
        switch (sUriMatcher.match(uri)) {
            case LAST_SYNC_TIMESTAMPS:
                count = db.delete(DATABASE_TABLE_LAST_SYNC_TIMESTAMPS, finalSelection,
                        selectionArgs);
                break;
            case EXPENSE_EXPENSE_TYPES:
                count = db.delete(DATABASE_TABLE_EXPENSE_EXPENSE_TYPES, finalSelection,
                        selectionArgs);
                break;
            case EXPENSE_CURRENCIES:
                count = db.delete(DATABASE_TABLE_EXPENSE_CURRENCIES, finalSelection, selectionArgs);
                break;
            case METADATA_STORED_IMAGES:
                count = db.delete(DATABASE_TABLE_METADATA, finalSelection, selectionArgs);
                break;
            case SEVERA_CASES:
                count = db.delete(DATABASE_TABLE_SEVERA_CASES, finalSelection, selectionArgs);
                break;
            case SEVERA_PRODUCTS:
                count = db.delete(DATABASE_TABLE_SEVERA_PRODUCTS, finalSelection, selectionArgs);
                break;
            case SEVERA_TAXES:
                count = db.delete(DATABASE_TABLE_SEVERA_TAXES, finalSelection, selectionArgs);
                break;
            case NETVISOR_PAYLOADS:
                count = db.delete(DATABASE_TABLE_NETVISOR_PAYLOADS, finalSelection, selectionArgs);
                break;
            case METADATA_TEMPORARY_IMAGES:
                mMetadaList.resetMetaData();
                count = 0;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private String getDeleteRequestSelection(String selection, int uri) {
        final String currentUser = VismaUtils.getCurrentUserId();
        final String currentCompany = VismaUtils.getCurrentCompanyId();

        final String userFilter = BaseColumns.OWNER_USER_ID + " = '" + currentUser + "'";
        final String userCompanyFilter = userFilter  + " AND " + BaseColumns
                .OWNER_COMPANY_ID
                + " = '" + currentCompany + "'";

        if (uri == METADATA_STORED_IMAGES) {
            if (selection.isEmpty()) {
                return userFilter;
            } else {
                return userFilter + " AND (" + selection + ")";
            }
        } else if (TextUtils.isEmpty(selection)) {
            return userCompanyFilter;
        } else {
           return userCompanyFilter + " AND (" + selection + ")";
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (sUriMatcher.match(uri) == METADATA_STORED_IMAGES) {
            mMetadaList.updateMetaData(values);
            return values.length;
        } else {
            return super.bulkInsert(uri, values);
        }

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        this.db = mOpenHelper.getWritableDatabase();

        final String currentUser = VismaUtils.getCurrentUserId();
        final String currentCompany = VismaUtils.getCurrentCompanyId();

        final String userFilter = BaseColumns.OWNER_USER_ID + " = '" + currentUser + "'"
                + " AND " + BaseColumns.OWNER_COMPANY_ID + " = '" + currentCompany + "'";

        int count;
        String finalSelection;
        if (TextUtils.isEmpty(selection)) {
            finalSelection = userFilter;
        } else {
            finalSelection = userFilter + " AND (" + selection + ")";
        }

        switch (sUriMatcher.match(uri)) {
            case LAST_SYNC_TIMESTAMPS:
                count = db.update(DATABASE_TABLE_LAST_SYNC_TIMESTAMPS, values, finalSelection,
                        selectionArgs);
                break;
            case EXPENSE_EXPENSE_TYPES:
                count = db.update(DATABASE_TABLE_EXPENSE_EXPENSE_TYPES, values, finalSelection,
                        selectionArgs);
                break;
            case EXPENSE_CURRENCIES:
                count = db.update(DATABASE_TABLE_EXPENSE_CURRENCIES, values, finalSelection,
                        selectionArgs);
                break;
            case METADATA_STORED_IMAGES:
                count = db.update(DATABASE_TABLE_METADATA, values, finalSelection, selectionArgs);
                break;
            case METADATA_TEMP_BITMAP:
                count = updateTempBitmap(values);
                break;
            default:
                throw new IllegalArgumentException();
        }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows updated.
        return count;
    }

    private static final int DATABASE_VERSION = 3;

    private static final String DATABASE_CREATE_TABLE_LAST_SYNC_TIMESTAMPS =
            "CREATE TABLE " + DATABASE_TABLE_LAST_SYNC_TIMESTAMPS + " ("
                    + LastSyncTimestamps._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + LastSyncTimestamps.OWNER_USER_ID + " TEXT NOT NULL,"
                    + LastSyncTimestamps.OWNER_COMPANY_ID + " TEXT NOT NULL,"
                    + LastSyncTimestamps.TYPE + " INTEGER,"
                    + LastSyncTimestamps.TIMESTAMP + " INTEGER DEFAULT 0,"
                    + LastSyncTimestamps.PAGE + " INTEGER DEFAULT 0,"
                    + " UNIQUE(" + LastSyncTimestamps.OWNER_USER_ID + ", " + LastSyncTimestamps
                    .OWNER_COMPANY_ID + ", "
                    + LastSyncTimestamps.TYPE + ") ON CONFLICT REPLACE"
                    + ");";

    private static final String DATABASE_CREATE_TABLE_EXPENSE_EXPENSE_TYPES =
            "CREATE TABLE " + DATABASE_TABLE_EXPENSE_EXPENSE_TYPES + " ("
                    + ExpenseExpenseTypes._ID + " integer primary key autoincrement,"
                    + ExpenseExpenseTypes.OWNER_USER_ID + " TEXT NOT NULL,"
                    + ExpenseExpenseTypes.OWNER_COMPANY_ID + " TEXT NOT NULL,"
                    + ExpenseExpenseTypes.NAME + " TEXT NOT NULL,"
                    + ExpenseExpenseTypes.CODE + " TEXT NOT NULL,"
                    + ExpenseExpenseTypes.VALID_FROM + " INTEGER NOT NULL,"
                    + ExpenseExpenseTypes.VALID_UNTIL + " INTEGER,"
                    + ExpenseExpenseTypes.TIMES_USED + " INTEGER NOT NULL,"
                    + " UNIQUE(" + ExpenseExpenseTypes.OWNER_USER_ID + ", " + ExpenseExpenseTypes
                    .OWNER_COMPANY_ID + ", "
                    + ExpenseExpenseTypes.CODE + ") ON CONFLICT REPLACE"
                    + ");";

    private static final String DATABASE_CREATE_TABLE_EXPENSE_CURRENCIES =
            "CREATE TABLE " + DATABASE_TABLE_EXPENSE_CURRENCIES + " ("
                    + ExpenseCurrencies._ID + " integer primary key autoincrement,"
                    + ExpenseCurrencies.OWNER_USER_ID + " TEXT NOT NULL,"
                    + ExpenseCurrencies.OWNER_COMPANY_ID + " TEXT NOT NULL,"
                    + ExpenseCurrencies.COUNTRY_NAME + " TEXT NOT NULL,"
                    + ExpenseCurrencies.CURRENCY_CODE + " TEXT NOT NULL,"
                    + ExpenseCurrencies.TIMES_USED + " INTEGER NOT NULL,"
                    + ExpenseCurrencies.GUID + " TEXT NOT NULL,"
                    + " UNIQUE(" + ExpenseCurrencies.OWNER_USER_ID + ", " + ExpenseCurrencies
                    .OWNER_COMPANY_ID + ", "
                    + ExpenseCurrencies.GUID + ") ON CONFLICT REPLACE"
                    + ");";

    private static final String DATABASE_CREATE_TABLE_METADATA =
            "create table " + DATABASE_TABLE_METADATA + " ("
                    + MetadataList._ID + " integer primary key autoincrement,"
                    + MetadataList.OWNER_USER_ID + " TEXT NOT NULL,"
                    + MetadataList.OWNER_COMPANY_ID + " TEXT NOT NULL,"
                    + MetadataList.METADATA_CAN_DELETE + " integer,"
                    + MetadataList.METADATA_COMMENT + " text,"
                    + MetadataList.METADATA_DATE + " integer," // long Date.getTime()
                    + MetadataList.METADATA_PHOTO_ID + " text,"
                    + MetadataList.METADATA_TYPE + " integer,"
                    + MetadataList.METADATA_IS_PAYED + " integer,"
                    + MetadataList.METADATA_PAYMENT_DATE + " integer,"
                    + MetadataList.METADATA_USING_QR_STRING + " text,"
                    + MetadataList.METADATA_NAME + " text,"
                    + MetadataList.METADATA_ORGANISATION_NUMBER + " text,"
                    + MetadataList.METADATA_REFERENCE_NUMBER + " text,"
                    + MetadataList.METADATA_DUE_AMOUNT + " real,"
                    + MetadataList.METADATA_HIGH_VAT_AMOUNT + " real,"
                    + MetadataList.METADATA_MIDDLE_VAT_AMOUNT + " real,"
                    + MetadataList.METADATA_LOW_VAT_AMOUNT + " real,"
                    + MetadataList.METADATA_ZERO_VAT_AMOUNT + " real,"
                    + MetadataList.METADATA_TOTAL_VAT_AMOUNT + " real,"
                    + MetadataList.METADATA_CURRENCY + " text,"
                    + MetadataList.METADATA_INVOICE_DATE + " integer,"
                    + MetadataList.METADATA_DUE_DATE + " integer,"
                    + MetadataList.METADATA_CUSTOM_DATA + " text,"
                    + MetadataList.METADATA_SEVERA_CUSTOM_DATA + " text,"
                    + MetadataList.METADATA_EXPENSE_CUSTOM_DATA + " text,"
                    + MetadataList.METADATA_IMAGE + " blob,"
                    + MetadataList.METADATA_USER + " text,"
                    + MetadataList.METADATA_NOT_SYNCED_DUE_TO_ERROR + " INTEGER DEFAULT 0,"
                    + MetadataList.METADATA_LOCAL_FILE_NAME + " TEXT,"
                    + MetadataList.METADATA_CONTENT_TYPE + " TEXT,"
                    + MetadataList.METADATA_ORIGINAL_FILE_NAME + " TEXT,"
                    + MetadataList.METADATA_IS_READY_FOR_PAYMENT + " INTEGER DEFAULT 0"
                    + ");";

    private static final String DATABASE_CREATE_TABLE_TEMP_BITMAP =
            "create table " + DATABASE_TABLE_TEMP_BITMAP + " ("
                    + TempBitmaps._ID + " integer primary key autoincrement,"
                    + TempBitmaps.OWNER_USER_ID + " TEXT NOT NULL,"
                    + TempBitmaps.OWNER_COMPANY_ID + " TEXT NOT NULL,"
                    + TempBitmaps.TEMP_BITMAP_IMAGE + " blob"
                    + ");";

    private static final String DATABASE_CREATE_TABLE_SEVERA_CASES =
            "create table " + DATABASE_TABLE_SEVERA_CASES + " ("
                    + SeveraCases._ID + " integer primary key autoincrement,"
                    + SeveraCases.OWNER_USER_ID + " TEXT NOT NULL,"
                    + SeveraCases.OWNER_COMPANY_ID + " TEXT NOT NULL,"
                    + SeveraCases.GUID + "  TEXT NOT NULL,"
                    + SeveraCases.CASE_NAME + "  TEXT NOT NULL,"
                    + SeveraCases.TASK + " TEXT"
                    + ");";

    private static final String DATABASE_CREATE_TABLE_SEVERA_PRODUCTS =
            "create table " + DATABASE_TABLE_SEVERA_PRODUCTS + " ("
                    + SeveraProducts._ID + " integer primary key autoincrement,"
                    + SeveraProducts.OWNER_USER_ID + " TEXT NOT NULL,"
                    + SeveraProducts.OWNER_COMPANY_ID + " TEXT NOT NULL,"
                    + SeveraProducts.GUID + "  TEXT NOT NULL,"
                    + SeveraProducts.PRODUCT_NAME + "  TEXT NOT NULL,"
                    + SeveraProducts.USE_START_AND_END_TIME + " INTEGER,"
                    + SeveraProducts.PRICE + " REAL,"
                    + SeveraProducts.CURRENCY_CODE + " TEXT,"
                    + SeveraProducts.VAT_PERCENTAGE + " REAL"
                    + ");";

    private static final String DATABASE_CREATE_TABLE_SEVERA_TAXES =
            "create table " + DATABASE_TABLE_SEVERA_TAXES + " ("
                    + SeveraTaxes._ID + " integer primary key autoincrement,"
                    + SeveraTaxes.OWNER_USER_ID + " TEXT NOT NULL,"
                    + SeveraTaxes.OWNER_COMPANY_ID + " TEXT NOT NULL,"
                    + SeveraTaxes.GUID + "  TEXT,"
                    + SeveraTaxes.IS_DEFAULT + " INTEGER,"
                    + SeveraTaxes.PERCENTAGE + " REAL"
                    + ");";

    private static final String DATABASE_CREATE_TABLE_NETVISOR_PAYLOADS =
            "create table " + DATABASE_TABLE_NETVISOR_PAYLOADS + " ("
                    + NetvisorPayloads._ID + " integer primary key autoincrement,"
                    + NetvisorPayloads.OWNER_USER_ID + " TEXT NOT NULL,"
                    + NetvisorPayloads.OWNER_COMPANY_ID + " TEXT NOT NULL,"
                    + NetvisorPayloads.PAYLOAD_ID + " TEXT NOT NULL,"
                    + NetvisorPayloads.TYPE + " INTEGER,"
                    + NetvisorPayloads.TITLE + " TEXT,"
                    + NetvisorPayloads.VALUES + " TEXT"
                    + ");";


    /**
     * Helper class that creates and manages the provider's underlying data repository.
     */

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_TABLE_LAST_SYNC_TIMESTAMPS);
            db.execSQL(DATABASE_CREATE_TABLE_EXPENSE_EXPENSE_TYPES);
            db.execSQL(DATABASE_CREATE_TABLE_EXPENSE_CURRENCIES);
            db.execSQL(DATABASE_CREATE_TABLE_METADATA);
            db.execSQL(DATABASE_CREATE_TABLE_TEMP_BITMAP);
            db.execSQL(DATABASE_CREATE_TABLE_SEVERA_CASES);
            db.execSQL(DATABASE_CREATE_TABLE_SEVERA_PRODUCTS);
            db.execSQL(DATABASE_CREATE_TABLE_SEVERA_TAXES);
            db.execSQL(DATABASE_CREATE_TABLE_NETVISOR_PAYLOADS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 1) {
                db.execSQL(DATABASE_CREATE_TABLE_METADATA);
                db.execSQL(DATABASE_CREATE_TABLE_TEMP_BITMAP);
                db.execSQL(DATABASE_CREATE_TABLE_SEVERA_CASES);
                db.execSQL(DATABASE_CREATE_TABLE_SEVERA_PRODUCTS);
                db.execSQL(DATABASE_CREATE_TABLE_SEVERA_TAXES);
                db.execSQL(DATABASE_CREATE_TABLE_NETVISOR_PAYLOADS);
                oldVersion = 2;
            }

            if (oldVersion == 2) {
                db.execSQL("ALTER TABLE " + DATABASE_TABLE_METADATA + " ADD COLUMN "
                        + MetadataList.METADATA_IS_READY_FOR_PAYMENT + " INTEGER DEFAULT 0");
            }
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            if (!db.isReadOnly()) {
                db.execSQL("PRAGMA foreign_keys = ON;"); // OFF by default
            }
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);

            db.setForeignKeyConstraintsEnabled(true);
        }
    }
}