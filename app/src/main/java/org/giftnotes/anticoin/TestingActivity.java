package org.giftnotes.anticoin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.giftnotes.anticoin.utilities.Account;
import org.giftnotes.anticoin.utilities.Crypto;
import org.giftnotes.anticoin.utilities.NetworkUtils;
import org.giftnotes.anticoin.utilities.Proprietary;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.spec.SecretKeySpec;

public class TestingActivity extends AppCompatActivity {

    public Button buttonTest;
    public TextView displayText;

    String pubAdmin = "nonesensefortesting";
    String privAdmin = "nonesensefortesting";
    String strHandle = "testHandle";
    String strFirst = "testFirst";
    String strLast = "testLast";
    String strPass = "myFancyPassword";
    String strTest = "initialized value";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        buttonTest = findViewById(R.id.button_testing);
        displayText = findViewById(R.id.tv_testing_display);

        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //innards();
                //testFunction();
                //storeInfo();
                //deviceStore();
                //decryptDisplay();
                //new networkTask().execute();
                //Proprietary.verifyAdmin(pubAdmin, privAdmin, strFirst, strLast, strPass);
                //verifyKeyPair();
                //strongEncryption();
                //pem2bytesTest();
                strTest=Crypto.verifiedDeviceSignature("Test String");
                Log.d("TESTING:  ", strTest);

            }
        });
    }

    public void pem2bytesTest(){
        Crypto crypto = new Crypto();
        crypto.generateKeys();
        crypto.pemWriter();
        String publicKeyPEM = crypto.getPublicKeyString();
        Log.d("****PUBLIC KEY:  ", publicKeyPEM);
        byte[] publicKeybytes = Crypto.pem2KeyBytes(publicKeyPEM);
        String publicKeyStr = new String(publicKeybytes);
        Log.d("****PUBLIC KEY:  ", publicKeyStr);
    }


    public void strongEncryption(){
        String message = "This is the message to encrypt and decrypt.";
        String pass = "pass";
        String encryption = Proprietary.EncryptStrong(message.getBytes(), pass);
        byte[] decryption = Proprietary.DecryptStrong(encryption, pass);
        String decrypted = new String(decryption);
        Log.d("****DECRYPTION:  ", decrypted);
    }

    public void verifyKeyPair(){
        Crypto crypto = new Crypto();
        crypto.generateKeys();
        crypto.pemWriter();
        String strPEMPublic = crypto.getPublicKeyString();
        String strAdminEncrypted = crypto.getPrivKeyProprietary("pass");

        byte[] bytesPubKey = Crypto.pem2KeyBytes(strPEMPublic);

        byte[] bytesPrivKey = Proprietary.DecryptStrong(strAdminEncrypted, "pass");
        bytesPrivKey = Base64.decode(bytesPrivKey, 2);
        //PublicKey publicKey = crypto.getPublicKey();
        //PrivateKey privateKey = crypto.getPrivateKey();
        crypto.checkKeyPair(bytesPubKey, bytesPrivKey);
    }

    public class networkTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String POST_URL = "http://anticoins.org/decentralizeATT/post.php";
            Context context = getApplicationContext();
            String postThisString = Account.getAdminPackage(strHandle, strFirst, strLast, strPass, context);
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
                displayText.setText(displayResults);
                Log.d("**RESULTS FROM PHP**", displayResults);
            }else {
                displayText.setText("NULL Results Fail");
            }
        }
    }

    public void deviceStore(){

        String strHandle = "testHandle";
        String strFirst = "testFirst";
        String strLast = "testLast";
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("handle", strHandle);
            jsonObject.put("first", strFirst);
            jsonObject.put("last", strLast);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String strInfo = jsonObject.toString();


        //String strTest = "This is the best string ever";
        //byte[] bytes = strTest.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = strInfo.getBytes(StandardCharsets.UTF_8);
        JSONObject encryption = Crypto.deviceEncryptSymmetric(bytes);
        String strStorage = encryption.toString();

        //Store encryption as file then read
        Context context = getApplicationContext();
        File file = new File(context.getFilesDir(), Account.getUserFilename());

        try {
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write(strStorage);
            //fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void decryptDisplay(){
        JSONObject encryption = null;
        String strFromFile = null;
        try {
            Context context2 = getApplicationContext();
            FileInputStream fis = context2.openFileInput(Account.getUserFilename());
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null){
                stringBuilder.append(line);
                line = reader.readLine();
            }
            strFromFile = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            encryption = new JSONObject(strFromFile);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        byte[] results = Crypto.deviceDecryptSymmetric(encryption);
        String output = new String(results);
        displayText.setText(output);
    }

/*    public void testFunction(){

        String strHandle = "testHandle";
        String strFirst = "testFirst";
        String strLast = "testLast";
        String pass = "myPasswrod12";
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("handle", strHandle);
            jsonObject.put("first", strFirst);
            jsonObject.put("last", strLast);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String strInfo = jsonObject.toString();


        //String strTest = "This is the best string ever";
        //byte[] bytes = strTest.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = strInfo.getBytes(StandardCharsets.UTF_8);
        JSONObject encryption = Proprietary.EncryptStrong(bytes, pass);
        String strStorage = encryption.toString();

        //Store encryption as file then read
        Context context = getApplicationContext();
        File file = new File(context.getFilesDir(), Account.getUserFilename());

        try {
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write(strStorage);
            //fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }




        String strFromFile = null;
        try {
            Context context2 = getApplicationContext();
            FileInputStream fis = context2.openFileInput(Account.getUserFilename());
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null){
                stringBuilder.append(line);
                line = reader.readLine();
            }
            strFromFile = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            encryption = new JSONObject(strFromFile);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        byte[] results = Proprietary.DecryptStrong(encryption, pass);
        String output = new String(results);
        displayText.setText(output);
    }*/

    /*    public void postAdminPackage(){
        String strHandle = "testHandle";
        String strFirst = "testFirst";
        String strLast = "testLast";
        String strPass = "myFancyPassword";
        Context context = getApplicationContext();
        String postThisString = Account.postAdminPackages(strHandle, strFirst, strLast, strPass, context);
        displayText.setText(postThisString);
    }*/


    //String alias, String strHandle, String strFirst, String strLast
/*    public void storeInfo (){

        String strHandle = "testHandle";
        String strFirst = "testFirst";
        String strLast = "testLast";
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("handle", strHandle);
            jsonObject.put("first", strFirst);
            jsonObject.put("last", strLast);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String strInfo = jsonObject.toString();
        String encrypted = Proprietary.proprietaryEncrypt(strInfo.getBytes(StandardCharsets.UTF_8),"password");
        byte[] decrypted = Proprietary.proprietaryDecrypt(encrypted, "password");
        String output = new String(decrypted, StandardCharsets.UTF_8);

        displayText.setText(output);

    }*/


/*
    public void innards(){
        Proprietary proprietary = new Proprietary();
        String strTest = "This is the best string ever";
        byte[] bytes = strTest.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = proprietary.innardEncrypt(bytes);
        byte[] decrypted = proprietary.innardDecrypt(encrypted);
        String strDecrypted = new String(decrypted, StandardCharsets.UTF_8);

        displayText.setText(strDecrypted);
    }
*/


}
