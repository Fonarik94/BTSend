package com.fonarik94;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.bluetooth.RemoteDevice;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class BluetoothDevice {
    private String bluetoothAddress;
    private String name;
    private RemoteDevice remoteDevice;
}
