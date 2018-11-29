package com.fonarik94;

import javax.bluetooth.*;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import java.io.*;
import java.util.*;

public class BluetoothManager {
    private static final UUID OBEX_FILE_PUSH = new UUID(0x1105);
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private String obexServiceUrl;
    private final Object inquiryCompletedEvent = new Object();
    private final Object serviceSearchCompletedEvent = new Object();

    private DiscoveryListener listener = new DiscoveryListener() {
        private String responseMessage;

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
            switch (respCode) {
                case DiscoveryListener.SERVICE_SEARCH_COMPLETED:
                    this.responseMessage = "Service search completed";
                    break;
                case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
                    this.responseMessage = "Service search terminated";
                    break;
                case DiscoveryListener.SERVICE_SEARCH_ERROR:
                    this.responseMessage = "Service search error";
                    break;
                case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS:
                    this.responseMessage = "Service search no records";
                    break;
                case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
                    this.responseMessage = "Device not reachable";
                    break;
                default:
                    this.responseMessage = "Error";
            }
            System.out.println(this.responseMessage);
            synchronized (serviceSearchCompletedEvent) {
                serviceSearchCompletedEvent.notifyAll();
            }
        }

        public void servicesDiscovered(int transID, ServiceRecord[] serviceRecords) {
            for (ServiceRecord record : serviceRecords) {
                String url = record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                if (url == null) {
                    continue;
                }
                obexServiceUrl = url;
                DataElement serviceName = record.getAttributeValue(0x0100);
                if (serviceName != null) {
                    System.out.println("service " + serviceName.getValue() + " found " + url);
                }
            }
        }
    };

    public List<BluetoothDevice> getAvailableDevices() throws BluetoothStateException {
        deviceList.clear();
        synchronized (inquiryCompletedEvent) {
            try {
                boolean started = LocalDevice.getLocalDevice()
                        .getDiscoveryAgent()
                        .startInquiry(DiscoveryAgent.GIAC, listener);
                if (started) {
                    System.out.println("wait for device inquiry to complete...");
                    inquiryCompletedEvent.wait();
                    System.out.println(deviceList.size() + " device(s) found");
                }
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
        return deviceList;
    }

    public String getAvailableServices(BluetoothDevice btDevice) {
        UUID[] searchUuidSet = new UUID[]{OBEX_FILE_PUSH};
        synchronized (serviceSearchCompletedEvent) {
            try {
                System.out.println("search services on " + btDevice.getBluetoothAddress() + " " + btDevice.getName());
                LocalDevice
                        .getLocalDevice()
                        .getDiscoveryAgent()
                        .searchServices(null, searchUuidSet, btDevice.getRemoteDevice(), listener);
                serviceSearchCompletedEvent.wait();
            } catch (BluetoothStateException | InterruptedException btStateEx) {
                btStateEx.printStackTrace();
            }
        }
        return obexServiceUrl;
    }

    public boolean available(String connectionURL) {
        try {
            ClientSession clientSession = (ClientSession) Connector.open(connectionURL);
            clientSession.connect(null);
            clientSession.disconnect(null);
            clientSession.close();
            return true;
        } catch (IOException e) {
            System.out.println("May be remote device not available. Check bluetooth. " + e.getMessage());
        }
        return false;
    }

    public boolean sendImage(String connectionURL, byte[] rawFile) {
        try (InputStream bis = new ByteArrayInputStream(rawFile)) {
            ClientSession clientSession = (ClientSession) Connector.open(connectionURL);
            HeaderSet hsConnectionReply = clientSession.connect(null);
            if (hsConnectionReply.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
                System.out.println("Failed to connect");
                return false;
            }
            int fileLength = bis.available();
            byte[] data = new byte[fileLength];
            bis.read(data, 0, fileLength);

            HeaderSet hsOperation = clientSession.createHeaderSet();
            hsOperation.setHeader(HeaderSet.COUNT, 1L);
            hsOperation.setHeader(HeaderSet.LENGTH, (long) fileLength);
            hsOperation.setHeader(HeaderSet.NAME, "image.jpg");
            hsOperation.setHeader(HeaderSet.TYPE, "image");

            Operation putOperation = clientSession.put(hsOperation);
            try(OutputStream outputStream = putOperation.openOutputStream()) {
                outputStream.write(data);
            }
            putOperation.close();
            clientSession.disconnect(null);
            clientSession.close();

        }  catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        return true;
    }
}

