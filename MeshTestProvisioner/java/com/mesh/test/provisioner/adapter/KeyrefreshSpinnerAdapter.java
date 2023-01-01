package com.mesh.test.provisioner.adapter;

import java.util.List;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mesh.test.provisioner.R;
import java.util.LinkedList;

public class KeyrefreshSpinnerAdapter extends BaseAdapter{

    private List<String> mData;
    private Context mContext;
    private LayoutInflater inflater;

    public KeyrefreshSpinnerAdapter(Context mContext, List<String> mData) {
        this.mContext = mContext;
        this.mData = mData;
        this.inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
    @Override
    public boolean isEnabled(int position) {
        // TODO Auto-generated method stub
        if(mData.get(position).equals("0x0001")){

            return false;
        }
        return super.isEnabled(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.keyrefresh_spinner_item, null);
            viewHolder = new ViewHolder();
            viewHolder.keyrefresh_linear = (LinearLayout) convertView
                    .findViewById(R.id.keyrefresh_linear);
            viewHolder.netkeyIndexTv = (TextView) convertView
                    .findViewById(R.id.netkeyIndex);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if(mData.get(position).equals("0x0001")){
            setEnabled(viewHolder.keyrefresh_linear ,false);
        } else {
            setEnabled(viewHolder.keyrefresh_linear ,true);
        }
        viewHolder.netkeyIndexTv.setText(mData.get(position));
        return convertView;

    }

    public void setEnabled(View view , boolean enabled) {
        if(null == view) {
            return;
        }
        if(view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup)view;
            LinkedList<ViewGroup> queue = new LinkedList<ViewGroup>();
            queue.add(viewGroup);
            while(!queue.isEmpty()) {
                ViewGroup current = queue.removeFirst();
                current.setEnabled(enabled);
                for(int i = 0;i<current.getChildCount();i++) {
                    if(current.getChildAt(i) instanceof ViewGroup ) {
                        queue.addLast((ViewGroup)current.getChildAt(i));
                    } else {
                        current.getChildAt(i).setEnabled(enabled);
                    }
                }
            }
        }else {
            view.setEnabled(enabled);
        }
    }

    public static class ViewHolder {
        public LinearLayout keyrefresh_linear;
        public TextView netkeyIndexTv;
    }


}
