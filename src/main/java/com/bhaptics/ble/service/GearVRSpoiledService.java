package com.bhaptics.ble.service;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import java.util.HashSet;
import java.util.Set;

public class GearVRSpoiledService extends TactosyBLEService implements GattHook {

    private static final String KEY_PREF = GearVRSpoiledService.class.getName() + ".KEY_PREF";
    private static final String KEY_SAVED_ADDRS = GearVRSpoiledService.class.getName() + ".KEY_SAVED_ADDRS";

    private SharedPreferences mSharedPref;
    private Set<String> mReservedAddrs;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);

        mSharedPref = getSharedPreferences(KEY_PREF, MODE_PRIVATE);

        mReservedAddrs = mSharedPref.getStringSet(KEY_SAVED_ADDRS, new HashSet<String>());

        // clear reserved addrs after trying to conenct.
        mSharedPref.edit().clear().apply();

        return ret;
    }

    @Override
    public IBinder onBind(Intent intent) {
        for (String addr : mReservedAddrs) {
            getServiceHandler(getThreadLooper()).connect(addr);
        }

        return super.onBind(intent);
    }

    @Override
    public void onConnect(BluetoothGatt gatt) {
        mReservedAddrs.add(gatt.getDevice().getAddress());

        mSharedPref.edit().putStringSet(KEY_SAVED_ADDRS, mReservedAddrs).apply();
    }

    @Override
    public void onDisconnect(BluetoothGatt gatt) {
        mReservedAddrs.remove(gatt.getDevice().getAddress());

        mSharedPref.edit().putStringSet(KEY_SAVED_ADDRS, mReservedAddrs).apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSharedPref.edit().putStringSet(KEY_SAVED_ADDRS, mReservedAddrs).apply();
    }
}
