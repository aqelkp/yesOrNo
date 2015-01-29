package com.lorentzos.swipecards;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MyActivity extends Activity {

    JSONParser jsonParser ;

    
    DatabaseHelper data = new DatabaseHelper(MyActivity.this);
    private int i;
    Boolean isAdapterEmpty = false;
    SwipeAdapter arrayAdapter;
    final ArrayList<ObjCard> alObjCards = new ArrayList<>();
    Boolean isCardsAvailable = false;
    int id_LastCard = 0;
    Utils utils = new Utils();

    @InjectView(R.id.frame) SwipeFlingAdapterView flingContainer;

    getJSON newTask;
    postResults postTask;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            Log.d("Reciever ", " Broacast Recieved");
            if (extras != null) {
                if (extras.getInt(GetCardsService.BROADCAST_STATUS) == 1){
                     data.open();
                    Log.d( "Service Last Card in reciever", Integer.toString(id_LastCard));
                    Cursor cur = data.getCardById(extras.getInt("objectId"));
                    getDataFromCursor(cur);
                    if (cur.getCount() > 0){
                        arrayAdapter.notifyDataSetChanged();
                        isAdapterEmpty = false;
                    }
                    data.close();


                }
                if (extras.getInt(GetCardsService.BROADCAST_STATUS) == 2){
                    Log.d("Reciever ", " Broacast Recieved To restart");
                    data.open();
                    if (data.getCountOfCards()<50) {
                        Intent getCards = new Intent(MyActivity.this, GetCardsService.class);
                        startService(getCards);
                    }
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        ButterKnife.inject(this);

        if (utils.checkConnection(this)){
            Intent getCards = new Intent(this, GetCardsService.class);
            startService(getCards);
        }

        //newTask=new getJSON();
        //newTask.execute();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(MyActivity.this)
                .threadPriority(Thread.MAX_PRIORITY)
                //.denyCacheImageMultipleSizesInMemory()
                //.diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                //.writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);


       
        
        data.open();
        Cursor cur = data.getAllCards();
        getDataFromCursor(cur);
        data.close();
        arrayAdapter = new SwipeAdapter(this, alObjCards);


        //arrayAdapter = new ArrayAdapter<>(this, R.layout.item, R.id.helloText, al );
        
        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {

            ImageButton bLeft = (ImageButton) findViewById(R.id.left);
            ImageButton bRight = (ImageButton) findViewById(R.id.right);

            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                //alLinks.remove(0);
                alObjCards.remove(0);
                
                arrayAdapter.notifyDataSetChanged();
                bLeft.setVisibility(View.VISIBLE);
                bRight.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                ObjCard currentCard = (ObjCard) dataObject;
                currentCard.setTruth(0);
                executePostResult(currentCard);
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                ObjCard currentCard = (ObjCard) dataObject;
                currentCard.setTruth(1);
                executePostResult(currentCard);
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                Intent getCards = new Intent(MyActivity.this, GetCardsService.class);
                startService(getCards);
                /*data.open();
                Cursor cur = data.getNewCards(id_LastCard);
                getDataFromCursor(cur);
                if (cur.getCount() > 0){
                    arrayAdapter.notifyDataSetChanged();
                    isAdapterEmpty = false;
                }
                data.close();
                */
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

                

                View view = flingContainer.getSelectedView();
                CardView card = (CardView) view.findViewById(R.id.layoutCard);
                
                //Log.d("scroll percent", Float.toString(scrollProgressPercent));
                if (scrollProgressPercent>0){
                    card.setCardBackgroundColor(getResources().getColor(R.color.green));
                    bLeft.setVisibility(View.INVISIBLE);
                    bRight.setVisibility(View.VISIBLE);
                  //  view.findViewById(R.id.layoutCard).setBackgroundColor(getResources().getColor(R.color.green));
                }else if (scrollProgressPercent<0){
                    card.setCardBackgroundColor(getResources().getColor(R.color.red));
                    bLeft.setVisibility(View.VISIBLE);
                    bRight.setVisibility(View.INVISIBLE);
                    //view.findViewById(R.id.layoutCard).setBackgroundColor(getResources().getColor(R.color.red));
                }else {
                    card.setCardBackgroundColor(getResources().getColor(R.color.white));
                    bLeft.setVisibility(View.VISIBLE);
                    bRight.setVisibility(View.VISIBLE);
                    //view.findViewById(R.id.layoutCard).setBackgroundColor(Color.WHITE);
                }
                
                //if (scrollProgressPercent == 1 || scrollProgressPercent == -1){
                //    bLeft.setVisibility(View.VISIBLE);
                //    bRight.setVisibility(View.VISIBLE);
                //}
//                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
//                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);

            }

            @Override
            public void onAdapterFinished(int adapterCount) {
                isAdapterEmpty = true;
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
               // makeToast(MyActivity.this, "Clicked!");
            }
        });

    }

    public void executePostResult( ObjCard card){
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            postTask = new postResults();
            postTask.execute(card);
        }else {
           Toast.makeText(MyActivity.this, "Cannot connect to internet. Please Check your connection and try again", Toast.LENGTH_SHORT).show();
        }
    }

    static void makeToast(Context ctx, String s){
        Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
    }

     void getDataFromCursor (Cursor c){
        while (c.moveToNext()){
            ObjCard newCard = new ObjCard();
            newCard.setId(c.getInt(0));
            newCard.setUrl(c.getString(1));
            newCard.setTag(c.getString(2));
            newCard.setQcflag(c.getInt(3));
            newCard.setKeyspace(c.getString(4));
            newCard.setTruth(c.getInt(5));
            newCard.setImage(c.getString(7));
            alObjCards.add(newCard);
            id_LastCard = newCard.getId();
        }
         Log.d( "Service Last Card", Integer.toString(id_LastCard));
       
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(GetCardsService.NOTIFICATION));
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }



    @OnClick(R.id.right)
    public void right() {
        /**
         * Trigger the right event manually.
         */
        if (isAdapterEmpty){
            Log.d("empty", "please dont click");
            makeToast(MyActivity.this, "Empty");
        }else {
//            View view = flingContainer.getSelectedView();
            flingContainer.getTopCardListener().selectRight();
            View view = flingContainer.getSelectedView();

            CardView card = (CardView) view.findViewById(R.id.layoutCard);
            card.setCardBackgroundColor(getResources().getColor(R.color.green));
            ObjCard currentCard = arrayAdapter.getItem(0);
            currentCard.setTruth(1);
            Log.d("id of the card left ", Integer.toString(currentCard.getId()));
            executePostResult(currentCard);
        }
    }

    @OnClick(R.id.left)
    public void left() {
        if (isAdapterEmpty){
            Log.d("empty", "please dont click");
            makeToast(MyActivity.this, "Empty");

        }else {
            flingContainer.getTopCardListener().selectLeft();
            View view = flingContainer.getSelectedView();
            CardView card = (CardView) view.findViewById(R.id.layoutCard);
            card.setCardBackgroundColor(getResources().getColor(R.color.red));
            ObjCard currentCard = arrayAdapter.getItem(0);
            currentCard.setTruth(1);
            executePostResult(currentCard);
        }
    }

    public class postResults extends AsyncTask<ObjCard, Void, Void>{

        
        ObjCard newObj = new ObjCard();
        JSONObject json;

        @Override
        protected Void doInBackground(ObjCard... params) {
            jsonParser = new JSONParser();
            
            ObjCard obj = params[0];
            List<NameValuePair> listParams = new ArrayList<NameValuePair>();
            listParams.add(new BasicNameValuePair("token", "turk"));
            listParams.add(new BasicNameValuePair("path", obj.getUrl()));
            
            listParams.add(new BasicNameValuePair("tag", obj.getTag()));
            listParams.add(new BasicNameValuePair("qcflag", Integer.toString(obj.getQcflag())));
            listParams.add(new BasicNameValuePair("keyspace", obj.getKeyspace()));
            listParams.add(new BasicNameValuePair("truth", Integer.toString(obj.getTruth())));
            json = jsonParser.makeHttpRequest("pushtoVerifiedQTurk", "POST", listParams, null);
            Log.d("JSON post", json.toString());
            data.open();
            data.deleteCardFromQue(obj.getId());
            data.close();
            Log.d("delete ", obj.getImage());
            File file = new File(obj.getImage().split("file://")[1]);
            if (file.exists()) {
                file.delete();
                Log.d("delete ", " file deleted");
            }

            return null;
        }

        

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    };

    public class getJSON extends AsyncTask<Void, Void, Void>{
        JSONObject mainJSON;
        String url;
        ObjCard newObj = new ObjCard();
       

        @Override
        protected Void doInBackground(Void... params) {
            jsonParser = new JSONParser();
            List<NameValuePair> listParams = new ArrayList<NameValuePair>();
            listParams.add(new BasicNameValuePair("token", "turk"));
            mainJSON = jsonParser.makeHttpRequest("popfromBinaryTurkQ", "POST", listParams, null);
            try {
                Log.d("JSON", mainJSON.toString());
                url  = mainJSON.getString("url");
                Log.d("JSON url", url);

                newObj.setUrl(mainJSON.getString("url"));
                newObj.setTag(mainJSON.getString("tag"));
                newObj.setQcflag(mainJSON.getInt("qcflag"));
                newObj.setKeyspace(mainJSON.getString("keyspace"));
                data.open();
                data.addCardObj(newObj);
                data.close();
                Log.d("JSON tag", mainJSON.getString("tag"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            alObjCards.add(newObj);
            arrayAdapter.notifyDataSetChanged();
            new getJSON().execute();

        }
    };


}
