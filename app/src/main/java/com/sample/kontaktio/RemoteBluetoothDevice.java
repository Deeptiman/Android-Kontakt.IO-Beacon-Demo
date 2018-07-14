package com.sample.kontaktio;

import android.os.Parcelable;

import com.kontakt.sdk.android.common.Proximity;
import com.kontakt.sdk.android.common.profile.DeviceProfile;

import java.util.UUID;

public interface RemoteBluetoothDevice extends Parcelable {

    double getDistance();

    long getTimestamp();

    String getAddress();

    Proximity getProximity();

    double getRssi();

    void setPassword(byte[] password);

    byte[] getPassword();

    String getFirmwareVersion();

    String getName();

    String getUniqueId();

    int getBatteryPower();

    int getTxPower();

    DeviceProfile getProfile();

    boolean isShuffled();

    interface Characteristics extends Parcelable{
        UUID getProximityUUID();
        int getMajor();
        int getMinor();
        int getPowerLevel();
        long getAdvertisingInterval();
        DeviceProfile getActiveProfile();
        String getModelName();
        String getNamespaceId();
        String getInstanceId();
        String getUrl();
        String getManufacturerName();
        String getBatteryLevel();
        String getFirmwareRevision();
        String getHardwareRevision();
        boolean isSecure();
    }
}
