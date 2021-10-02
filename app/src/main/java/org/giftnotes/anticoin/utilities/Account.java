package org.giftnotes.anticoin.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import org.giftnotes.anticoin.MainActivity;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;

//Start with admin package searchable by first and last name.  $10 fee keeps random people from verifying.
//Two key pairs are generated:  One device-specific, the other administrative
//The associated public keys for both are attached to the account on the blockchain and labeled respectively
//The device-specific private key is never exported and only exists in Keystore
//The admin private key is encrypted with proprietary encryption and a strong password
//then exported and stored on server, possibly also stored by user
//The admin private key is used to add and remove devices
//device public key will be added using admin key after usher creates handle

//check for existing admin package, if none, create package
//       If exists already, gather name & handle and check for existing account on chain
//       If not, prompt to create another
//       If yes, check for existing device key on chain
//               If not, create device key, send transaction
//               If yes, check for verification requests

// admin package includes: invite code, name, handle, public key, key type, signature of name and handle
//create admin key pair and make both parts available in string format
//create package for usher to sign






public class Account extends Object{

    public Account(){super();}

    String strB64DevicePublic;
    public String getStrB64DevicePublic(){return strB64DevicePublic;}

    static String KsAsymmetric = "Asymmetric";
    public static String getAliasAsymmetric () {return "Asymmetric";}

    static String KsSymmetric = "Symmetric";
    public static String getAliasSymmetric () {return "Symmetric";}

    static String userInfoFile = "UserInfo";
    public static String getUserFilename () {return userInfoFile;}

    static String encryptedKeyFile = "EncryptedKey";
    public static String getEncryptedKeyFile (){return encryptedKeyFile;}

    String handle = null;
    public void setHandle(String Handle){this.handle=Handle;}
    String first = null;
    String last = null;
    String device = null;
    String strDevicePublicPEM = null;
    String strSignatureAdmin = null;

    KeyPair keyPairAsymmetric = null;
    PublicKey devicePublic = null;

