package com.fonarik94;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.bluetooth.RemoteDevice;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
class BluetoothDevice implements Serializable{
    private String bluetoothAddress;
    private String name;
    private RemoteDevice remoteDevice;

}
