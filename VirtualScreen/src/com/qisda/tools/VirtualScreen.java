package com.qisda.tools;

import java.io.File;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;


public class VirtualScreen {
    /*
    public static void main(String[] args) {
        Shell shell = new Shell();
        ScreenShotDialog ss = new ScreenShotDialog(shell);
        shell.open();
        Display.getCurrent().dispose();
    }
    */
    
    public static void main(String[] args) {
        boolean device = false;
        boolean emulator = false;
        String serial = null;
        String filepath = null;
        boolean landscape = false;

        // init the lib
        // [try to] ensure ADB is running
        String adbLocation = System.getProperty("com.android.screenshot.bindir"); //$NON-NLS-1$
        if (adbLocation != null && adbLocation.length() != 0) {
            adbLocation += File.separator + "adb"; //$NON-NLS-1$
        } else {
            adbLocation = "adb"; //$NON-NLS-1$
        }

        AndroidDebugBridge.init(false /* debugger support */);

        try {
            AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(
                    adbLocation, true /* forceNewBridge */);

            // we can't just ask for the device list right away, as the internal thread getting
            // them from ADB may not be done getting the first list.
            // Since we don't really want getDevices() to be blocking, we wait here manually.
            int count = 0;
            while (bridge.hasInitialDeviceList() == false) {
                try {
                    Thread.sleep(100);
                    count++;
                } catch (InterruptedException e) {
                    // pass
                }

                // let's not wait > 10 sec.
                if (count > 100) {
                    System.err.println("Timeout getting device list!");
                    return;
                }
            }

            // now get the devices
            IDevice[] devices = bridge.getDevices();

            if (devices.length == 0) {
                printAndExit("No devices found!", true /* terminate */);
            }

            IDevice target = null;

            if (emulator || device) {
                for (IDevice d : devices) {
                    // this test works because emulator and device can't both be true at the same
                    // time.
                    if (d.isEmulator() == emulator) {
                        // if we already found a valid target, we print an error and return.
                        if (target != null) {
                            if (emulator) {
                                printAndExit("Error: more than one emulator launched!",
                                        true /* terminate */);
                            } else {
                                printAndExit("Error: more than one device connected!",true /* terminate */);
                            }
                        }
                        target = d;
                    }
                }
            } else if (serial != null) {
                for (IDevice d : devices) {
                    if (serial.equals(d.getSerialNumber())) {
                        target = d;
                        break;
                    }
                }
            } else {
                if (devices.length > 1) {
                    printAndExit("Error: more than one emulator or device available!",
                            true /* terminate */);
                }
                target = devices[0];
            }

            if (target != null) {
                display(target);
                /*
                try {
                    System.out.println("Taking screenshot from: " + target.getSerialNumber());
                    getDeviceImage(target, filepath, landscape);
                    System.out.println("Success.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */
            } else {
                printAndExit("Could not find matching device/emulator.", true /* terminate */);
            }
        } finally {
            AndroidDebugBridge.terminate();
        }
    }
    
    private static void display(IDevice devices) {
        Shell shell = new Shell();
        MyScreenShotDialog dlg = new MyScreenShotDialog(shell);
        dlg.open(devices);
        Display.getCurrent().dispose();
    }
    
    private static void printAndExit(String message, boolean terminate) {
        System.out.println(message);
        if (terminate) {
            AndroidDebugBridge.terminate();
        }
        System.exit(1);
    }
}
