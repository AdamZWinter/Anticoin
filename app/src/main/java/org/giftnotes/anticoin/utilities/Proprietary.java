package org.giftnotes.anticoin.utilities;

import android.util.Base64;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Proprietary extends Object {
    public Proprietary() {super();}

    public static String EncryptStrong(byte[] data, String password){
        String version = "beta";
        String propPass = null;
        try {
            String pepper = "activity";
            MessageDigest digestPepper = MessageDigest.getInstance("SHA-256");
            digestPepper.update(pepper.getBytes());
            byte[] pepperHash = digestPepper.digest();
            StringWriter pepperWriter = new StringWriter();
            int j = 0;
            while (j != pepperHash.length) {
                pepperWriter.write(pepperHash[j]);
                j++;
            }
            pepper = pepperWriter.toString();
            propPass = password+pepper;
            Log.d("***PROPPASS*****", propPass);
            //String hash = new String(pepperHash, StandardCharsets.UTF_8);  //This doesn't work
            //propPass = password+hash;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

//TODO: Remove Logs
        byte[] cipherResult = null;
        byte[] iv = null;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(propPass.getBytes());
            byte[] hash = digest.digest();
            SecretKeySpec secret = new SecretKeySpec(hash, "AES");
            Log.d("*******LOOK HERE*******", "secret initialized");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            Log.d("*******LOOK HERE*******", "Get instance done");
            //GCMParameterSpec spec = new GCMParameterSpec(128, new byte[12]);
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            Log.d("*******LOOK HERE*******", "init works");
            iv = cipher.getIV();
            cipherResult = cipher.doFinal(data);
            Log.d("*******LOOK HERE*******", "did Final");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        //} catch (InvalidAlgorithmParameterException e) {
          //  e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }


        cipherResult = innardEncrypt(cipherResult);
        cipherResult = innardEncrypt(cipherResult);
        cipherResult = innardEncrypt(cipherResult);

        byte[] encoding = Base64.encode(cipherResult, 2);
        String strURLEncryptedPrivate = new String(encoding);
        strURLEncryptedPrivate = strURLEncryptedPrivate.replace('+', '!');
        strURLEncryptedPrivate = strURLEncryptedPrivate.replace('/', '.');

        String strIV = new String(Base64.encode(iv, 2), StandardCharsets.UTF_8);
        strIV = strIV.replace('+', '!');
        strIV = strIV.replace('/', '.');

/*        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("iv", strIV);
            jsonObject.put("strURLEncrypted", strURLEncryptedPrivate);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        String output = strURLEncryptedPrivate+"~"+strIV+"~"+version;

        return output;
    }

    public static byte[] DecryptStrong(String strStrong, String password){
        Log.d("***DecryptStrong****", "Starting decryption");
        String[] arrayOfStrings = strStrong.split("~");
        String strEncryptedPrivate = arrayOfStrings[0];
        String strIV = arrayOfStrings[1];
        strIV = strIV.replace('!', '+');
        strIV = strIV.replace('.', '/');
        byte[] iv = Base64.decode(strIV, 2);
        String version = arrayOfStrings[2];  //TODO: this

/*        try {
            data = jsonObject.getString("strURLEncrypted");
            iv = jsonObject.getString("iv").getBytes(StandardCharsets.UTF_8);
            iv = Base64.decode(iv, 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        strEncryptedPrivate = strEncryptedPrivate.replace('!', '+');
        strEncryptedPrivate = strEncryptedPrivate.replace('.', '/');

        byte[] dataBytes = new byte[0];
        try {
            dataBytes = strEncryptedPrivate.getBytes("UTF-8");
            dataBytes = Base64.decode(dataBytes, 2);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        dataBytes = innardDecrypt(dataBytes);
        dataBytes = innardDecrypt(dataBytes);
        dataBytes = innardDecrypt(dataBytes);
        Log.d("***LOOK HERE****", "Through innard decryption");

        String propPass = null;
        try {
            String pepper = "activity";
            MessageDigest digestPepper = MessageDigest.getInstance("SHA-256");
            digestPepper.update(pepper.getBytes());
            byte[] pepperHash = digestPepper.digest();
            StringWriter pepperWriter = new StringWriter();
            int j = 0;
            while (j != pepperHash.length) {
                pepperWriter.write(pepperHash[j]);
                j++;
            }
            pepper = pepperWriter.toString();
            propPass = password+pepper;
            Log.d("****PROPPASS*****", propPass);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

//TODO: Remove Logs
        byte[] cipherResult = null;


        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(propPass.getBytes());
            byte[] hash = digest.digest();
            SecretKeySpec secret = new SecretKeySpec(hash, "AES");
            Log.d("*******LOOK HERE*******", "secret initialized");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            Log.d("*******LOOK HERE*******", "Get instance done");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secret, spec);
            Log.d("*******LOOK HERE*******", "init works");
            cipherResult = cipher.doFinal(dataBytes);
            Log.d("*******LOOK HERE*******", "did Final");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        Log.d("***DecryptStrong****", "End of decryption");
        return cipherResult;
    }

    //Bouncy Castle wants Key lengths 128/192/256 bits
    public static String propEncryptEZ(byte[] data, String password){

        String propPass = null;
        try {
            String pepper = "activity";
            MessageDigest digestPepper = MessageDigest.getInstance("SHA-256");
            digestPepper.update(pepper.getBytes());
            byte[] pepperHash = digestPepper.digest();
            StringWriter pepperWriter = new StringWriter();
            int j = 0;
            while (j != pepperHash.length) {
                pepperWriter.write(pepperHash[j]);
                j++;
            }
            pepper = pepperWriter.toString();
            propPass = password+pepper;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

//TODO: Remove Logs
        byte[] cipherResult = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(propPass.getBytes());
            byte[] hash = digest.digest();
            SecretKeySpec secret = new SecretKeySpec(hash, "AES");
            Log.d("*******LOOK HERE*******", "secret initialized");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            Log.d("*******LOOK HERE*******", "Get instance done");
            //IV not used in ECB mode
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            Log.d("*******LOOK HERE*******", "init works");
            cipherResult = cipher.doFinal(data);
            Log.d("*******LOOK HERE*******", "did Final");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        cipherResult = innardEncrypt(cipherResult);
        cipherResult = innardEncrypt(cipherResult);
        cipherResult = innardEncrypt(cipherResult);

        byte[] encoding = Base64.encode(cipherResult, 2);
        StringWriter writer = new StringWriter();
        int j = 0;
        while (j != encoding.length) {
            writer.write(encoding[j]);
            j++;
        }
        String strURLEncryptedPrivate = writer.toString();
        strURLEncryptedPrivate = strURLEncryptedPrivate.replace('+', '!');
        strURLEncryptedPrivate = strURLEncryptedPrivate.replace('/', '.');

        return strURLEncryptedPrivate;
    }

    public static byte[] propDecryptEZ(String data, String password){
        String strEncryptedPrivate;
        strEncryptedPrivate = data.replace('!', '+');
        strEncryptedPrivate = strEncryptedPrivate.replace('.', '/');

        byte[] dataBytes = new byte[0];
        try {
            dataBytes = strEncryptedPrivate.getBytes("UTF-8");
            dataBytes = Base64.decode(dataBytes, 2);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        dataBytes = innardDecrypt(dataBytes);
        dataBytes = innardDecrypt(dataBytes);
        dataBytes = innardDecrypt(dataBytes);

        String propPass = null;
        try {
            String pepper = "activity";
            MessageDigest digestPepper = MessageDigest.getInstance("SHA-256");
            digestPepper.update(pepper.getBytes());
            byte[] pepperHash = digestPepper.digest();
            StringWriter pepperWriter = new StringWriter();
            int j = 0;
            while (j != pepperHash.length) {
                pepperWriter.write(pepperHash[j]);
                j++;
            }
            pepper = pepperWriter.toString();
            propPass = password+pepper;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
//TODO: Remove Logs
        byte[] hash;
        byte[] cipherResult = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(propPass.getBytes());
            hash = digest.digest();
            SecretKeySpec secret = new SecretKeySpec(hash, "AES");
            Log.d("*******LOOK HERE*******", "secret initialized");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            Log.d("*******LOOK HERE*******", "Get instance done");
            cipher.init(Cipher.DECRYPT_MODE, secret);
            Log.d("*******LOOK HERE*******", "init works");
            cipherResult = cipher.doFinal(dataBytes);
            Log.d("*******LOOK HERE*******", "did Final");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return cipherResult;
    }

    //TODO: Remove Logs
    private static byte[] innardEncrypt (byte[] data){
        byte[] cipherResult = null;
        String propPass = "veryveryverysecret";

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(propPass.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest();
            SecretKeySpec secret = new SecretKeySpec(hash, "AES");
            //Log.d("*******LOOK HERE*******", "secret initialized");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            //Log.d("*******LOOK HERE*******", "Get instance done");
            //IV not used in ECB mode
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            //Log.d("*******LOOK HERE*******", "init works");
            cipherResult = cipher.doFinal(data);
            //Log.d("*******LOOK HERE*******", "did Final");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return cipherResult;
    }

    private static byte[] innardDecrypt (byte[] data){
        byte[] cipherResult = null;
        String propPass = "veryveryverysecret";

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(propPass.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest();
            SecretKeySpec secret = new SecretKeySpec(hash, "AES");
            //Log.d("*******LOOK HERE*******", "secret initialized");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            //Log.d("*******LOOK HERE*******", "Get instance done");
            //IV not used in ECB mode
            cipher.init(Cipher.DECRYPT_MODE, secret);
            //Log.d("*******LOOK HERE*******", "init works");
            cipherResult = cipher.doFinal(data);
            //Log.d("*******LOOK HERE*******", "did Final");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return cipherResult;
    }

}
