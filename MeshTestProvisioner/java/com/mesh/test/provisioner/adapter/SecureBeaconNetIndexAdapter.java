package com.mesh.test.provisioner.adapter;

import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.mesh.test.provisioner.R;
import android.util.Log;
import com.mesh.test.provisioner.util.MeshUtils;


public class SecureBeaconNetIndexAdapter extends BaseAdapter implements OnClickListener {

    private static final String TAG = "SecureBeaconNetIndexAdapter";

    public static HashMap<Integer, Boolean> state = new HashMap<Integer, Boolean>();
    private List<String> datas;
    private LayoutInflater inflater;
    private Context context;

    public SecureBeaconNetIndexAdapter(Context context,List<String> datas) {
        Log.i(TAG,"SecureBeaconNetIndexAdapter datas size = " + datas.size());
        this.context = context;
        this.datas = datas;
        this.inflater = LayoutInflater.from(context);
        init();
    }

    private void init() {
        state.clear();
        for (int i = 0; i < datas.size(); i++) {
            state.put(i, false);
        }
        Log.i(TAG,"init state size = " + state.size());
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView,
            ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.node_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.checkBox = (CheckBox) convertView
                    .findViewById(R.id.checkBoxId);
            viewHolder.textView = (TextView) convertView
                    .findViewById(R.id.textViewId);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textView.setText(datas.get(position));
        Log.i(TAG,"getView state size = " + state.size());
        viewHolder.checkBox.setChecked(state.get(position));
        viewHolder.checkBox.setTag(position);
        viewHolder.checkBox.setOnClickListener(this);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        Integer tag = (Integer) v.getTag();
        boolean isChecked = state.get(tag)? state.put(tag, false) : state.put(tag, true);
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        public CheckBox checkBox;
        public TextView textView;
    }

}
