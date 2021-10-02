package org.giftnotes.anticoin;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.giftnotes.anticoin.utilities.Account;
import org.giftnotes.anticoin.utilities.NetworkUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    public TextView tMainTop;
    public ProgressBar progressBar;
    public ProgressBar progressBar2;
    private static String handle = null;
    public static String getHandle(){return handle;}
    private String first = null;
    private String last = null;
    private static String device = null;
    public static String getDevice(){return device;}
    public static String getKeyType(){return "RSA512";}
    int NEW_ACCOUNT_ACTIVITY_REQUEST_CODE = 0;
    int SETTINGS_ACTIVITY_REQUEST_CODE = 1;
    String POST_URL = null;
    String postThisString = null;
    String response = "initialized valued... failed to change";
    String message = "initialized value... failed to change";

    Account account = new Account();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tMainTop = findViewById(R.id.tv_main_top);
        progressBar = findViewById(R.id.progressBarMain);
        progressBar2 = findViewById(R.id.progressBar2);

        load();
    }

    private void load(){
        Context contextApp = getApplicationContext();
        if(!Account.isEstablish(contextApp)){
            Context contextMain = MainActivity.this;
            Class classNA = NewAccountActivity.class;
            Intent intentNA = new Intent(contextMain, classNA);
            startActivityForResult(intentNA, NEW_ACCOUNT_ACTIVITY_REQUEST_CODE);
        }else {
            JSONObject jsonUserInfo = account.getUserInfo(contextApp);
            try {
                handle = jsonUserInfo.getString("handle");
                first = jsonUserInfo.getString("first");
                last = jsonUserInfo.getString("last");
                device = jsonUserInfo.getString("device");
                getStatus();
                //TODO: check existence of handle in database, get balances, check for handle in requests

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayMain(){
        tMainTop.setText("Username: "+handle+"\nFirst: "+first+"\nLast: "+last+"\nDevice:  "+device+"\nExists: "+response+"\n\n "+message);
    }

    private void getStatus(){
        POST_URL = "http://anticoins.org/decentralizeATT/status.php";
        postThisString = "handle="+handle+"&device="+device;
        Log.d("****GET STATUS:  ", postThisString);
        new  networkTask().execute();
    }

    JSONObject jsonStatus = new JSONObject();
    private void processResults(String results){
        String strError = null;
        String strExists = null;
        String strRequested = null;
        String strDeviceExists = null;
        try {
            jsonStatus = new JSONObject(results);
            strError = jsonStatus.getString("error");
            if(strError.equals("none")) {
                strExists = jsonStatus.getString("exists");
                strRequested = jsonStatus.getString("requested");
                strDeviceExists = jsonStatus.getString("deviceExists");
            }else {
                Toast toast = Toast.makeText(getApplicationContext(), strError, Toast.LENGTH_LONG);
                toast.show();
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        boolean boolExists = false;
        boolean boolRequested = false;
        boolean boolDevice = false;
        if(strExists != null && strRequested != null && strDeviceExists != null){
            boolExists = Boolean.parseBoolean(strExists);
            boolRequested = Boolean.parseBoolean(strRequested);
            boolDevice = Boolean.parseBoolean(strDeviceExists);
        }
        if(boolExists && boolDevice){
            message = "End of the road:  load balances and stuff";
            displayMain();
        }
        else if(boolExists && !boolDevice){
            message = "You cannot make transactions with this device until you add it.  " +
                    "To add this device, go to \"Settings\".";
            displayMain();
        }
        else if(!boolExists && boolRequested){
            message = "Your request for a new account has been submitted. " +
                    "Your new account must be verified by your friend with an existing account.  " +
                    "Your friend will need your first name, last name, and username to do that.";
            displayMain();
        }
        else if(!boolExists && !boolRequested && strExists != null && strRequested != null){
            message = "Server found no record of this username.  Re-submitting request....";
            displayMain();
            getStatus();
        }
        else {message = "Sorry, something went wrong.";}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        if (itemThatWasClickedId == R.id.intent_add_friend) {
            Context context = MainActivity.this;
            Class classNH = NewHandleActivity.class;
            Intent intentNH = new Intent(context, classNH);
            startActivity(intentNH);
            return true;
        }

        if (itemThatWasClickedId == R.id.intent_send) {
            Context context = MainActivity.this;
            Class classSend = SendActivity.class;
            Intent intentSend = new Intent(context, classSend);
            startActivity(intentSend);
            return true;
        }

        if (itemThatWasClickedId == R.id.intent_create_account) {
            Context context = MainActivity.this;
            Class classNA = NewAccountActivity.class;
            Intent intentNA = new Intent(context, classNA);
            startActivity(intentNA);
            return true;
        }

        if (itemThatWasClickedId == R.id.intent_testing) {
            Context context = MainActivity.this;
            Class classTest = TestingActivity.class;
            Intent intentTest = new Intent(context, classTest);
            startActivity(intentTest);
            return true;
        }

        if (itemThatWasClickedId == R.id.intent_settings) {
            Context context = MainActivity.this;
            Class classSettings = SettingsActivity.class;
            Intent intentSettings = new Intent(context, classSettings);
            startActivityForResult(intentSettings, SETTINGS_ACTIVITY_REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that it is the SecondActivity with an OK result
        if (requestCode == NEW_ACCOUNT_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                boolean created = data.getBooleanExtra("created", false);
                boolean restored = data.getBooleanExtra("restored", false);
                if(created) load();
                if(restored) load();
            }else {finish();}
        }

        if (requestCode == SETTINGS_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                boolean device = data.getBooleanExtra("device", false);
                if(device) load(); else load();
            }else {load();}
        }
    }

    public class networkTask extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                String Results = NetworkUtils.getResponseFromHttpUrl(POST_URL, postThisString);
                response = Results;
                return Results;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            progressBar.setProgress(progress[0]);
            progressBar2.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String results) {
            if (results != null) {
                response = results;
                Log.d("**MAIN ACTIVITY**", results);
                processResults(results);
                progressBar.setVisibility(View.INVISIBLE);
                progressBar2.setVisibility(View.INVISIBLE);
            }else {
                tMainTop.setText("NULL Results Fail");
            }
        }
    }

}
