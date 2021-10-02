package org.giftnotes.anticoin.utilities;


import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto extends Object{

    public Crypto(){
        super();
    }

    String publicKeyString;
    KeyPairGenerator keyPairGen;
    String stringSignature;
    PublicKey objectPublicKey;
    PrivateKey objectPrivateKey;
    byte[] signature = null;
    byte[] dataBytes = null;



    public void generateKeys(){
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyPairGen.initialize(2048);  //512 character key with 2048
        try {
            KeyPair keyPair = keyPairGen.generateKeyPair();
            objectPublicKey = keyPair.getPublic();
            objectPrivateKey = keyPair.getPrivate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPrivKeyProprietary(String pass) {
        byte[] encoding;
        encoding = objectPrivateKey.getEncoded();
        encoding = Base64.encode(encoding, 2);
        return Proprietary.EncryptStrong(encoding, pass);
    }

    public void pemWriter() {
        byte[] encoding;
        encoding = objectPublicKey.getEncoded();
        encoding = Base64.encode(encoding, 2);
        StringWriter writer = new StringWriter();
        int j = 0;
        while (j != encoding.length) {
            writer.write(encoding[j]);
            if ((j+1) % 64 == 0 & (j+1) != encoding.length) { writer.write("\n"); }
            j++;
        }
        publicKeyString ="-----BEGIN PUBLIC KEY-----\n"+writer.toString()+"\n-----END PUBLIC KEY-----";
        publicKeyString = publicKeyString.replace('+', '!');
        this.publicKeyString = publicKeyString.replace('/', '.');
    }

    public static String writePEM(byte[] encoding) {
        byte[] encodingB64 = Base64.encode(encoding, 2);
        StringWriter writer = new StringWriter();
        int j = 0;
        while (j != encodingB64.length) {
            writer.write(encodingB64[j]);
            if ((j+1) % 64 == 0 & (j+1) != encodingB64.length) { writer.write("\n"); }
            j++;
        }
        String formatPEM ="-----BEGIN PUBLIC KEY-----\n"+writer.toString()+"\n-----END PUBLIC KEY-----";
        formatPEM = formatPEM.replace('+', '!');
        formatPEM = formatPEM.replace('/', '.');
        return formatPEM;
    }

    public static byte[] pem2KeyBytes(String strPEMPublic){
        String myString = strPEMPublic.replace('.', '/');
        myString = myString.replace('!', '+');
        myString = myString.replaceAll("-----BEGIN PUBLIC KEY-----\n", "");
        myString = myString.replaceAll("\n-----END PUBLIC KEY-----", "");
        myString = myString.replaceAll("\n", "");
        Log.d("*****JUST THE STRING: ", myString);
        byte[] encoding = Base64.decode(myString.getBytes(StandardCharsets.UTF_8), 2);
        return encoding;
    }

    public String getPublicKeyString(){return publicKeyString;}



/*    public void sign(String Data) {
        try {
            this.dataBytes = Data.getBytes();
            Signature s = Signature.getInstance("SHA512withRSA");
            s.initSign(objectPrivateKey);
            s.update(dataBytes);
            this.signature = s.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        stringSignature= new String(Base64.encode(signature, 2));
        stringSignature = stringSignature.replace('+','!');
        this.stringSignature = stringSignature.replace('/','.');

        try {
            Signature objectVerifiable = Signature.getInstance("SHA512withRSA");
            objectVerifiable.initVerify(objectPublicKey);
            objectVerifiable.update(dataBytes);
            Boolean isVerified = objectVerifiable.verify(signature);
            if (isVerified) {
                Log.d("*******LOOK HERE*******", "Signature successfully verified");
            } else {
                Log.d("*******LOOK HERE*******", "signature NOT verified");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

    }

    public String getStringSignature(){
        return stringSignature;
    }*/

    public static boolean checkKeyPair(byte[] bytPublic, byte[] bytPrivate){
        //byte[] bytPublic = objectPublicKey.getEncoded();
        //byte[] bytPrivate = objectPrivateKey.getEncoded();
        boolean verified = false;
        PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(bytPrivate);
        PublicKey newPublicKey = null;
        PrivateKey newPrivateKey = null;
        try {
            newPublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytPublic));
            newPrivateKey = KeyFactory.getInstance("RSA").generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] someRandomBytes = "This is some random stuff".getBytes();
        byte[] otherSignature = null;
        try {
            Signature s = Signature.getInstance("SHA512withRSA");
            s.initSign(newPrivateKey);
            s.update(someRandomBytes);
            otherSignature = s.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        try {
            Signature v = Signature.getInstance("SHA512withRSA");
            v.initVerify(newPublicKey);
            v.update(someRandomBytes);
            verified = v.verify(otherSignature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        if(verified){
            Log.d("****VERIFICATION**: ", "Yes, Verified.  checked key pair");
            return true;
        }else {
            Log.d("****VERIFICATION**: ", "NO, failed");
            return false;
        }
    }

    public static JSONObject deviceEncryptSymmetric(byte[] data){
        byte[] cipherResult = null;
        byte[] iv = null;
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            Key secret = ks.getKey(Account.getAliasSymmetric(), null);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            //Log.d("*******LOOK HERE*******", "Get instance done");
            //GCMParameterSpec spec = new GCMParameterSpec(128, new byte[12]);
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            //Log.d("*******LOOK HERE*******", "init works");
            iv = cipher.getIV();
            cipherResult = cipher.doFinal(data);
            //Log.d("*******LOOK HERE*******", "did Final");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
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


        byte[] encoding = Base64.encode(cipherResult, 2);
        String strURLEncrypted = new String(encoding);
        strURLEncrypted = strURLEncrypted.replace('+', '!');
        strURLEncrypted = strURLEncrypted.replace('/', '.');

        JSONObject jsonObject = new JSONObject();
        String strIV = new String(Base64.encode(iv, 2), StandardCharsets.UTF_8);

        try {
            jsonObject.put("iv", strIV);
            jsonObject.put("strURLEncrypted", strURLEncrypted);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static byte[] deviceDecryptSymmetric(JSONObject jsonObject){

        Log.d("***LOOK HERE****", "Starting decryption");
        String data = null;
        byte[] iv = null;

        try {
            data = jsonObject.getString("strURLEncrypted");
            iv = jsonObject.getString("iv").getBytes(StandardCharsets.UTF_8);
            iv = Base64.decode(iv, 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String strEncrypted = data.replace('!', '+');
        strEncrypted = strEncrypted.replace('.', '/');

        byte[] dataBytes = null;
        try {
            dataBytes = strEncrypted.getBytes("UTF-8");
            dataBytes = Base64.decode(dataBytes, 2);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] cipherResult = null;

        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            Key secret = ks.getKey(Account.getAliasSymmetric(), null);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            //Log.d("*******LOOK HERE*******", "Get instance done");
            //GCMParameterSpec spec = new GCMParameterSpec(128, new byte[12]);
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secret, spec);
            //Log.d("*******LOOK HERE*******", "init works");
            cipherResult = cipher.doFinal(dataBytes);
            //Log.d("*******LOOK HERE*******", "did Final");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
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

        return cipherResult;
    }

    public static String verifiedAdminSignature(byte[] bytPublic, byte[] bytPrivate, String strToSign){
        boolean verified = false;

        PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(bytPrivate);

        PublicKey newPublicKey = null;
        PrivateKey newPrivateKey = null;
        try {
            newPublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytPublic));
            newPrivateKey = KeyFactory.getInstance("RSA").generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] bytesToSign = strToSign.getBytes(StandardCharsets.UTF_8);
        byte[] sig = null;
        try {
            Signature s = Signature.getInstance("SHA512withRSA");
            s.initSign(newPrivateKey);
            s.update(bytesToSign);
            sig = s.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        try {
            Signature v = Signature.getInstance("SHA512withRSA");
            v.initVerify(newPublicKey);
            v.update(strToSign.getBytes(StandardCharsets.UTF_8));
            verified = v.verify(sig);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        if(verified){
            Log.d("****VERIFICATION**: ", "Signature Verified");
            String output = new String(Base64.encode(sig, 2));
            output = output.replace('+', '!');
            output = output.replace('/', '.');
            return output;
        }else {
            Log.d("****VERIFICATION**: ", "NO, signature not verified");
            return null;
        }
    }

    public static String verifiedDeviceSignature(String dataToSign){
        boolean verified = false;
        String signature = null;

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        digest.update(dataToSign.getBytes(StandardCharsets.UTF_8));
        byte[] hash = digest.digest();

        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            //******This is a PrivateKeyEntry
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(Account.getAliasAsymmetric(), null);
            PrivateKey privateKey = privateKeyEntry.getPrivateKey();
            Signature s = Signature.getInstance("SHA512withRSA");
            s.initSign(privateKey);
            s.update(dataToSign.getBytes(StandardCharsets.UTF_8));  //TODO:  Change this to hash
            byte[] sig = s.sign();

            PublicKey publicKey = ks.getCertificate(Account.getAliasAsymmetric()).getPublicKey();

            Signature v = Signature.getInstance("SHA512withRSA");
            v.initVerify(publicKey);
            v.update(dataToSign.getBytes(StandardCharsets.UTF_8));  //TODO:  Change this to hash
            verified = v.verify(sig);
            String strSig = new String(Base64.encode(sig, 2));
            signature = strSig;
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        if(verified){
            Log.d("***verifiedDeviceSignature*: ", "Signature Verified");
            signature = signature.replace('+', '!');
            signature = signature.replace('/', '.');
            return signature;
        }else {
            return "Not verified.";
        }
    }


}
