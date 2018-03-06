package com.fonarik94;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static PreferencesManager userPrefs = new PreferencesManager();
    private static String serviceUrl = userPrefs.getServicePath();
    private static String workDir = userPrefs.getWorkDirPath();
    private static int updatePeriod = userPrefs.getUpdatePeriod();
    private static ScheduledExecutorService sceduler = Executors.newScheduledThreadPool(1);


    private static BluetoothManager bluetoothManager = new BluetoothManager();
    private static Map<Integer, BluetoothDevice> deviceMap = new HashMap<Integer, BluetoothDevice>();

    public static void main(String[] args) throws IOException {
        System.out.println("PhotoViewer 1.0");
        System.out.println("Select action: ");
        System.out.println("1. Run");
        System.out.println("2. Change update period (default 30 sec");
        System.out.println("3. Reset parameters");
        System.out.println("4. Exit");


            System.out.println("Input:");
            switch (input()) {
                case 1:
                    sceduler.scheduleAtFixedRate(task, 3, updatePeriod, TimeUnit.SECONDS);
                    break;
                case 2: setUpdatePeriod();
                    break;
                case 3:
                    userPrefs.resetAll();
                    break;
                case 4:
                    sceduler.shutdown();
                    System.exit(0);
                    break;
            }


/*//        pathChooser();
        deviceChooser();
        new ImageDownloader().downloadImage(workDir);
        File imageFile = new File(workDir);
        bluetoothManager.sendImage(serviceUrl, imageFile);*/

    }

    private static Runnable task = new Runnable() {
        @Override
        public void run() {
            deviceChooser();
            new ImageDownloader().downloadImage(workDir);
            File imageFile = new File(workDir);
            bluetoothManager.sendImage(serviceUrl, imageFile);
        }
    };

    private static int input(){
       Scanner scanner = new Scanner(System.in);
       return scanner.nextInt();

    }

    private static void deviceChooser() {
        if (serviceUrl == null) {
            int i = 1;
            for (BluetoothDevice btd : bluetoothManager.getAvailableDevices()) {
                deviceMap.put(i, btd);
                i++;
            }
            while (!deviceMap.isEmpty()) {
                for (Integer key : deviceMap.keySet()) {
                    System.out.println(key + ". " + deviceMap.get(key).getName());
                }
                System.out.print("Select device: ");
                int selectedDeviceIndex = input();
                if(selectedDeviceIndex>i||selectedDeviceIndex<0){
                    System.out.println("Wrong input!");
                    continue;
                }
                BluetoothDevice selectedDevice = deviceMap.get(selectedDeviceIndex);
                serviceUrl = bluetoothManager.getAvailableServices(selectedDevice);
                if (serviceUrl == null) {
                    System.out.println("OBEX File transfer service not available. Select another device:");
                    deviceMap.remove(selectedDeviceIndex);
                    continue;
                } else {
                    userPrefs.setServicePath(serviceUrl);
                    System.out.println("Saved device connection string: " + serviceUrl);
                    break;
                }
            }
        } else {
            System.out.println("Using device with connection URL: " + serviceUrl);
        }
    }

    private static void pathChooser() {
        String path;
            System.out.println("Select folder");
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Select Dir");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                path = chooser.getSelectedFile().toString();
                userPrefs.setWorkDirPath(path);
                System.out.println("Saved path: " + path);
                workDir = path;

        }
    }

    private static void setUpdatePeriod(){
        System.out.println("Input update period in seconds: ");
        int period = input();
        userPrefs.setUpdatePeriod(period);
        System.out.printf("Update period was set to %d seconds", period);
    }


}

