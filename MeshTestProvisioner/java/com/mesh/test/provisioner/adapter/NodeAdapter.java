package com.mesh.test.provisioner.adapter;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.mesh.test.provisioner.sqlite.Node;
import com.mesh.test.provisioner.util.MeshUtils;
import java.util.LinkedList;
import android.widget.LinearLayout;
import com.mediatek.bt.mesh.MeshConstants;
import java.util.Arrays;
import com.mesh.test.provisioner.R;
import com.mesh.test.provisioner.listener.RecyclerViewItemClickListener;
import com.mesh.test.provisioner.listener.RecyclerViewItemLongClickListener;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;


public class NodeAdapter extends RecyclerView.Adapter<NodeRecyclerViewHolder>{

    private static final String TAG = "NodeAdapter";

    private static final boolean DEBUG = true;

    private ArrayList<Node> nodeList;
    private Context mContext;
    private LayoutInflater inflater;
    private RecyclerViewItemClickListener mListener;
    private RecyclerViewItemLongClickListener mLongClickListener;
    public final static int ALL_ENABLE = -1;
    private int mPosition = -1;
    private int id = -1;
    private int itemPositin = -1;
    private SpannableStringBuilder strStyle = null;

    private void log(String string) {
        if(DEBUG) {
            Log.i(TAG, string);
        }
    }

    public void setOnItemClickListener(RecyclerViewItemClickListener mListener){
        this.mListener = mListener;
    }

    public void setOnItemLongClickListener(RecyclerViewItemLongClickListener mLongClickListener){
        this.mLongClickListener = mLongClickListener;
    }

    public NodeAdapter(Context context, ArrayList<Node> nodeList) {
        this.mContext = context;
        this.nodeList = nodeList;
        inflater = LayoutInflater.from(mContext);
    }

    public void setPosition(int position) {
        this.mPosition = position;
        notifyDataSetChanged();
    }

    public void setPosition(int position,boolean isRefresh) {
        this.mPosition = position;
        if(isRefresh) {
            notifyDataSetChanged();
        }
    }

    public int getPosition() {
        return mPosition;
    }

    public boolean addNode(Node node) {
        log("addNode mAddr = " + node.getAddr() + " , UUID = " + MeshUtils.intArrayToString(node.getUUID(), true) + " , GATTAdrr = " + node.getGattAddr());
        for(int i = 0 ; i < nodeList.size() ; i++) {
            if(nodeList.get(i).getAddr() == node.getAddr()) {
                log("Duplicate device UUID = " + MeshUtils.intArrayToString(node.getUUID(), true) + " , GATTAdrr = " + node.getGattAddr());
                return false;
            }
        }
        nodeList.add(node);
        notifyDataSetChanged();
        return true;
    }

    public List<Node> getNodes() {
        return nodeList;
    }

    public void removeNode(Node node) {
        log("removeNode mAddr = " + node.getAddr() + " , UUID = " + MeshUtils.intArrayToString(node.getUUID(), true) + " , GATTAdrr = " + node.getGattAddr());
        int index = -1;
        for(int i = 0 ; i < nodeList.size() ; i++) {
            if(nodeList.get(i).getAddr() == node.getAddr()) {
                index = i;
                break;
            }
        }
        if(index != -1) {
            nodeList.remove(index);
            notifyDataSetChanged();
        }else {
            log("not found device need to remove");
        }
    }

    public void removeNodeByIndex(int index) {
        log("removeNodeByIndex  mAddr = " + nodeList.get(index).getAddr() +
            " , UUID = " + MeshUtils.intArrayToString(nodeList.get(index).getUUID(), true) +
            " , GATTAdrr = " + nodeList.get(index).getGattAddr());
        nodeList.remove(index);
        notifyDataSetChanged();
    }

