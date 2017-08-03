package midatlandroid.final_project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

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

        TextView name;
        name = (TextView) view.findViewById(R.id.name);
        name.setText(item.name);

        TextView price;
        price = (TextView) view.findViewById(R.id.price);
        price.setText(Double.toString(item.price));

        TextView retailer;
        retailer = (TextView) view.findViewById(R.id.ret);
        retailer.setText(item.retailer);

        return view;
        }
    }
