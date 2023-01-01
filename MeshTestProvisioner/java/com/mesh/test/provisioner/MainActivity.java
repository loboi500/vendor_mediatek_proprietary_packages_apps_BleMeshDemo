package com.mesh.test.provisioner;

import com.mediatek.bt.mesh.BluetoothMesh;
import com.mediatek.bt.mesh.*;
import com.mediatek.bt.mesh.model.*;
import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.slideview.SlideSwitch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mesh.test.provisioner.adapter.UnprovisionedAdapter;
import com.mesh.test.provisioner.dialogfragment.ProvisionedDialogFragment;
import com.mesh.test.provisioner.dialogfragment.UnprovisionedDialogFragment;
import com.mesh.test.provisioner.listener.BluetoothMeshListener;
import com.mesh.test.provisioner.listener.NoDoubleOnItemClickListener;
import com.mesh.test.provisioner.model.Element;
import com.mesh.test.provisioner.model.Model;
import com.mesh.test.provisioner.sqlite.LouSQLite;
import com.mesh.test.provisioner.sqlite.MyCallBack;
import com.mesh.test.provisioner.sqlite.Node;
import com.mesh.test.provisioner.sqlite.StorageData;
import com.mesh.test.provisioner.sqlite.NodeData;
import com.mesh.test.provisioner.sqlite.ProvisionedDeviceEntry;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.mesh.test.provisioner.util.MeshUtils;
import com.mesh.test.provisioner.util.ACache;
import android.widget.ProgressBar;
import android.os.Handler;
import android.widget.Button;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.Spinner;
import android.content.DialogInterface;
import com.mesh.test.provisioner.sqlite.StorageData;
import android.content.BroadcastReceiver;
import com.mediatek.bt.mesh.MeshConstants;
import com.mesh.test.provisioner.sqlite.UnProvisionedDevice;
import android.widget.AdapterView;
import java.util.HashMap;
import java.util.Map;
import com.mesh.test.provisioner.adapter.CheckBoxAdapter;
import com.mesh.test.provisioner.adapter.KeyrefreshSpinnerAdapter;
import com.mesh.test.provisioner.adapter.MessageNetIndexAdapter;
import com.mesh.test.provisioner.adapter.SecureBeaconNetIndexAdapter;
import java.util.Iterator;
import java.io.IOException;
import android.content.res.Configuration;
import android.widget.RelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View.OnClickListener;
import com.mesh.test.provisioner.listener.RecyclerViewItemClickListener;
import com.mesh.test.provisioner.listener.RecyclerViewItemLongClickListener;
import com.mesh.test.provisioner.adapter.NodeAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.app.Dialog;
import android.view.Gravity;
import java.util.Timer;
import java.util.TimerTask;
import android.os.SystemProperties;
import android.support.v7.app.ActionBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.mesh.test.provisioner.bean.MeshFilterBean;
import com.mesh.test.provisioner.bean.MeshFilterWithNIDBean;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements SlideSwitch.SlideListener, AdapterView.OnItemClickListener, OnClickListener, RecyclerViewItemClickListener, RecyclerViewItemLongClickListener {

    private static final String TAG = "ProvisionerMainActivity";

    private static final boolean DEBUG = true;

    private SlideSwitch mSlideSwitch;
    private TextView slideText;
    private TextView closeDescription;
    private LinearLayout llState;
    private LinearLayout llOpen;
    private RelativeLayout llClose;
    private CustomRecyclerView nodeRecyclerView;
    private CustomRecyclerView unProvisionedRecyclerView;
    private NodeAdapter nodeAdapter;
    private UnprovisionedAdapter unProvisionedAdapter;
    private ProgressBar mProgressBar;
    private List<StorageData> storageDataList = new ArrayList<>();
    private List<NodeData> nodeDataList = new ArrayList<>();

    //adb group provision
    private ArrayList<int[]> uuidNoneExistent = new ArrayList<>();
    private ArrayList<int[]> uuidProFail = new ArrayList<>();
    private ArrayList<int[]> uuidConfigFail = new ArrayList<>();
    private ArrayList<int[]> uuidConfigSuccess = new ArrayList<>();
    private int uuidIndex = 0;
    private int[] uuid = null;
    private int deviceSize = 0;

    //adb group node reset
    private ArrayList<int[]> nodeResetNoneExistent = new ArrayList<>(); //not exist node list
    private ArrayList<Node> nodeResetList = new ArrayList<>();//need node reset, config success need, config fial not need
    private ArrayList<Node> nodeAllResetList = new ArrayList<>(); //all node reset list
    private HashMap<Integer, Node> ackNodeResetList = new HashMap<>();
    private ArrayList<Node> remainNodeList = new ArrayList<>(); //Total node - all node reset list, used keyrefresh
    private int[] nodeResetUUID = null;
    private int nodeResetSize = 0;

    private static final int ALL_NODE_RESET = 3;
    private static final int PARTIAL_NODE_RESET = 4;
    private static final int SINGLE_NODE_RESET = 5;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_LOCATION = 2;
    private static final int SCAN_DURATION = 20*1000;

    private static final String GENERIC_ONOFF_MSG = "Generic OnOff";
    private static final String LIGHT_LIGHTNESS_MSG = "Light Lightness Actual";
    private static final String LIGHT_LIGHTNESS_RANGE_MSG = "Light Lightness Range";
    private static final String LIGHT_CTL_MSG = "Light CTL Lightness";
    private static final String LIGHT_CTL_TEMP_RANGE_MSG = "Light CTL Temperature Range";
    private static final String LIGHT_HSL_MSG = "Light HSL Lightness";
    private static final String LIGHT_HSL_RANGE_MSG = "Light HSL Range";
    private static final String MESH_BROADCAST_PERMISSION = "android.permission.BLUETOOTH_PRIVILEGED";

    private static final String MESH_FILTER = "mesh_filter";
    private static final String MESH_FILTER_WITH_NID = "mesh_filter_with_nid";

    private BluetoothAdapter mBluetoothAdapter;
    private ProvisionerService mProvisionerService;
    //private Handler mHandler = new Handler();
    private Runnable mRunnable;

    private int addappkey_netkeyIndex = 0;
    private int keyfresh_netkeyIndex = 0;
    private int ota_op_appkeyIndex = 0;
    private int ota_op_netkeyIndex = 0;
    private int message_netkeyIndex = 0;
    private int beacon_netkeyIndex = 0;
    private boolean isOpenMeshSwitch = false;
    private boolean isOpen = false;
    private boolean isScanning = false;
    private boolean meshMode = false;
    private boolean enableFiltPbAdvStatus = true;
    private boolean enableFiltMeshMsgStatus = true;
    private boolean enableFiltUnprovBeaconStatus = true;
    private boolean enableFiltSecureBeaconStatus = true;
    private boolean enableFiltPbAdvWithNIDStatus = true;
    private boolean enableFiltMeshMsgWithNIDStatus = true;
    private boolean enableFiltUnprovBeaconWithNIDStatus = true;
    private boolean enableFiltSecureBeaconWithNIDStatus = true;
    private BluetoothMeshBroadCast mBluetoothMeshBroadCast;
    private IntentFilter statusFilter;
    private Intent serviceIntent;
    private List<Node> nodes = new ArrayList<>();
    private boolean isForeground = false;
    private ProvisionedDialogFragment mProvisionedDialogFragment;
    private LinearLayoutManager mNodeLayoutManager;
    private LinearLayoutManager mUnProvisionedLayoutManager;
    private int nodePosition;
    private Dialog dialogKeyrefresh;
    private Dialog dialogNodeReset;
    private Dialog dialogGroupSendMsg;
    private Dialog dialogGattConnect;
    private Dialog dialogNodeInfo;
    private Timer groupNodeResetTimer;
    private ACache mACache;
    private MeshFilterBean mMeshFilterBean = null;
    private MeshFilterWithNIDBean mMeshFilterWithNIDBean = null;

    private static final int MESH_STATE_IDLE = 0;
    private static final int MESH_STATE_PROVISIONING = 1;
    private static final int MESH_STATE_CONFIGING = 2;
    private static final int MESH_STATE_SENDMSGING = 3;
    private static final int MESH_STATE_NODERESETING = 4;
    private static final int MESH_STATE_KEYREFRESHING = 5;
    private static final int MESH_STATE_GATTCONNECTING = 6;

    private static final int MESH_PB_GATT = 7;
    private static final int MESH_PB_ADV = 8;
    private static final int MESH_FRIENDSHIP = 9;

    private int meshCurrentState = MESH_STATE_IDLE;

    private void log(String string) {
        if(DEBUG) {
            Log.i(TAG, string);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        log("onCreate");

        //Check Location permission
        //checkLocationPermission(this);

        // for backward compatible with MR1.1+GATT & MR2
        // it should load the static variable dynamically
        String strFeature = null;
        try {
            Class<?> cls = Class.forName("android.content.pm.PackageManager");
            Field field = cls.getField("FEATURE_BLUETOOTH_LE");
            strFeature = (String) field.get(cls);
        } catch (Exception e) {
            Toast.makeText(this, R.string.load_feature_fail + ":" + e.toString(),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!getPackageManager().hasSystemFeature(strFeature)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter. For API level 18 and above, get a
        // reference to
        // BluetoothAdapter through ProvisionerService.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MyApplication.friship_mode = false;
        setStatus("friendship disabled");

        mACache = ACache.get(this);

        mSlideSwitch = (SlideSwitch) findViewById(R.id.slideSwitch);
        slideText = (TextView) findViewById(R.id.switchText);
        closeDescription = (TextView) findViewById(R.id.close_description);
        llState = (LinearLayout) findViewById(R.id.llstate);
        llClose = (RelativeLayout) findViewById(R.id.llclose);
        llOpen = (LinearLayout) findViewById(R.id.llopen);
        mProgressBar = (ProgressBar)findViewById(R.id.refresh);
        nodeRecyclerView = (CustomRecyclerView) findViewById(R.id.node);
        nodeRecyclerView.setItemAnimator(null);
        unProvisionedRecyclerView = (CustomRecyclerView) findViewById(R.id.unprovisioned);
        int nodeShowNumber = getResources().getInteger(R.integer.node_show_number);
        log("nodeShowNumber = " + nodeShowNumber);
        mNodeLayoutManager = new LinearLayoutManager(this){
            @Override
            public void onMeasure(Recycler recycler, State state,
                    int widthSpec, int heightSpec) {
                int count = state.getItemCount();

                if (count > 0) {
                    if(count > nodeShowNumber){
                        count = nodeShowNumber;
                    }
                    int realHeight = 0;
                    int realWidth = 0;
                    for(int i = 0;i < count; i++){
                        View view = recycler.getViewForPosition(0);
                        if (view != null) {
                            measureChild(view, widthSpec, heightSpec);
                            int measuredWidth = View.MeasureSpec.getSize(widthSpec);
                            int measuredHeight = view.getMeasuredHeight();
                            realWidth = realWidth > measuredWidth ? realWidth : measuredWidth;
                            realHeight += measuredHeight;
                        }
                        setMeasuredDimension(realWidth, realHeight);
                    }
                } else {
                    super.onMeasure(recycler, state, widthSpec, heightSpec);
                }
            }
        };
        mUnProvisionedLayoutManager = new LinearLayoutManager(this);

        mNodeLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        nodeRecyclerView.setLayoutManager(mNodeLayoutManager);
        nodeRecyclerView.addItemDecoration(new RecycleViewDivider(this, RecycleViewDivider.HORIZONTAL_LIST));

        mUnProvisionedLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        unProvisionedRecyclerView.setLayoutManager(mUnProvisionedLayoutManager);
        unProvisionedRecyclerView.addItemDecoration(new RecycleViewDivider(this, RecycleViewDivider.HORIZONTAL_LIST));
        llState.setOnClickListener(this);
        nodeRecyclerView.setOnCreateContextMenuListener(this);
        getProvisionedData();
        nodeAdapter = new NodeAdapter(this,MyApplication.nodeList);
        unProvisionedAdapter = new UnprovisionedAdapter(this);
        nodeAdapter.setHasStableIds(true);
        nodeAdapter.setOnItemClickListener(this);
        nodeAdapter.setOnItemLongClickListener(this);
        unProvisionedAdapter.setOnItemClickListener(this);
        nodeRecyclerView.setAdapter(nodeAdapter);
        unProvisionedRecyclerView.setAdapter(unProvisionedAdapter);
        registerForContextMenu(nodeRecyclerView);
        mSlideSwitch.setSlideListener(this);
        setPropertiesFile();
        serviceIntent = new Intent(this, ProvisionerService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            log("startForegroundService");
            startForegroundService(serviceIntent);
        } else {
            log("startService");
            startService(serviceIntent);
        }
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
        getOverflowMenu();
        mBluetoothMeshBroadCast = new BluetoothMeshBroadCast();
        statusFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        statusFilter.addAction("android.action.PB_ADV_SEND_GENERIC_ONOFF_MSG");
        statusFilter.addAction("android.action.PB_GATT_SEND_GENERIC_ONOFF_MSG");
        statusFilter.addAction("android.action.FRIENDSHIP_SEND_GENERIC_ONOFF_MSG");
        statusFilter.addAction("android.action.MESH_GROUP_SEND_GENERIC_ONOFF_MSG");
        statusFilter.addAction("android.action.MESH_ON");
        statusFilter.addAction("android.action.MESH_OFF");
        statusFilter.addAction("android.action.PB_GATT_CONNECT");
        statusFilter.addAction("android.action.PB_GATT_DISCONNECT");
        statusFilter.addAction("android.action.MESH_ADV_PROVISION");
        statusFilter.addAction("android.action.MESH_GATT_PROVISION");
        statusFilter.addAction("android.action.MESH_ADV_REMOVE");
        statusFilter.addAction("android.action.MESH_GATT_REMOVE");
        statusFilter.addAction("android.action.MESH_SCAN");
        statusFilter.addAction("android.action.MESH_DUMP");
        statusFilter.addAction("android.action.MESH_GROUP_PROVISION");
        statusFilter.addAction("android.action.MESH_GROUP_NODE_RESET");
        statusFilter.addAction("android.action.PB_ADV_SEND_LIGHT_LIGHTNESS_MSG");
        statusFilter.addAction("android.action.PB_GATT_SEND_LIGHT_LIGHTNESS_MSG");
        statusFilter.addAction("android.action.MESH_GROUP_SEND_LIGHT_LIGHTNESS_MSG");
        statusFilter.addAction("android.action.PB_ADV_SEND_LIGHT_LIGHTNESS_RANGE_MSG");
        statusFilter.addAction("android.action.PB_GATT_SEND_LIGHT_LIGHTNESS_RANGE_MSG");
        statusFilter.addAction("android.action.MESH_GROUP_SEND_LIGHT_LIGHTNESS_RANGE_MSG");
        statusFilter.addAction("android.action.PB_ADV_SEND_LIGHT_CTL_MSG");
        statusFilter.addAction("android.action.PB_GATT_SEND_LIGHT_CTL_MSG");
        statusFilter.addAction("android.action.MESH_GROUP_SEND_LIGHT_CTL_MSG");
        statusFilter.addAction("android.action.PB_ADV_SEND_LIGHT_CTL_TEMP_RANGE_MSG");
        statusFilter.addAction("android.action.PB_GATT_SEND_LIGHT_CTL_TEMP_RANGE_MSG");
        statusFilter.addAction("android.action.MESH_GROUP_SEND_LIGHT_CTL_TEMP_RANGE_MSG");
        statusFilter.addAction("android.action.PB_ADV_SEND_LIGHT_HSL_MSG");
        statusFilter.addAction("android.action.PB_GATT_SEND_LIGHT_HSL_MSG");
        statusFilter.addAction("android.action.MESH_GROUP_SEND_LIGHT_HSL_MSG");
        statusFilter.addAction("android.action.PB_ADV_SEND_LIGHT_HSL_RANGE_MSG");
        statusFilter.addAction("android.action.PB_GATT_SEND_LIGHT_HSL_RANGE_MSG");
        statusFilter.addAction("android.action.MESH_GROUP_SEND_LIGHT_HSL_RANGE_MSG");
        statusFilter.addAction("android.action.SEND_LIGHT_HSL_SET_MSG");
        statusFilter.addAction("android.action.ADD_NETKEY");
        statusFilter.addAction("android.action.ADD_APPKEY");
        statusFilter.addAction("android.action.PB_ADV_FRIENDSHIP_INFO");
        statusFilter.addAction("android.action.ALL_PB_ADV_FRIENDSHIP_INFO");
        statusFilter.addAction("android.action.ADD_FRIENDSHIP_DEVICE");
        statusFilter.addAction("android.action.ADD_GROUP_FRIENDSHIP_DEVICE");
        statusFilter.addAction("android.action.GET_HEARTBEAT_STATUS");
        registerReceiver(mBluetoothMeshBroadCast, statusFilter, MESH_BROADCAST_PERMISSION, null);
        //setPropertiesFile();
        if(savedInstanceState != null) {
            log("savedInstanceState != null");
            isOpen = savedInstanceState.getBoolean("isOpen");
        }

    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mProvisionerService = ((ProvisionerService.LocalBinder) service).getService();
            log("mProvisionerService = " + mProvisionerService + " MeshDev APP is running = " + !(mProvisionerService.isMeshAvailable()));
            if (false == mProvisionerService.isMeshAvailable()) {
                mSlideSwitch.setSlideable(false);
                log("MeshDev APP is running, please close MeshDev");
                Toast.makeText(MainActivity.this, "MeshDev APP is running, please close MeshDev", Toast.LENGTH_SHORT).show();
                finish();
            }
            checkLocationPermission(MainActivity.this);
            mProvisionerService.setBluetoothMeshListener(mBluetoothMeshListener);
            if(isOpen) {
                mSlideSwitch.setSwitchState(false);
                isOpen = false;
            }
            log("storageDataList.size() = " + storageDataList.size());
            if(storageDataList.size() > 0) {
                StorageData mStorageData = storageDataList.get(0);
                mProvisionerService.dataRecovery(mStorageData, MyApplication.nodeList);
            } else {    //fresh start, clear provisioner service data
                log("fresh start, clear provisioner service data , mProvisionerService.isMeshAvailable() = " + mProvisionerService.isMeshAvailable());
                if (true == mProvisionerService.isMeshAvailable()) {
                    mProvisionerService.dataReset();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mProvisionerService = null;

        }
    };

    public void setPropertiesFile(){
        String filePath = "/data/data/" + this.getPackageName() + "/mesh_properties.txt";
        String content = null;
        try {
            content = MeshUtils.readFileFromAssets(this,"mesh_properties.txt");
            if(content == null) {
                log("mesh_properties.txt parse fail");
                return;
            }
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(! MeshUtils.isFileExisted(filePath)){
            MeshUtils.save(filePath, content);
        }
        int nodeSize = MyApplication.nodeList.size();
        int elementOrNodeAddress = 0;
        if (nodeSize > 0) {
            //Node node = MyApplication.nodeList.get(nodeSize - 1); // last node
            //int elementSize = node.getElements().size();
            //if (elementSize > 0) {
                //Element element = node.getElements().get(elementSize - 1); //last element
                //elementOrNodeAddress =  element.getAddress();
            //} else {
                //elementOrNodeAddress = node.getAddr();
            //}
            for (int i = nodeSize - 1; i >=0; i--) {
                Node node = MyApplication.nodeList.get(i);
                if (node.getCmdAddFriDev()) {
                    continue;
                } else { // last no LPN node
                    int elementSize = node.getElements().size();
                    if (elementSize > 0) {
                        Element element = node.getElements().get(elementSize - 1); //last element
                        elementOrNodeAddress =  element.getAddress();
                    } else {
                        elementOrNodeAddress = node.getAddr();
                    }
                    break;
                }
            }
        }
        log("elementOrNodeAddress = " + elementOrNodeAddress);
        String last_element_addr = SystemProperties.get("persist.bluetooth.last.element.addr", "NoExist");
        log("last_element_addr = " + last_element_addr);
        if (last_element_addr.equals("NoExist")) {
            MeshUtils.writeProperties(filePath, "last_element_addr", ((elementOrNodeAddress == 0)? "100" : (elementOrNodeAddress + "")));
        }
    }


    public void getProvisionedData() {
        log("getProvisionedData()");
        storageDataList = LouSQLite.query(MyCallBack.TABLE_NAME_STORAGE
                , "select * from " + MyCallBack.TABLE_NAME_STORAGE
                , null);
        nodeDataList = LouSQLite.query(MyCallBack.TABLE_NAME_NODE
                , "select * from " + MyCallBack.TABLE_NAME_NODE
                , null);
        log("storageDataList size = " + storageDataList.size());
        log("nodeDataList size = " + nodeDataList.size());
        if(storageDataList.size() == 0) {
            StorageData data = new StorageData();
            data.setId(ProvisionedDeviceEntry.STORAGE_ID);
            LouSQLite.insert(MyCallBack.TABLE_NAME_STORAGE, data);
        } else {
            ArrayList<NetKey> mAllNetKey = storageDataList.get(0).getAllNetKey();
            if(mAllNetKey != null) {
                if (mAllNetKey.size() > 0) {
                    for (int i = 0; i <= mAllNetKey.size() -1; i++) {
                        NetKey key = mAllNetKey.get(i);
                        log("key.getState() = " + key.getState());
                        if (key.getState() != MeshConstants.MESH_KEY_REFRESH_STATE_NONE) {
                            log("net key state is not MESH_KEY_REFRESH_STATE_NONE , need delete sqlite data");
                            //LouSQLite.delete(MyCallBack.TABLE_NAME_STORAGE, ProvisionedDeviceEntry.COLEUM_NAME_ID + "=?", new String[]{storageDataList.get(0).getId()});
                            LouSQLite.deleteFrom(MyCallBack.TABLE_NAME_STORAGE);
                            LouSQLite.deleteFrom(MyCallBack.TABLE_NAME_NODE);
                            storageDataList = LouSQLite.query(MyCallBack.TABLE_NAME_STORAGE
                                    , "select * from " + MyCallBack.TABLE_NAME_STORAGE
                                    , null);
                            nodeDataList = LouSQLite.query(MyCallBack.TABLE_NAME_NODE
                                    , "select * from " + MyCallBack.TABLE_NAME_NODE
                                    , null);
                            log("storageDataList size = " + storageDataList.size());
                            log("nodeDataList size = " + nodeDataList.size());
                            if(storageDataList.size() == 0 && nodeDataList.size() == 0) {
                                log("delete data success");
                                StorageData data = new StorageData();
                                data.setId(ProvisionedDeviceEntry.STORAGE_ID);
                                LouSQLite.insert(MyCallBack.TABLE_NAME_STORAGE, data);
                                return;
                            }else {
                                log("delete data fail");
                            }
                        }
                    }
                } else {
                    log("mAllNetKey.size() = 0");
                }
            }
            MyApplication.nodeList.clear();
            for (int i = 0; i < nodeDataList.size(); i++) {
                MyApplication.nodeList.add(nodeDataList.get(i).getNode());
            }
            for (int i = 0; i < MyApplication.nodeList.size(); i++) {
                Node node = MyApplication.nodeList.get(i);
                node.setActiveStatus(2);    //always set to unknown by default,, it shall be updated by heartbeat event
                node.setCurrentHeartBeatNumber(0);
                node.setPreHeartBeatNumber(0);
                node.setContinueLost(0);
                node.setMaxLost(0);
                node.setHeartBeatTime(0);
                node.setHeartBeatTimerNumber(0);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        log("onResume");
        isForeground = true;
        if (mProvisionerService != null) {
            if (false == mProvisionerService.isMeshAvailable()) {
                mSlideSwitch.setSlideable(false);
                Toast.makeText(MainActivity.this, "MeshDev APP is running, please kill it first", Toast.LENGTH_SHORT).show();
                return;
            }
            mSlideSwitch.setSlideable(true);
        }
        //Check if Bluetooth is turned on. If it is not open, pop up a prompt box and request to open Bluetooth.
        promptBtDialogIfNeeded();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        log("onPause");
        isForeground = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        log("onSaveInstanceState");
        outState.putBoolean("isOpen",isOpenMeshSwitch);
    }


    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        log("onStop");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        log("onConfigurationChanged");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log("onDestroy");
        if(null != mProvisionerService && mProvisionerService.isEnable()){
            mProvisionerService.setBluetoothMeshEnabled(false);
            mProvisionerService.setBluetoothMeshListener(null);
        }
        unbindService(mServiceConnection);
        stopService(serviceIntent);
        unregisterReceiver(mBluetoothMeshBroadCast);
        mProvisionerService = null;
        if (groupNodeResetTimer != null) {
            groupNodeResetTimer.cancel();
            groupNodeResetTimer = null;
        }
        //if(null != mHandler){
            //mHandler.removeCallbacksAndMessages(null);
            //mHandler = null;
        //}
    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        menu.findItem(R.id.addNetkey).setEnabled(isOpenMeshSwitch);
        menu.findItem(R.id.addAppkey).setEnabled(isOpenMeshSwitch);
        menu.findItem(R.id.enableFilter).setEnabled(isOpenMeshSwitch);
        menu.findItem(R.id.enableFilterWithNID).setEnabled(isOpenMeshSwitch);
        menu.findItem(R.id.currentFilterStatus).setEnabled(isOpenMeshSwitch);
        menu.findItem(R.id.disableFilter).setEnabled(isOpenMeshSwitch);
        menu.findItem(R.id.keyRefresh).setEnabled(isOpenMeshSwitch);
        menu.findItem(R.id.startScan).setEnabled(isOpenMeshSwitch && !isScanning && (unProvisionedAdapter.getPosition() < 0 && nodeAdapter.getPosition() < 0));
        menu.findItem(R.id.stopScan).setEnabled(isOpenMeshSwitch && isScanning);
        menu.findItem(R.id.meshMode).setEnabled(isOpenMeshSwitch);
        menu.findItem(R.id.meshMode_on).setEnabled(isOpenMeshSwitch && (mProvisionerService.getMeshMode() != MeshConstants.MESH_MODE_ON));
        menu.findItem(R.id.meshMode_off).setEnabled(isOpenMeshSwitch && (mProvisionerService.getMeshMode() != MeshConstants.MESH_MODE_OFF));
        menu.findItem(R.id.meshMode_standby).setEnabled(isOpenMeshSwitch && (mProvisionerService.getMeshMode() != MeshConstants.MESH_MODE_STANDBY));
        menu.findItem(R.id.nodeInfo).setEnabled(isOpenMeshSwitch);
        menu.findItem(R.id.ota_op).setEnabled(isOpenMeshSwitch);
        menu.findItem(R.id.friendship).setChecked(MyApplication.friship_mode);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.addNetkey:
            int result = mProvisionerService.addNewNetKey();
            break;
        case R.id.addAppkey:
            showAddAppKeyDialog();
            break;
        case R.id.enableFilter:
            showEnableMeshFilterDialog();
            break;
        case R.id.enableFilterWithNID:
            showEnableMeshFilterWithNIDDialog();
            break;
        case R.id.currentFilterStatus:
            mMeshFilterBean = (MeshFilterBean)mACache.getAsObject(MESH_FILTER);
            if (mMeshFilterBean != null) {
                String str = "enableMeshFilter "
                    + "\n pbAdv = " + mMeshFilterBean.getPbAdvStatus()
                    + "\n meshMessage = " + mMeshFilterBean.getMeshMsgStatus()
                    + "\n unprovBeacon = " + mMeshFilterBean.getUnprovBeaconStatus()
                    + "\n secureBeacon = " + mMeshFilterBean.getSecureBeaconStatus();
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();
            }
            mMeshFilterWithNIDBean = (MeshFilterWithNIDBean)mACache.getAsObject(MESH_FILTER_WITH_NID);
            if (mMeshFilterWithNIDBean != null) {
                String str = "enableMeshFilterWithNID"
                    + "\n pbAdv = " + mMeshFilterWithNIDBean.getPbAdvStatus()
                    + "\n meshMessageWithNID = " + mMeshFilterWithNIDBean.getMeshMsgStatus()
                    + "\n meshMessageNetIndex = " + Arrays.toString(mMeshFilterWithNIDBean.getMeshMessageNetIndex())
                    + "\n unprovBeacon = " + mMeshFilterWithNIDBean.getUnprovBeaconStatus()
                    + "\n secureBeaconWithNetworkID = " + mMeshFilterWithNIDBean.getSecureBeaconStatus()
                    + "\n secureBeaconNetIndex = " + Arrays.toString(mMeshFilterWithNIDBean.getSecureBeaconNetIndex());
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();
            }
            if (mMeshFilterBean == null && mMeshFilterWithNIDBean == null) {
                Toast.makeText(MainActivity.this, "current not set mesh filter", Toast.LENGTH_LONG).show();
            }
            break;
        case R.id.disableFilter:
            if (mProvisionerService != null) {
                if (mProvisionerService.disableMeshFilter() == 0) {
                    mACache.remove(MESH_FILTER);
                    mACache.remove(MESH_FILTER_WITH_NID);
                    enableFiltPbAdvStatus = true;
                    enableFiltMeshMsgStatus = true;
                    enableFiltUnprovBeaconStatus = true;
                    enableFiltSecureBeaconStatus = true;
                    enableFiltPbAdvWithNIDStatus = true;
                    enableFiltMeshMsgWithNIDStatus = true;
                    enableFiltUnprovBeaconWithNIDStatus = true;
                    enableFiltSecureBeaconWithNIDStatus = true;
                    Toast.makeText(MainActivity.this,"disable mesh filter success",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this,"disable mesh filter fail",Toast.LENGTH_LONG).show();
                }
            }
            break;
        case R.id.keyRefresh:
            showKeyRefreshDialog();
            break;
        case R.id.startScan:
            unProvisionedAdapter.clear();
            mProgressBar.setVisibility(View.VISIBLE);
            mProvisionerService.startUnProvsionScan();
            isScanning = true;
            break;
        case R.id.stopScan:
            mProgressBar.setVisibility(View.GONE);
            mProvisionerService.stopUnProvsionScan();
            isScanning = false;
            break;
        case R.id.meshMode_on:
            if(mProvisionerService.setMeshMode(MeshConstants.MESH_MODE_ON)) {
                Toast.makeText(this, "set mesh mode on success", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "set mesh mode on fail", Toast.LENGTH_SHORT).show();
            }
            break;
        case R.id.meshMode_off:
            if(mProvisionerService.setMeshMode(MeshConstants.MESH_MODE_OFF)) {
                Toast.makeText(this, "set mesh mode off success", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "set mesh mode off fail", Toast.LENGTH_SHORT).show();
            }
            break;
        case R.id.meshMode_standby:
            if(mProvisionerService.setMeshMode(MeshConstants.MESH_MODE_STANDBY)) {
                Toast.makeText(this, "set mesh mode standby success", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "set mesh mode standby fail", Toast.LENGTH_SHORT).show();
            }
            break;
        case R.id.home:
            moveTaskToBack(true);
            break;
        case R.id.nodeInfo:
            showNodeInfo();
            break;
        case R.id.ota_op_start:
            showOtaOpStartDialog();
            break;
        case R.id.ota_op_stop:
            mProvisionerService.otaStop();
            break;
        case R.id.ota_op_apply:
            mProvisionerService.otaApply();
            break;
        case R.id.friendship:
            MyApplication.friship_mode = !MyApplication.friship_mode;
            log("onOptionsItemSelected, friendship, isOpenMeshSwitch: " + isOpenMeshSwitch + " , friship_mode: " + MyApplication.friship_mode);
            if (isOpenMeshSwitch) {
                mProvisionerService.setBluetoothMeshEnabled(false);
                unProvisionedAdapter.setPosition(UnprovisionedAdapter.ALL_ENABLE,true);
                nodeAdapter.setPosition(NodeAdapter.ALL_ENABLE);
                unProvisionedAdapter.clear();
                mProvisionerService.setBluetoothMeshEnabled(true);
            }
            if (MyApplication.friship_mode) {
                setStatus("friendship enabled");
            } else {
                setStatus("friendship disabeld");
            }
            break;
        default:
            break;
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        log("onCreateContextMenu ");
        if(unProvisionedAdapter.getPosition() < 0 && nodeAdapter.getPosition() < 0) {
            Node node = (MyApplication.nodeList).get(nodePosition);
            if (node.getCmdAddFriDev()) {
                menu.add(0,0,0,"Node Reset").setEnabled(false);
                menu.add(0,1,0,"All Node Reset").setEnabled(false);
                menu.add(0,2,0,"Remove").setEnabled(false);
                menu.add(0,3,0,"Only Remove");
            } else {
                menu.add(0,0,0,"Node Reset");
                menu.add(0,1,0,"All Node Reset");
                menu.add(0,2,0,"Remove");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        log("onContextItemSelected ---> select position = " + nodePosition + " , item id = " + item.getItemId());
        switch (item.getItemId()) {
            case 0: // Node Reset
                adbGroupNodeReset(SINGLE_NODE_RESET);
                break;
            case 1: // All Node Reset
                adbGroupNodeReset(ALL_NODE_RESET);
                break;
            case 2: // Remove
                dialogKeyrefresh = new Dialog(MainActivity.this, R.style.Custom_Progress);
                dialogKeyrefresh.setContentView(R.layout.mul_picture_progressbar);
                dialogKeyrefresh.getWindow().getAttributes().gravity = Gravity.CENTER;
                dialogKeyrefresh.setCanceledOnTouchOutside(false);
                dialogKeyrefresh.setCancelable(false);
                dialogKeyrefresh.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        log("onContextItemSelected ---> remove keyrefresh dialog dismiss");
                        meshCurrentState = MESH_STATE_IDLE;
                    }

                });
                dialogKeyrefresh.show();
                meshCurrentState = MESH_STATE_KEYREFRESHING;
                Node nodeRemove = (MyApplication.nodeList).get(nodePosition);
                mProvisionerService.removeNode(nodeRemove, false);
                nodeAdapter.removeNode(nodeRemove);
                break;
            case 3: //only remove from UI
                nodeRemove = (MyApplication.nodeList).get(nodePosition);
                mProvisionerService.updateNodeData(new NodeData(nodeRemove), MyApplication.NODE_DATA_DELETE);
                mProvisionerService.getNetKeyMap().get(nodeRemove.getCurrNetkeyIndex()).getNodes().remove(nodeRemove.getAddr());
                mProvisionerService.getNodes().remove(nodeRemove.getAddr());
                mProvisionerService.updateStorageData(MyApplication.STORAGE_DATA_ALL);
                nodeAdapter.removeNode(nodeRemove);
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);

    }

    private void showEnableMeshFilterDialog() {
        log("showEnableMeshFilterDialog");
        ArrayList<String> msgNetkeyIndexDatas = new ArrayList<String>();
        ArrayList<String> beconnNetkeyIndexDatas = new ArrayList<String>();
        for(int i = 0;i < mProvisionerService.getNetKeyCnt(); i++){
            msgNetkeyIndexDatas.add(i + "");
            beconnNetkeyIndexDatas.add(i + "");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.enable_mesh_filter, null);
        builder.setView(dialoglayout);
        builder.setCancelable(false);

        RadioGroup pbAdvRG = (RadioGroup) dialoglayout.findViewById(R.id.pbAdvRG);
        final RadioButton pbAdvOffRB = (RadioButton)dialoglayout.findViewById(R.id.pbAdvOffRB);
        final RadioButton pbAdvOnRB = (RadioButton)dialoglayout.findViewById(R.id.pbAdvOnRB);
        pbAdvRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == pbAdvOffRB.getId()) {
                    enableFiltPbAdvStatus = false;
                }else if(checkedId == pbAdvOnRB.getId()){
                    enableFiltPbAdvStatus = true;
                }
            }
        });

        RadioGroup meshMessageRG = (RadioGroup) dialoglayout.findViewById(R.id.meshMessageRG);
        final RadioButton meshMessageOffRB = (RadioButton)dialoglayout.findViewById(R.id.meshMessageOffRB);
        final RadioButton meshMessageOnRB = (RadioButton)dialoglayout.findViewById(R.id.meshMessageOnRB);
        meshMessageRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == meshMessageOffRB.getId()) {
                    enableFiltMeshMsgStatus = false;
                }else if(checkedId == meshMessageOnRB.getId()){
                    enableFiltMeshMsgStatus = true;
                }
            }
        });


        RadioGroup unprovBeaconRG = (RadioGroup) dialoglayout.findViewById(R.id.unprovBeaconRG);
        final RadioButton unprovBeaconOffRB = (RadioButton)dialoglayout.findViewById(R.id.unprovBeaconOffRB);
        final RadioButton unprovBeaconOnRB = (RadioButton)dialoglayout.findViewById(R.id.unprovBeaconOnRB);
        unprovBeaconRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == unprovBeaconOffRB.getId()) {
                    enableFiltUnprovBeaconStatus = false;
                }else if(checkedId == unprovBeaconOnRB.getId()){
                    enableFiltUnprovBeaconStatus = true;
                }
            }
        });


        RadioGroup secureBeaconRG = (RadioGroup) dialoglayout.findViewById(R.id.secureBeaconRG);
        final RadioButton secureBeaconOffRB = (RadioButton)dialoglayout.findViewById(R.id.secureBeaconOffRB);
        final RadioButton secureBeaconOnRB = (RadioButton)dialoglayout.findViewById(R.id.secureBeaconOnRB);
        secureBeaconRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == secureBeaconOffRB.getId()) {
                    enableFiltSecureBeaconStatus = false;
                }else if(checkedId == secureBeaconOnRB.getId()){
                    enableFiltSecureBeaconStatus = true;
                }
            }
        });

        mMeshFilterBean = (MeshFilterBean)mACache.getAsObject(MESH_FILTER);
        if (mMeshFilterBean == null) {
            mMeshFilterBean = new MeshFilterBean();
        } else {
            if (mMeshFilterBean.getPbAdvStatus()) {
                pbAdvRG.check(pbAdvOnRB.getId());
            } else {
                pbAdvRG.check(pbAdvOffRB.getId());
            }
            if (mMeshFilterBean.getMeshMsgStatus()) {
                meshMessageRG.check(meshMessageOnRB.getId());
            } else {
                meshMessageRG.check(meshMessageOffRB.getId());
            }
            if (mMeshFilterBean.getUnprovBeaconStatus()) {
                unprovBeaconRG.check(unprovBeaconOnRB.getId());
            } else {
                unprovBeaconRG.check(unprovBeaconOffRB.getId());
            }
            if (mMeshFilterBean.getSecureBeaconStatus()) {
                secureBeaconRG.check(secureBeaconOnRB.getId());
            } else {
                secureBeaconRG.check(secureBeaconOffRB.getId());
            }
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                mMeshFilterBean.setPbAdvStatus(enableFiltPbAdvStatus);
                mMeshFilterBean.setMeshMsgStatus(enableFiltMeshMsgStatus);
                mMeshFilterBean.setUnprovBeaconStatus(enableFiltUnprovBeaconStatus);
                mMeshFilterBean.setSecureBeaconStatus(enableFiltSecureBeaconStatus);
                mACache.put(MESH_FILTER, mMeshFilterBean);

                if (mACache.getAsObject(MESH_FILTER_WITH_NID) != null){
                    mACache.remove(MESH_FILTER_WITH_NID);
                }

                if (mProvisionerService != null) {
                    int status = mProvisionerService.enableMeshFilter(enableFiltPbAdvStatus, enableFiltMeshMsgStatus,
                        enableFiltUnprovBeaconStatus, enableFiltSecureBeaconStatus);
                    switch (status) {
                        case 0:
                            Toast.makeText(MainActivity.this,"enable mesh filter success",Toast.LENGTH_LONG).show();
                            log("enable Mesh Filter success");
                            break;
                        case -2:
                            Toast.makeText(MainActivity.this,"enable mesh filter fail, network number exceed 4",Toast.LENGTH_LONG).show();
                            log("enable Mesh Filter fail, network number exceed 4");
                            break;
                        case -3:
                            Toast.makeText(MainActivity.this,"enable mesh filter fail, cannot find netkey by netIndex",Toast.LENGTH_LONG).show();
                            log("enable Mesh Filter fail, cannot find netkey by netIndex");

                            break;
                        default:
                            Toast.makeText(MainActivity.this,"enable mesh filterr fail, unknow reason",Toast.LENGTH_LONG).show();
                            log("enable Mesh Filter fail, unknow reason");
                            break;
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.create().show();

    }

    private void showEnableMeshFilterWithNIDDialog() {
        log("showEnableMeshFilterWithNIDDialog");
        ArrayList<String> msgNetkeyIndexDatas = new ArrayList<String>();
        ArrayList<String> beconnNetkeyIndexDatas = new ArrayList<String>();
        for(int i = 0;i < mProvisionerService.getNetKeyCnt(); i++){
            msgNetkeyIndexDatas.add(i + "");
            beconnNetkeyIndexDatas.add(i + "");
        }

        final MessageNetIndexAdapter msgNetIndexAdapter = new MessageNetIndexAdapter(MyApplication.getApplication(), msgNetkeyIndexDatas);
        final SecureBeaconNetIndexAdapter beconnNetIndexAdapter = new SecureBeaconNetIndexAdapter(MyApplication.getApplication(), beconnNetkeyIndexDatas);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.enable_mesh_filter_with_nid, null);
        builder.setView(dialoglayout);
        builder.setCancelable(false);
        final ListView meshMessageListview = (ListView) dialoglayout.findViewById(R.id.meshMessageList);
        final ListView secureBeaconListview = (ListView) dialoglayout.findViewById(R.id.secureBeaconList);

        RadioGroup pbAdvRG = (RadioGroup) dialoglayout.findViewById(R.id.pbAdvRG);
        final RadioButton pbAdvOffRB = (RadioButton)dialoglayout.findViewById(R.id.pbAdvOffRB);
        final RadioButton pbAdvOnRB = (RadioButton)dialoglayout.findViewById(R.id.pbAdvOnRB);
        pbAdvRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == pbAdvOffRB.getId()) {
                    enableFiltPbAdvWithNIDStatus = false;
                }else if(checkedId == pbAdvOnRB.getId()){
                    enableFiltPbAdvWithNIDStatus = true;
                }
            }
        });

        RadioGroup meshMessageRG = (RadioGroup) dialoglayout.findViewById(R.id.meshMessageRG);
        final RadioButton meshMessageOffRB = (RadioButton)dialoglayout.findViewById(R.id.meshMessageOffRB);
        final RadioButton meshMessageOnRB = (RadioButton)dialoglayout.findViewById(R.id.meshMessageOnRB);
        meshMessageRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == meshMessageOffRB.getId()) {
                    enableFiltMeshMsgWithNIDStatus = false;
                    meshMessageListview.setVisibility(View.GONE);
                }else if(checkedId == meshMessageOnRB.getId()){
                    enableFiltMeshMsgWithNIDStatus = true;
                    meshMessageListview.setVisibility(View.VISIBLE);
                }
            }
        });


        RadioGroup unprovBeaconRG = (RadioGroup) dialoglayout.findViewById(R.id.unprovBeaconRG);
        final RadioButton unprovBeaconOffRB = (RadioButton)dialoglayout.findViewById(R.id.unprovBeaconOffRB);
        final RadioButton unprovBeaconOnRB = (RadioButton)dialoglayout.findViewById(R.id.unprovBeaconOnRB);
        unprovBeaconRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == unprovBeaconOffRB.getId()) {
                    enableFiltUnprovBeaconWithNIDStatus = false;
                }else if(checkedId == unprovBeaconOnRB.getId()){
                    enableFiltUnprovBeaconWithNIDStatus = true;
                }
            }
        });


        RadioGroup secureBeaconRG = (RadioGroup) dialoglayout.findViewById(R.id.secureBeaconRG);
        final RadioButton secureBeaconOffRB = (RadioButton)dialoglayout.findViewById(R.id.secureBeaconOffRB);
        final RadioButton secureBeaconOnRB = (RadioButton)dialoglayout.findViewById(R.id.secureBeaconOnRB);
        secureBeaconRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == secureBeaconOffRB.getId()) {
                    enableFiltSecureBeaconWithNIDStatus = false;
                    secureBeaconListview.setVisibility(View.GONE);
                }else if(checkedId == secureBeaconOnRB.getId()){
                    enableFiltSecureBeaconWithNIDStatus = true;
                    secureBeaconListview.setVisibility(View.VISIBLE);
                }
            }
        });

        mMeshFilterWithNIDBean = (MeshFilterWithNIDBean)mACache.getAsObject(MESH_FILTER_WITH_NID);
        if (mMeshFilterWithNIDBean == null) {
            mMeshFilterWithNIDBean = new MeshFilterWithNIDBean();
        } else {
            if (mMeshFilterWithNIDBean.getPbAdvStatus()) {
                pbAdvRG.check(pbAdvOnRB.getId());
            } else {
                pbAdvRG.check(pbAdvOffRB.getId());
            }
            if (mMeshFilterWithNIDBean.getMeshMsgStatus()) {
                meshMessageRG.check(meshMessageOnRB.getId());
            } else {
                meshMessageRG.check(meshMessageOffRB.getId());
            }
            if (mMeshFilterWithNIDBean.getUnprovBeaconStatus()) {
                unprovBeaconRG.check(unprovBeaconOnRB.getId());
            } else {
                unprovBeaconRG.check(unprovBeaconOffRB.getId());
            }
            if (mMeshFilterWithNIDBean.getSecureBeaconStatus()) {
                secureBeaconRG.check(secureBeaconOnRB.getId());
            } else {
                secureBeaconRG.check(secureBeaconOffRB.getId());
            }
            if (mMeshFilterWithNIDBean.getMeshMessageNetIndex() != null) {
                for (int i = 0; i < mMeshFilterWithNIDBean.getMeshMessageNetIndex().length; i++) {
                    msgNetIndexAdapter.state.put((mMeshFilterWithNIDBean.getMeshMessageNetIndex())[i], true);
                }
            }
            if (mMeshFilterWithNIDBean.getSecureBeaconNetIndex() != null) {
                for (int i = 0; i < mMeshFilterWithNIDBean.getSecureBeaconNetIndex().length; i++) {
                    beconnNetIndexAdapter.state.put((mMeshFilterWithNIDBean.getSecureBeaconNetIndex())[i], true);
                }
            }

        }

        meshMessageListview.setDivider(null);
        meshMessageListview.setAdapter(msgNetIndexAdapter);

        secureBeaconListview.setDivider(null);
        secureBeaconListview.setAdapter(beconnNetIndexAdapter);


        meshMessageListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                MessageNetIndexAdapter.ViewHolder viewHolder = (MessageNetIndexAdapter.ViewHolder) view.getTag();
                viewHolder.checkBox.toggle();
                MessageNetIndexAdapter.state.put(position, viewHolder.checkBox.isChecked());
            }
        });

        secureBeaconListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                SecureBeaconNetIndexAdapter.ViewHolder viewHolder = (SecureBeaconNetIndexAdapter.ViewHolder) view.getTag();
                viewHolder.checkBox.toggle();
                SecureBeaconNetIndexAdapter.state.put(position, viewHolder.checkBox.isChecked());
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                ArrayList<String> meshMessageNetIndexlist = new ArrayList<>();
                ArrayList<String> secureBeaconNetIndexlist = new ArrayList<>();
                Iterator<Integer> iterator = MessageNetIndexAdapter.state.keySet().iterator();
                while (iterator.hasNext()) {
                    Integer next = iterator.next();
                    Boolean able = MessageNetIndexAdapter.state.get(next);
                    if (able) {
                        meshMessageNetIndexlist.add(msgNetkeyIndexDatas.get(next));
                    }
                }
                iterator = SecureBeaconNetIndexAdapter.state.keySet().iterator();
                while (iterator.hasNext()) {
                    Integer next = iterator.next();
                    Boolean able = SecureBeaconNetIndexAdapter.state.get(next);
                    if (able) {
                        secureBeaconNetIndexlist.add(beconnNetkeyIndexDatas.get(next));
                    }
                }

                log("meshMessageNetIndexlist = " + meshMessageNetIndexlist.toString());
                log("secureBeaconNetIndexlist = " + secureBeaconNetIndexlist.toString());

                int[] meshMessageNetIndex = null;
                int[] secureBeaconNetIndex = null;
                if (meshMessageNetIndexlist.size() > 0) {
                    meshMessageNetIndex = new int[meshMessageNetIndexlist.size()];
                    for (int i = 0; i < meshMessageNetIndexlist.size(); i++) {
                        try {
                            meshMessageNetIndex[i] = Integer.parseInt(meshMessageNetIndexlist.get(i));
                        } catch (Exception e) {
                            log(e.getMessage());
                        }

                    }
                }
                if (secureBeaconNetIndexlist.size() > 0) {
                    secureBeaconNetIndex = new int[secureBeaconNetIndexlist.size()];
                    for (int i = 0; i < secureBeaconNetIndexlist.size(); i++) {
                        try {
                            secureBeaconNetIndex[i] = Integer.parseInt(secureBeaconNetIndexlist.get(i));
                        } catch (Exception e) {
                            log(e.getMessage());
                        }
                    }
                }

                log("meshMessageNetIndex = " + Arrays.toString(meshMessageNetIndex));
                log("secureBeaconNetIndex = " + Arrays.toString(secureBeaconNetIndex));

                mMeshFilterWithNIDBean.setPbAdvStatus(enableFiltPbAdvWithNIDStatus);
                mMeshFilterWithNIDBean.setMeshMsgStatus(enableFiltMeshMsgWithNIDStatus);
                mMeshFilterWithNIDBean.setUnprovBeaconStatus(enableFiltUnprovBeaconWithNIDStatus);
                mMeshFilterWithNIDBean.setSecureBeaconStatus(enableFiltSecureBeaconWithNIDStatus);
                mMeshFilterWithNIDBean.setMeshMessageNetIndex(meshMessageNetIndex);
                mMeshFilterWithNIDBean.setSecureBeaconNetIndex(secureBeaconNetIndex);
                mACache.put(MESH_FILTER_WITH_NID, mMeshFilterWithNIDBean);

                if (mACache.getAsObject(MESH_FILTER) != null){
                    mACache.remove(MESH_FILTER);
                }

                if (mProvisionerService != null) {
                    int status = mProvisionerService.enableMeshFilterWithNID(enableFiltPbAdvWithNIDStatus, enableFiltMeshMsgWithNIDStatus, meshMessageNetIndex,
                        enableFiltUnprovBeaconWithNIDStatus, enableFiltSecureBeaconWithNIDStatus, secureBeaconNetIndex);
                    switch (status) {
                        case 0:
                            Toast.makeText(MainActivity.this,"enable mesh filter with NID success",Toast.LENGTH_LONG).show();
                            log("enable Mesh Filter with NID success");
                            break;
                        case -2:
                            Toast.makeText(MainActivity.this,"enable mesh filter with NID fail, network number exceed 4",Toast.LENGTH_LONG).show();
                            log("enable Mesh Filter with NID fail, network number exceed 4");
                            break;
                        case -3:
                            Toast.makeText(MainActivity.this,"enable mesh filter with NID fail, cannot find netkey by netIndex",Toast.LENGTH_LONG).show();
                            log("enable Mesh Filter with NID fail, cannot find netkey by netIndex");

                            break;
                        default:
                            Toast.makeText(MainActivity.this,"enable mesh filterr with NID fail, unknow reason",Toast.LENGTH_LONG).show();
                            log("enable Mesh Filter with NID fail, unknow reason");
                            break;
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.create().show();

    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subTitle);
    }

    private void showAddAppKeyDialog() {
        log("showAddAppKeyDialog");
        ArrayList<String> netkeyIndexDatas = new ArrayList<String>();
        ArrayList<Integer> netkeyIndexs = mProvisionerService.getNetKeyIndexs();
        for(int i = 0;i < netkeyIndexs.size(); i++){
            netkeyIndexDatas.add(MeshUtils.decimalToHexString("%04X", netkeyIndexs.get(i)));
        }
        ArrayAdapter<String> appkeyAdapter = new ArrayAdapter<String>(MyApplication.getApplication(),android.R.layout.simple_spinner_dropdown_item,netkeyIndexDatas);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.add_app_key, null);
        builder.setView(dialoglayout);
        builder.setCancelable(false);
        Spinner spinner = (Spinner) dialoglayout.findViewById(R.id.netkeyIndexSpinner);
        spinner.setAdapter(appkeyAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                addappkey_netkeyIndex = (int)MeshUtils.hexSrtingToDecimal(netkeyIndexDatas.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                int result = mProvisionerService.addNewAppKey(addappkey_netkeyIndex);
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }


    private void showKeyRefreshDialog() {
        log("showKeyRefreshDialog");
        //ArrayList<Integer> netkeyIndexDatas = new ArrayList<Integer>();
        List<String> netkeyIndexDatas = new ArrayList<String>();
        for(int i = 0;i < mProvisionerService.getNetKeyCnt(); i++){
            netkeyIndexDatas.add(MeshUtils.decimalToHexString("%04X",i));
        }
        HashMap<Integer,Node> mapNodes = mProvisionerService.getNodesByNetKey(keyfresh_netkeyIndex);
        log("mapNodes.size() =" + mapNodes.size());
        nodes.clear();
        for(Map.Entry<Integer,Node> entry:mapNodes.entrySet()) {
            nodes.add(entry.getValue());
        }
        log("nodes.size() =" + nodes.size());
        final CheckBoxAdapter cbAdapter = new CheckBoxAdapter(MyApplication.getApplication(),nodes);
        //ArrayAdapter<String> netkeyAdapter = new ArrayAdapter<String>(MyApplication.getApplication(),android.R.layout.simple_spinner_dropdown_item,netkeyIndexDatas);
        KeyrefreshSpinnerAdapter netkeyAdapter = new KeyrefreshSpinnerAdapter(MyApplication.getApplication(), netkeyIndexDatas);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.key_refresh, null);
        builder.setView(dialoglayout);
        builder.setCancelable(false);
        Spinner spinner = (Spinner) dialoglayout.findViewById(R.id.netkeyIndexSpinner);
        final ListView cbListView = (ListView) dialoglayout.findViewById(R.id.nodelist);
        spinner.setAdapter(netkeyAdapter);
        cbListView.setDivider(null);
        cbListView.setAdapter(cbAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                log("showKeyRefreshDialog onItemSelected");
                keyfresh_netkeyIndex = (int)MeshUtils.hexSrtingToDecimal(netkeyIndexDatas.get(position));
                HashMap<Integer,Node> mapNodes = mProvisionerService.getNodesByNetKey(keyfresh_netkeyIndex);
                log("mapNodes.size() =" + mapNodes.size());
                nodes.clear();
                for(Map.Entry<Integer,Node> entry:mapNodes.entrySet()) {
                    nodes.add(entry.getValue());
                }
                log("nodes.size() =" + nodes.size());
                cbAdapter.nodeChange(nodes);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                log("showKeyRefreshDialog onNothingSelected");

            }
        });
        cbListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                CheckBoxAdapter.ViewHolder viewHolder = (CheckBoxAdapter.ViewHolder) view.getTag();
                viewHolder.checkBox.toggle();
                CheckBoxAdapter.state.put(position, viewHolder.checkBox.isChecked());
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                ArrayList<Node> nodelist = new ArrayList<>();
                Iterator<Integer> iterator = CheckBoxAdapter.state.keySet().iterator();
                while (iterator.hasNext()) {
                    Integer next = iterator.next();
                    Boolean able = CheckBoxAdapter.state.get(next);
                    if (able) {
                        nodelist.add(nodes.get(next));
                    }
                }
                keyrefresh(keyfresh_netkeyIndex, nodelist);
                /*
                log("nodelist.size() = " + nodelist.size());
                dialogKeyrefresh = new Dialog(MainActivity.this, R.style.Custom_Progress);
                dialogKeyrefresh.setContentView(R.layout.mul_picture_progressbar);
                dialogKeyrefresh.getWindow().getAttributes().gravity = Gravity.CENTER;
                dialogKeyrefresh.setCanceledOnTouchOutside(false);
                dialogKeyrefresh.show();
                log("dialogKeyrefresh = " + dialogKeyrefresh);
                if(nodelist.size() > 0){
                    mProvisionerService.keyRefreshStart(keyfresh_netkeyIndex,nodelist);
                } else {
                    mProvisionerService.keyRefreshStart(keyfresh_netkeyIndex, null);
                }
                */
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.create().show();

    }

    private void showNodeInfo(){
        HashMap<Integer, Node> nodeMap = mProvisionerService.getNodes();
        int totalNode = nodeMap.size();
        int gattDevNum = 0;
        int advDevNum = 0;
        int hbOnlineNum = 0;
        int hbOfflineNum = 0;
        int hbUnknownNUm = 0;
        int hbMaxLostOverFive = 0;
        int configSuccessNum = 0;
        int configFailNum = 0;
        int fsTotalNum = 0;
        int fsOnNum = 0;
        int fsOffNum = 0;
        for(Map.Entry<Integer,Node> entry:nodeMap.entrySet()) {
            Node node = entry.getValue();
            if (node.getNodeBearer() == MeshConstants.MESH_BEARER_GATT) {
                gattDevNum++;
            } else if (node.getNodeBearer() == MeshConstants.MESH_BEARER_ADV) {
                advDevNum++;
            }
            if (node.getActiveStatus() == 0) {
                hbOfflineNum++;
            } else if (node.getActiveStatus() == 1) {
                hbOnlineNum++;
            } else {
                hbUnknownNUm++;
            }
            if (node.isConfigSuccess()) {
                configSuccessNum++;
            } else {
                configFailNum++;
            }
            if (node.getMaxLost() >= 5) {
                hbMaxLostOverFive++;
            }
            if (node.getCmdAddFriDev()){
                fsTotalNum++;
                if (node.getFSStatus() == MeshConstants.MESH_FRIENDSHIP_ESTABLISHED) {
                    fsOnNum++;
                } else {
                    fsOffNum++;
                }
            }
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("Node Total :  ").append(totalNode).append("\n");
        buffer.append("        PB-GATT:  ").append(gattDevNum).append("\n");
        buffer.append("        PB-ADV:  ").append(advDevNum).append("\n\n\n");
        buffer.append("Heartbeat Status:  ").append("\n");
        buffer.append("        Online:  ").append(hbOnlineNum).append("\n");
        buffer.append("        Offline:  ").append(hbOfflineNum).append("\n");
        buffer.append("        Unknown:  ").append(hbUnknownNUm).append("\n");
        buffer.append("        Max Lost >= 5:  ").append(hbMaxLostOverFive).append("\n\n\n");
        buffer.append("Config Status:  ").append("\n");
        buffer.append("        Config Success:  ").append(configSuccessNum).append("\n");
        buffer.append("        Config Fail:  ").append(configFailNum).append("\n\n\n");
        buffer.append("FriendShip Status:  ").append("\n");
        buffer.append("        FriendShip Total:  ").append(fsTotalNum).append("\n");
        buffer.append("        FriendShip On:  ").append(fsOnNum).append("\n");
        buffer.append("        FriendShip Off:  ").append(fsOffNum);
        dialogNodeInfo = new Dialog(MainActivity.this, R.style.Custom_Progress);
        dialogNodeInfo.setContentView(R.layout.node_info);
        TextView tvTip = (TextView) dialogNodeInfo.findViewById(R.id.tipTextView);
        tvTip.setText(buffer.toString());
        dialogNodeInfo.getWindow().getAttributes().gravity = Gravity.CENTER;
        dialogNodeInfo.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                dialogNodeInfo = null;
            }

        });
        dialogNodeInfo.show();
        //Toast.makeText(MainActivity.this, buffer.toString(), Toast.LENGTH_SHORT).show();
        log(buffer.toString());
    }

    private void showOtaOpStartDialog() {
        log("showOtaOpStartDialog");
        ArrayList<Integer> netkeyIndexDatas = new ArrayList<Integer>();
        ArrayList<Integer> appkeyIndexDatas = new ArrayList<Integer>();
        for(int i = 0;i < mProvisionerService.getNetKeyCnt(); i++){
            netkeyIndexDatas.add(i);
        }
        for(int i = 0;i < mProvisionerService.getAppKeyCnt(); i++){
            appkeyIndexDatas.add(i);
        }
        HashMap<Integer,Node> mapNodes = mProvisionerService.getNodesByNetKey(ota_op_netkeyIndex);
        log("mapNodes.size() =" + mapNodes.size());
        nodes.clear();
        for(Map.Entry<Integer,Node> entry:mapNodes.entrySet()) {
            nodes.add(entry.getValue());
        }
        log("nodes.size() =" + nodes.size());
        final CheckBoxAdapter cbAdapter = new CheckBoxAdapter(MyApplication.getApplication(),nodes);
        ArrayAdapter<Integer> netkeyAdapter = new ArrayAdapter<Integer>(MyApplication.getApplication(),android.R.layout.simple_spinner_dropdown_item,netkeyIndexDatas);
        ArrayAdapter<Integer> appkeyAdapter = new ArrayAdapter<Integer>(MyApplication.getApplication(),android.R.layout.simple_spinner_dropdown_item,appkeyIndexDatas);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.ota_op_start, null);
        builder.setView(dialoglayout);
        builder.setCancelable(false);
        Spinner netKeyindexSpinner = (Spinner) dialoglayout.findViewById(R.id.netkeyIndexSpinner);
        Spinner appKeyindexSpinner = (Spinner) dialoglayout.findViewById(R.id.appkeyIndexSpinner);
        final ListView cbListView = (ListView) dialoglayout.findViewById(R.id.nodelist);
        netKeyindexSpinner.setAdapter(netkeyAdapter);
        appKeyindexSpinner.setAdapter(appkeyAdapter);
        cbListView.setDivider(null);
        cbListView.setAdapter(cbAdapter);
        netKeyindexSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                log("showOtaOpStartDialog netKeyindexSpinner onItemSelected");
                ota_op_netkeyIndex = netkeyIndexDatas.get(position);
                HashMap<Integer,Node> mapNodes = mProvisionerService.getNodesByNetKey(ota_op_netkeyIndex);
                log("mapNodes.size() =" + mapNodes.size());
                nodes.clear();
                for(Map.Entry<Integer,Node> entry:mapNodes.entrySet()) {
                    nodes.add(entry.getValue());
                }
                log("nodes.size() =" + nodes.size());
                cbAdapter.nodeChange(nodes);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                log("showOtaOpStartDialog onNothingSelected onNothingSelected");

            }
        });
        appKeyindexSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                log("showOtaOpStartDialog appKeyindexSpinner onItemSelected");
                ota_op_appkeyIndex = appkeyIndexDatas.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                log("showOtaOpStartDialog appKeyindexSpinner onNothingSelected");

            }
        });

        cbListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                CheckBoxAdapter.ViewHolder viewHolder = (CheckBoxAdapter.ViewHolder) view.getTag();
                viewHolder.checkBox.toggle();
                CheckBoxAdapter.state.put(position, viewHolder.checkBox.isChecked());
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                ArrayList<Node> nodelist = new ArrayList<>();
                Iterator<Integer> iterator = CheckBoxAdapter.state.keySet().iterator();
                while (iterator.hasNext()) {
                    Integer next = iterator.next();
                    Boolean able = CheckBoxAdapter.state.get(next);
                    if (able) {
                        nodelist.add(nodes.get(next));
                        log("checked node = " + nodes.get(next).getAddr());
                    }
                }
                log("nodelist.size() = " + nodelist.size());
                if(nodelist.size() > 0){
                    mProvisionerService.otaStart(ota_op_netkeyIndex, nodelist, ota_op_appkeyIndex);
                } else {
                    //do something
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.create().show();

    }

    @Override
    public void open() {
        log("open mesh");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProvisionerService.setEnabled(llState,false);
                mSlideSwitch.setSlideable(false);
                llState.setClickable(false);
                mProvisionerService.setBluetoothMeshEnabled(true);
                slideText.setText(getResources().getString(R.string.switch_open));
                mProgressBar.setVisibility(View.VISIBLE);
                closeDescription.setText(R.string.turning_on);
                meshCurrentState = MESH_STATE_IDLE;
                //isOpenMeshSwitch = true;
                //isScanning = true;
            }
        });
    }

    @Override
    public void close() {
        log("close mesh");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProvisionerService.setBluetoothMeshEnabled(false);
                slideText.setText(getResources().getString(R.string.switch_close));
                llClose.setVisibility(View.VISIBLE);
                llOpen.setVisibility(View.GONE);
                unProvisionedAdapter.setPosition(UnprovisionedAdapter.ALL_ENABLE,true);
                nodeAdapter.setPosition(NodeAdapter.ALL_ENABLE);
                unProvisionedAdapter.clear();
                isOpenMeshSwitch = false;
                isScanning = false;
                meshCurrentState = MESH_STATE_IDLE;
                printfGroupProvResult();
                //if(null != mRunnable){
                    //mHandler.removeCallbacks(mRunnable);
                //}
                Toast.makeText(MainActivity.this, "mesh off", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llstate:
                if(isOpenMeshSwitch) {
                    mSlideSwitch.setSwitchState(false);
                }else {
                    mSlideSwitch.setSwitchState(true);
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.unprovisioned:
                if(unProvisionedAdapter.getPosition() < 0 ) {
                    UnProvisionedDevice mUnProvisionedDevice = unProvisionedAdapter.getUnprovisionedDevice(position);
                    if (mUnProvisionedDevice == null) {
                        log("onItemClick , not fonund unProvisionedDevice , position = " + position);
                        return;
                    }
                    int netkeyIndexSize = mProvisionerService.getNetKeyCnt();
                    int appkeyIndexSize = mProvisionerService.getAppKeyCnt();
                    showUnprovisionedFragment(position, mUnProvisionedDevice, mProvisionerService.getNetKeyIndexs(), mProvisionerService.getAppkeyIndexMaps());
                }
               break;
            case R.id.node:
                ArrayList<MeshMessage> messages = mProvisionerService.getSupportedMessages((MyApplication.nodeList).get(position));
                ArrayList<Integer> groupAddrDatas = mProvisionerService.getGroupAddrList();
                showProvisionedDialogFragment(position,messages,groupAddrDatas);
                break;
            default:
                break;
        }
    }

    @Override
    public void onRecyclerViewItemClick(View view,int position) {
        log("onRecyclerViewItemClick , position = " + position);
        if(view.getId() == nodeAdapter.getId()) {
            if(unProvisionedAdapter.getPosition() < 0 && nodeAdapter.getPosition() < 0 ) {
                ArrayList<MeshMessage> messages = mProvisionerService.getSupportedMessages((MyApplication.nodeList).get(position));
                ArrayList<Integer> groupAddrDatas = mProvisionerService.getGroupAddrList();
                MyApplication.netkeyindex = mProvisionerService.getNetKeyCnt();
                MyApplication.appkeyindex = mProvisionerService.getAppKeyCnt();
                showProvisionedDialogFragment(position,messages,groupAddrDatas);
            }
        }else if(view.getId() == unProvisionedAdapter.getId()) {
            if(unProvisionedAdapter.getPosition() < 0 && nodeAdapter.getPosition() < 0) {
                UnProvisionedDevice mUnProvisionedDevice = unProvisionedAdapter.getUnprovisionedDevice(position);
                if (mUnProvisionedDevice == null) {
                    log("onRecyclerViewItemClick , not fonund unProvisionedDevice , position = " + position);
                    return;
                }
                //int netkeyIndexSize = mProvisionerService.getNetKeyCnt();
                //int appkeyIndexSize = mProvisionerService.getAppKeyCnt();
                showUnprovisionedFragment(position, mUnProvisionedDevice, mProvisionerService.getNetKeyIndexs(), mProvisionerService.getAppkeyIndexMaps());
            }
        }
    }

    @Override
    public void onRecyclerViewItemLongClick(View view,int position) {
        nodePosition = position;
    }

    private void showProvisionedDialogFragment(int position,ArrayList<MeshMessage> messages,ArrayList<Integer> groupAddrDatas) {
        FragmentTransaction mFragTransaction = getFragmentManager().beginTransaction();
        //Check whether there is a Fragment corresponding to the tag through the tag
        Fragment fragment =  getFragmentManager().findFragmentByTag("ProvisionedDialogFragment");
        if(fragment!=null){
            //In order not to display DialogFragment repeatedly, remove the DialogFragment that is being displayed before displaying the DialogFragment
            mFragTransaction.remove(fragment);
        }
        mProvisionedDialogFragment = ProvisionedDialogFragment.newInstance(position,messages,groupAddrDatas);
        //Display a Fragment and add a tag to the Fragment. The fragment can be found by findFragmentByTag
        mProvisionedDialogFragment.show(mFragTransaction, "ProvisionedDialogFragment");
        mProvisionedDialogFragment.setBluetoothMeshListener(mBluetoothMeshListener);
    }

    private void showUnprovisionedFragment(int position,UnProvisionedDevice unProvisionedDevice,
                        ArrayList<Integer> netKeyIndexs, HashMap<Integer, ArrayList<Integer>> appkeyIndexMaps) {
        FragmentTransaction mFragTransaction = getFragmentManager().beginTransaction();
        //Check whether there is a Fragment corresponding to the tag through the tag
        Fragment fragment =  getFragmentManager().findFragmentByTag("UnprovisionedDialogFragment");
        if(fragment!=null){
            //In order not to display DialogFragment repeatedly, remove the DialogFragment that is being displayed before displaying the DialogFragment
            mFragTransaction.remove(fragment);
        }
        UnprovisionedDialogFragment dialogFragment = UnprovisionedDialogFragment.newInstance(position, unProvisionedDevice, netKeyIndexs, appkeyIndexMaps);
        //Display a Fragment and add a tag to the Fragment. The fragment can be found by findFragmentByTag
        dialogFragment.show(mFragTransaction, "UnprovisionedDialogFragment");
        dialogFragment.setBluetoothMeshListener(mBluetoothMeshListener);
    }

    /**
     * ACCESS_FINE_LOCATION is a dangerous permission
     * so needs to check whether the ACCESS_FINE_LOCATION permission is authorized at runtime
     */
    public static boolean checkLocationPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ENABLE_LOCATION);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (REQUEST_ENABLE_LOCATION == requestCode) {
            if (grantResults == null || grantResults.length == 0) {
                log("grantResults == null || grantResults.length == 0");
                return;
            }
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                invalidateOptionsMenu();
            } else {
                Toast.makeText(this, "Location permission is not granted.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void promptBtDialogIfNeeded() {
        // Ensures Bluetooth is enabled on the device. If Bluetooth is not
        // currently enabled,
        // fire an intent to display a dialog asking the user to grant
        // permission to enable it.
        log("promptBtDialogIfNeeded , Bluetooth state: " + mBluetoothAdapter.getState());
        if (BluetoothAdapter.STATE_OFF == mBluetoothAdapter.getState()
                || BluetoothAdapter.STATE_TURNING_OFF == mBluetoothAdapter.getState()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        log("onActivityResult");
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            log("deny");
            finish();
            return;
        }
        if(isOpenMeshSwitch) {
            log("allow");
            /*
            mProvisionerService.setBluetoothMeshEnabled(false);
            unProvisionedAdapter.setPosition(UnprovisionedAdapter.ALL_ENABLE,true);
            nodeAdapter.setPosition(NodeAdapter.ALL_ENABLE);
            unProvisionedAdapter.clear();
            mProvisionerService.setBluetoothMeshEnabled(true);
            isScanning = true;
            */
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void inviteProvisioningTimeout(){
        mRunnable = new Runnable(){
            @Override
            public void run(){
                if(MyApplication.isProvisioning){
                    unProvisionedAdapter.setPosition(UnprovisionedAdapter.ALL_ENABLE);
                    //Toast.makeText(MainActivity.this, "provisioning timeout", Toast.LENGTH_SHORT).show();
                }
                isScanning = false;
                meshCurrentState = MESH_STATE_IDLE;
            }

        };
        //mHandler.postDelayed(mRunnable,60*1000);
    }

    class BluetoothMeshBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            log("onReceive() action = " + action);

            if (action == null) {
                log("onReceive() Received intent with null action");
                return;
            }

            if ((action != "android.action.MESH_ON") && (action != BluetoothAdapter.ACTION_STATE_CHANGED)) {
                if(!isOpenMeshSwitch) {
                    log("onReceive() when mesh is off, can't send any command except MESH_ON and ACTION_STATE_CHANGED");
                    Toast.makeText(MainActivity.this, " can't send command when mesh off", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            switch(action){
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    //STATE_OFF = 10, STATE_TURNING_ON = 11, STATE_ON = 12, STATE_TURNING_OFF = 13
                    log("BluetoothAdapter.ACTION_STATE_CHANGED , Bluetooth State: " + blueState);
                    switch(blueState){
                        case BluetoothAdapter.STATE_OFF:
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            if(isForeground) {
                                promptBtDialogIfNeeded();
                            }else if(mProvisionerService.isEnable()){
                                //mSlideSwitch.setSwitchState(false);
                            }
                            break;
                    }
                    break;
                case "android.action.PB_ADV_SEND_GENERIC_ONOFF_MSG":
                    adbSendMsg(intent, MESH_PB_ADV, GENERIC_ONOFF_MSG);
                    break;
                case "android.action.PB_GATT_SEND_GENERIC_ONOFF_MSG":
                    adbSendMsg(intent, MESH_PB_GATT, GENERIC_ONOFF_MSG);
                    break;
                case "android.action.FRIENDSHIP_SEND_GENERIC_ONOFF_MSG":
                    adbSendMsg(intent, MESH_FRIENDSHIP, GENERIC_ONOFF_MSG);
                    break;
                case "android.action.MESH_GROUP_SEND_GENERIC_ONOFF_MSG":
                    adbGroupSendMsg(intent, GENERIC_ONOFF_MSG);
                    break;
                case "android.action.MESH_ON":
                    log("android.action.MESH_ON");
                    if(isOpenMeshSwitch) {
                        Toast.makeText(MainActivity.this, "mesh already on", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    mSlideSwitch.setSwitchState(true);
                    break;
                case "android.action.MESH_OFF":
                    log("android.action.MESH_OFF");
                    if(!isOpenMeshSwitch) {
                        Toast.makeText(MainActivity.this, "mesh already off", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (!(meshCurrentState == MESH_STATE_PROVISIONING || meshCurrentState == MESH_STATE_CONFIGING || meshCurrentState == MESH_STATE_IDLE)) {
                        log("mesh maybe not idle state or provision , config state , meshCurrentState = " + meshCurrentState);
                        return;
                    }
                    mSlideSwitch.setSwitchState(false);
                    break;
                case "android.action.PB_GATT_CONNECT":
                    adbGattConnectOrDisConnect(intent, true);
                    break;
                case "android.action.PB_GATT_DISCONNECT":
                    adbGattConnectOrDisConnect(intent, false);
                    break;
                case "android.action.MESH_ADV_PROVISION":
                    adbProvision(intent, false);
                    break;
                case "android.action.MESH_GATT_PROVISION":
                    adbProvision(intent, true);
                    break;
                case "android.action.MESH_ADV_REMOVE":
                    adbRemoveNode(intent, false);
                    break;
                case "android.action.MESH_GATT_REMOVE":
                    adbRemoveNode(intent, true);
                    break;
                case "android.action.MESH_SCAN":
                    log("android.action.MESH_SCAN");
                    if(!isOpenMeshSwitch) {
                        Toast.makeText(MainActivity.this, "mesh is off, please open mesh", Toast.LENGTH_SHORT).show();
                        log("mesh is off, please open mesh");
                        break;
                    }
                    if (meshCurrentState != MESH_STATE_IDLE) {
                        log("mesh maybe not idle state , meshCurrentState = " + meshCurrentState);
                        return;
                    }
                    if(!(unProvisionedAdapter.getPosition() < 0 && nodeAdapter.getPosition() < 0)) {
                        Toast.makeText(MainActivity.this, "Provisioning or Configing , Please try again scan later", Toast.LENGTH_SHORT).show();
                        log("provisioning or configing , Please try again scan later");
                        break;
                    }
                    if(isScanning) {
                        mProvisionerService.stopUnProvsionScan();
                    }
                    unProvisionedAdapter.clear();
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProvisionerService.startUnProvsionScan();
                    isScanning = true;
                    break;
                case "android.action.MESH_GROUP_PROVISION":
                    log("android.action.MESH_GROUP_PROVISION");
                    adbGroupProvision();
                    break;
                case "android.action.MESH_GROUP_NODE_RESET":
                    log("android.action.MESH_GROUP_NODE_RESET");
                    adbGroupNodeReset(PARTIAL_NODE_RESET);
                    break;
                case "android.action.PB_ADV_SEND_LIGHT_LIGHTNESS_MSG":
                    log("android.action.PB_ADV_SEND_LIGHT_LIGHTNESS_MSG");
                    adbSendMsg(intent, MESH_PB_ADV, LIGHT_LIGHTNESS_MSG);
                    break;
                case "android.action.PB_GATT_SEND_LIGHT_LIGHTNESS_MSG":
                    log("android.action.PB_GATT_SEND_LIGHT_LIGHTNESS_MSG");
                    adbSendMsg(intent, MESH_PB_GATT, LIGHT_LIGHTNESS_MSG);
                    break;
                case "android.action.MESH_GROUP_SEND_LIGHT_LIGHTNESS_MSG":
                    log("android.action.MESH_GROUP_SEND_LIGHT_LIGHTNESS_MSG");
                    adbGroupSendMsg(intent, LIGHT_LIGHTNESS_MSG);
                    break;
                case "android.action.PB_ADV_SEND_LIGHT_LIGHTNESS_RANGE_MSG":
                    log("android.action.PB_ADV_SEND_LIGHT_LIGHTNESS_RANGE_MSG");
                    adbSendMsg(intent, MESH_PB_ADV, LIGHT_LIGHTNESS_RANGE_MSG);
                    break;
                case "android.action.PB_GATT_SEND_LIGHT_LIGHTNESS_RANGE_MSG":
                    log("android.action.PB_GATT_SEND_LIGHT_LIGHTNESS_RANGE_MSG");
                    adbSendMsg(intent, MESH_PB_GATT, LIGHT_LIGHTNESS_RANGE_MSG);
                    break;
                case "android.action.MESH_GROUP_SEND_LIGHT_LIGHTNESS_RANGE_MSG":
                    log("android.action.MESH_GROUP_SEND_LIGHT_LIGHTNESS_RANGE_MSG");
                    adbGroupSendMsg(intent, LIGHT_LIGHTNESS_RANGE_MSG);
                    break;
                case "android.action.PB_ADV_SEND_LIGHT_CTL_MSG":
                    log("android.action.PB_ADV_SEND_LIGHT_CTL_MSG");
                    adbSendMsg(intent, MESH_PB_ADV, LIGHT_CTL_MSG);
                    break;
                case "android.action.PB_GATT_SEND_LIGHT_CTL_MSG":
                    log("android.action.PB_GATT_SEND_LIGHT_CTL_MSG");
                    adbSendMsg(intent, MESH_PB_GATT, LIGHT_CTL_MSG);
                    break;
                case "android.action.MESH_GROUP_SEND_LIGHT_CTL_MSG":
                    log("android.action.MESH_GROUP_SEND_LIGHT_CTL_MSG");
                    adbGroupSendMsg(intent, LIGHT_CTL_MSG);
                    break;
                case "android.action.PB_ADV_SEND_LIGHT_CTL_TEMP_RANGE_MSG":
                    log("android.action.PB_ADV_SEND_LIGHT_CTL_TEMP_RANGE_MSG");
                    adbSendMsg(intent, MESH_PB_ADV, LIGHT_CTL_TEMP_RANGE_MSG);
                    break;
                case "android.action.PB_GATT_SEND_LIGHT_CTL_TEMP_RANGE_MSG":
                    log("android.action.PB_GATT_SEND_LIGHT_CTL_TEMP_RANGE_MSG");
                    adbSendMsg(intent, MESH_PB_GATT, LIGHT_CTL_TEMP_RANGE_MSG);
                    break;
                case "android.action.MESH_GROUP_SEND_LIGHT_CTL_TEMP_RANGE_MSG":
                    log("android.action.MESH_GROUP_SEND_LIGHT_CTL_TEMP_RANGE_MSG");
                    adbGroupSendMsg(intent, LIGHT_CTL_TEMP_RANGE_MSG);
                    break;
                case "android.action.PB_ADV_SEND_LIGHT_HSL_MSG":
                    log("android.action.PB_ADV_SEND_LIGHT_HSL_MSG");
                    adbSendMsg(intent, MESH_PB_ADV, LIGHT_HSL_MSG);
                    break;
                case "android.action.PB_GATT_SEND_LIGHT_HSL_MSG":
                    log("android.action.PB_GATT_SEND_LIGHT_HSL_MSG");
                    adbSendMsg(intent, MESH_PB_GATT, LIGHT_HSL_MSG);
                    break;
                case "android.action.MESH_GROUP_SEND_LIGHT_HSL_MSG":
                    log("android.action.MESH_GROUP_SEND_LIGHT_HSL_MSG");
                    adbGroupSendMsg(intent, LIGHT_HSL_MSG);
                    break;
                case "android.action.PB_ADV_SEND_LIGHT_HSL_RANGE_MSG":
                    log("android.action.PB_ADV_SEND_LIGHT_HSL_RANGE_MSG");
                    adbSendMsg(intent, MESH_PB_ADV, LIGHT_HSL_RANGE_MSG);
                    break;
                case "android.action.PB_GATT_SEND_LIGHT_HSL_RANGE_MSG":
                    log("android.action.PB_GATT_SEND_LIGHT_HSL_RANGE_MSG");
                    adbSendMsg(intent, MESH_PB_GATT, LIGHT_HSL_RANGE_MSG);
                    break;
                case "android.action.MESH_GROUP_SEND_LIGHT_HSL_RANGE_MSG":
                    log("android.action.MESH_GROUP_SEND_LIGHT_HSL_RANGE_MSG");
                    adbGroupSendMsg(intent, LIGHT_HSL_RANGE_MSG);
                    break;
                case "android.action.SEND_LIGHT_HSL_SET_MSG":
                    log("android.action.SEND_LIGHT_HSL_SET_MSG");
                    adbSendLightHSLMsg(intent);
                    break;
                case "android.action.ADD_NETKEY":
                    log("android.action.ADD_NETKEY");
                    adbAddNewNetKey(intent);
                    break;
                case "android.action.ADD_APPKEY":
                    log("android.action.ADD_APPKEY");
                    adbAddNewAppkey(intent);
                    break;
                case "android.action.PB_ADV_FRIENDSHIP_INFO":
                    log("android.action.PB_ADV_FRIENDSHIP_INFO");
                    adbPrintFSInfo(intent);
                    break;
                case "android.action.ALL_PB_ADV_FRIENDSHIP_INFO":
                    log("android.action.ALL_PB_ADV_FRIENDSHIP_INFO");
                    adbPrintAllFSInfo(intent);
                    break;
                case "android.action.ADD_FRIENDSHIP_DEVICE":
                    log("android.action.ADD_FRIENDSHIP_DEVICE");
                    adbAddFriendShipDevice(intent);
                    break;
                case "android.action.ADD_GROUP_FRIENDSHIP_DEVICE":
                    log("android.action.ADD_GROUP_FRIENDSHIP_DEVICE");
                    adbAddGroupFriendShipDevice();
                    break;
                case "android.action.GET_HEARTBEAT_STATUS":
                    log("android.action.GET_HEARTBEAT_STATUS");
                    adbGetHeartbeatStatus(intent);
                    break;
                default :
                    break;
            }
        }
    }

    private void adbGetHeartbeatStatus(Intent intent){
        log("adbGetHeartbeatStatus");
        if (!isOpenMeshSwitch) {
            log("adbGetHeartbeatStatus ---> mesh is off, please first open mesh");
            Toast.makeText(MainActivity.this, "mesh is off, please first open mesh", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.setLength(0);
        String mGattAddr = intent.getStringExtra("gattAddr");
        String uuidStr = intent.getStringExtra("uuid");
        log(buffer.append("adbGetHeartbeatStatus ---> mGattAddr = ").append(mGattAddr).append(" , uuidStr = ").append(uuidStr).toString());
        int[] uuid = new int[16];
        String gattAddress = null;
        Node mNode = null;
        boolean gatt_bearer = false;
        if (mGattAddr != null && uuidStr == null) {
            gattAddress = mGattAddr;
            mNode = mProvisionerService.getNodeByGattAddr(gattAddress);
            gatt_bearer = true;
        } else if (mGattAddr == null && uuidStr != null){
            uuid = MeshUtils.StringToIntArray(uuidStr);
            mNode = mProvisionerService.getNodeByUUID(uuid);
            gatt_bearer = false;
        } else {
            log("adbGetHeartbeatStatus ---> param set error");
            return;
        }
        if (mNode == null) {
            log("adbGetHeartbeatStatus ---> not find device in Node list or param format set error");
            return;
        }
        buffer.setLength(0);
        if (gatt_bearer) {
            buffer.append(mGattAddr);
        } else {
            buffer.append(MeshUtils.intArrayToString(mNode.getUUID(), true));
        }
        buffer.append(", heartbeat status: ").append(MeshUtils.getActiveStatus(mNode.getActiveStatus()));
        log(buffer.toString());
    }

    private void adbAddGroupFriendShipDevice() {
        log("adbAddGroupFriendShipDevice");
        if (!isOpenMeshSwitch) {
            log("adbAddGroupFriendShipDevice ---> mesh is off, please first open mesh");
            Toast.makeText(MainActivity.this, "mesh is off, please first open mesh", Toast.LENGTH_SHORT).show();
            return;
        }
        if (meshCurrentState != MESH_STATE_IDLE) {
            log("adbAddGroupFriendShipDevice ---> mesh maybe not idle state , meshCurrentState = " + meshCurrentState);
            return;
        }
        if(!(unProvisionedAdapter.getPosition() < 0 && nodeAdapter.getPosition() < 0)) {
            Toast.makeText(MainActivity.this, "MeshProv is Provisioning or Configing , please try again later", Toast.LENGTH_SHORT).show();
            log("adbAddGroupFriendShipDevice ---> MeshProv is Provisioning or Configing , please try again later");
            return;
        }
        int[] frishipNetKey = MeshUtils.StringToIntArray(mProvisionerService.frishipNetKey);
        int[] frishipAppKey = MeshUtils.StringToIntArray(mProvisionerService.frishipAppKey);
        if (frishipNetKey == null || frishipNetKey.length != 16) {
            log("adbAddGroupFriendShipDevice ---> frishipNetKey set error , frishipNetKey = " + MeshUtils.intArrayToString(frishipNetKey, " "));
            Toast.makeText(MainActivity.this, "frishipNetKey set error", Toast.LENGTH_SHORT).show();
            return;
        }
        if (frishipAppKey == null || frishipAppKey.length != 16) {
            log("adbAddGroupFriendShipDevice ---> frishipAppKey set error , frishipAppKey = " + MeshUtils.intArrayToString(frishipAppKey, " "));
            Toast.makeText(MainActivity.this, "frishipAppKey set error", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> frishipAddrList = mProvisionerService.frishipAddrList;
        if (frishipAddrList.size() == 0) {
            log("adbAddGroupFriendShipDevice ---> frishipAddr not set");
            Toast.makeText(MainActivity.this, "frishipAddr not set", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<NetKey> netKeyList = new ArrayList<NetKey>(mProvisionerService.getNetKeyMap().values());
        ArrayList<AppKey> appKeyList = new ArrayList<AppKey>(mProvisionerService.getAppKeyMap().values());
        boolean hasfrishipNetKey = false;
        boolean hasfrishipAppKey = false;
        for (int i = 0; i < netKeyList.size(); i++) {
            int netkeyIndex = netKeyList.get(i).getIndex();
            int[] netkey = netKeyList.get(i).getValue();
            if (netkeyIndex == 0x0001 && Arrays.equals(netkey, frishipNetKey)) {
                hasfrishipNetKey = true;
                break;
            }
        }
        for (int i = 0; i < appKeyList.size(); i++) {
            int appkeyIndex = appKeyList.get(i).getIndex();
            int appkeyBondNetkeyIndex = appKeyList.get(i).getBoundNetKeyIndex();
            int[] appkey = appKeyList.get(i).getValue();
            if (appkeyIndex == 0x0001 && appkeyBondNetkeyIndex == 0x0001 && Arrays.equals(appkey, frishipAppKey)) {
                hasfrishipAppKey = true;
                break;
            }
        }
        log("adbAddGroupFriendShipDevice ---> hasfrishipNetKey = " + hasfrishipNetKey + " , hasfrishipAppKey = " + hasfrishipAppKey);
        if (hasfrishipNetKey && hasfrishipAppKey) { //netkey and appkey has been added before
            addFrishipDev(frishipAddrList);
        } else {
            if (mProvisionerService.addNewNetKey(frishipNetKey) == 0) {
                if (mProvisionerService.addNewAppKey(0x0001, frishipAppKey) == 0) {
                    addFrishipDev(frishipAddrList);
                } else {
                    log("adbAddGroupFriendShipDevice ---> addNewAppKey fail");
                }
            } else {
                log("adbAddGroupFriendShipDevice ---> addNewNetKey fail");
            }

        }
    }

    private void addFrishipDev(ArrayList<String> frishipAddrList) {
        for (int i = 0; i < frishipAddrList.size(); i++) {
            int frishipAddr = 0;
            try {
                frishipAddr = Integer.parseInt(frishipAddrList.get(i).substring(2),16);
                Node mNode = new Node();
                mNode.setNodeBearer(MeshConstants.MESH_BEARER_ADV);
                mNode.setConfigSuccess(true);
                mNode.setCurrNetkeyIndex(0x0001);
                mNode.addNetKey(0x0001);
                mNode.addAppKey(0x0001);
                mNode.setAddr(frishipAddr);
                mNode.setCmdAddFriDev(true);
                mNode.setFriDevName(mProvisionerService.frishipNameList.get(i));

                Model model = new Model(MeshConstants.MESH_MODEL_SIG_MODEL_ID_GENERIC_ONOFF_SERVER);
                model.getBoundAppKeySet().add(0x0001);
                mNode.addModel(model);

                mNode.addSupportedRXMsg(frishipAddr, model.getID(), model);


                //mNode.addSupportedRXMsg(frishipAddr, MeshConstants.MESH_MODEL_SIG_MODEL_ID_GENERIC_ONOFF_SERVER, null);
                if (nodeAdapter.addNode(mNode)){
                    mProvisionerService.getNodes().put(frishipAddr, mNode);

                    mProvisionerService.getNetKeyMap().get(0x0001).getNodes().put(frishipAddr, mNode);
                    mProvisionerService.updateStorageData(MyApplication.STORAGE_DATA_ALL);

                    LouSQLite.insert(MyCallBack.TABLE_NAME_NODE, new NodeData(mNode));
                    nodeRecyclerView.scrollToPosition(nodeAdapter.getItemCount() - 1);
                } else {
                    log("adbAddGroupFriendShipDevice ---> has same address in Node list");
                }

            } catch (Exception e) {
                log("adbAddGroupFriendShipDevice ---> frishipAddr format set error, frishipAddr = " + frishipAddrList.get(i));
            }
        }

    }

    private void adbAddFriendShipDevice(Intent intent) {
        log("adbAddFriendShipDevice");
        if (!isOpenMeshSwitch) {
            log("adbAddFriendShipDevice ---> mesh is off, please first open mesh");
            Toast.makeText(MainActivity.this, "mesh is off, please first open mesh", Toast.LENGTH_SHORT).show();
            return;
        }
        if (meshCurrentState != MESH_STATE_IDLE) {
            log("adbAddFriendShipDevice ---> mesh maybe not idle state , meshCurrentState = " + meshCurrentState);
            return;
        }
        if(!(unProvisionedAdapter.getPosition() < 0 && nodeAdapter.getPosition() < 0)) {
            Toast.makeText(MainActivity.this, "MeshProv is Provisioning or Configing , please try again later", Toast.LENGTH_SHORT).show();
            log("adbAddFriendShipDevice ---> MeshProv is Provisioning or Configing , please try again later");
            return;
        }

        String frishipName = intent.getStringExtra("frishipName");
        String addressStr = intent.getStringExtra("address");
        String netKeyIndexStr = intent.getStringExtra("netKeyIndex");
        String appKeyIndexStr = intent.getStringExtra("appKeyIndex");
        log("adbAddFriendShipDevice ---> frishipName = " + frishipName + " , addressStr = " + addressStr +
            " , netKeyIndexStr = " + netKeyIndexStr + " , appKeyIndexStr = " + appKeyIndexStr);
        int address = 0;
        int netKeyIndex = 0;
        int appKeyIndex = 0;
        try {
            address = Integer.parseInt(addressStr.substring(2),16);
            netKeyIndex = Integer.parseInt(netKeyIndexStr.substring(2),16);
            appKeyIndex = Integer.parseInt(appKeyIndexStr.substring(2),16);
        } catch (Exception e) {
            log("adbAddFriendShipDevice ---> addressStr or netKeyIndex or appKeyIndex param set error");
            Toast.makeText(MainActivity.this, "param set error", Toast.LENGTH_SHORT).show();
            return;
        }

        Node mNode = new Node();
        mNode.setNodeBearer(MeshConstants.MESH_BEARER_ADV);
        mNode.setConfigSuccess(true);
        mNode.setCurrNetkeyIndex(netKeyIndex);
        mNode.addNetKey(netKeyIndex);
        mNode.addAppKey(appKeyIndex);
        mNode.setAddr(address);
        mNode.setCmdAddFriDev(true);
        mNode.setFriDevName(frishipName);

        if (nodeAdapter.addNode(mNode)){
            mProvisionerService.getNodes().put(address, mNode);

            mProvisionerService.getNetKeyMap().get(netKeyIndex).getNodes().put(address, mNode);
            mProvisionerService.updateStorageData(MyApplication.STORAGE_DATA_ALL);

            LouSQLite.insert(MyCallBack.TABLE_NAME_NODE, new NodeData(mNode));
            nodeRecyclerView.scrollToPosition(nodeAdapter.getItemCount() - 1);
        } else {
            log("adbAddFriendShipDevice ---> has same address in Node list");
        }

    }

    private void adbPrintAllFSInfo(Intent intent) {
        log("adbPrintAllFSInfo");
        if (!isOpenMeshSwitch) {
            log("adbPrintAllFSInfo ---> mesh is off, please first open mesh");
            Toast.makeText(MainActivity.this, "mesh is off, please first open mesh", Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap<Integer, Node> mNodes = mProvisionerService.getNodes();
        ArrayList<Node> nodeList = new ArrayList<Node>(mNodes.values());
        int nodeListSize = nodeList.size();
        StringBuffer buffer = new StringBuffer();
        log("<---adbPrintAllFSInfo , start pritnf all node friendship info --->");
        for (int i = 0; i < nodeListSize; i++) {
            Node node = nodeList.get(i);
            buffer.setLength(0);
            if (node.getNodeBearer() == MeshConstants.MESH_BEARER_ADV) {
                buffer.append("UUID: " + MeshUtils.intArrayToString(uuid, true));
                buffer.append(" , friendship status: " + getFriendshipStatus(node.getFSStatus()));
                if (node.getFSStatus() == MeshConstants.MESH_FRIENDSHIP_ESTABLISHED ||
                    node.getFSStatus() == MeshConstants.MESH_FRIENDSHIP_ESTABLISH_FAILED) {
                    buffer.append(" , cost time: " + node.getFSOnCostTime());
                }
                buffer.append("  ,  Off Num: " + node.getFSContOff());
            } else if (node.getNodeBearer() == MeshConstants.MESH_BEARER_GATT) {
               buffer.append("PB-GATT , addr: " + node.getAddr());
            }
            log(buffer.toString());
        }
        log("<---adbPrintAllFSInfo , end pritnf all node friendship info---->");
    }

    private void adbPrintFSInfo(Intent intent) {
        log("adbPrintFSInfo");
        if (!isOpenMeshSwitch) {
            log("adbPrintFSInfo ---> mesh is off, please first open mesh");
            Toast.makeText(MainActivity.this, "mesh is off, please first open mesh", Toast.LENGTH_SHORT).show();
            return;
        }
        String uuidStr = intent.getStringExtra("uuid");
        log("adbPrintFSInfo ---> uuidStr = " + uuidStr);
        int[] uuid = MeshUtils.StringToIntArray(uuidStr);
        if (uuid == null) {
            log("adbPrintFSInfo , PB-ADV uuid set errror");
            return;
        }
        Node node = mProvisionerService.getNodeByUUID(uuid);
        if (node == null) {
            log("adbPrintFSInfo , not found Node by uuid");
            return;
        }
        StringBuffer buffer = new StringBuffer("adbPrintFSInfo");
        buffer.append(" , friendship status: " + getFriendshipStatus(node.getFSStatus()));
        if (node.getFSStatus() == MeshConstants.MESH_FRIENDSHIP_ESTABLISHED ||
            node.getFSStatus() == MeshConstants.MESH_FRIENDSHIP_ESTABLISH_FAILED) {
            buffer.append(" , cost time: " + node.getFSOnCostTime());
        }
        buffer.append("  ,  Off Num: " + node.getFSContOff());
        log(buffer.toString());
    }

    private String getFriendshipStatus(int fsStatus) {
        log("getFriendshipStatus, fsStatus: " + fsStatus);
        switch(fsStatus){
            case MeshConstants.MESH_FRIENDSHIP_ESTABLISHED:
                return "On";
            case MeshConstants.MESH_FRIENDSHIP_ESTABLISH_FAILED:
                return "On Fail";
            case MeshConstants.MESH_FRIENDSHIP_TERMINATED:
            case MeshConstants.MESH_FRIENDSHIP_REQUEST_FRIEND_TIMEOUT:
            case MeshConstants.MESH_FRIENDSHIP_SELECT_FRIEND_TIMEOUT:
                return "Off";
            default:
                log("getFriendShipStatus , fsStatus = " + fsStatus);
                return "unknown";
        }

    }

    private void adbSendLightHSLMsg(Intent intent) {
        log("adbSendLightHSLMsg");
        if(!isOpenMeshSwitch) {
            log("adbSendLightHSLMsg ---> mesh is off, please first open mesh");
            Toast.makeText(MainActivity.this, "mesh is off, please first open mesh", Toast.LENGTH_SHORT).show();
            return;
        }
        String dstStr = intent.getStringExtra("dst");
        String srcStr = intent.getStringExtra("src");
        String ttlStr = intent.getStringExtra("ttl");
        String netKeyIndexStr = intent.getStringExtra("netKeyIndex");
        String appKeyIndexStr = intent.getStringExtra("appKeyIndex");
        String payloadStr = intent.getStringExtra("payload");
        log("adbSendLightHSLMsg ---> dstStr = " + dstStr + " , srcStr = " + srcStr +
            " , ttlStr = " + ttlStr + " , netKeyIndexStr = " + netKeyIndexStr + " , appKeyIndexStr = " + appKeyIndexStr);
        int dst = 0;
        int src = 0;
        int ttl = 0;
        int netKeyIndex = 0;
        int appKeyIndex = 0;
        try {
            dst = Integer.parseInt(dstStr.substring(2),16);
            src = Integer.parseInt(srcStr.substring(2),16);
            ttl = Integer.parseInt(ttlStr);
            netKeyIndex = Integer.parseInt(netKeyIndexStr.substring(2),16);
            appKeyIndex = Integer.parseInt(appKeyIndexStr.substring(2),16);
        } catch (Exception e) {
            log("adbSendLightHSLMsg ---> dst or src or ttl or netKeyIndex or appKeyIndex param set error");
            Toast.makeText(MainActivity.this, "param set error", Toast.LENGTH_SHORT).show();
            return;
        }
        int[] payload = MeshUtils.StringToIntArray(payloadStr);
        if (payload == null) {
            log("adbSendLightHSLMsg ---> payload param set error , payload = " + payloadStr);
            Toast.makeText(MainActivity.this, "payload param set error", Toast.LENGTH_SHORT).show();
            return;
        }
        mProvisionerService.sendPacket(dst, src, ttl, netKeyIndex, appKeyIndex, payload);
    }

    private void adbAddNewNetKey(Intent intent) {
        log("adbAddNewNetKey");
        if(!isOpenMeshSwitch) {
            log("adbAddNewNetKey ---> mesh is off, please first open mesh");
            Toast.makeText(MainActivity.this, "mesh is off, please first open mesh", Toast.LENGTH_SHORT).show();
            return;
        }
        String netKeyStr = intent.getStringExtra("netKey");
        int[] netKey = MeshUtils.StringToIntArray(netKeyStr);
        if (netKey == null || netKey.length != 16) {
            log("adbAddNewNetKey ---> netKey param set error , netKey = " + MeshUtils.intArrayToString(netKey, " "));
            Toast.makeText(MainActivity.this, "netKey param set error", Toast.LENGTH_SHORT).show();
            return;
        }
        mProvisionerService.addNewNetKey(netKey);
    }

    private void adbAddNewAppkey(Intent intent) {
        log("adbAddNewAppkey");
        if(!isOpenMeshSwitch) {
            log("adbAddNewAppkey ---> mesh is off, please first open mesh");
            Toast.makeText(MainActivity.this, "mesh is off, please first open mesh", Toast.LENGTH_SHORT).show();
            return;
        }
        String appKeyStr = intent.getStringExtra("appKey");
        String netKeyIndexStr = intent.getStringExtra("netKeyIndex");
        int[] appKey = MeshUtils.StringToIntArray(appKeyStr);
        if (appKey == null || appKey.length != 16) {
            log("adbAddNewAppkey ---> appKey param set error , appkey = " + MeshUtils.intArrayToString(appKey, " "));
            Toast.makeText(MainActivity.this, "appKey param set error", Toast.LENGTH_SHORT).show();
            return;
        }
        int netKeyIndex = 0;
        try {
            netKeyIndex = Integer.parseInt(netKeyIndexStr.substring(2),16);
        } catch (Exception e) {
            log("adbAddNewAppkey ---> netKeyIndex param set error , netKeyIndexStr = " + netKeyIndexStr);
            Toast.makeText(MainActivity.this, "netKeyIndex param set error", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<Integer> netKeyIndexs = mProvisionerService.getNetKeyIndexs();
        boolean flag = false;
        for (int i = 0; i < netKeyIndexs.size(); i++) {
            if (netKeyIndex == netKeyIndexs.get(i)) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            log("adbAddNewAppkey ---> netKeyIndex param set valid , it not exist");
            Toast.makeText(MainActivity.this, "netKeyIndex param set valid , it not exist", Toast.LENGTH_SHORT).show();
            return;
        }
        mProvisionerService.addNewAppKey(netKeyIndex, appKey);
    }

    private void adbFrishipDevSendMsg(Intent intent){
        log("adbFrishipDevSendMsg");
    }

    private void adbSendMsg(Intent intent, int deviceType, String msgType) {
        log("adbSendMsg ---> deviceType = " + deviceType + " , msgType = " + msgType);
        if(!isOpenMeshSwitch) {
            log("adbSendMsg ---> mesh is off");
            return;
        }
        if (meshCurrentState != MESH_STATE_IDLE) {
            log("adbSendMsg ---> mesh maybe not idle state , meshCurrentState = " + meshCurrentState);
            return;
        }
        if(!(unProvisionedAdapter.getPosition() < 0 && nodeAdapter.getPosition() < 0)) {
            Toast.makeText(MainActivity.this, "MeshProv is Provisioning or Configing , please try again later", Toast.LENGTH_SHORT).show();
            log("adbSendMsg ---> MeshProv is Provisioning or Configing , please try again later");
            return;
        }
        String mGattAddr = intent.getStringExtra("gattAddr");
        String uuidStr = intent.getStringExtra("uuid");
        String appkeyIndexStr = intent.getStringExtra("appkeyIndex");
        String groupAddrStr = intent.getStringExtra("groupAddr");
        String on_offStr = intent.getStringExtra("on_off");
        String lightnessStr = intent.getStringExtra("lightness");
        String tempStr = intent.getStringExtra("temp");
        String deltaUVStr = intent.getStringExtra("deltaUV");
        String hueString = intent.getStringExtra("hue");
        String saturationString = intent.getStringExtra("saturation");
        String opCodeStr = intent.getStringExtra("opCode");
        String elementAddrStr = intent.getStringExtra("elementAddr");
        String friendshipAddrStr = intent.getStringExtra("frishipAddrStr");

        int[] uuid = new int[16];
        String gattAddress = null;
        int appkeyIndex = 0x0000; // appkeyindex default is 0x0000
        int groupAddr = 0x0000; // groupAddr default is 0x0000
        int elementAddr = -1;
        Node node = null;

        if(deviceType == MESH_PB_GATT) {
            if(mGattAddr == null) { // not set PB-GATT address
                Toast.makeText(MainActivity.this, "not set PB-GATT ddress", Toast.LENGTH_SHORT).show();
                log("adbSendMsg ---> PB-GATT , not set PB-GATT ddress");
                return;
            }else{ // set PB-GATT address
                if(MyApplication.nodeList.size() <= 0) {
                    log("adbSendMsg ---> PB-GATT , node list is null");
                    return;
                }else{
                    int i = 0;
                    for(i = 0; i < MyApplication.nodeList.size(); i++) {
                        if(MyApplication.nodeList.get(i).getNodeBearer() == MeshConstants.MESH_BEARER_GATT) {
                            log("adbSendMsg ---> PB-GATT , node Gatt address = " + MyApplication.nodeList.get(i).getGattAddr());
                            log("adbSendMsg ---> PB-GATT , input gatt address = " + mGattAddr);
                            if(mGattAddr.equals(MyApplication.nodeList.get(i).getGattAddr())) {
                                gattAddress = MyApplication.nodeList.get(i).getGattAddr();
                                node = MyApplication.nodeList.get(i);
                                elementAddr = MyApplication.nodeList.get(i).getAddr();
                                break;
                            }
                        }
                    }
                    if(i == MyApplication.nodeList.size()) {
                        log("adbSendMsg ---> PB-GATT , not find match node in node list");
                        return;
                    }
                }
            }
        } else if (deviceType == MESH_PB_ADV){
            if(uuidStr == null) { // not set PB-ADV uuid
                Toast.makeText(MainActivity.this, "not set PB-ADV UUID", Toast.LENGTH_SHORT).show();
                log("adbSendMsg ---> PB-ADV , not set PB-ADV UUID");
                return;
            }else { // set PB-ADV uuid
                if(MyApplication.nodeList.size() <= 0) {
                    log("adbSendMsg ---> PB-ADV , node list is null");
                    return;
                }else {
                    int i = 0;
                    for(i = 0; i < MyApplication.nodeList.size(); i++) {
                        if(MyApplication.nodeList.get(i).getNodeBearer() == MeshConstants.MESH_BEARER_ADV) {
                            log("adbSendMsg ---> PB-ADV , node uuid = " + MeshUtils.intArrayToString(MyApplication.nodeList.get(i).getUUID(), true));
                            log("adbSendMsg ---> PB-ADV , input uuid = " + MeshUtils.intArrayToString(MeshUtils.StringToIntArray(uuidStr), true));
                            if(Arrays.equals(MyApplication.nodeList.get(i).getUUID(), MeshUtils.StringToIntArray(uuidStr))) {
                                uuid = MyApplication.nodeList.get(i).getUUID();
                                node = MyApplication.nodeList.get(i);
                                elementAddr = MyApplication.nodeList.get(i).getAddr();
                                break;
                            }
                        }
                    }
                    if(i == MyApplication.nodeList.size()) {
                        log("adbSendMsg ---> PB-ADV , not find match node in node list");
                        return;
                    }
                }
            }

        } else if (deviceType == MESH_FRIENDSHIP) {
            if (friendshipAddrStr == null) {
                Toast.makeText(MainActivity.this, "not set Friendship address", Toast.LENGTH_SHORT).show();
                log("adbSendMsg ---> friendship device , not set Friendship address");
                return;
            }
            int friendshipAddr = (int)(MeshUtils.hexSrtingToDecimal(friendshipAddrStr));
            if (friendshipAddr == -1) {
                log("adbSendMsg ---> friendship device , friendshipAddr set error, friendshipAddr: " + friendshipAddr);
                return;
            }
           if(MyApplication.nodeList.size() <= 0) {
                log("adbSendMsg ---> friendship device , node list is null");
                return;
            } else {
                int i = 0;
                for(i = 0; i < MyApplication.nodeList.size(); i++) {
                    if(MyApplication.nodeList.get(i).getCmdAddFriDev()) {
                        if(MyApplication.nodeList.get(i).getAddr() == friendshipAddr) {
                            node = MyApplication.nodeList.get(i);
                            elementAddr = MyApplication.nodeList.get(i).getAddr();
                            break;
                        }
                    }
                }
                if(i == MyApplication.nodeList.size()) {
                    log("adbSendMsg ---> friendship device , not find match node in node list");
                    return;
                }
            }
            appkeyIndex = 0x0001; // friendship appkeyindex default is 0x0001
            groupAddr = 0x0000; //friendship groupAddr default is 0x0000
        }
        if(node == null) {
            log("adbSendMsg ---> node == null");
            return;
        }
        if (appkeyIndexStr == null) { // not set appkeyIndex
            log("adbSendMsg ---> not set appkeyIndex parameter, appkeyIndex default value is 0x0000");
        } else { // set appkeyIndex
            appkeyIndex = (int)(MeshUtils.hexSrtingToDecimal(appkeyIndexStr));
            if (appkeyIndex == -1) { //set invalid appkeyIndex
                log("adbSendMsg ---> set invalid appkeyIndex parameter = " + appkeyIndexStr);
                return;
            }
            log("adbSendMsg ---> set appkeyIndex parameter = " + appkeyIndexStr);
            if (deviceType == MESH_FRIENDSHIP) {
                appkeyIndex = 0x0001; // friendship appkeyindex only 0x0001
            }
        }
        if (groupAddrStr == null) { // not set groupAddr
            log("adbSendMsg ---> not set groupAddr parameter, groupAddr default value is 0x0000");
        } else{ // set groupAddr
            groupAddr = (int)(MeshUtils.hexSrtingToDecimal(groupAddrStr));
            if (groupAddr == -1) { //set invalid groupAddr
                log("adbSendMsg ---> set invalid groupAddr parameter = " + appkeyIndexStr);
                return;
            }
            log("adbSendMsg ---> set groupAddr parameter = " + groupAddrStr);
            if (deviceType == MESH_FRIENDSHIP) {
                appkeyIndex = 0x0000; // friendship appkeyindex only 0x0000
            }
        }
        if (mProvisionerService.getNodes().size() <= 0) {
            log("adbSendMsg ---> ProversionService mNodes size is 0");
            return;
        }
        ArrayList<MeshMessage> messages = mProvisionerService.getSupportedMessages(node);
        MeshMessage mMeshMessage = null;
        int[] payload = null;
        int opCode = -1;

        if (opCodeStr == null) { //if not set opcode parameter , set default opcode parameter
            switch(msgType){
                case GENERIC_ONOFF_MSG:
                    opCode = MeshConstants.MESH_MSG_GENERIC_ONOFF_SET;
                    log("adbSendMsg ---> not set opCode , set default opCode = MESH_MSG_GENERIC_ONOFF_SET");
                    break;
                case LIGHT_LIGHTNESS_MSG:
                    opCode = MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_SET;
                    log("adbSendMsg ---> not set opCode , set default opCode = MESH_MSG_LIGHT_LIGHTNESS_SET");
                    break;
                case LIGHT_CTL_MSG:
                    opCode = MeshConstants.MESH_MSG_LIGHT_CTL_SET;
                    log("adbSendMsg ---> not set opCode , set default opCode = MESH_MSG_LIGHT_CTL_SET");
                    break;
                case LIGHT_HSL_MSG:
                    opCode = MeshConstants.MESH_MSG_LIGHT_HSL_SET;
                    log("adbSendMsg ---> not set opCode , set default opCode = MESH_MSG_LIGHT_HSL_SET");
                    break;
                default:
                    log("adbSendMsg ---> not set opCode , not find match msgType , msgType = " + msgType);
                    break;
            }
        } else { //set opcode parameter
            switch(opCodeStr){
                case "MESH_MSG_GENERIC_ONOFF_GET":
                    if (msgType.equals(GENERIC_ONOFF_MSG)) {
                        opCode = MeshConstants.MESH_MSG_GENERIC_ONOFF_GET;
                        log("adbSendMsg ---> set opCode = MESH_MSG_GENERIC_ONOFF_GET");
                    }
                    break;
                case "MESH_MSG_GENERIC_ONOFF_SET":
                    if (msgType.equals(GENERIC_ONOFF_MSG)) {
                        opCode = MeshConstants.MESH_MSG_GENERIC_ONOFF_SET;
                        log("adbSendMsg ---> set opCode = MESH_MSG_GENERIC_ONOFF_SET");
                    }
                    break;
                case "MESH_MSG_GENERIC_ONOFF_SET_UNRELIABLE":
                    if (msgType.equals(GENERIC_ONOFF_MSG)) {
                        opCode = MeshConstants.MESH_MSG_GENERIC_ONOFF_SET_UNRELIABLE;
                        log("adbSendMsg ---> set opCode = MESH_MSG_GENERIC_ONOFF_SET_UNRELIABLE");
                    }
                    break;
                case "MESH_MSG_LIGHT_LIGHTNESS_GET":
                    if (msgType.equals(LIGHT_LIGHTNESS_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_GET;
                        log("adbSendMsg ---> set opCode = MESH_MSG_LIGHT_LIGHTNESS_GET");
                    }
                    break;
                case "MESH_MSG_LIGHT_LIGHTNESS_SET":
                    if (msgType.equals(LIGHT_LIGHTNESS_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_SET;
                        log("adbSendMsg ---> set opCode = MESH_MSG_LIGHT_LIGHTNESS_SET");
                    }
                    break;
                case "MESH_MSG_LIGHT_LIGHTNESS_SET_UNACKNOWLEDGED":
                    if (msgType.equals(LIGHT_LIGHTNESS_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_SET_UNACKNOWLEDGED;
                        log("adbSendMsg ---> set opCode = MESH_MSG_LIGHT_LIGHTNESS_SET_UNACKNOWLEDGED");
                    }
                    break;
                case "MESH_MSG_LIGHT_LIGHTNESS_RANGE_GET":
                    if (msgType.equals(LIGHT_LIGHTNESS_RANGE_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_RANGE_GET;
                        log("adbSendMsg ---> set opCode = MESH_MSG_LIGHT_LIGHTNESS_RANGE_GET");
                    }
                    break;
                case "MESH_MSG_LIGHT_CTL_GET":
                    if (msgType.equals(LIGHT_CTL_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_CTL_GET;
                        log("adbSendMsg ---> set opCode = MESH_MSG_LIGHT_CTL_GET");
                    }
                    break;
                case "MESH_MSG_LIGHT_CTL_SET":
                    if (msgType.equals(LIGHT_CTL_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_CTL_SET;
                        log("adbSendMsg ---> set opCode = MESH_MSG_LIGHT_CTL_SET");
                    }
                    break;
                case "MESH_MSG_LIGHT_CTL_SET_UNACKNOWLEDGED":
                    if (msgType.equals(LIGHT_CTL_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_CTL_SET_UNACKNOWLEDGED;
                        log("adbSendMsg ---> set opCode = MESH_MSG_LIGHT_CTL_SET_UNACKNOWLEDGED");
                    }
                    break;
                case "MESH_MSG_LIGHT_CTL_TEMPERATURE_RANGE_GET":
                    if (msgType.equals(LIGHT_CTL_TEMP_RANGE_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_CTL_TEMPERATURE_RANGE_GET;
                        log("adbSendMsg ---> set opCode = MESH_MSG_LIGHT_CTL_TEMPERATURE_RANGE_GET");
                    }
                    break;
                case "MESH_MSG_LIGHT_HSL_GET":
                    if (msgType.equals(LIGHT_HSL_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_HSL_GET;
                        log("adbSendMsg ---> set opCode = MESH_MSG_LIGHT_HSL_GET");
                    }
                    break;
                case "MESH_MSG_LIGHT_HSL_SET":
                    if (msgType.equals(LIGHT_HSL_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_HSL_SET;
                        log("adbSendMsg ---> set opCode = MESH_MSG_LIGHT_HSL_SET");
                    }
                    break;
                case "MESH_MSG_LIGHT_HSL_SET_UNACKNOWLEDGED":
                    if (msgType.equals(LIGHT_HSL_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_HSL_SET_UNACKNOWLEDGED;
                        log("adbSendMsg ---> set opCode = MESH_MSG_LIGHT_HSL_SET_UNACKNOWLEDGED");
                    }
                    break;
                case "MESH_MSG_LIGHT_HSL_RANGE_GET":
                    if (msgType.equals(LIGHT_HSL_RANGE_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_HSL_RANGE_GET;
                        log("adbSendMsg ---> set opCode = MESH_MSG_LIGHT_HSL_RANGE_GET");
                    }
                    break;
                default:
                    log("adbSendMsg ---> not find match opCode");
                    break;
            }
        }

        if (opCode == -1) {
            log("adbSendMsg ---> set invalid opCode parameter , opcdeStr = " + opCodeStr);
            return;
        }

        for(int i = 0; i < messages.size(); i++) {
            MeshMessage message = messages.get(i);
            if(opCode == message.getOpCode() && elementAddr == message.getElementAddr()) {
                mMeshMessage = message;
                break;
            }
        }
        if(mMeshMessage == null) {
            log("adbSendMsg ---> mMeshMessage == null");
            return;
        }

        switch(msgType){
            case GENERIC_ONOFF_MSG: //Generic OnOff
                if (opCode == MeshConstants.MESH_MSG_GENERIC_ONOFF_SET || opCode == MeshConstants.MESH_MSG_GENERIC_ONOFF_SET_UNRELIABLE) {
                    if ("on".equals(on_offStr)) {
                        payload = new int[1];
                        payload[0] = 1;
                    } else if("off".equals(on_offStr)) {
                        payload = new int[1];
                        payload[0] = 0;
                    } else {
                        log("adbSendMsg ---> GENERIC_ONOFF_MSG , on_offStr is invalid value , on_offStr = " + on_offStr);
                        return;
                    }

                } else if (opCode == MeshConstants.MESH_MSG_GENERIC_ONOFF_GET) {
                    //MESH_MSG_GENERIC_ONOFF_GET , not need payload parameter
                } else {
                    log("adbSendMsg ---> GENERIC_ONOFF_MSG , opcode set error , opcode = " + opCode);
                    return;
                }
                break;
            case LIGHT_LIGHTNESS_MSG: //Light Lightness
                if (opCode == MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_SET || opCode == MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_SET_UNACKNOWLEDGED) {
                    if (lightnessStr == null) {
                        Toast.makeText(MainActivity.this, "not set Light Lightness lightness parameter", Toast.LENGTH_SHORT).show();
                        log("adbSendMsg ---> LIGHT_LIGHTNESS_MSG , not set Light Lightness lightness parameter");
                        return;
                    }
                    if (MeshUtils.isMatchInternal(lightnessStr, 0x0001, 0xFFFF)) {
                        payload = new int[1];
                    } else {
                        log("adbSendMsg ---> LIGHT_LIGHTNESS_MSG , param error , lightnessStr = " + lightnessStr);
                        return;
                    }
                    payload[0] = Integer.parseInt(lightnessStr);
                } else if (opCode == MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_GET) {
                    //MESH_MSG_LIGHT_LIGHTNESS_GET , not need payload parameter
                } else {
                    log("adbSendMsg ---> LIGHT_LIGHTNESS_MSG , opcode set error , opcode = " + opCode);
                    return;
                }
                break;
            case LIGHT_LIGHTNESS_RANGE_MSG: //Light Lightness Range
                break;
            case LIGHT_CTL_MSG: //Light CTL
                if (opCode == MeshConstants.MESH_MSG_LIGHT_CTL_SET || opCode == MeshConstants.MESH_MSG_LIGHT_CTL_SET_UNACKNOWLEDGED) {
                    if (lightnessStr == null) {
                        Toast.makeText(MainActivity.this, "not set light ctl lightness parameter", Toast.LENGTH_SHORT).show();
                        log("adbSendMsg ---> LIGHT_CTL_MSG , not set light ctl lightness parameter");
                        return;
                    }
                    if(tempStr == null) {
                        Toast.makeText(MainActivity.this, "not set light ctl temperature parameter", Toast.LENGTH_SHORT).show();
                        log("adbSendMsg ---> LIGHT_CTL_MSG , not set light ctl temperature parameter");
                        return;
                    }
                    if(deltaUVStr == null) {
                        Toast.makeText(MainActivity.this, "not set light ctl deltaUV parameter", Toast.LENGTH_SHORT).show();
                        log("adbSendMsg ---> LIGHT_CTL_MSG , not set light ctl deltaUV parameter");
                        return;
                    }
                    if (MeshUtils.isMatchInternal(lightnessStr, 0x0001, 0xFFFF) &&
                        MeshUtils.isMatchInternal(tempStr, 0x0320, 0x4E20) &&
                        MeshUtils.isMatchInternal(deltaUVStr, 0x0000, 0xFFFF)) {
                        payload = new int[3];

                    } else {
                        log("adbSendMsg --->  LIGHT_CTL_MSG , param error , lightnessStr = " + lightnessStr + " , tempStr = " + tempStr + " , deltaUVStr = " + deltaUVStr);
                        return;
                    }
                    payload[0] = Integer.parseInt(lightnessStr);
                    payload[1] = Integer.parseInt(tempStr);
                    payload[2] = Integer.parseInt(deltaUVStr);

                } else if (opCode == MeshConstants.MESH_MSG_LIGHT_CTL_GET) {
                    //MESH_MSG_LIGHT_CTL_GET , not need payload parameter
                } else {
                    log("adbSendMsg ---> LIGHT_CTL_MSG , opcode set error , opcode = " + opCode);
                    return;
                }
                break;
            case LIGHT_CTL_TEMP_RANGE_MSG: //Light CTL Temperature
                break;
            case LIGHT_HSL_MSG: //Light HSL
                if (opCode == MeshConstants.MESH_MSG_LIGHT_HSL_SET|| opCode == MeshConstants.MESH_MSG_LIGHT_HSL_SET_UNACKNOWLEDGED) {
                    if (lightnessStr == null) {
                        Toast.makeText(MainActivity.this, "not set light hsl lightness parameter", Toast.LENGTH_SHORT).show();
                        log("adbSendMsg ---> LIGHT_HSL_MSG , not set light hsl lightness parameter");
                        return;
                    }
                    if(hueString == null) {
                        Toast.makeText(MainActivity.this, "not set light hsl hue parameter", Toast.LENGTH_SHORT).show();
                        log("adbSendMsg ---> LIGHT_HSL_MSG , not set light hsl hue parameter");
                        return;
                    }
                    if(saturationString == null) {
                        Toast.makeText(MainActivity.this, "not set light hsl saturation parameter", Toast.LENGTH_SHORT).show();
                        log("adbSendMsg ---> LIGHT_HSL_MSG , not set light hsl saturation parameter");
                        return;
                    }
                    if (MeshUtils.isMatchInternal(lightnessStr, 0x0001, 0xFFFF) &&
                        MeshUtils.isMatchInternal(hueString, 0x0000, 0xFFFF) &&
                        MeshUtils.isMatchInternal(saturationString, 0x0000, 0xFFFF)) {
                        payload = new int[3];
                    } else {
                        log("adbSendMsg --->  LIGHT_HSL_MSG , param error , lightnessStr = " + lightnessStr + " , tempStr = " + tempStr + " , deltaUVStr = " + deltaUVStr);
                        return;
                    }
                    payload[0] = Integer.parseInt(lightnessStr);
                    payload[1] = Integer.parseInt(hueString);
                    payload[2] = Integer.parseInt(saturationString);

                } else if (opCode == MeshConstants.MESH_MSG_LIGHT_HSL_GET) {
                    //MESH_MSG_LIGHT_HSL_GET , not need payload parameter
                } else {
                    log("adbSendMsg ---> LIGHT_HSL_MSG , opcode set error , opcode = " + opCode);
                    return;
                }

                break;
            case LIGHT_HSL_RANGE_MSG: //Light HSL Range
                break;
            default:
                break;
        }

        if(mProvisionerService != null) {
            mProvisionerService.sendMessage(mMeshMessage,node,appkeyIndex,groupAddr,payload);
            meshCurrentState = MESH_STATE_SENDMSGING;
        }
    }

    private void adbGroupSendMsg(Intent intent, String msgType) {
        log("adbGroupSendMsg ---> msgType = " + msgType);
        if(!isOpenMeshSwitch) {
            log("adbGroupSendMsg ---> mesh is off");
            return;
        }
        if (meshCurrentState != MESH_STATE_IDLE) {
            log("adbGroupSendMsg ---> mesh maybe not idle state , meshCurrentState = " + meshCurrentState);
            return;
        }
        if(!(unProvisionedAdapter.getPosition() < 0 && nodeAdapter.getPosition() < 0)) {
            Toast.makeText(MainActivity.this, "MeshProv is Provisioning or Configing , please try again later", Toast.LENGTH_SHORT).show();
            log("adbGroupSendMsg ---> MeshProv is Provisioning or Configing , please try again later");
            return;
        }
        String groupAddrStr = intent.getStringExtra("groupAddr");
        String appkeyIndexStr = intent.getStringExtra("appkeyIndex");
        String on_offStr = intent.getStringExtra("on_off");
        String lightnessStr = intent.getStringExtra("lightness");
        String hueString = intent.getStringExtra("hue");
        String saturationString = intent.getStringExtra("saturation");
        String tempStr = intent.getStringExtra("temp");
        String deltaUVStr = intent.getStringExtra("deltaUV");
        String opCodeStr = intent.getStringExtra("opCode");


        int appkeyIndex = 0x0000; // appkeyindex default is 0x0000
        int groupAddr = 0xFFFF; // groupAddr default is 0xFFFF
        Node node = null;
        if(mProvisionerService.getNodes().size() <= 0) {
            log("adbGroupSendMsg ---> ProversionService mNodes size is 0");
            return;
        }
        node = MyApplication.nodeList.get(0);
        if (node == null) {
            log("adbGroupSendMsg ---> node == null");
            return;
        }
        if(appkeyIndexStr == null) { // not set appkeyIndex
            log("adbGroupSendMsg ---> not set appkeyIndex parameter , appkeyIndex default value is 0x0000");
        }else { // set appkeyIndex
            appkeyIndex = (int)(MeshUtils.hexSrtingToDecimal(appkeyIndexStr));
            if (appkeyIndex == -1) {
                log("adbGroupSendMsg ---> set invalid appkeyIndex parameter = " + appkeyIndexStr);
                return;
            } else {
                log("adbGroupSendMsg ---> set appkeyIndex parameter = " + appkeyIndexStr);
            }
        }
        if(groupAddrStr == null) { // not set groupAddr
            log("adbGroupSendMsg ---> not set groupAddr parameter , groupAddr default value is 0xFFFF");
        }else{ // not set groupAddr
            groupAddr = (int)(MeshUtils.hexSrtingToDecimal(groupAddrStr));
            if (groupAddr != 0xFFFF) {
                log("adbGroupSendMsg ---> set groupAddr parameter error, groupAddr value is " + groupAddrStr);
                return;
            }
        }
        int[] payload = null;
        int opCode = -1;

        if (opCodeStr == null) { //if not set opcode parameter , set default opcode parameter
            switch(msgType){
                case GENERIC_ONOFF_MSG:
                    opCode = MeshConstants.MESH_MSG_GENERIC_ONOFF_SET;
                    log("adbGroupSendMsg ---> not set opCode , set default opCode = MESH_MSG_GENERIC_ONOFF_SET");
                    break;
                case LIGHT_LIGHTNESS_MSG:
                    opCode = MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_SET;
                    log("adbGroupSendMsg ---> not set opCode , set default opCode = MESH_MSG_LIGHT_LIGHTNESS_SET");
                    break;
                case LIGHT_CTL_MSG:
                    opCode = MeshConstants.MESH_MSG_LIGHT_CTL_SET;
                    log("adbGroupSendMsg ---> not set opCode , set default opCode = MESH_MSG_LIGHT_CTL_SET");
                    break;
                case LIGHT_HSL_MSG:
                    opCode = MeshConstants.MESH_MSG_LIGHT_HSL_SET;
                    log("adbGroupSendMsg ---> not set opCode , set default opCode = MESH_MSG_LIGHT_HSL_SET");
                    break;
                default:
                    log("adbGroupSendMsg ---> not set opCode , not find match msgType , msgType = " + msgType);
                    break;
            }
        } else { //set opcode parameter
            switch(opCodeStr){
                case "MESH_MSG_GENERIC_ONOFF_GET":
                    if (msgType.equals(GENERIC_ONOFF_MSG)) {
                        opCode = MeshConstants.MESH_MSG_GENERIC_ONOFF_GET;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_GENERIC_ONOFF_GET");
                    }
                    break;
                case "MESH_MSG_GENERIC_ONOFF_SET":
                    if (msgType.equals(GENERIC_ONOFF_MSG)) {
                        opCode = MeshConstants.MESH_MSG_GENERIC_ONOFF_SET;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_GENERIC_ONOFF_SET");
                    }
                    break;
                case "MESH_MSG_GENERIC_ONOFF_SET_UNRELIABLE":
                    if (msgType.equals(GENERIC_ONOFF_MSG)) {
                        opCode = MeshConstants.MESH_MSG_GENERIC_ONOFF_SET_UNRELIABLE;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_GENERIC_ONOFF_SET_UNRELIABLE");
                    }
                    break;
                case "MESH_MSG_LIGHT_LIGHTNESS_GET":
                    if (msgType.equals(LIGHT_LIGHTNESS_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_GET;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_LIGHT_LIGHTNESS_GET");
                    }
                    break;
                case "MESH_MSG_LIGHT_LIGHTNESS_SET":
                    if (msgType.equals(LIGHT_LIGHTNESS_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_SET;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_LIGHT_LIGHTNESS_SET");
                    }
                    break;
                case "MESH_MSG_LIGHT_LIGHTNESS_SET_UNACKNOWLEDGED":
                    if (msgType.equals(LIGHT_LIGHTNESS_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_SET_UNACKNOWLEDGED;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_LIGHT_LIGHTNESS_SET_UNACKNOWLEDGED");
                    }
                    break;
                case "MESH_MSG_LIGHT_LIGHTNESS_RANGE_GET":
                    if (msgType.equals(LIGHT_LIGHTNESS_RANGE_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_RANGE_GET;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_LIGHT_LIGHTNESS_RANGE_GET");
                    }
                    break;
                case "MESH_MSG_LIGHT_CTL_GET":
                    if (msgType.equals(LIGHT_CTL_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_CTL_GET;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_LIGHT_CTL_GET");
                    }
                    break;
                case "MESH_MSG_LIGHT_CTL_SET":
                    if (msgType.equals(LIGHT_CTL_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_CTL_SET;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_LIGHT_CTL_SET");
                    }
                    break;
                case "MESH_MSG_LIGHT_CTL_SET_UNACKNOWLEDGED":
                    if (msgType.equals(LIGHT_CTL_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_CTL_SET_UNACKNOWLEDGED;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_LIGHT_CTL_SET_UNACKNOWLEDGED");
                    }
                    break;
                case "MESH_MSG_LIGHT_CTL_TEMPERATURE_RANGE_GET":
                    if (msgType.equals(LIGHT_CTL_TEMP_RANGE_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_CTL_TEMPERATURE_RANGE_GET;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_LIGHT_CTL_TEMPERATURE_RANGE_GET");
                    }
                    break;
                case "MESH_MSG_LIGHT_HSL_GET":
                    if (msgType.equals(LIGHT_HSL_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_HSL_GET;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_LIGHT_HSL_GET");
                    }
                    break;
                case "MESH_MSG_LIGHT_HSL_SET":
                    if (msgType.equals(LIGHT_HSL_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_HSL_SET;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_LIGHT_HSL_SET");
                    }
                    break;
                case "MESH_MSG_LIGHT_HSL_SET_UNACKNOWLEDGED":
                    if (msgType.equals(LIGHT_HSL_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_HSL_SET_UNACKNOWLEDGED;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_LIGHT_HSL_SET_UNACKNOWLEDGED");
                    }
                    break;
                case "MESH_MSG_LIGHT_HSL_RANGE_GET":
                    if (msgType.equals(LIGHT_HSL_RANGE_MSG)) {
                        opCode = MeshConstants.MESH_MSG_LIGHT_HSL_RANGE_GET;
                        log("adbGroupSendMsg ---> set opCode = MESH_MSG_LIGHT_HSL_RANGE_GET");
                    }
                    break;
                default:
                    log("adbGroupSendMsg ---> not find match opCode");
                    break;
            }

        }


        if (opCode == -1) {
            log("adbGroupSendMsg ---> set invalid opCode parameter");
            return;
        }

        switch(msgType){
            case GENERIC_ONOFF_MSG: //Generic OnOff
                if (opCode == MeshConstants.MESH_MSG_GENERIC_ONOFF_SET || opCode == MeshConstants.MESH_MSG_GENERIC_ONOFF_SET_UNRELIABLE) {
                    if ("on".equals(on_offStr)) {
                        payload = new int[1];
                        payload[0] = 1;
                    } else if("off".equals(on_offStr)) {
                        payload = new int[1];
                        payload[0] = 0;
                    } else {
                        log("adbGroupSendMsg ---> GENERIC_ONOFF_MSG , on_offStr is invalid value , on_offStr = " + on_offStr);
                        return;
                    }

                } else if (opCode == MeshConstants.MESH_MSG_GENERIC_ONOFF_GET) {
                    //MESH_MSG_GENERIC_ONOFF_GET , not nedd payload parameter
                } else {
                    log("adbGroupSendMsg ---> GENERIC_ONOFF_MSG , opcode set error , opcode = " + opCode);
                    return;
                }
                break;
            case LIGHT_LIGHTNESS_MSG: //Light Lightness
                if (opCode == MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_SET || opCode == MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_SET_UNACKNOWLEDGED) {
                    if (lightnessStr == null) {
                        Toast.makeText(MainActivity.this, "not set Light Lightness lightness parameter", Toast.LENGTH_SHORT).show();
                        log("adbGroupSendMsg ---> LIGHT_LIGHTNESS_MSG , not set Light Lightness lightness parameter");
                        return;
                    }
                    if (MeshUtils.isMatchInternal(lightnessStr, 0x0001, 0xFFFF)) {
                        payload = new int[1];
                    } else {
                        log("adbGroupSendMsg ---> LIGHT_LIGHTNESS_MSG , param error , lightnessStr = " + lightnessStr);
                        return;
                    }
                    payload[0] = Integer.parseInt(lightnessStr);
                } else if (opCode == MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_GET) {
                    //MESH_MSG_LIGHT_LIGHTNESS_GET , not nedd payload parameter
                } else {
                    log("adbGroupSendMsg ---> LIGHT_LIGHTNESS_MSG , opcode set error , opcode = " + opCode);
                    return;
                }
                break;
            case LIGHT_LIGHTNESS_RANGE_MSG: //Light Lightness Range
                break;
            case LIGHT_CTL_MSG: //Light CTL
                if (opCode == MeshConstants.MESH_MSG_LIGHT_CTL_SET || opCode == MeshConstants.MESH_MSG_LIGHT_CTL_SET_UNACKNOWLEDGED) {
                    if (lightnessStr == null) {
                        Toast.makeText(MainActivity.this, "not set light ctl lightness parameter", Toast.LENGTH_SHORT).show();
                        log("adbGroupSendMsg ---> LIGHT_CTL_MSG , not set light ctl lightness parameter");
                        return;
                    }
                    if(tempStr == null) {
                        Toast.makeText(MainActivity.this, "not set light ctl temperature parameter", Toast.LENGTH_SHORT).show();
                        log("adbGroupSendMsg ---> LIGHT_CTL_MSG , not set light ctl temperature parameter");
                        return;
                    }
                    if(deltaUVStr == null) {
                        Toast.makeText(MainActivity.this, "not set light ctl deltaUV parameter", Toast.LENGTH_SHORT).show();
                        log("adbGroupSendMsg ---> LIGHT_CTL_MSG , not set light ctl deltaUV parameter");
                        return;
                    }
                    if (MeshUtils.isMatchInternal(lightnessStr, 0x0001, 0xFFFF) &&
                        MeshUtils.isMatchInternal(tempStr, 0x0320, 0x4E20) &&
                        MeshUtils.isMatchInternal(deltaUVStr, 0x0000, 0xFFFF)) {
                        payload = new int[3];

                    } else {
                        log("adbGroupSendMsg --->  LIGHT_CTL_MSG , param error , lightnessStr = " + lightnessStr + " , tempStr = " + tempStr + " , deltaUVStr = " + deltaUVStr);
                        return;
                    }
                    payload[0] = Integer.parseInt(lightnessStr);
                    payload[1] = Integer.parseInt(tempStr);
                    payload[2] = Integer.parseInt(deltaUVStr);

                } else if (opCode == MeshConstants.MESH_MSG_LIGHT_CTL_GET) {
                    //MESH_MSG_LIGHT_CTL_GET , not nedd payload parameter
                } else {
                    log("adbGroupSendMsg ---> LIGHT_CTL_MSG , opcode set error , opcode = " + opCode);
                    return;
                }
                break;
            case LIGHT_CTL_TEMP_RANGE_MSG: //Light CTL Temperature Range
                break;
            case LIGHT_HSL_MSG: //Light HSL
                if (opCode == MeshConstants.MESH_MSG_LIGHT_HSL_SET|| opCode == MeshConstants.MESH_MSG_LIGHT_HSL_SET_UNACKNOWLEDGED) {
                    if (lightnessStr == null) {
                        Toast.makeText(MainActivity.this, "not set light hsl lightness parameter", Toast.LENGTH_SHORT).show();
                        log("adbSendMsg ---> LIGHT_HSL_MSG , not set light hsl lightness parameter");
                        return;
                    }
                    if(hueString == null) {
                        Toast.makeText(MainActivity.this, "not set light hsl hue parameter", Toast.LENGTH_SHORT).show();
                        log("adbSendMsg ---> LIGHT_HSL_MSG , not set light hsl hue parameter");
                        return;
                    }
                    if(saturationString == null) {
                        Toast.makeText(MainActivity.this, "not set light hsl saturation parameter", Toast.LENGTH_SHORT).show();
                        log("adbSendMsg ---> LIGHT_HSL_MSG , not set light hsl saturation parameter");
                        return;
                    }
                    if (MeshUtils.isMatchInternal(lightnessStr, 0x0001, 0xFFFF) &&
                        MeshUtils.isMatchInternal(hueString, 0x0000, 0xFFFF) &&
                        MeshUtils.isMatchInternal(saturationString, 0x0000, 0xFFFF)) {
                        payload = new int[3];
                    } else {
                        log("adbSendMsg --->  LIGHT_HSL_MSG , param error , lightnessStr = " + lightnessStr + " , tempStr = " + tempStr + " , deltaUVStr = " + deltaUVStr);
                        return;
                    }
                    payload[0] = Integer.parseInt(lightnessStr);
                    payload[1] = Integer.parseInt(hueString);
                    payload[2] = Integer.parseInt(saturationString);
                } else if (opCode == MeshConstants.MESH_MSG_LIGHT_HSL_GET) {
                    //MESH_MSG_LIGHT_HSL_GET , not need payload parameter
                } else {
                    log("adbSendMsg ---> LIGHT_HSL_MSG , opcode set error , opcode = " + opCode);
                    return;
                }
                break;
            case LIGHT_HSL_RANGE_MSG: //Light HSL Range
                break;
            default:
                break;
        }

        MeshMessage mMeshMessage = new MeshMessage(opCode, node.getAddr(), null, null);
        if(mProvisionerService != null) {
            mProvisionerService.sendMessage(mMeshMessage,node,appkeyIndex,groupAddr,payload);
            meshCurrentState = MESH_STATE_SENDMSGING;
        }
    }

    private void adbProvision(Intent intent, boolean PB_GATT) {
        log("adbProvision ---> PB-GATT : " + PB_GATT);
        if(!isOpenMeshSwitch) {
            log("adbProvision ---> mesh is off");
            return;
        }
        if (meshCurrentState != MESH_STATE_IDLE) {
            log("adbProvision ---> mesh maybe not idle state , meshCurrentState = " + meshCurrentState);
            return;
        }
        if(!(unProvisionedAdapter.getPosition() < 0 && nodeAdapter.getPosition() < 0)) {
            Toast.makeText(MainActivity.this, "MeshProv is Provisioning or Configing , please try again later", Toast.LENGTH_SHORT).show();
            log("adbProvision ---> MeshProv is Provisioning or Configing , please try again later");
            return;
        }
        String mGattAddr = intent.getStringExtra("gattAddr");
        String uuidStr = intent.getStringExtra("uuid");
        log("adbProvision ---> mGattAddr = " + mGattAddr + "uuidStr = " + uuidStr);
        UnProvisionedDevice mUnProvisionedDevice = null;
        if(PB_GATT) {
            if(mGattAddr == null) {
                Toast.makeText(MainActivity.this, "not set PB-GATT ddress", Toast.LENGTH_SHORT).show();
                log("adbProvision ---> not set PB-GATT ddress");
                return;
            }else{
                mUnProvisionedDevice = unProvisionedAdapter.getUnprovisionedDeviceByGATTAddr(mGattAddr);
            }
        }else{
            if(uuidStr == null) {
                Toast.makeText(MainActivity.this, "not set PB-ADV UUID", Toast.LENGTH_SHORT).show();
                log("adbProvision ---> not set PB-ADV UUID");
                return;
            }else{
                mUnProvisionedDevice = unProvisionedAdapter.getUnprovisionedDeviceByUUID(MeshUtils.StringToIntArray(uuidStr));
            }
        }
        if(mUnProvisionedDevice == null) {
            Toast.makeText(MainActivity.this, "not found unprovisioned device", Toast.LENGTH_SHORT).show();
            log("adbProvision ---> not found unprovisioned device");
            return;
        }
        int position = unProvisionedAdapter.getPositionByUnProvisioned(mUnProvisionedDevice);
        if(position == -1) {
            log("adbProvision ---> position = -1");
            return;
        }
        unProvisionedAdapter.setPosition(position);
        if (isScanning) {
            mProvisionerService.stopUnProvsionScan();
            mProgressBar.setVisibility(View.GONE);
            isScanning = false;
        }
        mProvisionerService.inviteProvisioning(mUnProvisionedDevice.getBearerType(),mUnProvisionedDevice.getUUID(),
            mUnProvisionedDevice.getAddress(),
            mUnProvisionedDevice.getAddressType(),
            mUnProvisionedDevice.getGattDevName(),
            0,
            0);
        meshCurrentState = MESH_STATE_PROVISIONING;

    }

    private void adbRemoveNode(Intent intent, boolean PB_GATT) {
        log("adbRemoveNode ---> PB-GATT : " + PB_GATT);
        if(!isOpenMeshSwitch) {
            log("mesh is off");
            return;
        }
        if (meshCurrentState != MESH_STATE_IDLE) {
            log("adbRemoveNode ---> mesh maybe not idle state , meshCurrentState = " + meshCurrentState);
            return;
        }
        if(!(unProvisionedAdapter.getPosition() < 0 && nodeAdapter.getPosition() < 0)) {
            Toast.makeText(MainActivity.this, "MeshProv is Provisioning or Configing , please try again later", Toast.LENGTH_SHORT).show();
            log("MeshProv is Provisioning or Configing , please try again later");
            return;
        }
        String mGattAddr = intent.getStringExtra("gattAddr");
        String uuidStr = intent.getStringExtra("uuid");
        Node node = null;
        if(PB_GATT) {
            if(mGattAddr == null) {
                Toast.makeText(MainActivity.this, "not set PB-GATT ddress", Toast.LENGTH_SHORT).show();
                log("not set PB-GATT ddress");
                return;
            }else{
                if(MyApplication.nodeList.size() <= 0) {
                    log("node list is null");
                    return;
                }else{
                    int i = 0;
                    for(i = 0; i < MyApplication.nodeList.size(); i++) {
                        if(MyApplication.nodeList.get(i).getNodeBearer() == MeshConstants.MESH_BEARER_GATT) {
                            log("node Gatt address = " + MyApplication.nodeList.get(i).getGattAddr());
                            log("input gatt address = " + mGattAddr);
                            if(mGattAddr.equals(MyApplication.nodeList.get(i).getGattAddr())) {
                                node = MyApplication.nodeList.get(i);
                                break;
                            }
                        }
                    }
                    if(i == MyApplication.nodeList.size()) {
                        log("not find match node in node list");
                        return;
                    }
                }
            }
        }else{
            if(uuidStr == null) { // not set PB-ADV uuid
                Toast.makeText(MainActivity.this, "not set PB-ADV UUID", Toast.LENGTH_SHORT).show();
                log("not set PB-ADV UUID");
                return;
            }else { // set PB-ADV uuid
                if(MyApplication.nodeList.size() <= 0) {
                    log("node list is null");
                    return;
                }else {
                    int i = 0;
                    for(i = 0; i < MyApplication.nodeList.size(); i++) {
                        if(MyApplication.nodeList.get(i).getNodeBearer() == MeshConstants.MESH_BEARER_ADV) {
                            log("node uuid = " + MeshUtils.intArrayToString(MyApplication.nodeList.get(i).getUUID(), true));
                            log("input uuid = " + MeshUtils.intArrayToString(MeshUtils.StringToIntArray(uuidStr), true));
                            if(Arrays.equals(MyApplication.nodeList.get(i).getUUID(), MeshUtils.StringToIntArray(uuidStr))) {
                                node = MyApplication.nodeList.get(i);
                                break;
                            }
                        }
                    }
                    if(i == MyApplication.nodeList.size()) {
                        log("not find match node in node list");
                        return;
                    }
                }
            }

        }
        if(node == null) {
            log("node == null");
            return;
        }
        dialogKeyrefresh = new Dialog(MainActivity.this, R.style.Custom_Progress);
        dialogKeyrefresh.setContentView(R.layout.mul_picture_progressbar);
        dialogKeyrefresh.getWindow().getAttributes().gravity = Gravity.CENTER;
        dialogKeyrefresh.setCanceledOnTouchOutside(false);
        dialogKeyrefresh.setCancelable(false);
        dialogKeyrefresh.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                log("adbRemoveNode ---> keyrefresh dialog dismiss");
                meshCurrentState = MESH_STATE_IDLE;
            }

        });
        dialogKeyrefresh.show();
        meshCurrentState = MESH_STATE_KEYREFRESHING;
        mProvisionerService.removeNode(node, true);
        nodeAdapter.removeNode(node);
    }

    private void adbGattConnectOrDisConnect(Intent intent, boolean connect) {
        log("adbGattConnectOrDisConnect ---> connect: " + connect);
        if(!isOpenMeshSwitch) {
            log("mesh is off");
            return;
        }
        if (meshCurrentState != MESH_STATE_IDLE) {
            log("adbGattConnectOrDisConnect ---> mesh maybe not idle state , meshCurrentState = " + meshCurrentState);
            return;
        }
        if(!(unProvisionedAdapter.getPosition() < 0 && nodeAdapter.getPosition() < 0)) {
            Toast.makeText(MainActivity.this, "MeshProv is Provisioning or Configing , please try again later", Toast.LENGTH_SHORT).show();
            log("adbGattConnectOrDisConnect ---> MeshProv is Provisioning or Configing , please try again later");
            return;
        }
        String mgattAddress = intent.getStringExtra("gattAddr");
        Node nodeGatt = null;
        if(mgattAddress == null){
            log("adbGattConnectOrDisConnect ---> PB-GATT not set gattAddr");
            return;
        }
        if(MyApplication.nodeList.size() <= 0) {
            log("adbGattConnectOrDisConnect ---> node list is null");
            return;
        }else{
            int i = 0;
            for(i = 0; i < MyApplication.nodeList.size(); i++) {
                if(MyApplication.nodeList.get(i).getNodeBearer() == MeshConstants.MESH_BEARER_GATT) {
                    log("adbGattConnectOrDisConnect ---> node Gatt address = " + MyApplication.nodeList.get(i).getGattAddr());
                    log("adbGattConnectOrDisConnect ---> input gatt address = " + mgattAddress);
                    if(mgattAddress.equals(MyApplication.nodeList.get(i).getGattAddr())) {
                        nodeGatt = MyApplication.nodeList.get(i);
                        break;
                    }
                }
            }
            if(i == MyApplication.nodeList.size()) {
                log("adbGattConnectOrDisConnect ---> not find match node in node list");
                return;
            }
        }
        if(nodeGatt == null) {
            log("adbGattConnectOrDisConnect ---> nodeGatt == null");
            return;
        }
        if(nodeGatt.getNodeBearer() != MeshConstants.MESH_BEARER_GATT){
            log("adbGattConnectOrDisConnect ---> PB-ADV");
            return;
        }
        if(connect) {
            dialogGattConnect = new Dialog(MainActivity.this, R.style.Custom_Progress);
            dialogGattConnect.setContentView(R.layout.mul_picture_progressbar);
            TextView tip = (TextView)dialogGattConnect.findViewById(R.id.tipTextView);
            tip.setText("PB-GATT Connect...");
            dialogGattConnect.getWindow().getAttributes().gravity = Gravity.CENTER;
            dialogGattConnect.setCanceledOnTouchOutside(false);
            dialogGattConnect.setCancelable(false);
            dialogGattConnect.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    log("adbGattConnectOrDisConnect ---> gatt connect dialog dismiss");
                    meshCurrentState = MESH_STATE_IDLE;
                }

            });
            dialogGattConnect.show();
            meshCurrentState = MESH_STATE_GATTCONNECTING;
            mProvisionerService.GattConnect(nodeGatt);
        }else {
            mProvisionerService.GattDisconnect(nodeGatt);
        }
    }

    private void adbGroupProvision() {
        log("adbGroupProvision()");
        if (!isOpenMeshSwitch) {
            log("mesh is off");
            return;
        }
        if (meshCurrentState != MESH_STATE_IDLE) {
            log("adbGroupProvision ---> mesh maybe not idle state , meshCurrentState = " + meshCurrentState);
            return;
        }
        deviceSize = mProvisionerService.uuidList.size();
        log("adbGroupProvision ---> deviceSize == " + deviceSize);
        if (deviceSize == 0) {
            log("adbGroupProvision ---> maybe no config device UUID or config incorrect UUID in mesh_properties.txt");
            return;
        }
        if (isScanning) {
            mProvisionerService.stopUnProvsionScan();
            mProgressBar.setVisibility(View.GONE);
            isScanning = false;
        }
        provision();
    }

    private void provision() {
        log("provision()");
        if (!isOpenMeshSwitch) {
            log("mesh is off");
            return;
        }
        for (int i = uuidIndex; i < deviceSize; i++) {
            uuidIndex++;
            uuid = MeshUtils.StringToIntArray(mProvisionerService.uuidList.get(i));
            log("uuid = " + MeshUtils.intArrayToString(uuid, true) + " , index = " + uuidIndex);
            if (uuid == null) { //mesh_properties.txt contain invalid uuid
                if (i == (deviceSize - 1)) { // last uuid device
                    printfGroupProvResult();
                    return;
                } else {
                    continue;
                }
            }
            UnProvisionedDevice mUnProvisionedDevice = unProvisionedAdapter.getUnprovisionedDeviceByUUID(uuid);
            if (mUnProvisionedDevice == null) { // uuid not found in unprovisioned device list
                log("not scan uuid : " + MeshUtils.intArrayToString(uuid, true));
                uuidNoneExistent.add(uuid);
                if (i == (deviceSize - 1)) { // last uuid device
                    printfGroupProvResult();
                }
            } else {
                int position = unProvisionedAdapter.getPositionByUnProvisioned(mUnProvisionedDevice);
                if(position == -1) {
                    log("position = -1 , it not should happen");
                } else {
                    unProvisionedAdapter.setPosition(position);
                    mProvisionerService.inviteProvisioning(mUnProvisionedDevice.getBearerType(),mUnProvisionedDevice.getUUID(),
                        mUnProvisionedDevice.getAddress(),
                        mUnProvisionedDevice.getAddressType(),
                        mUnProvisionedDevice.getGattDevName(),
                        0,
                        0);
                    meshCurrentState = MESH_STATE_PROVISIONING;
                    return;
                }
            }
        }
    }

    private void printfGroupProvResult() {
        log("printfGroupProvResult()");
        log("devicesize = " + deviceSize);
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < uuidNoneExistent.size(); i++) {
            str.append("uuidNoneExistent ").append(i + 1).append(" = ").append(MeshUtils.intArrayToString(uuidNoneExistent.get(i), true));
            log(str.toString());
            str.delete(0, str.length());
        }
        for (int i = 0; i < uuidProFail.size(); i++) {
            str.append("uuidProFail ").append(i + 1).append(" = ").append(MeshUtils.intArrayToString(uuidProFail.get(i), true));
            log(str.toString());
            str.delete(0, str.length());
        }
        for (int i = 0; i < uuidConfigFail.size(); i++) {
            str.append("uuidConfigFail ").append(i + 1).append(" = ").append(MeshUtils.intArrayToString(uuidConfigFail.get(i), true));
            log(str.toString());
            str.delete(0, str.length());
        }
        for (int i = 0; i < uuidConfigSuccess.size(); i++) {
            str.append("uuidConfigSuccess ").append(i + 1).append(" = ").append(MeshUtils.intArrayToString(uuidConfigSuccess.get(i), true));
            log(str.toString());
            str.delete(0, str.length());
        }
        str = null;
        uuidNoneExistent.clear();
        uuidProFail.clear();
        uuidConfigFail.clear();
        uuidConfigSuccess.clear();
        uuidIndex = 0;
        uuid = null;
        deviceSize = 0;
    }

    private void adbGroupNodeReset(int state) {
        log("adbGroupNodeReset() ---> state = " + state);
        if (!isOpenMeshSwitch) {
            log("adbGroupNodeReset() ---> mesh is off");
            return;
        }
        if ((state == PARTIAL_NODE_RESET) && meshCurrentState != MESH_STATE_IDLE) {
            log("adbGroupNodeReset ---> mesh maybe not idle state , meshCurrentState = " + meshCurrentState);
            return;
        }
        switch (state) {
            case ALL_NODE_RESET:
                nodeResetSize = mProvisionerService.getNodes().size();
                break;
            case PARTIAL_NODE_RESET:
                nodeResetSize = mProvisionerService.nodeResetList.size();
                break;
            case SINGLE_NODE_RESET:
                nodeResetSize = nodePosition + 1;
                break;
            default:
               break;
        }
        log("adbGroupNodeReset() ----> nodeResetSize == " + nodeResetSize);
        if (nodeResetSize == 0) {
            return;
        }
        if (isScanning) {
            mProvisionerService.stopUnProvsionScan();
            mProgressBar.setVisibility(View.GONE);
            isScanning = false;
        }
        nodeReset(state);
    }

    private void nodeReset(int state) {
        log("nodeReset() , state = " + state);
        nodeResetList.clear();
        nodeAllResetList.clear();
        nodeResetNoneExistent.clear();
        switch (state) {
            case ALL_NODE_RESET:
                for(Map.Entry<Integer,Node> entry:mProvisionerService.getNodes().entrySet()) {
                    Node node = entry.getValue();
                    if (node != null) {
                        if (node.getCmdAddFriDev()) {
                            continue;
                        }
                        nodeAllResetList.add(node);
                        if (node.isConfigSuccess()) {
                            nodeResetList.add(node);
                        } else {
                            log("all Node reset , but this node config fail , uuid = " + MeshUtils.intArrayToString(node.getUUID(),true) + " , gattAddr = " + node.getGattAddr());
                        }
                        nodeAdapter.removeNode(node);
                    } else {
                        log("this node not found");
                    }
                }
                break;
            case PARTIAL_NODE_RESET:
                for (int i = 0; i < nodeResetSize; i++) {
                    nodeResetUUID = MeshUtils.StringToIntArray(mProvisionerService.nodeResetList.get(i));
                    log("nodeResetUUID = " + MeshUtils.intArrayToString(nodeResetUUID, true));
                    Node node = mProvisionerService.getNodeByUUID(nodeResetUUID);
                    if (node == null) {
                        log("this node not found");
                        nodeResetNoneExistent.add(nodeResetUUID);
                    } else {
                        if (node.getCmdAddFriDev()) {
                            continue;
                        }
                        nodeAllResetList.add(node);
                        if (node.isConfigSuccess()) {
                            nodeResetList.add(node);
                        } else {
                            log("partial Node reset , but this node config fail , uuid = " + MeshUtils.intArrayToString(node.getUUID(),true));
                        }
                        nodeAdapter.removeNode(node);
                    }
                }

                break;
            case SINGLE_NODE_RESET:
                Node node = (MyApplication.nodeList).get(nodePosition);
                if (node == null) {
                    log("this node not found");
                    nodeResetNoneExistent.add(nodeResetUUID);
                } else {
                    if (node.getCmdAddFriDev()) {
                        break;
                    }
                    nodeAllResetList.add(node);
                    if (node.isConfigSuccess()) {
                        nodeResetList.add(node);
                    } else {
                        log("single Node reset , but this node config fail , uuid = " + MeshUtils.intArrayToString(node.getUUID(),true)+ " , gattAddr = " + node.getGattAddr());
                    }
                    nodeAdapter.removeNode(node);
                }
                break;
            default:
               break;
        }

        log("nodeAllResetList size = " + nodeAllResetList.size() + " , nodeResetList size = " + nodeResetList.size());
        if (nodeAllResetList.size() == 0) {
            log("group node reset list is null");
            return;
        }
        mProvisionerService.groupConfigMsgNodeReset(nodeAllResetList);
        remainNodeList.clear();
        for(Map.Entry<Integer, Node> entry:mProvisionerService.getNodes().entrySet()) {
            Node node = entry.getValue();
            if (node.getCmdAddFriDev()) {
                continue;
            }
            for (int j = 0; j < nodeAllResetList.size(); j++) {
                if (node.getAddr() == nodeAllResetList.get(j).getAddr()) {
                    break;
                }
            }
            remainNodeList.add(node);
        }
        if (nodeResetList.size() > 0) {
            dialogNodeReset = new Dialog(this, R.style.Custom_Progress);
            dialogNodeReset.setContentView(R.layout.mul_picture_progressbar);
            TextView tip = (TextView)dialogNodeReset.findViewById(R.id.tipTextView);
            if (state == ALL_NODE_RESET) {
                tip.setText("Group NodeReset...");
            } else if (state == PARTIAL_NODE_RESET) {
                tip.setText("Group NodeReset...");
            } else if (state == SINGLE_NODE_RESET) {
                tip.setText("NodeReset...");
            }
            dialogNodeReset.getWindow().getAttributes().gravity = Gravity.CENTER;
            dialogNodeReset.setCanceledOnTouchOutside(false);
            dialogNodeReset.setCancelable(false);
            dialogNodeReset.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    log("nodeReset ---> dialogNodeReset dismiss");
                    if (null != groupNodeResetTimer) {
                        groupNodeResetTimer.cancel();
                        groupNodeResetTimer = null;
                    }
                    if (null != dialogNodeReset) {
                        dialogNodeReset = null;
                    }
                    //meshCurrentState = MESH_STATE_IDLE;
                }

            });
            dialogNodeReset.show();
            meshCurrentState = MESH_STATE_NODERESETING;
            groupNodeResetTimer = new Timer();
            groupNodeResetTimer.schedule(new TimerTask() {
                public void run() {
                    try {
                        log("groupNodeReset timeout , not all node ack reset");
                        StringBuilder nodeResetStr = new StringBuilder("reset node :");
                        StringBuilder ackNodeResetStr = new StringBuilder("ack reset node :");
                        for (int i = 0; i < nodeResetList.size(); i++) {
                            nodeResetStr.append("  ").append(MeshUtils.decimalToHexString("%04X", nodeResetList.get(i).getAddr()));
                        }
                        for(Map.Entry<Integer,Node> entry:ackNodeResetList.entrySet()) {
                            int address = entry.getKey();
                            ackNodeResetStr.append("  ").append(MeshUtils.decimalToHexString("%04X", address));
                        }
                        log(nodeResetStr.toString());
                        log(ackNodeResetStr.toString());
                        nodeResetStr = null;
                        ackNodeResetStr = null;
                        nodeResetList.clear();
                        ackNodeResetList.clear();
                        if (null != dialogNodeReset) {
                            dialogNodeReset.dismiss();
                            dialogNodeReset = null;
                        }
                        if (null != groupNodeResetTimer) {
                            groupNodeResetTimer.cancel();
                            groupNodeResetTimer = null;
                        }
                        meshCurrentState = MESH_STATE_IDLE;
                        keyrefresh(0, remainNodeList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, nodeResetList.size()*2000*5 + 5000);
        } else { //directly keyrefresh
            keyrefresh(0, remainNodeList);
        }
    }

    public Node getNodeByAddr(int src) {
        for (int i = 0; i < nodeResetList.size(); i++) {
            Node node = nodeResetList.get(i);
            if (node.getAddr() == src) {
                return node;
            }
        }
        return null;
    }

    public void keyrefresh(int netKeyIndex, ArrayList<Node> targetNodes) {
        log("keyrefresh ---> netKeyIndex = " + netKeyIndex + " , targetNodes size = " + targetNodes.size());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogKeyrefresh = new Dialog(MainActivity.this, R.style.Custom_Progress);
                dialogKeyrefresh.setContentView(R.layout.mul_picture_progressbar);
                dialogKeyrefresh.getWindow().getAttributes().gravity = Gravity.CENTER;
                dialogKeyrefresh.setCanceledOnTouchOutside(false);
                dialogKeyrefresh.setCancelable(false);
                dialogKeyrefresh.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        log("keyrefresh ---> keyrefresh dialog dismiss");
                        meshCurrentState = MESH_STATE_IDLE;
                    }

                });
                dialogKeyrefresh.show();
                meshCurrentState = MESH_STATE_KEYREFRESHING;
                if(targetNodes.size() > 0){
                    mProvisionerService.keyRefreshStart(keyfresh_netkeyIndex, targetNodes);
                } else {
                    mProvisionerService.keyRefreshStart(keyfresh_netkeyIndex, null);
                }
            }
        });
    }

    private void adbGetProvisionTime(Intent intent , boolean PB_GATT) {
        log("adbProvision , PB-GATT : " + PB_GATT);
        if(!isOpenMeshSwitch) {
            log("mesh is off");
            return;
        }
        String mGattAddr = intent.getStringExtra("gattAddr");
        String uuidStr = intent.getStringExtra("uuid");
        log("mGattAddr = " + mGattAddr + "uuidStr = " + uuidStr);
        Node node = null;
        if (mProvisionerService.getNodes().size() == 0) {
            log("node list is null");
            return;
        }
        if (PB_GATT) {
            if (mGattAddr == null) {
                Toast.makeText(MainActivity.this, "not set PB-GATT ddress", Toast.LENGTH_SHORT).show();
                log("not set PB-GATT ddress");
                return;
            }
            node = mProvisionerService.getNodeByGattAddr(mGattAddr);
        } else {
            if (uuidStr == null) {
                Toast.makeText(MainActivity.this, "not set PB-ADV UUID", Toast.LENGTH_SHORT).show();
                log("not set PB-ADV UUID");
                return;
            }
            node = mProvisionerService.getNodeByUUID(MeshUtils.StringToIntArray(uuidStr));
        }
        if (node == null) {
            log("not find match node in node list");
            return;
        }
        double mProvisionTime = node.getProvisioningTime();
        log("mProvisionTime = " + mProvisionTime);
        StringBuilder timeStr = new StringBuilder("provision time : ");
        Toast.makeText(MainActivity.this, timeStr.append(mProvisionTime).append("s").toString(), Toast.LENGTH_SHORT).show();
    }

    private void adbGetConfigTime(Intent intent , boolean PB_GATT) {
        log("adbProvision , PB-GATT : " + PB_GATT);
        if(!isOpenMeshSwitch) {
            log("mesh is off");
            return;
        }
        String mGattAddr = intent.getStringExtra("gattAddr");
        String uuidStr = intent.getStringExtra("uuid");
        log("mGattAddr = " + mGattAddr + "uuidStr = " + uuidStr);
        Node node = null;
        if (mProvisionerService.getNodes().size() == 0) {
            log("node list is null");
            return;
        }
        if (PB_GATT) {
            if (mGattAddr == null) {
                Toast.makeText(MainActivity.this, "not set PB-GATT ddress", Toast.LENGTH_SHORT).show();
                log("not set PB-GATT ddress");
                return;
            }
            node = mProvisionerService.getNodeByGattAddr(mGattAddr);
        } else {
            if (uuidStr == null) {
                Toast.makeText(MainActivity.this, "not set PB-ADV UUID", Toast.LENGTH_SHORT).show();
                log("not set PB-ADV UUID");
                return;
            }
            node = mProvisionerService.getNodeByUUID(MeshUtils.StringToIntArray(uuidStr));
        }
        if (node == null) {
            log("not find match node in node list");
            return;
        }
        double mConfigTime = node.getConfigTime();
        log("mConfigTime = " + mConfigTime);
        StringBuilder timeStr = new StringBuilder("config time : ");
        Toast.makeText(MainActivity.this, timeStr.append(mConfigTime).append("s").toString(), Toast.LENGTH_SHORT).show();
    }

    private void adbGetHBMaxLostNumber(Intent intent , boolean PB_GATT) {
        log("adbProvision , PB-GATT : " + PB_GATT);
        if(!isOpenMeshSwitch) {
            log("mesh is off");
            return;
        }
        String mGattAddr = intent.getStringExtra("gattAddr");
        String uuidStr = intent.getStringExtra("uuid");
        log("mGattAddr = " + mGattAddr + "uuidStr = " + uuidStr);
        Node node = null;
        if (mProvisionerService.getNodes().size() == 0) {
            log("node list is null");
            return;
        }
        if (PB_GATT) {
            if (mGattAddr == null) {
                Toast.makeText(MainActivity.this, "not set PB-GATT ddress", Toast.LENGTH_SHORT).show();
                log("not set PB-GATT ddress");
                return;
            }
            node = mProvisionerService.getNodeByGattAddr(mGattAddr);
        } else {
            if (uuidStr == null) {
                Toast.makeText(MainActivity.this, "not set PB-ADV UUID", Toast.LENGTH_SHORT).show();
                log("not set PB-ADV UUID");
                return;
            }
            node = mProvisionerService.getNodeByUUID(MeshUtils.StringToIntArray(uuidStr));
        }
        if (node == null) {
            log("not find match node in node list");
            return;
        }
        int mHbMaxLostNumber = node.getMaxLost();
        log("mHbMaxLostNumber = " + mHbMaxLostNumber);
        StringBuilder timeStr = new StringBuilder("HeartBeat Max Lost Number : ");
        Toast.makeText(MainActivity.this, timeStr.append(mHbMaxLostNumber).toString(), Toast.LENGTH_SHORT).show();
    }


    private void adbGetNodeStatus(Intent intent , boolean PB_GATT) {
        log("adbProvision , PB-GATT : " + PB_GATT);
        if(!isOpenMeshSwitch) {
            log("mesh is off");
            return;
        }
        String mGattAddr = intent.getStringExtra("gattAddr");
        String uuidStr = intent.getStringExtra("uuid");
        log("mGattAddr = " + mGattAddr + "uuidStr = " + uuidStr);
        Node node = null;
        if (mProvisionerService.getNodes().size() == 0) {
            log("node list is null");
            return;
        }
        if (PB_GATT) {
            if (mGattAddr == null) {
                Toast.makeText(MainActivity.this, "not set PB-GATT ddress", Toast.LENGTH_SHORT).show();
                log("not set PB-GATT ddress");
                return;
            }
            node = mProvisionerService.getNodeByGattAddr(mGattAddr);
        } else {
            if (uuidStr == null) {
                Toast.makeText(MainActivity.this, "not set PB-ADV UUID", Toast.LENGTH_SHORT).show();
                log("not set PB-ADV UUID");
                return;
            }
            node = mProvisionerService.getNodeByUUID(MeshUtils.StringToIntArray(uuidStr));
        }
        if (node == null) {
            log("not find match node in node list");
            return;
        }
        StringBuilder statusStr = new StringBuilder("Node Status : ");
        if(node.getActiveStatus() == 1) {
            statusStr.append("Online");
        }else if (node.getActiveStatus() == 0){
            statusStr.append("Offline");
        } else {
            statusStr.append("Unknown");
        }
        log("statusStr = " + statusStr);
        Toast.makeText(MainActivity.this, statusStr.toString(), Toast.LENGTH_SHORT).show();
    }


    private NoDoubleOnItemClickListener mNoDoubleOnItemClickListener = new NoDoubleOnItemClickListener() {
        @Override
        public void onNoDoubleClick(AdapterView<?> parent, View view, int position, long id) {
            log("unProvisionedAdapter.getPosition() = " + unProvisionedAdapter.getPosition());
            log("provisionedAdapter.getPosition() = " + nodeAdapter.getPosition());
            switch (parent.getId()) {
                case R.id.unprovisioned:
                    if(unProvisionedAdapter.getPosition() < 0 && nodeAdapter.getPosition() < 0) {
                        UnProvisionedDevice mUnProvisionedDevice = unProvisionedAdapter.getUnprovisionedDevice(position);
                        if (mUnProvisionedDevice == null) {
                            log("mNoDoubleOnItemClickListener , not fonund unProvisionedDevice , position = " + position);
                            return;
                        }
                        showUnprovisionedFragment(position, mUnProvisionedDevice, mProvisionerService.getNetKeyIndexs(), mProvisionerService.getAppkeyIndexMaps());
                    }
                    break;
                case R.id.node:
                    if(unProvisionedAdapter.getPosition() < 0 && nodeAdapter.getPosition() < 0 ) {
                        ArrayList<MeshMessage> messages = mProvisionerService.getSupportedMessages((MyApplication.nodeList).get(position));
                        ArrayList<Integer> groupAddrDatas = mProvisionerService.getGroupAddrList();
                        MyApplication.netkeyindex = mProvisionerService.getNetKeyCnt();
                        MyApplication.appkeyindex = mProvisionerService.getAppKeyCnt();
                        showProvisionedDialogFragment(position,messages,groupAddrDatas);
                    }
                    break;
                default:
                   break;
            }
        }
     };


    private BluetoothMeshListener mBluetoothMeshListener = new BluetoothMeshListener() {
        @Override
        public void onMeshEnabled() {
            log("onMeshEnabled()");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProvisionerService.setEnabled(llState,true);
                    mSlideSwitch.setSlideable(true);
                    llState.setClickable(true);
                    llClose.setVisibility(View.GONE);
                    llOpen.setVisibility(View.VISIBLE);
                    closeDescription.setText(R.string.close_description);

                    mMeshFilterBean = (MeshFilterBean)mACache.getAsObject(MESH_FILTER);
                    if (mMeshFilterBean != null && mProvisionerService != null) {
                        mProvisionerService.enableMeshFilter(mMeshFilterBean.getPbAdvStatus(),
                                                             mMeshFilterBean.getMeshMsgStatus(),
                                                             mMeshFilterBean.getUnprovBeaconStatus(),
                                                             mMeshFilterBean.getSecureBeaconStatus());
                    }
                    mMeshFilterWithNIDBean = (MeshFilterWithNIDBean)mACache.getAsObject(MESH_FILTER_WITH_NID);
                    if (mMeshFilterWithNIDBean != null && mProvisionerService != null) {
                        mProvisionerService.enableMeshFilterWithNID(mMeshFilterWithNIDBean.getPbAdvStatus(),
                                                                    mMeshFilterWithNIDBean.getMeshMsgStatus(),
                                                                    mMeshFilterWithNIDBean.getMeshMessageNetIndex(),
                                                                    mMeshFilterWithNIDBean.getUnprovBeaconStatus(),
                                                                    mMeshFilterWithNIDBean.getSecureBeaconStatus(),
                                                                    mMeshFilterWithNIDBean.getSecureBeaconNetIndex());
                    }

                    //mProvisionerService.setMeshMode(MeshConstants.MESH_MODE_STANDBY);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProvisionerService.startUnProvsionScan();
                    isOpenMeshSwitch = true;
                    isScanning = true;
                    Toast.makeText(MainActivity.this, "mesh on", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public void inviteProvisioning(int[] UUID,int bearerType,String address,int addrType,String devName, int netKeyIndex,int appKeyIndex,int position) {
            log("inviteProvisioning() , UUID = " + MeshUtils.intArrayToString(UUID, true) + " , Gatt address = " + address + " , bearerType = " + bearerType + " , position = " + position);
            unProvisionedAdapter.setPosition(position);
            if (isScanning) {
                mProvisionerService.stopUnProvsionScan();
                mProgressBar.setVisibility(View.GONE);
                isScanning = false;
            }
            //MyApplication.isProvisioning = true;
            //MyApplication.provisioningTime = System.currentTimeMillis();
            mProvisionerService.inviteProvisioning(bearerType, UUID, address, addrType, devName, netKeyIndex, appKeyIndex);
            meshCurrentState = MESH_STATE_PROVISIONING;
            //if(null != mRunnable){
                //mHandler.removeCallbacks(mRunnable);
            //}
            //inviteProvisioningTimeout();
        }

        @Override
        public void onProvDone(int address, int[] deviceKey, boolean success, boolean gatt_bearer, double provision_time) {
            log("onProvDone() , address = " + MeshUtils.decimalToHexString("%04x", address) + " , success = " + success + " , gatt_bearer = " + gatt_bearer + " , provision_time = " + provision_time);
            if (unProvisionedAdapter.getPosition() == -1) {
                log("may be provision timeout !!!");
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //MyApplication.provisioningTime = System.currentTimeMillis() - MyApplication.provisioningTime;
                    //MyApplication.configTime = System.currentTimeMillis();
                    //double time = MeshUtils.getDoubleDecimal(MyApplication.provisioningTime*1.0/1000);
                    //if(null != mRunnable){
                        //mHandler.removeCallbacks(mRunnable);
                    //}
                    Node mNode = mProvisionerService.getNodeByAddr(address);
                    StringBuffer buffer = new StringBuffer();
                    if (success) {
                        log(getResources().getString(R.string.prov_success));
                        UnProvisionedDevice mUnProvisionedDevice = unProvisionedAdapter.getUnprovisionedDevice(unProvisionedAdapter.getPosition());
                        if (mUnProvisionedDevice == null) {
                            log("not found UnProvisionedDevice , but it should not happen");
                            return;
                        }
                        unProvisionedAdapter.removeUnprovisionedDevice(mUnProvisionedDevice);
                        unProvisionedAdapter.setPosition(UnprovisionedAdapter.ALL_ENABLE,false);
                        //log("node = " + mProvisionerService.getNodeByAddr(address));
                        //log("uuid = " + mProvisionerService.getNodeByAddr(address).getUUID());
                        //log("onProvDone MyApplication nodeList size = " + MyApplication.nodeList.size());
                        nodeAdapter.addNode(mNode);
                        //log("onProvDone MyApplication nodeList size = " + MyApplication.nodeList.size());
                        nodeAdapter.setPosition((MyApplication.nodeList).size()-1);
                        nodeRecyclerView.scrollToPosition(nodeAdapter.getItemCount() - 1);
                        MyApplication.isProvisioning = false;
                        meshCurrentState = MESH_STATE_CONFIGING;
                        buffer.setLength(0);
                        if (gatt_bearer) {
                            buffer.append(mNode.getGattAddr());
                        } else {
                            buffer.append(MeshUtils.intArrayToString(mNode.getUUID(), true));
                        }
                        buffer.append(" , provision success , time cost : ").append(provision_time).append("s");
                        log(buffer.toString());
                        buffer.setLength(0);
                        buffer.append("provision success\n").append("time cost: ").append(provision_time).append("s");
                        Toast.makeText(MainActivity.this, buffer.toString(), Toast.LENGTH_SHORT).show();

                    } else {
                        log(getResources().getString(R.string.prov_fail));
                        MyApplication.isProvisioning = false;
                        if (mProvisionerService != null) {
                            if (mProvisionerService.getMeshMode() != MeshConstants.MESH_MODE_STANDBY) {
                                mProvisionerService.setMeshMode(MeshConstants.MESH_MODE_STANDBY); // provision fail , need set mesh standby mode
                            }
                        }
                        buffer.setLength(0);
                        buffer.append("provision fail\n").append("time cost: ").append(provision_time).append("s");
                        Toast.makeText(MainActivity.this, buffer.toString(), Toast.LENGTH_LONG).show();
                        unProvisionedAdapter.setPosition(UnprovisionedAdapter.ALL_ENABLE);
                        meshCurrentState = MESH_STATE_IDLE;
                        if (uuidIndex > 0) { // group provision
                            uuidProFail.add(uuid);
                            if (uuidIndex < deviceSize) {
                                // if provision fail in group provision , it should wait 10s
                                try {
                                    Thread.sleep(10*1000);
                                    provision();
                                } catch (Exception e) {

                                }
                            } else {
                                printfGroupProvResult();
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void onProvScanComplete() {
            log("onProvScanComplete()");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.GONE);
                    isScanning = false;
                }
            });
        }

        @Override
        public void onScanUnProvDevice(int[] uuid, int oobInfom, int[] uriHash, int rssi) {
            log("onScanUnProvDevice() uuid = " + MeshUtils.intArrayToString(uuid, true));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //TODO update UI to add PB-ADV UD
                    UnProvisionedDevice mUnProvisionedDevice = new UnProvisionedDevice(MeshConstants.MESH_BEARER_ADV,uuid,null,0,null);
                    boolean result = unProvisionedAdapter.addUnprovisionedDevice(mUnProvisionedDevice);
                    List<Node> nodes = nodeAdapter.getNodes();
                    for(int i=0 ; i < nodes.size() ; i++){
                        if(Arrays.equals(uuid,nodes.get(i).getUUID())) {
                            nodeAdapter.removeNodeByIndex(i);
                            break;
                        }
                    }
                    /*
                    if (result) {
                        for (int i = 0; i < mProvisionerService.uuidList.size(); i ++) {
                            if (!mProvisionerService.isProvOrConfiging && Arrays.equals(MeshUtils.StringToIntArray(mProvisionerService.uuidList.get(i)), uuid)) {
                                int position = unProvisionedAdapter.getPositionByUnProvisioned(mUnProvisionedDevice);
                                if(position == -1) {
                                    log("position = -1");
                                    return;
                                }
                                unProvisionedAdapter.setPosition(position);
                                mProvisionerService.stopUnProvsionScan();
                                mProgressBar.setVisibility(View.GONE);
                                isScanning = true;
                                mProvisionerService.inviteProvisioning(mUnProvisionedDevice.getBearerType(),mUnProvisionedDevice.getUUID(),
                                    mUnProvisionedDevice.getAddress(),
                                    mUnProvisionedDevice.getAddressType(),
                                    mUnProvisionedDevice.getGattDevName(),
                                    0,
                                    0);
                                break;
                            }
                        }
                    }
                    */
                }
            });
        }

        @Override
        public void onScanUnProvDevicePBGatt(String name, String addr, int addrType, int rssi) {
            log("onScanUnProvDevicePBGatt() addr = " + addr);
            if (null != name) {
                log("onScanUnProvDevicePBGatt() name = " + name);
            } else {
                log("name is null, this shall not happen");
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //TODO update UI to add PB-GATT UD
                    boolean isExistGatt = false;
                    boolean result = false;
                    UnProvisionedDevice mUnProvisionedDevice = null;
                    List<Node> nodes = nodeAdapter.getNodes();
                    log("nodes size = " + nodes.size());
                    /*
                    if(nodes.size() == 0) {
                        unProvisionedAdapter.addUnprovisionedDevice(new UnProvisionedDevice(MeshConstants.MESH_BEARER_GATT,null,addr,addrType,name));
                    }
                    */
                    for(int i=0 ; i < nodes.size() ; i++){
                        Node node = nodes.get(i);
                        log("node = " + node);
                        log("node.getNodeBearer() = " + node.getNodeBearer());
                        log("node.getGattAddr() = " + node.getGattAddr() );
                        if ((null != node) && (node.getNodeBearer() == MeshConstants.MESH_BEARER_GATT)) {
                            log("node.getGattAddr() = " + node.getGattAddr() );
                            isExistGatt = true;
                            if((node.getGattAddr().compareTo(addr) == 0) && name.contains("Provisioning")) {
                                mUnProvisionedDevice = new UnProvisionedDevice(MeshConstants.MESH_BEARER_GATT, null, addr, addrType, name);
                                result = unProvisionedAdapter.addUnprovisionedDevice(mUnProvisionedDevice);
                                nodeAdapter.removeNodeByIndex(i);
                                break;
                            }else if(node.getGattAddr().compareTo(addr) != 0) {
                                mUnProvisionedDevice = new UnProvisionedDevice(MeshConstants.MESH_BEARER_GATT, null, addr, addrType, name);
                                result = unProvisionedAdapter.addUnprovisionedDevice(mUnProvisionedDevice);
                            }
                        }
                    }
                    if(!isExistGatt) {
                        log("isExistGatt node.getGattAddr() = " + addr);
                        mUnProvisionedDevice = new UnProvisionedDevice(MeshConstants.MESH_BEARER_GATT, null, addr, addrType, name);
                        result = unProvisionedAdapter.addUnprovisionedDevice(mUnProvisionedDevice);
                    }
                    /*
                    if (result) {
                        for (int i = 0; i < mProvisionerService.uuidList.size(); i ++) {
                            if (!mProvisionerService.isProvOrConfiging && addr.equals(mProvisionerService.uuidList.get(i))) {
                                int position = unProvisionedAdapter.getPositionByUnProvisioned(mUnProvisionedDevice);
                                if(position == -1) {
                                    log("position = -1");
                                    return;
                                }
                                unProvisionedAdapter.setPosition(position);
                                mProvisionerService.stopUnProvsionScan();
                                mProgressBar.setVisibility(View.GONE);
                                isScanning = true;
                                mProvisionerService.inviteProvisioning(mUnProvisionedDevice.getBearerType(),mUnProvisionedDevice.getUUID(),
                                    mUnProvisionedDevice.getAddress(),
                                    mUnProvisionedDevice.getAddressType(),
                                    mUnProvisionedDevice.getGattDevName(),
                                    0,
                                    0);
                                break;
                            }
                        }
                    }
                    */
                }
            });
        }

        @Override
        public void onProvStateChanged(Node node, boolean success) {
            log("onProvStateChanged() node uuid = " + MeshUtils.intArrayToString(node.getUUID(), true));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuffer buffer = new StringBuffer();
                    if (success) {
                        switch (node.getConfigState()) {
                            case ProvisionerService.MESH_NODE_CONFIG_STATE_GET_COMPOSITION_DATA: {
                                //TODO Composition Data Getting
                                log("ProvisionerService.MESH_NODE_CONFIG_STATE_GET_COMPOSITION_DATA");
                                break;
                            }
                            case ProvisionerService.MESH_NODE_CONFIG_STATE_ADD_APPKEY: {
                                //TODO App Key adding
                                log("ProvisionerService.MESH_NODE_CONFIG_STATE_ADD_APPKEY");
                                break;
                            }
                            case ProvisionerService.MESH_NODE_CONFIG_STATE_MODEL_APP_BIND: {
                                //TODO Model App Binding
                                log("ProvisionerService.MESH_NODE_CONFIG_STATE_MODEL_APP_BIND");
                                break;
                            }
                            case ProvisionerService.MESH_NODE_CONFIG_STATE_IDLE: {
                                //TODO provision config complete successfully
                                log("ProvisionerService.MESH_NODE_CONFIG_STATE_IDLE");
                                log("config success , config time = " + node.getConfigTime() + "s");
                                if (mProvisionerService != null) {
                                    mProvisionerService.isProvOrConfiging = false;
                                    if (mProvisionerService.getMeshMode() != MeshConstants.MESH_MODE_STANDBY) {
                                        mProvisionerService.setMeshMode(MeshConstants.MESH_MODE_STANDBY); // config success , need set standby mode
                                    }
                                }
                                //provisionedAdapter.setPosition(ProvisionedAdapter.ALL_ENABLE);
                                //MyApplication.configTime = System.currentTimeMillis() - MyApplication.configTime;
                                //double time = MeshUtils.getDoubleDecimal(MyApplication.configTime*1.0/1000);
                                nodeAdapter.nodeTimeChange(node,NodeAdapter.ALL_ENABLE);
                                meshCurrentState = MESH_STATE_IDLE;
                                buffer.setLength(0);
                                if (node.getNodeBearer() == MeshConstants.MESH_BEARER_ADV) {
                                    buffer.append(MeshUtils.intArrayToString(node.getUUID(), true));
                                } else if (node.getNodeBearer() == MeshConstants.MESH_BEARER_GATT) {
                                    buffer.append(node.getGattAddr());
                                }
                                buffer.append(" , Config success , time cost: ").append(node.getConfigTime()).append("s");
                                log(buffer.toString());
                                buffer.setLength(0);
                                buffer.append("Config success\n").append("time cost: ").append(node.getConfigTime()).append("s");
                                Toast.makeText(MainActivity.this, buffer.toString(), Toast.LENGTH_SHORT).show();
                                if (uuidIndex > 0) { //group provision
                                    uuidConfigSuccess.add(uuid);
                                    if (uuidIndex < deviceSize) {
                                        provision();
                                    } else {
                                        printfGroupProvResult();
                                    }
                                }
                                break;
                            }
                            default:
                                break;
                        }
                    } else {
                        //TODO provisioning config failed
                        //log("config fail , config time = " + node.getConfigTime() + "s , ProvisionerService.MESH_NODE_CONFIG_STATE_IDLE");
                        if (mProvisionerService != null) {
                            mProvisionerService.isProvOrConfiging = false;
                            if (mProvisionerService.getMeshMode() != MeshConstants.MESH_MODE_STANDBY) {
                                mProvisionerService.setMeshMode(MeshConstants.MESH_MODE_STANDBY); // config fail , need set standby mode
                            }
                        }
                        //provisionedAdapter.setPosition(ProvisionedAdapter.ALL_ENABLE);
                        //MyApplication.configTime = System.currentTimeMillis() - MyApplication.configTime;
                        //double time = MeshUtils.getDoubleDecimal(MyApplication.configTime*1.0/1000);
                        nodeAdapter.nodeTimeChange(node,NodeAdapter.ALL_ENABLE);
                        meshCurrentState = MESH_STATE_IDLE;
                        buffer.setLength(0);
                        if (node.getNodeBearer() == MeshConstants.MESH_BEARER_ADV) {
                            buffer.append(MeshUtils.intArrayToString(node.getUUID(), true));
                        } else if (node.getNodeBearer() == MeshConstants.MESH_BEARER_GATT) {
                            buffer.append(node.getGattAddr());
                        }
                        buffer.append(" , Config fail , time cost: ").append(node.getConfigTime()).append("s");
                        log(buffer.toString());
                        buffer.setLength(0);
                        buffer.append("Config fail\n").append("time cost: ").append(node.getConfigTime()).append("s");
                        Toast.makeText(MainActivity.this, buffer.toString(), Toast.LENGTH_SHORT).show();
                        if (uuidIndex > 0) { // group provision
                            uuidConfigFail.add(uuid);
                            if (uuidIndex < deviceSize) {
                                // if config fail in group provision , it should wait 10s , process remain data in adv queue
                                try {
                                    Thread.sleep(10*1000);
                                    provision();
                                } catch (Exception e) {

                                }
                            } else {
                                printfGroupProvResult();
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void sendMessage(MeshMessage msg, int position, int appKeyIndex, int groupAddr, int[] payload) {
            mProvisionerService.sendMessage(msg,(MyApplication.nodeList).get(position),appKeyIndex,groupAddr,payload);
            meshCurrentState = MESH_STATE_SENDMSGING;
        }

        @Override
        public void gattProvisioningConnectFail() {
            log("gattProvisioningConnectFail");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //if(null != mRunnable){
                        //mHandler.removeCallbacks(mRunnable);
                    //}
                    unProvisionedAdapter.setPosition(UnprovisionedAdapter.ALL_ENABLE);
                    Toast.makeText(MainActivity.this,"PB-Gatt provisioning service connect fail",Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void gattProxyConnectFail() {
            log("gattProxyConnectFail");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    nodeAdapter.setPosition(NodeAdapter.ALL_ENABLE);
                    Toast.makeText(MainActivity.this,"PB-Gatt proxy service connect fail",Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onNodeMsgRecieved(BluetoothMeshAccessRxMessage msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (msg == null) {
                        log("onNodeMsgRecieved , msg == null");
                        meshCurrentState = MESH_STATE_IDLE;
                        return;
                    }
                    int src = msg.getSrcAddr();
                    double time = MeshUtils.getDoubleDecimal(MyApplication.sendMessageTime*1.0/1000);
                    log("onNodeMsgRecieved , src = " + MeshUtils.decimalToHexString("%04X", src)+ " , status: " + msg.getBuffer()[0]);
                    meshCurrentState = MESH_STATE_IDLE;
                    switch (msg.getOpCode()) {
                        case MeshConstants.MESH_MSG_GENERIC_ONOFF_STATUS: { //Generic OnOff Status
                            String status = null;
                            int presentOnOff = msg.getBuffer()[0];
                            if ( presentOnOff == 0) {
                                status = "Off";
                            } else if ( presentOnOff == 1) {
                                status = "On";
                            } else {
                                status = "unknown";
                            }
                            StringBuilder sb = new StringBuilder("<<Generic OnOff Status>> from Node ");
                            sb.append(MeshUtils.decimalToHexString("%04X", src))
                              .append("\r\nStatus: " + status)
                              .append("\r\nTime: ")
                              .append(time)
                              .append("s");
                            log(sb.toString());
                            Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_STATUS: { //Light Lightness Status
                            StringBuilder sb = new StringBuilder("<<Light Lightness Status>> from Node ");
                            sb.append(MeshUtils.decimalToHexString("%04X", src));
                            if (msg.getBuffer().length == 2) {
                                sb.append("\r\nLightness = " + MeshUtils.getCombineNuber(msg.getBuffer()[1], msg.getBuffer()[0]));
                            }
                            sb.append("\r\nTime: ").append(time).append("s");
                            log(sb.toString());
                            Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_LIGHT_LIGHTNESS_RANGE_STATUS: { //Light Lightness Range Status
                            StringBuilder sb = new StringBuilder("<<Light Lightness Range Status>> from Node ");
                            sb.append(MeshUtils.decimalToHexString("%04X", src))
                              .append("\r\nStatus Code = " + msg.getBuffer()[0])
                              .append("\r\nRangeMin = " + MeshUtils.getCombineNuber(msg.getBuffer()[2], msg.getBuffer()[1]))
                              .append("\r\nRangeMax = " + MeshUtils.getCombineNuber(msg.getBuffer()[4], msg.getBuffer()[3]))
                              .append("\r\nTime: ")
                              .append(time)
                              .append("s");
                            log(sb.toString());
                            Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_LIGHT_CTL_TEMPERATURE_STATUS: { //Light CTL Temperature  Status
                            StringBuilder sb = new StringBuilder("<<Light CTL Temp Status>> from Node ");
                            sb.append(MeshUtils.decimalToHexString("%04X", src))
                              .append("\r\nLightness = " + MeshUtils.getCombineNuber(msg.getBuffer()[1], msg.getBuffer()[0]))
                              .append("\r\nTime: ")
                              .append(time)
                              .append("s");
                            log(sb.toString());
                            Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_LIGHT_CTL_TEMPERATURE_RANGE_STATUS: { //Light CTL Temperature  Range Status
                            StringBuilder sb = new StringBuilder("<<Light CTL Temp Range Status>> from Node ");
                            sb.append(MeshUtils.decimalToHexString("%04X", src))
                              .append("\r\nStatus Code = " + msg.getBuffer()[0])
                              .append("\r\nRangeMin = " + MeshUtils.getCombineNuber(msg.getBuffer()[2], msg.getBuffer()[1]))
                              .append("\r\nRangeMax = " + MeshUtils.getCombineNuber(msg.getBuffer()[4], msg.getBuffer()[3]))
                              .append("\r\nTime: ")
                              .append(time)
                              .append("s");
                            log(sb.toString());
                            Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_LIGHT_CTL_STATUS: { //Light CTL Status
                            StringBuilder sb = new StringBuilder("<<Light CTL Status>> from Node ");
                            sb.append(MeshUtils.decimalToHexString("%04X", src));
                            if (msg.getBuffer().length == 2) {
                                sb.append("\r\nCTL Lightness = " + MeshUtils.getCombineNuber(msg.getBuffer()[1], msg.getBuffer()[0]));
                            } else if (msg.getBuffer().length == 4) {
                                sb.append("\r\nCTL Lightness = " + MeshUtils.getCombineNuber(msg.getBuffer()[1], msg.getBuffer()[0]));
                                sb.append("\r\nCTL Temperature = " + MeshUtils.getCombineNuber(msg.getBuffer()[3], msg.getBuffer()[2]));
                            }
                            sb.append("\r\nTime: ").append(time).append("s");
                            log(sb.toString());
                            Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_LIGHT_HSL_STATUS: { //Light HSL Status
                            StringBuilder sb = new StringBuilder("<<Light HSL Status>> from Node ");
                            sb.append(MeshUtils.decimalToHexString("%04X", src));
                            if (msg.getBuffer().length == 2) {
                                sb.append("\r\nHSL Lightness = " + MeshUtils.getCombineNuber(msg.getBuffer()[1], msg.getBuffer()[0]));
                            } else if (msg.getBuffer().length == 4) {
                                sb.append("\r\nHSL Lightness = " + MeshUtils.getCombineNuber(msg.getBuffer()[1], msg.getBuffer()[0]));
                                sb.append("\r\nHSL Hue = " + MeshUtils.getCombineNuber(msg.getBuffer()[3], msg.getBuffer()[2]));
                            } else if (msg.getBuffer().length == 6) {
                                sb.append("\r\nHSL Lightness = " + MeshUtils.getCombineNuber(msg.getBuffer()[1], msg.getBuffer()[0]));
                                sb.append("\r\nHSL Hue = " + MeshUtils.getCombineNuber(msg.getBuffer()[3], msg.getBuffer()[2]));
                                sb.append("\r\nHSL Saturation = " + MeshUtils.getCombineNuber(msg.getBuffer()[5], msg.getBuffer()[4]));
                            }
                            sb.append("\r\nTime: ").append(time).append("s");
                            log(sb.toString());
                            Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_LIGHT_HSL_RANGE_STATUS: { //Light HSL Range Status
                            StringBuilder sb = new StringBuilder("<<Light HSL Range Status>> from Node ");
                            sb.append(MeshUtils.decimalToHexString("%04X", src))
                              .append("\r\nStatus Code = " + msg.getBuffer()[0])
                              .append("\r\nHue Range Min = " + MeshUtils.getCombineNuber(msg.getBuffer()[2], msg.getBuffer()[1]))
                              .append("\r\nHue Range Max = " + MeshUtils.getCombineNuber(msg.getBuffer()[4], msg.getBuffer()[3]))
                              .append("\r\nSaturation Range Min = " + MeshUtils.getCombineNuber(msg.getBuffer()[6], msg.getBuffer()[5]))
                              .append("\r\nSaturation Range Max = " + MeshUtils.getCombineNuber(msg.getBuffer()[8], msg.getBuffer()[7]))
                              .append("\r\nTime: ")
                              .append(time)
                              .append("s");
                            log(sb.toString());
                            Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        default:
                            break;
                    }
                }
            });
        }

        @Override
        public void onStorageDataChange(StorageData data, int type) {
            //TODO Update database
            log("onStorageDataChange , type = " + type);
            List<StorageData> datas = LouSQLite.query(MyCallBack.TABLE_NAME_STORAGE
                , "select * from " + MyCallBack.TABLE_NAME_STORAGE
                , null);
            StorageData mStorageData = datas.get(0);
            switch (type) {
                case MyApplication.STORAGE_DATA_ALL:
                    //MyApplication.nodeList = data.getAllNode();
                    //mStorageData.setAllNode(data.getAllNode());
                    mStorageData.setAllNetKey(data.getAllNetKey());
                    mStorageData.setAllAppKey(data.getAllAppKey());
                    mStorageData.setAllGroupAddrList(data.getAllGroupAddr());
                    break;
                case MyApplication.STORAGE_DATA_NETKEY:
                    mStorageData.setAllNetKey(data.getAllNetKey());
                    break;
                case MyApplication.STORAGE_DATA_APPKEY:
                    mStorageData.setAllAppKey(data.getAllAppKey());
                    break;
                case MyApplication.STORAGE_DATA_GROUP_ADDR:
                    mStorageData.setAllGroupAddrList(data.getAllGroupAddr());
                    break;
                default:
                    break;
            }
            LouSQLite.update(MyCallBack.TABLE_NAME_STORAGE,mStorageData,ProvisionedDeviceEntry.COLEUM_NAME_ID + "=?",new String[]{mStorageData.getId()});
        }

        @Override
        public void onNodeDataChange(NodeData nodeData, int type) {
            //TODO Update database
            log("onNodeDataChange , type = " + type);
            switch (type) {
                case MyApplication.NODE_DATA_ADD:
                    LouSQLite.insert(MyCallBack.TABLE_NAME_NODE, nodeData);
                    break;
                case MyApplication.NODE_DATA_DELETE:
                    LouSQLite.delete(MyCallBack.TABLE_NAME_NODE, ProvisionedDeviceEntry.COLEUM_NAME_ADDRESS + "=?",new String[]{nodeData.getAddress()});
                    break;
                case MyApplication.NODE_DATA_UPDATE:
                    LouSQLite.update(MyCallBack.TABLE_NAME_NODE, nodeData, ProvisionedDeviceEntry.COLEUM_NAME_ADDRESS + "=?",new String[]{nodeData.getAddress()});
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onNodeActiveStatusChange(Node node, int active) {
            log("onNodeActiveStatusChange");
            //TODO update UI
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    nodeAdapter.nodeStateChange(node, active);
                }
            });
        }

        @Override
        public void onFriendShipStatus(Node node) {
            log("onFriendShipStatus, address: " + MeshUtils.decimalToHexString("%04X", node.getAddr()));
            //TODO update UI
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    nodeAdapter.nodeFriendShipStateChange(node);
                }
            });
        }

        @Override
        public void onNodeRemoved(boolean result, Node node) {
            log("onNodeRemoved result: " + (result ? "success" : "fail"));
            //TODO update UI
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    nodeAdapter.removeNode(node);
                }
            });
        }

        @Override
        public void onConfigMsgModelSubAdd(Node node, int eleAddr, int subAddr, long modelId) {
            log("onConfigMsgModelSubAdd");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProvisionerService.configMsgModelSubAdd(node,eleAddr,subAddr,modelId);
                }
            });
        }

        @Override
        public void onConfigMsgNetKeyAdd(Node node, int targetNetkeyIdx) {
            log("onConfigMsgNetKeyAdd");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProvisionerService.configMsgNetKeyAdd(node,targetNetkeyIdx);
                }
            });
        }

        @Override
        public void onConfigMsgAppKeyAdd(Node node, int targetNetKeyIndex, int appKeyIdx) {
            log("onConfigMsgAppKeyAdd");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProvisionerService.configMsgAppKeyAdd(node, targetNetKeyIndex, appKeyIdx);
                }
            });
        }


        @Override
        public void onConfigMsgCompositionDataGet(Node node, int page) {
            log("onConfigMsgCompositionDataGet");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProvisionerService.configMsgCompositionDataGet(node, page);
                }
            });
        }

        @Override
        public void onConfigMsgNodeReset(int position) {
            log("onConfigMsgNodeReset");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProvisionerService.configMsgNodeReset(MyApplication.nodeList.get(position), false, 1);
                    nodeAdapter.removeNode((MyApplication.nodeList).get(position));
                }
            });
        }

        @Override
        public void onKeyRefreshComplete(int netkeyIndex, boolean result) {
            log("onKeyRefreshComplete ---> netkey index = " + netkeyIndex + ", result: " + (result ? "SUCCESS" : "FAIL"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    log("onKeyRefreshComplete ---> dialogKeyrefresh = " + dialogKeyrefresh);
                    meshCurrentState = MESH_STATE_IDLE;
                    remainNodeList.clear();
                    MyApplication.keyrefreshTime = System.currentTimeMillis() - MyApplication.keyrefreshTime;
                    if (mProvisionerService != null) {
                        mProvisionerService.isKeyrefreshing = false;
                        if (mProvisionerService.getMeshMode() != MeshConstants.MESH_MODE_STANDBY) {
                            if (mProvisionerService.getMeshMode() != MeshConstants.MESH_MODE_STANDBY) {
                                mProvisionerService.setMeshMode(MeshConstants.MESH_MODE_STANDBY); // keyrefresh done , need set standby mode
                            }
                        }
                    }
                    if (null != dialogKeyrefresh) {
                        dialogKeyrefresh.dismiss();
                        dialogKeyrefresh = null;
                    }
                    String keyrefreshStr = "onKeyRefreshComplete ---> Key refresh for netkey " + netkeyIndex + " complete: " +
                        (result ? "SUCCESS" : "FAIL") +" , time = " + MeshUtils.getDoubleDecimal(MyApplication.keyrefreshTime*1.0/1000) + "s";
                    log(keyrefreshStr);
                    Toast.makeText(MainActivity.this, keyrefreshStr, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onGroupSendMsg(int status, List<String> lists) {
            log("onGroupSendMsg ---> status = " + status);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (status) {
                        case MyApplication.GROUP_SEND_MSG_START:
                            dialogGroupSendMsg = new Dialog(MainActivity.this, R.style.Custom_Progress);
                            dialogGroupSendMsg.setContentView(R.layout.mul_picture_progressbar);
                            TextView tip = (TextView)dialogGroupSendMsg.findViewById(R.id.tipTextView);
                            tip.setText("Group SendMessage...");
                            dialogGroupSendMsg.getWindow().getAttributes().gravity = Gravity.CENTER;
                            dialogGroupSendMsg.setCanceledOnTouchOutside(false);
                            dialogGroupSendMsg.setCancelable(false);
                            dialogGroupSendMsg.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    log("onGroupSendMsg ---> group sendMessage dialog dismiss");
                                    meshCurrentState = MESH_STATE_IDLE;
                                }

                            });
                            dialogGroupSendMsg.show();
                            break;
                        case MyApplication.GROUP_SEND_MSG_END:
                            log("lists = " + lists);
                            meshCurrentState = MESH_STATE_IDLE;
                            String filePath = "/data/data/" + MainActivity.this.getPackageName() + "/group_message_time.txt";
                            StringBuffer buffer = new StringBuffer();
                            for (int i = 0; i < lists.size(); i++) {
                                buffer.append(lists.get(i));
                                if (i < lists.size() - 1) {
                                    buffer.append("\r\n");
                                }
                            }
                            MeshUtils.save(filePath, buffer.toString());
                            if (null != dialogGroupSendMsg) {
                                dialogGroupSendMsg.dismiss();
                                dialogGroupSendMsg = null;
                            }
                            String groupSendMsgStr = "group send msg complete , time = " + MeshUtils.getDoubleDecimal(MyApplication.groupSendMessageTime*1.0/1000) + "s";
                            log(groupSendMsgStr);
                            Toast.makeText(MainActivity.this, groupSendMsgStr, Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                }
            });
        }

        @Override
        public void GattConnect(int position) {
            log("GattConnect");
            dialogGattConnect = new Dialog(MainActivity.this, R.style.Custom_Progress);
            dialogGattConnect.setContentView(R.layout.mul_picture_progressbar);
            TextView tip = (TextView)dialogGattConnect.findViewById(R.id.tipTextView);
            tip.setText("PB-GATT Connect...");
            dialogGattConnect.getWindow().getAttributes().gravity = Gravity.CENTER;
            dialogGattConnect.setCanceledOnTouchOutside(false);
            dialogGattConnect.setCancelable(false);
            dialogGattConnect.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    log("GattConnect ---> gatt connect dialog dismiss");
                    meshCurrentState = MESH_STATE_IDLE;
                }

            });
            dialogGattConnect.show();
            meshCurrentState = MESH_STATE_GATTCONNECTING;
            mProvisionerService.GattConnect((MyApplication.nodeList).get(position));
        }

        @Override
        public void GattDisconnect(int position) {
            log("GattDisconnect");
            mProvisionerService.GattDisconnect((MyApplication.nodeList).get(position));
        }

        @Override
        public void GattConnectStatusChange(Node node, boolean connect) {
            log("GattConnectStatusChange ---> node address = " + MeshUtils.decimalToHexString("%04x", node.getAddr()) + " , connect = " + connect);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    meshCurrentState = MESH_STATE_IDLE;
                    if(mProvisionedDialogFragment != null) {
                        mProvisionedDialogFragment.updateGattConnectStatus(connect);
                    }
                    if(connect) {
                        if (null != dialogGattConnect) {
                            dialogGattConnect.dismiss();
                            dialogGattConnect = null;
                        }
                        Toast.makeText(MainActivity.this, "GattConnectStatusChange ---> PB-GATT( Node address:"+
                            MeshUtils.decimalToHexString("%04X", node.getAddr()) + " ) " + "Connect success", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(MainActivity.this, "GattConnectStatusChange ---> PB-GATT disconnect success", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void GattConnectTimeout() {
            log("GattConnectTimeout");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    meshCurrentState = MESH_STATE_IDLE;
                    if (null != dialogGattConnect) {
                        dialogGattConnect.dismiss();
                        dialogGattConnect = null;
                    }
                    if(mProvisionedDialogFragment != null) {
                        mProvisionedDialogFragment.gattConnectTimeout();
                    }
                }
            });
        }

        @Override
        public void dialogFragmentCancel() {
            log("dialogFragmentCancel");
            if(mProvisionedDialogFragment != null) {
                mProvisionedDialogFragment = null;
            }
        }

        @Override
        public void updateHeartBeatReceive() {
            log("updateHeartBeatReceive");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    nodeAdapter.heartBeatChange();
                }
            });
        }

        @Override
        public void provisionTimeout(int time) {
            log("provisionTimeout");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    meshCurrentState = MESH_STATE_IDLE;
                    if(MyApplication.isProvisioning){
                        MyApplication.isProvisioning = false;
                        mProvisionerService.isProvOrConfiging = false;
                        if (mProvisionerService.getMeshMode() != MeshConstants.MESH_MODE_STANDBY) {
                            mProvisionerService.setMeshMode(MeshConstants.MESH_MODE_STANDBY); // provision timeout , need set standby mode
                        }
                        String provMsg = "provision Timeout" + "\n"
                                + "provision time: " + time + "s";
                        log(provMsg);
                        Toast.makeText(MainActivity.this,provMsg,Toast.LENGTH_LONG).show();
                        unProvisionedAdapter.setPosition(UnprovisionedAdapter.ALL_ENABLE);
                        if (uuidIndex > 0) { //group privision
                            uuidProFail.add(uuid);
                            if (uuidIndex < deviceSize) {
                                // if provision fail in group provision , it should wait 10s
                                try {
                                    Thread.sleep(10*1000);
                                    provision();
                                } catch (Exception e) {

                                }
                            } else {
                                printfGroupProvResult();
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void addNewNetKey(boolean result, int netkeyIndex) {
            log("addNewNetKey , result = " + result + " , netkeyIndex = " + netkeyIndex);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (result) {
                       Toast.makeText(MainActivity.this, "Add netkey Index " + MeshUtils.decimalToHexString("%04X", netkeyIndex) + " success", Toast.LENGTH_SHORT).show();
                    } else {
                       Toast.makeText(MainActivity.this, "Add Netkey fail", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void addNewAppKey(boolean result, int netkeyIndex, int appkeyIndex) {
            log("addNewAppKey , result = " + result + " , netkeyIndex = " + netkeyIndex  + " , appkeyIndex = " + appkeyIndex);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (result) {
                       Toast.makeText(MainActivity.this, "Add addkey Index " + MeshUtils.decimalToHexString("%04X", appkeyIndex)
                                + " with netkeyIndex " +  MeshUtils.decimalToHexString("%04X", netkeyIndex) + " success", Toast.LENGTH_SHORT).show();
                    } else {
                       Toast.makeText(MainActivity.this, "Add addkey " + " with netkeyIndex "
                                + MeshUtils.decimalToHexString("%04X", netkeyIndex) + " fail", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void onBluetoothReset() {
            log("onBluetoothReset");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isOpenMeshSwitch) {
                        mProvisionerService.setBluetoothMeshEnabled(false);
                        unProvisionedAdapter.setPosition(UnprovisionedAdapter.ALL_ENABLE,true);
                        nodeAdapter.setPosition(NodeAdapter.ALL_ENABLE);
                        unProvisionedAdapter.clear();
                        mProvisionerService.setBluetoothMeshEnabled(true);
                        isScanning = true;
                    }
                }
            });
        }


        @Override
        public void onConfigMsgAck(BluetoothMeshAccessRxMessage msg) {

            int src = msg.getSrcAddr();
            String srcStr = MeshUtils.decimalToHexString("%04X", src);
            log("onConfigMsgAck , address = " + srcStr + " , opCode = " + MeshUtils.decimalToHexString("%04X", msg.getOpCode()));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //int src = msg.getSrcAddr();
                    //String srcStr = MeshUtils.decimalToHexString("%04X", src);

                    switch (msg.getOpCode()) {
                        case MeshConstants.MESH_MSG_CONFIG_BEACON_STATUS: {
                            Toast.makeText(MainActivity.this, "<<Config Beacon Status>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_COMPOSITION_DATA_STATUS: {
                            Toast.makeText(MainActivity.this, "<<Config Composition Data Status>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_DEFAULT_TTL_STATUS: {
                            Toast.makeText(MainActivity.this, "<<Config Default TTL Status>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_GATT_PROXY_STATUS: {
                            Toast.makeText(MainActivity.this, "<<Config GATT Proxy Status>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_FRIEND_STATUS: {
                            Toast.makeText(MainActivity.this, "<<Config Friend Status>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_MODEL_PUBLICATION_STATUS: {
                            Toast.makeText(MainActivity.this, "<<Config Model Publication Status>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_MODEL_SUBSCRIPTION_STATUS: {
                            Toast.makeText(MainActivity.this, "<<Config Model Subsctiption Status>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_NETWORK_TRANSMIT_STATUS: {
                            Toast.makeText(MainActivity.this, "<<Config Network Transmit Status>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_RELAY_STATUS: {
                            log("<<Config Relay Status>> from Node " + srcStr);
                            //Toast.makeText(MainActivity.this, "<<Config Relay Status>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_SIG_MODEL_SUBSCRIPTION_LIST: {
                            Toast.makeText(MainActivity.this, "<<Config SIG Model Sbscripion List>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_VENDOR_MODEL_SUBSCRIPTION_LIST: {
                            Toast.makeText(MainActivity.this, "<<Config Vendor Model Subscription List>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_NETKEY_LIST: {
                            Toast.makeText(MainActivity.this, "<<Config NetKey List>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_NETKEY_STATUS: {
                            log("<<Config NetKey Status>> from Node " + srcStr);
                            //Toast.makeText(MainActivity.this, "<<Config NetKey Status>> from Node " + src, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_APPKEY_LIST: {
                            Toast.makeText(MainActivity.this, "<<Config AppKey List>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_APPKEY_STATUS: {
                            Toast.makeText(MainActivity.this, "<<Config AppKey Status>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_MODEL_APP_STATUS: {
                            Toast.makeText(MainActivity.this, "<<Config Model App Status>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_SIG_MODEL_APP_LIST: {
                            Toast.makeText(MainActivity.this, "<<Config SIG Model App List>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_VENDOR_MODEL_APP_LIST: {
                            Toast.makeText(MainActivity.this, "<<Config Vendor Model App List>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_NODE_IDENTITY_STATUS: {
                            Toast.makeText(MainActivity.this, "<<Config Node Identity Status>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_NODE_RESET_STATUS: {
                            log("NODE_RESET_STATUS , src = " + srcStr);
                            if (nodeResetList.size() == 0) {
                                log("nodeResetList.size() == 0");
                                return;
                            }
                            Node node = getNodeByAddr(src);
                            if (null == node) {
                                log("null == node");
                                return;
                            }
                            if (ackNodeResetList.containsKey(src)) { // repeat
                                log("MESH_MSG_CONFIG_NODE_RESET_STATUS , ackNodeResetList alread contain");
                                return;
                            }
                            ackNodeResetList.put(src, node);
                            log("nodeResetList size = " + nodeResetList.size() + " , ackNodeResetList size = " + ackNodeResetList.size());
                            if (ackNodeResetList.size() == nodeResetList.size()) { // all group node ack
                                log("all node ack node reset");
                                meshCurrentState = MESH_STATE_IDLE;
                                if (null != dialogNodeReset) {
                                    dialogNodeReset.dismiss();
                                    dialogNodeReset = null;
                                }
                                if (null != groupNodeResetTimer) {
                                    groupNodeResetTimer.cancel();
                                    groupNodeResetTimer = null;
                                }
                                nodeResetList.clear();
                                ackNodeResetList.clear();
                                keyrefresh(0, remainNodeList);
                            }
                            //Toast.makeText(MainActivity.this, "<<Config Node Reset Status>> from Node " + src, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_KEY_REFRESH_PHASE_STATUS: {
                            log("<<Config Key Refresh Phase Status>> from Node " + srcStr);
                            //Toast.makeText(MainActivity.this, "<<Config Key Refresh Phase Status>> from Node " + src, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_HEARTBEAT_PUBLICATION_STATUS: {
                            Toast.makeText(MainActivity.this, "<<Config Heartbeat Publication Status>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case MeshConstants.MESH_MSG_CONFIG_HEARTBEAT_SUBSCRIPTION_STATUS: {
                            Toast.makeText(MainActivity.this, "<<Config Heartbeat Subscription Status>> from Node " + srcStr, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
            });

        }
    };

}
