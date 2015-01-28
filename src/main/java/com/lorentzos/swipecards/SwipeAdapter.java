package com.lorentzos.swipecards;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;


public class SwipeAdapter extends BaseAdapter {

    public final static String TAG = "TEST_ADAPTER";

    private Context context;
    private ArrayList<ObjCard> items;

    DisplayImageOptions options;
    

    public SwipeAdapter(Context context, ArrayList<ObjCard> items) {

        this.context = context;
        this.items = items;
       // this.tags = tags;

         options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading)
                .showImageForEmptyUri(R.drawable.loading)
                .showImageOnFail(R.drawable.loading)
                //.resetViewBeforeLoading(true)
                .cacheInMemory(true)
              //  .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
//                .displayer(new FadeInBitmapDisplayer(300))

                .build();
    }

    

    @Override
    public int getCount() {
        return this.items.size();
    }

    
    @Override
    public ObjCard getItem(int position) {
        return this.items.get(position);
    }



    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ObjCard item = getItem(position);
        String tag = item.getTag() ;
        String url = item.getUrl();
        String image = item.getImage();
        String completeText = "Is this a photo of <b>" + 
                tag +
                "</b> ?";

        Log.d(TAG, "item: " + item + ", position: " + position);

        View view = convertView;

        if (view == null) {

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(R.layout.item_card, parent, false);

            Log.d("null", "view is null");
        } else {
            Log.d("null", "view is not null");
            //holder = (ViewHolder) view.getTag();
        }
        TextView tvDisplay = (TextView) view.findViewById(R.id.tvQuestion);
        tvDisplay.setText(Html.fromHtml(completeText));
        ImageView ivDisplay = (ImageView) view.findViewById(R.id.iwDisplay);
        String tempUrl = "https://s3.amazonaws.com/hvergeimageset/dataset/hyperverge/scene/cave/1421105731344_604.6635736711323.jpg";
        Log.d("Image ", image);
        ImageLoader.getInstance().displayImage(image, ivDisplay, options);
        return view;
    }

}
