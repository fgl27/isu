/* from magisk */
package com.bhb27.isu.tools;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public abstract class SafetyNetHelper
implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mGoogleApiClient;

    public SafetyNetHelper(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
            .addApi(SafetyNet.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.d(Constants.TAG, "SN: Google API fail");
        handleResults(-2);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(Constants.TAG, "SN: Google API Connected");
        safetyNetCheck();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(Constants.TAG, "SN: Google API Suspended");
        handleResults(-3);
    }

    public void requestTest() {
        // Connect Google Service
        mGoogleApiClient.connect();
    }

    private void safetyNetCheck() {
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
                        handleResults(decoded.getBoolean("ctsProfileMatch") ? 1 : 0);
                    } catch (JSONException ignored) {}
                } else {
                    Log.d(Constants.TAG, "SN: No response");
                    handleResults(-1);
                }
                // Disconnect
                mGoogleApiClient.disconnect();
            });
    }

    public abstract void handleResults(int i);
}
