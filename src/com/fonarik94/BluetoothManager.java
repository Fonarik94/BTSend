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
    private final UUID OBEX_FILE_PUSH = new UUID(0x1105);
    private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    private String ObexServiceUrl;
    private final Object inquiryCompletedEvent = new Object();
    private final Object serviceSearchCompletedEvent = new Object();
    private ClientSession clientSession;
    private HeaderSet hsConnectionReply;


    private DiscoveryListener listener = new DiscoveryListener() {
        private int responseCode;
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
            responseCode = respCode;
            String responseMsg = "Error";
            switch (respCode) {
                case 1:
                    responseMsg = "Service search completed";
                    break;
                case 2:
                    responseMsg = "Service search terminated";
                    break;
                case 3:
                    responseMsg = "Service search error";
                    break;
                case 4:
                    responseMsg = "Service search no records";
                    break;
                case 6:
                    responseMsg = "Device not reachable";
                    break;
            }
            responseMessage = responseMsg;
            System.out.println(responseMsg);
            synchronized (serviceSearchCompletedEvent) {
                serviceSearchCompletedEvent.notifyAll();
            }
        }

        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            for (ServiceRecord aServRecord : servRecord) {
                String url = aServRecord.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                if (url == null) {
                    continue;
                }
                ObexServiceUrl = url;
                DataElement serviceName = aServRecord.getAttributeValue(0x0100);
                if (serviceName != null) {
                    System.out.println("service " + serviceName.getValue() + " found " + url);
                }
            }
        }

        public int getResponseCode() {
            return responseCode;
        }

        public String getResponseMessage() {
            return responseMessage;
        }
    };

    public List<BluetoothDevice> getAvailableDevices() throws BluetoothStateException {
        deviceList.clear();
        synchronized (inquiryCompletedEvent) {
            try {
                boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
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
                LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(null, searchUuidSet, btDevice.getRemoteDevice(), listener);
                serviceSearchCompletedEvent.wait();
            } catch (BluetoothStateException | InterruptedException btStateEx) {
                btStateEx.printStackTrace();
            }
        }
        return ObexServiceUrl;
    }

    public boolean available(String connectionURL) {
        try {
            clientSession = (ClientSession) Connector.open(connectionURL);
            clientSession.connect(null);
            clientSession.disconnect(null);
            clientSession.close();
            return true;
        } catch (IOException e) {
            System.out.println("May be remote device not available. Check bluetooth. " + e.getMessage());
        }

        return false;
    }

    public boolean sendImage(String connectionURL, File img) {
        int fileLength = 0;
        try (InputStream fis = new FileInputStream(img)) {
            clientSession = (ClientSession) Connector.open(connectionURL);
            hsConnectionReply = clientSession.connect(null);
            if (hsConnectionReply.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
                System.out.println("Failed to connect");
                return false;
            }
            fileLength = fis.available();
            byte[] data = new byte[fileLength];
            fis.read(data, 0, fileLength);

            HeaderSet hsOperation = clientSession.createHeaderSet();
            hsOperation.setHeader(HeaderSet.COUNT, 1L);
            hsOperation.setHeader(HeaderSet.LENGTH, (long) fileLength);
            hsOperation.setHeader(HeaderSet.NAME, img.getName());
            hsOperation.setHeader(HeaderSet.TYPE, "image");

            Operation putOperation = clientSession.put(hsOperation);
            OutputStream outputStream = putOperation.openOutputStream();

            outputStream.write(data);
            outputStream.close();
            putOperation.close();
            clientSession.disconnect(null);
            clientSession.close();

        } catch (FileNotFoundException fne) {
            System.out.println("File not found. \n" + fne.getMessage());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        return true;
    }
}

