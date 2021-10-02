package org.giftnotes.anticoin.TransactionTypes;

import android.util.Log;

import org.giftnotes.anticoin.MainActivity;
import org.giftnotes.anticoin.utilities.Crypto;
import java.util.ArrayList;

public class NewHandle {

    public NewHandle(){
        super();
    }

    private String type = "newHandle";
    private String ledger = "na";

    //TODO : add device ID to identify which key is being used

    public String getSignedPackage (String handle, String publicKey, String keyType){
        String dataToSign = type+ledger+MainActivity.getHandle()+handle+publicKey+keyType;
        String signature = Crypto.verifiedDeviceSignature(dataToSign);

        ArrayList ArrayList = new ArrayList();
        ArrayList.add("type="+this.type);
        ArrayList.add("&ledger="+this.ledger);
        ArrayList.add("&usher="+MainActivity.getHandle());
        ArrayList.add("&device="+ MainActivity.getDevice());
        ArrayList.add("&handle="+handle);
        ArrayList.add("&publicKey="+publicKey);
        ArrayList.add("&keyType="+keyType);
        ArrayList.add("&signature="+signature);

        StringBuilder stringBuilder = new StringBuilder();
        for (Object components:ArrayList)
        {
            String component = components.toString();
            stringBuilder.append(component);
        }
        String postThisString = stringBuilder.toString();
        Log.d("*******LOOK HERE*******", postThisString);
        return postThisString;
    }

    public String getSearchString (String strFirst, String strLast, String strHandle){
        ArrayList ArrayList = new ArrayList();
        ArrayList.add("handle="+strHandle);
        ArrayList.add("&first="+strFirst);
        ArrayList.add("&last="+strLast);

        StringBuilder stringBuilder = new StringBuilder();
        for (Object components:ArrayList)
        {
            String component = components.toString();
            stringBuilder.append(component);
        }
        String postThisString = stringBuilder.toString();
        Log.d("*******LOOK HERE*******", postThisString);
        return postThisString;
    }

}

