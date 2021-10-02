package org.giftnotes.anticoin.TransactionTypes;

import android.util.Log;
import java.util.ArrayList;

public class Send {

    public Send(){
        super();
    }

    private String type = "send";
    public String getType(){
        return type;
    }

    private String ledger = "default";
    public String getLedger(){
        return ledger;
    }

    private String handle;
    public void setHandle (String handleSet){
        this.handle=handleSet;
    }
    public String getHandle(){
        return handle;
    }

    private String recipient;
    public void setRecipient (String recipientSet){
        this.recipient=recipientSet;
    }
    public String getRecipient(){
        return recipient;
    }

    private String amount;
    public void setAmount (String amountSet){
        this.amount=amountSet;
    }
    public String getAmount(){
        return amount;
    }

    private String dataToSign;
    public void setDataToSign (){this.dataToSign=type+ledger+handle+recipient+amount;}
    public String getDataToSign(){
        return dataToSign;
    }

    private String signature;
    public void setSignature (String signatureSet){
        this.signature=signatureSet;
    }
    public String getSignature(){
        return signature;
    }

    //TODO : add device ID to identify which key is being used

    private String postThisString;
    public String setPostThisString (){
        ArrayList ArrayList = new ArrayList();
        ArrayList.add("type="+this.type);
        ArrayList.add("&book="+this.ledger);
        ArrayList.add("&handle="+this.handle);
        ArrayList.add("&recipient="+this.recipient);
        ArrayList.add("&amount="+this.amount);
        ArrayList.add("&signature="+this.signature);

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
    public String getPostThisString (){
        return postThisString;
    }

}
