package com.mesh.test.provisioner.bean;

import java.io.Serializable;
import java.util.Arrays;

public class MeshFilterBean implements Serializable {

    private boolean enableFiltPbAdvStatus = false;
    private boolean enableFiltMeshMsgStatus = false;
    private boolean enableFiltUnprovBeaconStatus = false;
    private boolean enableFiltSecureBeaconStatus = false;
    private int[] meshMessageNetIndex = null;
    private int[] secureBeaconNetIndex = null;

    public MeshFilterBean() {
    }

    public void setPbAdvStatus(boolean enableFiltPbAdvStatus) {
        this.enableFiltPbAdvStatus = enableFiltPbAdvStatus;
    }

    public boolean getPbAdvStatus() {
        return this.enableFiltPbAdvStatus;
    }

    public void setMeshMsgStatus(boolean enableFiltMeshMsgStatus) {
        this.enableFiltMeshMsgStatus = enableFiltMeshMsgStatus;
    }

    public boolean getMeshMsgStatus() {
        return this.enableFiltMeshMsgStatus;
    }

    public void setUnprovBeaconStatus(boolean enableFiltUnprovBeaconStatus) {
        this.enableFiltUnprovBeaconStatus = enableFiltUnprovBeaconStatus;
    }

    public boolean getUnprovBeaconStatus() {
        return this.enableFiltUnprovBeaconStatus;
    }

    public void setSecureBeaconStatus(boolean enableFiltSecureBeaconStatus) {
        this.enableFiltSecureBeaconStatus = enableFiltSecureBeaconStatus;
    }

    public boolean getSecureBeaconStatus() {
        return this.enableFiltSecureBeaconStatus;
    }

    public String toString() {
        return "enableFiltPbAdvStatus =" + enableFiltPbAdvStatus + ", enableFiltMeshMsgStatus = " + enableFiltMeshMsgStatus
            + " , enableFiltUnprovBeaconStatus = " + enableFiltUnprovBeaconStatus + ", enableFiltSecureBeaconStatus = " + enableFiltSecureBeaconStatus;
    }
}
