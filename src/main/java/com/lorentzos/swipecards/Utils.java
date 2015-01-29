package com.lorentzos.swipecards;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

/**
 * Created by aqel on 20/1/15.
 */
public class Utils {
    public static String serverLink = "http://54.175.12.103/v0/";

    public Boolean checkConnection (Context context){

        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            return true;
        }else {
            return false;
        }
    }

    public Intent passIntent (ObjCard theObj, Intent intent){
        intent.putExtra("url", theObj.getUrl());
        intent.putExtra("tag", theObj.getTag());
        intent.putExtra("qcflag", theObj.getQcflag());
        intent.putExtra("keyspace", theObj.getKeyspace());
        intent.putExtra("id", theObj.getId());
        intent.putExtra("image", theObj.getImage());
        return intent;
    }

    public ObjCard getObject (Intent intent){
        Bundle extras = intent.getExtras();
        ObjCard NewObj = new ObjCard();
        NewObj.setUrl(extras.getString("url"));
        NewObj.setTag(extras.getString("tag"));
        NewObj.setQcflag(extras.getInt("qcflag"));
        NewObj.setKeyspace(extras.getString("keyspace"));
        NewObj.setId(extras.getInt("id"));
        NewObj.setImage(extras.getString("image"));
        return NewObj;
    }
}
