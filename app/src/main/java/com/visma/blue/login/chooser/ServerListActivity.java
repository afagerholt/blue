package com.visma.blue.login.chooser;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.visma.blue.R;
import com.visma.blue.login.BaseLoginActivity;
import com.visma.blue.misc.ErrorMessage;
import com.visma.blue.network.BlueNetworkError;
import com.visma.blue.network.OnlineResponseCodes;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.containers.CreateServerListAnswer;
import com.visma.blue.network.containers.ServerData;
import com.visma.blue.network.requests.GetServerListRequest;
import com.visma.common.VismaAlertDialog;
import com.visma.common.util.Util;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerListActivity extends BaseLoginActivity {

    private static final String SERVER_LIST_CHECK_REGEX = "https://(.*)vismaonline(.*)";

    private ServerListAdapter mServerListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blue_activity_server_list);
        setActionBar();
        setServerList();
    }

    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setServerList() {
        StickyListHeadersListView integrationsList = (StickyListHeadersListView) findViewById(R
                .id.blue_server_list);
        if (integrationsList != null) {
            ServerData production = VolleySingleton.getInstance().getServerData(VolleySingleton
                    .ServerType.LIVE);
            ArrayList<ServerData> serverList = VolleySingleton.getInstance()
                    .getSavedServerList(ServerListActivity.this);
            serverList.add(0, production);
            updateServerList(serverList);

            integrationsList.setAdapter(mServerListAdapter);
            integrationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ServerData serverData = (ServerData) parent
                            .getItemAtPosition(position);
                    if (serverData != null) {
                        VolleySingleton.getInstance().setAndSaveServer(ServerListActivity.this,
                                serverData);
                        onServerSelection();
                    }
                }
            });

            if (serverList.size() <= 1 && Util.isConnectedOrConnecting(ServerListActivity.this)) {
                refreshServerList();
            }
        }

    }

    private void onServerSelection() {
        finish();
    }

    private void refreshServerList() {
        final VismaAlertDialog alert = new VismaAlertDialog(ServerListActivity.this);
        alert.showProgressBar();

        final GetServerListRequest<CreateServerListAnswer> request = new
                GetServerListRequest<>(ServerListActivity.this,
                CreateServerListAnswer.class,
                new Response.Listener<CreateServerListAnswer>() {
                    @Override
                    public void onResponse(final CreateServerListAnswer response) {
                        final Activity activity = ServerListActivity.this;
                        if (activity == null || activity.isFinishing()) {
                            return;
                        }

                        if (response.serverList != null) {
                            ArrayList<ServerData> filteredList = filterServerList(response
                                    .serverList);
                            VolleySingleton.getInstance().saveServerList(ServerListActivity
                                    .this, filteredList);
                            filteredList.add(0, VolleySingleton.getInstance()
                                    .getServerData(VolleySingleton.ServerType.LIVE));
                            updateServerList(filteredList);
                        }
                        alert.closeDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int errorMessageId;
                        if (error instanceof BlueNetworkError) {
                            BlueNetworkError blueNetworkError = (BlueNetworkError) error;
                            errorMessageId = ErrorMessage.getErrorMessage(blueNetworkError
                                    .blueError, false);
                        } else {
                            errorMessageId = ErrorMessage.getErrorMessage(OnlineResponseCodes
                                    .NotSet, false);
                        }
                        alert.showError(errorMessageId);
                    }
                });

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance().addToRequestQueue(request);

    }

    private ArrayList<ServerData> filterServerList(ArrayList<ServerData> serverFist) {
        ArrayList<ServerData> filteredList = new ArrayList<>();
        Pattern pattern = Pattern.compile(SERVER_LIST_CHECK_REGEX, Pattern.CASE_INSENSITIVE);
        for (ServerData serverData : serverFist) {
            Matcher matcher = pattern.matcher(serverData.url);
            if (matcher.find()) {
                filteredList.add(serverData);
            }
        }

        return filteredList;
    }

    private void updateServerList(ArrayList<ServerData> serverList) {
        if (mServerListAdapter == null) {
            mServerListAdapter = new ServerListAdapter(this, serverList,
                    VolleySingleton.getInstance().getServer());
        } else {
            mServerListAdapter.update(serverList);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.blue_menu_activity_server_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.blue_menu_refresh_server_list) {
            refreshServerList();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}