    public synchronized void nodeStateChange(Node node, int active){
        for(int i = 0;i < nodeList.size();i++) {
            Node mNode = nodeList.get(i);
            if(mNode.getAddr()== node.getAddr()){
                mNode.setActiveStatus(active);
                nodeList.set(i,mNode);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public synchronized void nodeFriendShipStateChange(Node node){
        for (int i = 0;i < nodeList.size();i++) {
            Node mNode = nodeList.get(i);
            if (mNode.getAddr()== node.getAddr()){
                nodeList.set(i,mNode);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void nodeTimeChange(Node node, int position){
        this.mPosition = position;
        for(int i = 0;i < nodeList.size();i++) {
            Node mNode = nodeList.get(i);
            if(mNode.getAddr()== node.getAddr()){
                nodeList.set(i,mNode);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public synchronized void heartBeatChange() {
        notifyDataSetChanged();
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

    public int getId() {
        return id;
    }

    public int getItemPosition() {
        return itemPositin;
    }


    @Override
    public int getItemCount() {
        if(nodeList!=null){
            return nodeList.size();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(NodeRecyclerViewHolder viewHolder, final int position) {
        Node node = nodeList.get(position);
        viewHolder.tvNodeNum.setText((position + 1) + "");
        if(node.getNodeBearer() == MeshConstants.MESH_BEARER_ADV) {
            if (node.getCmdAddFriDev()) {
                viewHolder.textView.setText(node.getFriDevName() + " ( " + MeshUtils.decimalToHexString("%04X", node.getAddr()) + " )");
                viewHolder.status_ll.setVisibility(View.GONE);
            } else {
                viewHolder.textView.setText(MeshUtils.intArrayToString(node.getUUID(), true));
                viewHolder.status_ll.setVisibility(View.VISIBLE);
            }
        }else if(node.getNodeBearer() == MeshConstants.MESH_BEARER_GATT){
            viewHolder.status_ll.setVisibility(View.VISIBLE);
            if(null != node.getGattDevName()) {
                viewHolder.textView.setText(node.getGattAddr() + " (" + node.getGattDevName() + ")");
            }else {
                viewHolder.textView.setText(node.getGattAddr() + " ( null ) ");
            }
        }
        viewHolder.heartbeatReceive_ll.setVisibility(View.GONE);
        viewHolder.friendShip_ll.setVisibility(View.GONE);
        StringBuffer strStatus = new StringBuffer();
        if(mPosition == position ) { //Provisioning or Configing
            setEnabled(viewHolder.linearLayout,false);
            viewHolder.status_tv.setText("config...");
            viewHolder.status.setVisibility(View.GONE);
            viewHolder.pro_conf_time.setVisibility(View.GONE);
        }else {
            setEnabled(viewHolder.linearLayout,true);
            viewHolder.status.setVisibility(View.VISIBLE);
            viewHolder.pro_conf_time.setVisibility(View.VISIBLE);
            viewHolder.status_tv.setText("Status: ");
            viewHolder.status.setText(MeshUtils.getActiveStatus(node.getActiveStatus()));
            if (node.getActiveStatus() == 1) {
                viewHolder.status.setTextColor(Color.GREEN);
            } else if (node.getActiveStatus() == 0){
                viewHolder.status.setTextColor(Color.RED);
            } else {
                viewHolder.status.setTextColor(Color.BLACK);
            }
            double mProvisioningTime = node.getProvisioningTime();
            double mConfigTime = node.getConfigTime();
            StringBuffer heartBeat = new StringBuffer("HB Period: 32s   ,   Time: ");
            StringBuffer friendShip = new StringBuffer();
            if(!node.isConfigSuccess()) {
                //log("config fail , uuid = " + MeshUtils.intArrayToString(node.getUUID()) + " , address = " + node.getGattAddr());
                strStatus.setLength(0);
                if (mProvisioningTime > 0) { // provision success
                    strStatus.append("  ,  Prov: ");
                    strStatus.append(mProvisioningTime + "s");
                }
                strStatus.append("   ,   Config fail!");
                viewHolder.pro_conf_time.setText(strStatus.toString());
                viewHolder.heartbeatReceive_ll.setVisibility(View.GONE);
                viewHolder.friendShip_ll.setVisibility(View.GONE);
            }else {
                //log("config success , uuid = " + MeshUtils.intArrayToString(node.getUUID()) + " , address = " + node.getGattAddr());
                strStatus.setLength(0);
                strStatus.append("  ,  Prov: ");
                strStatus.append(mProvisioningTime + "s");
                strStatus.append("   ,   Config: ");
                strStatus.append(mConfigTime + "s");
                viewHolder.pro_conf_time.setText(strStatus.toString());
                if (node.getCmdAddFriDev()){
                    viewHolder.heartbeatReceive_ll.setVisibility(View.GONE);
                } else {
                    viewHolder.heartbeatReceive_ll.setVisibility(View.VISIBLE);
                }

                heartBeat.append(node.getHeartBeatTime()/1000 + "s   ,   HB Num: ");
                heartBeat.append(node.getCurrentHeartBeatNumber()+"  ,  Cont Lost: ");
                heartBeat.append(node.getContinueLost()+ "  ,  Max Lost: ");
                int heartBeatStart = heartBeat.toString().length();
                heartBeat.append(node.getMaxLost());
                int heartBeatEnd = heartBeat.toString().length();

                strStyle = new SpannableStringBuilder(heartBeat.toString());
                if (node.getMaxLost() >= 5) {
                    strStyle.setSpan(new ForegroundColorSpan(Color.RED),heartBeatStart,heartBeatEnd,Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                } else {
                    strStyle.setSpan(new ForegroundColorSpan(Color.BLACK),heartBeatStart,heartBeatEnd,Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                }

                viewHolder.heartbeatReceive_tv.setText(strStyle);

                if (node.getNodeBearer() == MeshConstants.MESH_BEARER_ADV) {
                    viewHolder.friendShip_ll.setVisibility(View.VISIBLE);
                    if (node.getFSStatus() == MeshConstants.MESH_FRIENDSHIP_ESTABLISHED) {
                        friendShip.setLength(0);
                        friendShip.append(" (Cost Time: ");
                        friendShip.append(node.getFSOnCostTime() + " s)");
                        viewHolder.fs_status.setText("On");
                        viewHolder.fs_status.setTextColor(Color.GREEN);
                        viewHolder.fs_time.setText(friendShip.toString());
                    } else if (node.getFSStatus() == MeshConstants.MESH_FRIENDSHIP_ESTABLISH_FAILED) {
                        friendShip.setLength(0);
                        friendShip.append(" (Cost Time: ");
                        friendShip.append(node.getFSOnCostTime() + " s)");
                        viewHolder.fs_status.setText("On Fail");
                        viewHolder.fs_status.setTextColor(Color.BLACK);
                        viewHolder.fs_time.setText(friendShip.toString());
                    } else {
                        viewHolder.fs_status.setText("Off");
                        viewHolder.fs_status.setTextColor(Color.BLACK);
                        viewHolder.fs_time.setText("");
                    }
                    viewHolder.off_num.setText(node.getFSContOff() + "");
                    if (node.getFSContOff() > 0) {
                        viewHolder.off_num.setTextColor(Color.RED);
                    } else {
                        viewHolder.off_num.setTextColor(Color.BLACK);
                    }
                } else {
                    viewHolder.friendShip_ll.setVisibility(View.GONE);
                }
            }
        }
    }


    @Override
    public NodeRecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
        View view = inflater.inflate(R.layout.provisioned_list,viewGroup,false);
        id =  view.getId();
        NodeRecyclerViewHolder viewHoler = new NodeRecyclerViewHolder(view,mListener,mLongClickListener);
        return viewHoler;
    }


}

class NodeRecyclerViewHolder extends ViewHolder implements OnClickListener, OnLongClickListener {

    public TextView textView;
    public TextView tvNodeNum;

    public LinearLayout status_ll;
    public TextView status_tv;
    public TextView status;
    public TextView pro_conf_time;

    public LinearLayout heartbeatReceive_ll;
    public TextView heartbeatReceive_tv;

    public LinearLayout friendShip_ll;
    public TextView fs_status;
    public TextView fs_time;
    public TextView off_num;

    public LinearLayout linearLayout;
    private RecyclerViewItemClickListener mListener;
    private RecyclerViewItemLongClickListener mLongClickListener;

    public NodeRecyclerViewHolder(View itemView, RecyclerViewItemClickListener listener, RecyclerViewItemLongClickListener longClickListener) {
        super(itemView);
        textView = (TextView)itemView.findViewById(R.id.text);
        tvNodeNum = (TextView)itemView.findViewById(R.id.nodeNum);
        status_ll = (LinearLayout)itemView.findViewById(R.id.status_ll);
        status_tv = (TextView)itemView.findViewById(R.id.status_tv);
        status = (TextView)itemView.findViewById(R.id.status);
        pro_conf_time = (TextView)itemView.findViewById(R.id.pro_conf_time);
        heartbeatReceive_ll = (LinearLayout)itemView.findViewById(R.id.heartbeatReceive_ll);
        heartbeatReceive_tv = (TextView)itemView.findViewById(R.id.heartbeatReceive_tv);
        friendShip_ll = (LinearLayout)itemView.findViewById(R.id.friendShip_ll);
        fs_status = (TextView)itemView.findViewById(R.id.fs_status);
        fs_time = (TextView)itemView.findViewById(R.id.fs_time);
        off_num = (TextView)itemView.findViewById(R.id.off_num);
        linearLayout = (LinearLayout)itemView.findViewById(R.id.llprovisioned);
        this.mListener = listener;
        this.mLongClickListener = longClickListener;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View view) {
        if(mLongClickListener != null){
            mLongClickListener.onRecyclerViewItemLongClick(view, getPosition());
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        if(mListener != null){
            mListener.onRecyclerViewItemClick(view, getPosition());
        }
    }

}

