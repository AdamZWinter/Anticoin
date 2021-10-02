package org.giftnotes.anticoin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.giftnotes.anticoin.utilities.Crypto;
import org.giftnotes.anticoin.utilities.NetworkUtils;
import org.giftnotes.anticoin.TransactionTypes.NewHandle;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.Toast;


public class NewHandleActivity extends AppCompatActivity {

    private TextView mResults;
    private Button mSearch;
    private Button mTransmitButton;
    private EditText mNew_First;
    private EditText mNew_Last;
    private EditText mNew_Handle;

    String strFirst = null;
    String strLast = null;
    String strHandle = null;
    private String POST_URL = null;
    private String postThisString = null;
    private Boolean isSearch = false;
    private Boolean isTransmit = false;

    NewHandle newHandle = new NewHandle();
    Crypto objectCrypto = new Crypto();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_handle);
        mResults = findViewById(R.id.tv_post_results);
        mSearch = findViewById(R.id.button_search);
        mTransmitButton = findViewById(R.id.button_transmit);
        mNew_First = findViewById(R.id.new_handle_first);
        mNew_Last = findViewById(R.id.new_handle_last);
        mNew_Handle = findViewById(R.id.new_handle_handle);

        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSearch = true; isTransmit=false;
                setSearch();
                new networkTask().execute();
            }
        });

        mTransmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTransmit=true; isSearch=false;
                new networkTask().execute();
            }
        });
    }

    public void setSearch(){
        POST_URL = "http://anticoins.org/decentralizeATT/searchRequests.php";
        strFirst = mNew_First.getText().toString();
        strLast = mNew_Last.getText().toString();
        strHandle = mNew_Handle.getText().toString();
        postThisString = newHandle.getSearchString(strFirst, strLast, strHandle);
    }

    public void continueSearch(String strJSONobj){
        Context context = getApplicationContext();
        try {
            JSONObject searchResponse = new JSONObject(strJSONobj);
            String errorMsg = searchResponse.getString("error");
            if(errorMsg.equals("none")){
                String foundMsg = searchResponse.getString("found");
                Boolean found = Boolean.parseBoolean(foundMsg);
                if(found){
                    Log.d("****continueSearch:  ", "Found user.");
                    String publicAdmin = searchResponse.getString("publicAdmin");
                    String keyType = searchResponse.getString("keyType");
                    mResults.setText("Found: "+strFirst+" "+strLast+"\nConfirm to add friend: ");
                    mTransmitButton.setVisibility(View.VISIBLE);
                    mSearch.setVisibility(View.INVISIBLE);
                    postThisString = newHandle.getSignedPackage(strHandle, publicAdmin, keyType);
                    POST_URL = "http://anticoins.org/decentralizeATT/post.php";
                }else {
                    Toast toast = Toast.makeText(context, "Username NOT available, choose another, or restore", Toast.LENGTH_LONG);
                    toast.show();}
            }else{
                Toast toast = Toast.makeText(context, errorMsg, Toast.LENGTH_LONG);
                toast.show(); }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void continueTransmit(String strJSONobj){
        Context context = getApplicationContext();
        try {
            JSONObject searchResponse = new JSONObject(strJSONobj);
            String errorMsg = searchResponse.getString("error");
            if(errorMsg.equals("none")){
                String foundMsg = searchResponse.getString("found");
                Boolean found = Boolean.parseBoolean(foundMsg);
                if(found){
                    Log.d("****continueSearch:  ", "Found user.");

                }else {
                    Toast toast = Toast.makeText(context, "Username NOT available, choose another, or restore", Toast.LENGTH_LONG);
                    toast.show();}
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
                if(isSearch){
                    isSearch = false;
                    continueSearch(results);
                }
                if(isTransmit){
                    isTransmit = false;
                    continueTransmit(results);
                }
                Log.d("**NEW HANDLE ACTIVITY**", results);
            }else {
                mResults.setText("NULL Results Fail");
            }
        }
    }



}
