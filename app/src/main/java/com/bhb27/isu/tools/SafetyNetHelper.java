/* from magisk */
package com.bhb27.isu.tools;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNet;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;

import com.bhb27.isu.tools.Constants;
import com.bhb27.isu.R;

public abstract class SafetyNetHelper
implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mGoogleApiClient;
    private Result ret;
    protected FragmentActivity mActivity;

    public SafetyNetHelper(FragmentActivity activity) {
        ret = new Result();
        mActivity = activity;
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
            .enableAutoManage(activity, this)
            .addApi(SafetyNet.API)
            .addConnectionCallbacks(this)
            .build();
    }

    // Entry point to start test
    public void requestTest() {
        // Connect Google Service
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.d(Constants.TAG, "SN: Google API fail");
        ret.errmsg = result.getErrorMessage();
        handleResults(ret);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(Constants.TAG, "SN: Google API Suspended");
        switch (i) {
            case CAUSE_NETWORK_LOST:
                ret.errmsg = mActivity.getString(R.string.safetyNet_network_loss);
                break;
            case CAUSE_SERVICE_DISCONNECTED:
                ret.errmsg = mActivity.getString(R.string.safetyNet_service_disconnected);
                break;
        }
        handleResults(ret);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(Constants.TAG, "SN: Google API Connected");
        // Create nonce
        byte[] nonce = new byte[24];
        new SecureRandom().nextBytes(nonce);

        Log.d(Constants.TAG, "SN: Check with nonce: " + Base64.encodeToString(nonce, Base64.DEFAULT));

        // Call SafetyNet
        SafetyNet.SafetyNetApi.attest(mGoogleApiClient, nonce)
            .setResultCallback(result -> {
                Status status = result.getStatus();
                if (status.isSuccess()) {
                    String json = new String(Base64.decode(result.getJwsResult().split("\\.")[1], Base64.DEFAULT));
                    Log.d(Constants.TAG, "SN: Response: " + json);
                    try {
                        JSONObject decoded = new JSONObject(json);
                        ret.ctsProfile = decoded.getBoolean("ctsProfileMatch");
                        ret.basicIntegrity = decoded.getBoolean("basicIntegrity");
                        ret.failed = false;
                    } catch (JSONException e) {
                        Log.d(Constants.TAG, "SN: result JSONException");
                        ret.errmsg = mActivity.getString(R.string.safetyNet_res_invalid);
                    }
                } else {
                    Log.d(Constants.TAG, "SN: No response");
                    ret.errmsg = mActivity.getString(R.string.safetyNet_no_response);
                }
                // Disconnect
                mGoogleApiClient.stopAutoManage(mActivity);
                mGoogleApiClient.disconnect();
                handleResults(ret);
            });
    }

    // Callback function to save the results
    public abstract void handleResults(Result result);

    public static class Result {
        public boolean failed = true;
        public String errmsg;
        public boolean ctsProfile = false;
        public boolean basicIntegrity = false;
    }
}
