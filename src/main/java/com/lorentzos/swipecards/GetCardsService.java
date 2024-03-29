package com.lorentzos.swipecards;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
    InputStream input = null;
    OutputStream output = null;
    HttpURLConnection connection = null;


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
        }
        sendBroadCast(2, 0);

//        ResultReceiver rec = intent.getParcelableExtra("receiver");
        // Extract additional values from the bundle
//        String val = intent.getStringExtra("foo");
        // To send a message to the Activity, create a pass a Bundle
//        Bundle bundle = new Bundle();
//        bundle.putString("resultValue", "My Result Value. Passed in: " + val);
        // Here we call send passing a resultCode and the bundle of extras
//        rec.send(Activity.RESULT_OK, bundle);
    }

    private void sendBroadCast(int status, int id) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(BROADCAST_STATUS, status);
        intent.putExtra("objectId", id);
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


                    URL urlImage = new URL(newObj.getUrl());
                    //URL urlImage = new URL("https://s3-ap-southeast-1.amazonaws.com/hvergeasiabucket/1421143271122_861.13356705755.jpg");
                    Log.d("JSON tag", "Downlaoding Images");
                    Long time = System.currentTimeMillis();
                    connection = (HttpURLConnection) urlImage.openConnection();
                    connection.connect();

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        String error  = "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage();
                        Log.d("error", error);
                    }

                    // download the file
                    input = connection.getInputStream();
                    //output = new FileOutputStream(filePath);
                    OutputStream output = openFileOutput(fileName , Context.MODE_PRIVATE);

                    byte dataImage[] = new byte[10 * 4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(dataImage)) != -1) {
                        total += count;
                        output.write(dataImage, 0, count);
                    }
                    Long a = System.currentTimeMillis() - time;
                    Log.e("Download ", "Finished " + fileName);
                    Log.e("Download ", "Time taken : " + Long.toString(a));


                    /*
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
                    Log.e("Download ", "Time taken : " + Long.toString(a));*/
                    data.open();
                    long idLong = data.addCardObj(newObj);
                    newObj.setId((int)idLong);
                    data.close();
                    sendBroadCast(1, newObj.getId());



                }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


    }
}
