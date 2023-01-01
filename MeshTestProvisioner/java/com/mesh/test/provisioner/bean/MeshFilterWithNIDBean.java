package com.mesh.test.provisioner.bean;

import java.io.Serializable;
import java.util.Arrays;

public class MeshFilterWithNIDBean implements Serializable {

    private boolean enableFiltPbAdvStatus = false;
    private boolean enableFiltMeshMsgStatus = false;
    private boolean enableFiltUnprovBeaconStatus = false;
    private boolean enableFiltSecureBeaconStatus = false;
    private int[] meshMessageNetIndex = null;
    private int[] secureBeaconNetIndex = null;

    public MeshFilterWithNIDBean() {
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

    public void setMeshMessageNetIndex(int[] meshMessageNetIndex) {
        this.meshMessageNetIndex = meshMessageNetIndex;
    }

    public int[] getMeshMessageNetIndex() {
        return this.meshMessageNetIndex;
    }

    public void setSecureBeaconNetIndex(int[] secureBeaconNetIndex) {
        this.secureBeaconNetIndex = secureBeaconNetIndex;
    }

    public int[] getSecureBeaconNetIndex() {
        return this.secureBeaconNetIndex;
    }

    public String toString() {
        return "enableFiltPbAdvStatus =" + enableFiltPbAdvStatus + ", enableFiltMeshMsgStatus = " + enableFiltMeshMsgStatus
            + ", meshMessageNetIndex=" + Arrays.toString(meshMessageNetIndex) + ", enableFiltUnprovBeaconStatus = " + enableFiltUnprovBeaconStatus
            + ", enableFiltSecureBeaconStatus = " + enableFiltSecureBeaconStatus + ", secureBeaconNetIndex = " + Arrays.toString(secureBeaconNetIndex);

    }
}
