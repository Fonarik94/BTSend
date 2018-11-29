package com.fonarik94;

import org.apache.commons.cli.*;

import javax.bluetooth.BluetoothStateException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static final int DEFAULT_UPDATE_PERIOD = 30;
    private static PreferencesManager userPrefs = new PreferencesManager();
    private static String serviceUrl = userPrefs.getServicePath();
    private static int updatePeriod = userPrefs.getUpdatePeriod();
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static BluetoothManager bluetoothManager = new BluetoothManager();
    private static Map<Integer, BluetoothDevice> deviceMap = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Options options = new Options();
        Option runOption = new Option("r", "Run parameter help's run app from scheduler");
        runOption.setRequired(false);
        options.addOption(runOption);
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        if ((cmd != null) & cmd.hasOption("r") & (serviceUrl != null)) {
            run();
        } else {
            menu();
        }
    }

    private static void run() {
        scheduler.scheduleAtFixedRate(task, 3, updatePeriod, TimeUnit.SECONDS);
    }

    private static Runnable task = () -> {
        try {
            deviceSelector();
            if (bluetoothManager.available(serviceUrl)) {
                byte[] rawFile = ImageDownloader.download();
                bluetoothManager.sendImage(serviceUrl, rawFile);
            }
        } catch (IOException e) {
            System.out.println("Download error. Check network");
        }
    };

    private static void deviceSelector() throws BluetoothStateException {
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
                int selectedDeviceIndex = scanner.nextInt();
                if (selectedDeviceIndex > i || selectedDeviceIndex < 0) {
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
        }
    }

    private static void setUpdatePeriod(int period) {
        updatePeriod = period;
        userPrefs.setUpdatePeriod(period);
        System.out.printf("Update period was set to %d seconds \n", period);
    }

    private static void menu() {
        boolean end = false;
        System.out.println("PhotoViewer 1.0");
        while(!end) {
            System.out.println("Select action: ");
            System.out.println("1. Run");
            System.out.println("2. Change update period (now it " + updatePeriod + " sec)");
            System.out.println("3. Reset parameters");
            System.out.println("4. Exit");
            System.out.println("Input:");
            switch (scanner.nextInt()) {
                case 1:
                    run();
                    end = true;
                    break;
                case 2:
                    System.out.println("Input update period in seconds: ");
                    setUpdatePeriod(scanner.nextInt());
                    break;
                case 3:
                    userPrefs.resetAll();
                    setUpdatePeriod(DEFAULT_UPDATE_PERIOD);
                    serviceUrl = null;
                    System.out.println("Parameters set to default values");
                    break;
                case 4:
                    scheduler.shutdown();
                    System.exit(0);
                    break;
            }
        }
    }
}

