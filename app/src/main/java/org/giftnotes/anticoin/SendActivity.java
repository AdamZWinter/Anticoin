package org.giftnotes.anticoin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.giftnotes.anticoin.TransactionTypes.Send;
import org.giftnotes.anticoin.utilities.Crypto;
import org.giftnotes.anticoin.utilities.NetworkUtils;

public class SendActivity extends AppCompatActivity {

    private TextView sResults;
    private EditText sRecipient;
    private EditText sAmount;
    private Button sButtonSend;
    private TextView sStringDisplay;

    Send send = new Send();
    Crypto objectCrypto = new Crypto();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        sStringDisplay = findViewById(R.id.tv_send_string2_post);
        sResults = findViewById(R.id.tv_send_results);
        sRecipient = findViewById(R.id.recipient);
        sAmount = findViewById(R.id.amount);
        sButtonSend = findViewById(R.id.buttonSend);
        sButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transmit();
            }
        });
        sResults.setText("Handle for this user is: "+MainActivity.getHandle());

    }

    private void transmit (){
        send.setHandle("Gravity");
        send.setRecipient(sRecipient.getText().toString());
        send.setAmount(sAmount.getText().toString());
        send.setDataToSign();
        //objectCrypto.useDefaultKey();
        objectCrypto.generateKeys();   //only for test purposes
        //objectCrypto.sign(send.getDataToSign());
        //send.setSignature(objectCrypto.getStringSignature());
        new networkTask().execute();
    }

    private class networkTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String POST_URL = "http://anticoins.org/decentralizeATT/post.php";
            String postThisString = send.setPostThisString();
            try {
                String Results = NetworkUtils.getResponseFromHttpUrl(POST_URL, postThisString);
                return Results;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String displayResults) {
            if (displayResults != null) {
                sResults.setText(displayResults);
                sStringDisplay.setText(send.getPostThisString());
                Log.d("**RESULTS FROM PHP**", displayResults);
            }else {
                sResults.setText("NULL Results Fail");
            }
        }
    }



}
