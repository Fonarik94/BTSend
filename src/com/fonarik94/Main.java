package com.fonarik94;

import com.sun.org.apache.bcel.internal.generic.Select;

import java.util.*;

/**
 * Minimal Device Discovery example.
 */
public class Main {
    public static void main(String[] args) {
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
            List<String> serviceList = bluetoothService.getAvailableServices(deviceMap.get(scaner.nextInt()));
            System.out.println(serviceList.size());
            for (String service : serviceList) {
                System.out.println(service);
            }
        }


    }

}