    public JSONObject getUserInfo(Context context){
        JSONObject encryption = null;
        String strFromFile = null;
        JSONObject jsonResults = null;
        try {
            FileInputStream fis = context.openFileInput(Account.getUserFilename());
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null){
                stringBuilder.append(line);
                line = reader.readLine();
            }
            strFromFile = stringBuilder.toString();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            encryption = new JSONObject(strFromFile);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        byte[] results = Crypto.deviceDecryptSymmetric(encryption);
        String strResults = new String(results);
        try {
            jsonResults = new JSONObject(strResults);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            handle = jsonResults.getString("handle");
            first = jsonResults.getString("first");
            last = jsonResults.getString("last");
            device = jsonResults.getString("device");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonResults;
    }

    public static boolean isEstablish (Context context){
        boolean isSymmetric = false;
        boolean isUserInfo = false;
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            if(ks.containsAlias(KsSymmetric)){isSymmetric = true;}
            else {isSymmetric = false;}
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        File userInfo = context.getFileStreamPath(Account.getUserFilename());
        if(userInfo.exists()){isUserInfo=true;}else {isUserInfo=false;}

        if(isSymmetric && isUserInfo){
            Log.d("****ESTABLISHED**: ", "UserInfo and Symmetric key both found in existence");
            return true;
        } else {return false;}
    }

    public boolean Establish (String strHandle, String strFirst, String strLast, String strPass, String strDevice, Context context){
        //On establish:
        //1) Device symmetric key is stored for use with sensitive data files
        //2) Device asymmetric key is stored for signing transactions with this device
        //3) User info is stored on this device
                //The following is done from NewAccountActivity
                //4) Admin asymmetric is generated
                 //5) Admin public key in PEM format, along with handle, first and last, are posted to requests on wallet server
                 //6) Admin private key is encrypted with proprietary encryption and stored on wallet server

        if(!this.isEstablish(context)){
            storeKeySymmetric();     //device
            //Assymetric key is not stored here - It is stored when adding device in Settings
            //TODO:  Check inputs before doing anything
            storeUserInfo(strHandle, strFirst, strLast, strDevice, context);
            handle = strHandle;
            first = strFirst;
            last = strLast;
            device = strDevice;
        }else {return false;}
        return isEstablish(context);
    }

    private void storeKeySymmetric(){
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(KsSymmetric, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build();
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.d("****LOOK HERE", "OOPS no such algorithm");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            Log.d("****LOOK HERE", "oops no such provider");
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            Log.d("****LOOK HERE", "ooops algorithm parameter problem");
        }
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            if(ks.aliases() != null){Log.d("****HERE***", "Found aliases:  ");
                Enumeration<String> aliases = ks.aliases();
                while (aliases.hasMoreElements()){
                    Log.d("\n",aliases.nextElement() );
                }
            }else{Log.d("*****HERE***", "No Asymmetric key alias exists");}
            if(!ks.containsAlias(KsSymmetric)){Log.d("*****HERE***", "No Symmetric key alias exists");}
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void storeKeyAsymmetric(){
        //Keys will be accessed after admin key is verified on chain.  This is called from continueSetDevice.
        KeyPairGenerator kpGenerator = null;
        try {
            kpGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        try {
            kpGenerator.initialize(new KeyGenParameterSpec.Builder(KsAsymmetric, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setDigests(KeyProperties.DIGEST_SHA512, KeyProperties.DIGEST_SHA256)
                    .setKeySize(2048)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1, KeyProperties.ENCRYPTION_PADDING_RSA_OAEP, KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1, KeyProperties.SIGNATURE_PADDING_RSA_PSS)
                    .build());
            keyPairAsymmetric = kpGenerator.generateKeyPair();
            devicePublic = keyPairAsymmetric.getPublic();
            byte[] encoding = devicePublic.getEncoded();
            strDevicePublicPEM = Crypto.writePEM(encoding);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    private void storeUserInfo (String strHandle, String strFirst, String strLast, String strDevice, Context context){
        String strInfo = null;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("handle", strHandle);
            jsonObject.put("first", strFirst);
            jsonObject.put("last", strLast);
            jsonObject.put("device", strDevice);
            //TODO:  Add preferences:  preferred charity URL
            strInfo = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        byte[] bytes = strInfo.getBytes(StandardCharsets.UTF_8);
        JSONObject encryption = Crypto.deviceEncryptSymmetric(bytes);
        String strStorage = encryption.toString();
        File file = new File(context.getFilesDir(), Account.getUserFilename());
        try {
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write(strStorage);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Admin asymmetric is generated
    // Admin private key is encrypted with proprietary encryption and stored on device and on wallet server using handle? to identify it
    // Admin public key in PEM format, along with handle, first and last, are posted to requests on wallet server
    public static String getAdminPackage (String strHandle, String strFirst, String strLast, String strPass, Context context){
        String postThisString = null;
        Crypto adminCrypto = new Crypto();
        adminCrypto.generateKeys();
        String adminPKEncrypted = adminCrypto.getPrivKeyProprietary(strPass);  //TODO:  Add first and last to proprietary encryption
        File keyFile = new File(context.getFilesDir(), Account.getEncryptedKeyFile());
        try {
            FileWriter fileWriter = new FileWriter(keyFile, false);
            fileWriter.write(adminPKEncrypted);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        adminCrypto.pemWriter();
        String strURLpublicAdmin = adminCrypto.getPublicKeyString();

        ArrayList ArrayList = new ArrayList();
        ArrayList.add("type=newDevice");
        ArrayList.add("&ledger=na");
        ArrayList.add("&handle="+strHandle);
        ArrayList.add("&first="+strFirst);
        ArrayList.add("&last="+strLast);
        ArrayList.add("&publicAdmin="+strURLpublicAdmin);
        ArrayList.add("&privateAdmin="+adminPKEncrypted);
        ArrayList.add("&keyType="+ MainActivity.getKeyType());

        StringBuilder stringBuilder = new StringBuilder();
        for (Object components:ArrayList)
        {
            String component = components.toString();
            stringBuilder.append(component);
        }
        postThisString = stringBuilder.toString();
        Log.d("*******LOOK HERE*******", postThisString);
        return postThisString;
    }

    public String getDevicePackage (byte[] bytesPublicKey, byte[] bytesPrivateKey){
        String postThisString = null;
        //handle and device are set with getUserInfo just before calling this method
        //strDevicePublicPEM is set with storeKeyAsymmetric which is called by continueSetDevice just before this
        String strToSign = handle+device+strDevicePublicPEM;
        strSignatureAdmin = Crypto.verifiedAdminSignature(bytesPublicKey, bytesPrivateKey, strToSign);

        ArrayList ArrayList = new ArrayList();
        ArrayList.add("type=newDevice");
        ArrayList.add("&ledger=na");
        ArrayList.add("&handle="+handle);
        ArrayList.add("&device="+device);
        ArrayList.add("&publicDevice="+strDevicePublicPEM);
        ArrayList.add("&keyType="+MainActivity.getKeyType());
        ArrayList.add("&signature="+strSignatureAdmin);

        StringBuilder stringBuilder = new StringBuilder();
        for (Object components:ArrayList)
        {
            String component = components.toString();
            stringBuilder.append(component);
        }
        postThisString = stringBuilder.toString();
        Log.d("*******LOOK HERE*******", postThisString);
        return postThisString;
    }

    public void deleteAll (Context context){

        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            if(ks.aliases() != null) {
                Enumeration<String> aliases = ks.aliases();
                while (aliases.hasMoreElements()) {
                    ks.deleteEntry(aliases.nextElement());
                    Log.d("****HERE", "Found aliases, deleting all ");
                }
            }
            if(!ks.containsAlias(KsAsymmetric)){Log.d("*****HERE", "No Asymmetric key alias exists");}

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String[] files = context.fileList();
        if(files.length != 0) {
            for (int i = 0; i < files.length; i++) {
                Log.d("Filename:", files[i]);
                if(context.deleteFile(files[i])){
                    Log.d("*****DELETED: ", files[i]);
                }
            }
        }
    }


}
