package cms.aa.com.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 806828 on 9/9/2019.
 */

public class NetworkStatsAdapter extends ArrayAdapter<String> {

    public NetworkStatsAdapter (Context context, int textViewResourceId, List<String> objects) {
        super(context, textViewResourceId, objects);
    }




    /*@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        String myString = getItem(position);
        if(myString.contains("SINR")){
            TextView tv = (TextView) view.findViewById(android.R.id.text1);
          //  String[] sinrSplit = myString.split("SINR: ");
          //  Log.i("SINR Split", sinrSplit[0]);
            tv.setTextColor(Color.RED);
        }


        return view;
    }*/

}
