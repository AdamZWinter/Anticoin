package org.giftnotes.anticoin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.giftnotes.anticoin.utilities.Account;
import org.giftnotes.anticoin.utilities.Crypto;
import org.giftnotes.anticoin.utilities.NetworkUtils;
import org.giftnotes.anticoin.utilities.Proprietary;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends AppCompatActivity {

    EditText ePass;
    Button bDeleteAll;
    Button bAddDevice;

    String strPass = null;
    String POST_URL = null;
    String postThisString = null;

    Boolean isAdmin = false;
    Boolean isDevice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ePass = findViewById(R.id.settings_pass);
        bDeleteAll = findViewById(R.id.button_delete_all);
        bAddDevice = findViewById(R.id.button_add_device);

        bAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            isAdmin = true;
            setDevice();
            new networkTask().execute();
            }
        });

        bDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account deleteAll = new Account();
                deleteAll.deleteAll(getApplicationContext());
            }
        });
    }

    public void setDevice(){
        strPass = ePass.getText().toString();
        POST_URL = "http://anticoins.org/decentralizeATT/getAdmin.php";
        postThisString = "handle="+MainActivity.getHandle();
        Log.d("****setDevice:  ", postThisString);
    }

    public void continueSetDevice(String strJSONobj){
        isDevice=true;
        Context context = getApplicationContext();
        Boolean isOwner = false;
        byte[] publicKey = null;
        byte[] privateKey = null;
        try {
            JSONObject keys = new JSONObject(strJSONobj);
            String strError = keys.getString("error");
            if(strError.equals("none")) {
                String strPubAdmin = keys.getString("publicAdmin");
                String strPrivAdmin = keys.getString("privateAdmin");
                publicKey = Crypto.pem2KeyBytes(strPubAdmin);
                byte[] decrypted = Proprietary.DecryptStrong(strPrivAdmin, strPass);
                privateKey = Base64.decode(decrypted, 2);
                isOwner = Crypto.checkKeyPair(publicKey, privateKey);
            }
            else{
                Toast toast = Toast.makeText(context, strError, Toast.LENGTH_LONG);
                toast.show(); }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(isOwner){
            Toast toast = Toast.makeText(context, "Account Verified.", Toast.LENGTH_SHORT);
            toast.show();
            Account account = new Account();
            JSONObject notUsed = account.getUserInfo(context);  //loads User Info into account object for DevicePackage
            notUsed = null;
            account.storeKeyAsymmetric();
            postThisString = account.getDevicePackage(publicKey, privateKey);
            POST_URL = "http://anticoins.org/decentralizeATT/newDevice.php";
            new networkTask().execute();
        } else {
            Toast toast = Toast.makeText(context, "Failed: Check your password.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void continueDevice (String strJSONobj){
        Log.d("****continueDevice:  ", strJSONobj);
        Context context = getApplicationContext();
        try {
            JSONObject deviceResponse = new JSONObject(strJSONobj);
            String errorMsg = deviceResponse.getString("error");
            if(errorMsg.equals("none")){
                String message = deviceResponse.getString("message");
                String successMsg = deviceResponse.getString("success");
                Boolean success = Boolean.parseBoolean(successMsg);
                if(success){
                    Log.d("****Success:  ", message);
                    Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
                    toast.show();
                    Intent intent = new Intent();
                    intent.putExtra("device", true);
                    setResult(RESULT_OK, intent);
                    finish();
                }else {
                    Log.d("****Failed:  ", message);
                    Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
                    toast.show();
                }
            }else{
                Toast toast = Toast.makeText(context, errorMsg, Toast.LENGTH_LONG);
                toast.show(); }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class networkTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                String Results = NetworkUtils.getResponseFromHttpUrl(POST_URL, postThisString);
                return Results;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String results) {
            if (results != null) {
                Log.d("****onPostExecute:  ", results);
                if (isAdmin){
                    isAdmin = false;
                    continueSetDevice(results);
                }
                else if (isDevice){
                    isDevice = false;
                    continueDevice(results);
                }
                else {Log.d("****onPostExecute:  ", "No direction to continue.");}
            }else {
                Toast toast = Toast.makeText(getApplicationContext(), "PostExecute: Null response from server", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }


}
