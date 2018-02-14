package com.fonarik94;

import javax.bluetooth.*;
import javax.bluetooth.UUID;
import java.io.IOException;
import java.util.*;


public class BluetoothService {
    private final UUID OBEX_FILE_TRANSFER = new UUID(0x1106);
    private final UUID OBEX_FILE_PUSH = new UUID(0x1105);
    private final UUID OBEX_SYNC_PROFILE = new UUID(0x1104);
    private final UUID OBEX_BASIC_IMAGE_PROFILE = new UUID(0x111A);
    private final UUID OBEX_PHONE_BOOK_ACCES = new UUID(0x1130);
    private final UUID OBEX_PRINTING = new UUID(0x1122);
    private final UUID HEADSET = new UUID(0x1108);
    private final UUID AVRCP = new UUID(0x110F);
    private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    private List<String> serviceFound = new ArrayList<String>();
    private final Object inquiryCompletedEvent = new Object();
    private final Object serviceSearchCompletedEvent = new Object();

    private DiscoveryListener listener = new DiscoveryListener() {

        public void deviceDiscovered(RemoteDevice btRemoteDevice, DeviceClass cod) {
            BluetoothDevice discoveredDevice = new BluetoothDevice();
            discoveredDevice.setBluetoothAddress(btRemoteDevice.getBluetoothAddress());
            discoveredDevice.setRemoteDevice(btRemoteDevice);
            try {
                discoveredDevice.setName(btRemoteDevice.getFriendlyName(false));
            } catch (IOException cantGetDeviceName) {
                cantGetDeviceName.printStackTrace();
            } finally {
                deviceList.add(discoveredDevice);
            }
        }

        public void inquiryCompleted(int discType) {
            synchronized (inquiryCompletedEvent) {
                inquiryCompletedEvent.notifyAll();
            }
        }

        public void serviceSearchCompleted(int transID, int respCode) {
            System.out.println("service search completed! \n Response code " + respCode);
            synchronized (serviceSearchCompletedEvent) {
                serviceSearchCompletedEvent.notifyAll();
            }
        }

        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            System.out.println("Serv Record Length: "+ servRecord.length);
            for (int i = 0; i < servRecord.length; i++) {
                String url = servRecord[i].getConnectionURL(ServiceRecord.AUTHENTICATE_ENCRYPT, false);
                if (url == null) {
                    continue;
                }
                serviceFound.add(url);
                DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
                if (serviceName != null) {
                    System.out.println("service " + serviceName.getValue() + " found " + url);
                } else {
                    System.out.println("service found " + url);
                }
            }
        }
    };

    public List<BluetoothDevice> getAvailableDevices() {
        deviceList.clear();

        synchronized (inquiryCompletedEvent) {
            try {
                boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
                if (started) {
                    System.out.println("wait for device inquiry to complete...");
                    inquiryCompletedEvent.wait();
                    System.out.println(deviceList.size() + " device(s) found");
                }
            } catch (BluetoothStateException btStateEx) {
                btStateEx.printStackTrace();
            } catch (InterruptedException interruptedEx) {

            }
        }
        return deviceList;
    }

    public List<String> getAvailableServices(BluetoothDevice btDevice) {
        UUID[] searchUuidSet = new UUID[]{OBEX_FILE_TRANSFER, HEADSET, AVRCP, OBEX_FILE_PUSH, OBEX_BASIC_IMAGE_PROFILE, OBEX_PRINTING,OBEX_SYNC_PROFILE, OBEX_PHONE_BOOK_ACCES};
        int[] attrIDs = new int[]{0x0001, 0x0002, 0x0003, 0x0004, 0x0005, 0x0006, 0x0007, 0x0008, 0x0009, 0x000A, 0x000C, 0x000E, 0x000F, 0x0010, 0x0011, 0x0012, 0x0014, 0x0016, 0x0017, 0x0019, 0x001b, 0x001E,0x001F,0x0100, };

        synchronized (serviceSearchCompletedEvent) {
            try {
                System.out.println("search services on " + btDevice.getBluetoothAddress() + " " + btDevice.getName());
                LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, btDevice.getRemoteDevice(), listener);
                serviceSearchCompletedEvent.wait();
            } catch (BluetoothStateException btStateEx) {
                btStateEx.printStackTrace();
            } catch (InterruptedException interruptedEx) {
                interruptedEx.printStackTrace();
            }
        }
        return serviceFound;
    }
}

