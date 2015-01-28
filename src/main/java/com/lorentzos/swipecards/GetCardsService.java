package com.lorentzos.swipecards;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class GetCardsService extends IntentService {

    private static final String LOG_TAG = "ServiceTag ";
    public static final String BROADCAST_STATUS = "ServiceTag ";
    public static final String NOTIFICATION = "com.lorentzos.swipecards.broadcast";
    DatabaseHelper data;
    int statusCode1 = 404;
    JSONObject mainJSON;
    String url;
    ObjCard newObj = new ObjCard();
    JSONParser jsonParser;

    public GetCardsService() {
        super("GetPosts");
        data =new DatabaseHelper(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Service ", "Started Service");
        for (int i = 0; i < 10; i++){
            try {
                pullCards();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendBroadCast(1);
        }
        sendBroadCast(2);

//        ResultReceiver rec = intent.getParcelableExtra("receiver");
        // Extract additional values from the bundle
//        String val = intent.getStringExtra("foo");
        // To send a message to the Activity, create a pass a Bundle
//        Bundle bundle = new Bundle();
//        bundle.putString("resultValue", "My Result Value. Passed in: " + val);
        // Here we call send passing a resultCode and the bundle of extras
//        rec.send(Activity.RESULT_OK, bundle);
    }

    private void sendBroadCast(int status) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(BROADCAST_STATUS, status);
       //intent.putExtra((Serializable)newObj);
        //intent.putExtra("myObject", new Gson().toJson(myobject);
        sendBroadcast(intent);
        Log.d("Reciever", "BroadCast Send");
    }

    private void pullCards() throws URISyntaxException, IOException {
        // Create a new HttpClient and Post Header


            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost();

            try {
                // Add your data
                String url = Utils.serverLink + "popfromBinaryTurkQ";
                Log.d("url", url);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("token", "turk"));
                httpPost.setURI(new URI(url));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                Log.d(LOG_TAG + " PP: HTTP POST", EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairs)));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httpPost);

                String response_message = EntityUtils.toString(response.getEntity());
                //String response_result = response.getFirstHeader("result").getValue();
                //get all headers
                //response_entity.
                Log.d(LOG_TAG, response_message);
                // Get headers and result

                statusCode1 = response.getStatusLine().getStatusCode();
                if (statusCode1 == 200) {
                    String fileName = Long.toString(System.currentTimeMillis())+".jpg";
                    String filePath = "file://"+getFilesDir()+"/"+fileName;

                    JSONObject mainJSON = new JSONObject(response_message);
                    newObj.setUrl(mainJSON.getString("url"));
                    newObj.setTag(mainJSON.getString("tag"));
                    newObj.setQcflag(mainJSON.getInt("qcflag"));
                    newObj.setKeyspace(mainJSON.getString("keyspace"));
                    newObj.setImage(filePath);
                    Log.d("JSON tag", mainJSON.getString("tag"));

                    //Downloading image
                    // Create a new HttpClient and Post Header
                    Log.d("JSON tag", "Downlaoding Images");
                    Long time = System.currentTimeMillis();
                    HttpGet httpget = new HttpGet();
                    httpclient = new DefaultHttpClient();
                    httpget.setURI(new URI(newObj.getUrl()));
                    response = httpclient.execute(httpget);

                    BufferedHttpEntity httpEntity = new BufferedHttpEntity(response.getEntity());
                    InputStream is=httpEntity.getContent();

                    OutputStream outputStream = openFileOutput(fileName , Context.MODE_PRIVATE);
                    try {
                        final byte[] buffer = new byte[100 * 1024];
                        int read;

                        while ((read = is.read(buffer)) != -1)
                            outputStream.write(buffer, 0, read);

                        outputStream.flush();
                    } finally {
                        outputStream.close();
                    }
                    File neww = getFilesDir();
                    Log.e("LOGTAG", "" + neww.getAbsolutePath());
                    Long a = System.currentTimeMillis() - time;
                    Log.e("Download ", "Finished " + fileName);
                    Log.e("Download ", "Time taken : " + Long.toString(a));
                    data.open();
                    data.addCardObj(newObj);
                    data.close();


                }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


    }
}
