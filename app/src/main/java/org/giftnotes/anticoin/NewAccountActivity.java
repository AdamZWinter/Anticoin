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
import android.widget.TextView;
import android.widget.Toast;
import org.giftnotes.anticoin.utilities.Account;
import org.giftnotes.anticoin.utilities.Crypto;
import org.giftnotes.anticoin.utilities.NetworkUtils;
import org.giftnotes.anticoin.utilities.Proprietary;
import org.json.JSONException;
import org.json.JSONObject;

public class NewAccountActivity extends AppCompatActivity {

    public EditText eUserHandle;
    public EditText eFirstName;
    public EditText eLastName;
    public EditText ePassword;
    public EditText eNickname;
    public Button bCreateAccount;
    public Button bRestore;
    public Button bDeleteAll;
    public TextView tDisplay;

    String strHandle = null;
    String strFirst = null;
    String strLast = null;
    String strPass = null;
    String strDevice = null;
    String POST_URL = null;
    String postThisString = null;
    boolean isRestore = false;
    boolean isCreate = false;

    Account account = new Account();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);
        eUserHandle = findViewById(R.id.user_handle);
        eFirstName = findViewById(R.id.user_first_name);
        eLastName = findViewById(R.id.user_last_name);
        ePassword = findViewById(R.id.user_password);
        eNickname = findViewById(R.id.device_nickname);
        bCreateAccount = findViewById(R.id.button_new_key_store);
        bRestore = findViewById(R.id.button_restore);
        bDeleteAll = findViewById(R.id.button_delete_all_alias);
        tDisplay = findViewById(R.id.tv_new_account_display);

        bCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Check inputs
                isCreate = true; isRestore=false;
                setCreate();
                new networkTask().execute();
            }
        });

        bRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Check inputs
                isRestore = true; isCreate=false;
                setRestore();
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

    private void setCreate(){
        strHandle = eUserHandle.getText().toString();
        strFirst = eFirstName.getText().toString();
        strLast = eLastName.getText().toString();
        strPass = ePassword.getText().toString();
        strDevice = eNickname.getText().toString();
        Context context = getApplicationContext();
        POST_URL = "http://anticoins.org/decentralizeATT/NARequest.php";
        postThisString = Account.getAdminPackage(strHandle, strFirst, strLast, strPass, context);
    }

    private void continueCreate(String strJSONobj){
        Context context = getApplicationContext();
        try {
            JSONObject jsonResponse = new JSONObject(strJSONobj);
            String errorMsg = jsonResponse.getString("error");

            if(errorMsg.equals("none")){
                String availableMsg = jsonResponse.getString("available");
                Boolean available = Boolean.parseBoolean(availableMsg);
                if(available){
                    String successMsg = jsonResponse.getString("success");
                    Boolean success = Boolean.parseBoolean(successMsg);
                    String message = jsonResponse.getString("message");
                    if(success){
                        if(account.Establish(strHandle, strFirst, strLast, strPass, strDevice, context)){
                            Toast toast = Toast.makeText(context, "Success!", Toast.LENGTH_LONG);
                            toast.show();
                            Log.d("**SUCCESS: ", "Request Posted successfully and UserInfo saved.");
                            Intent intent = new Intent();
                            intent.putExtra("created", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
                                toast.show();}
                    }else{
                        Toast toast2 = Toast.makeText(context, message, Toast.LENGTH_LONG);
                        toast2.show();}
                }else {
                    Toast toast = Toast.makeText(context, "Username NOT available, choose another, or restore", Toast.LENGTH_LONG);
                    toast.show();}
            }else{
                Toast toast = Toast.makeText(context, errorMsg, Toast.LENGTH_LONG);
                toast.show(); }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] files = context.fileList();    //testing
        for(int i=0; i<files.length; i++){      //testing
            Log.d("Filename:", files[i]);  //testing
        }
    }

    private void setRestore(){
        strHandle = eUserHandle.getText().toString();
        strFirst = eFirstName.getText().toString();
        strLast = eLastName.getText().toString();
        strPass = ePassword.getText().toString();
        strDevice = eNickname.getText().toString();
        POST_URL = "http://anticoins.org/decentralizeATT/getAdmin.php";
        postThisString = "handle="+strHandle;
    }

    private void continueRestore(String strJSONobj){
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
            if(account.Establish(strHandle, strFirst, strLast, strPass, strDevice, context)){
                Toast toast2 = Toast.makeText(context, "Established", Toast.LENGTH_SHORT);
                toast2.show();
                Log.d("**Restore Established: ", "Done");
                Intent intent = new Intent();
                intent.putExtra("restored", true);
                setResult(RESULT_OK, intent);
                finish();
            } else {Toast toast2 = Toast.makeText(context, "Failed to Establish", Toast.LENGTH_LONG);
                    toast2.show();}
        } else {
            Toast toast = Toast.makeText(context, "Failed: Check your creds.", Toast.LENGTH_LONG);
            toast.show();
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
                tDisplay.setText(results);
                Log.d("**NEW ACCOUNT ACTIVITY:**", results);
                Toast toast = Toast.makeText(getApplicationContext(), "Connected to network", Toast.LENGTH_SHORT);
                toast.show();
                if (isCreate){
                    isCreate = false;
                    continueCreate(results);
                }
                if (isRestore){
                    isRestore = false;
                    continueRestore(results);
                }
            }else {
                tDisplay.setText("NULL Results Fail");
                Toast toast = Toast.makeText(getApplicationContext(), "PostExecute: Null response from server", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

}
