package com.fonarik94;

import com.sun.org.apache.bcel.internal.generic.Select;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Minimal Device Discovery example.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        BluetoothService bluetoothService = new BluetoothService();
        Map<Integer, BluetoothDevice> deviceMap = new HashMap<Integer, BluetoothDevice>();
        int i=1;
        for (BluetoothDevice btd: bluetoothService.getAvailableDevices()) {
            deviceMap.put(i, btd);
            i++;
        }
        Set<Integer> keys = deviceMap.keySet();
        for (Integer key: keys){
            System.out.println(key+". "+ deviceMap.get(key).getName());
        }
        if(!deviceMap.isEmpty()) {
            System.out.print("Select device for service scan : ");
            Scanner scaner = new Scanner(System.in);
            BluetoothDevice selectedDevice = deviceMap.get(scaner.nextInt());

            List<String> serviceList = bluetoothService.getAvailableServices(selectedDevice);
            for (String service : serviceList) {
                System.out.println(service);
            }

            File imageFile = new File("D:\\image.jpg");
            bluetoothService.sendImage(selectedDevice,serviceList.get(0),imageFile);
        }


    }

}