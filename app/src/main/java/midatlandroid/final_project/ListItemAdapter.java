package midatlandroid.final_project;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Created by kanners on 7/28/2017.
 */
public class ListItemAdapter extends ArrayAdapter<ListSearchItem> {
     private LayoutInflater mInflater;

    public ListItemAdapter(Context context, int rid, List<ListSearchItem> list) {
        super(context, rid, list);
        mInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ListSearchItem item = (ListSearchItem) getItem(position);
        View view = mInflater.inflate(R.layout.list_item, null);

        ImageView imageView;
        imageView = (ImageView) view.findViewById(R.id.store_image);
        imageView.setImageBitmap(item.image);

        TextView name;
        name = (TextView) view.findViewById(R.id.name);
        name.setText(item.name);

        TextView price;
        price = (TextView) view.findViewById(R.id.price);
        String cost = '$' + Double.toString(item.price);
        price.setText(cost);
        price.setTextColor(Color.rgb(105,105,105));


        TextView retailer;
        retailer = (TextView) view.findViewById(R.id.ret);
        retailer.setText(item.retailer);
        if (item.retailer.equals("Walmart")) {
            retailer.setTextColor(Color.rgb(0,125,198));
        } else if (item.retailer.equals("Ebay")) {
            retailer.setTextColor(Color.rgb(229, 50, 56));
        }


        return view;
        }
    }
