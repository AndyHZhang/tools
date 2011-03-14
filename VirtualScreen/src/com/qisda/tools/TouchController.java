package com.qisda.tools;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.monkeyrunner.MonkeyManager;
import com.android.monkeyrunner.adb.LoggingOutputReceiver;

public class TouchController implements MouseListener, MouseMoveListener {

    private IDevice mDevice;
    private MonkeyManager mManager;
    private boolean mScaled;
    private boolean mTouchDown;

    private TouchListener mListener;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public interface TouchListener {
        public void onClick();
    }

    public void setTouchListener(TouchListener listener) {
        mListener = listener;
    }

    public TouchController(IDevice device, boolean scaled) {
        mDevice = device;

        mManager = createManager("127.0.0.1", 6789);

        mScaled = scaled;
    }

    public void mouseMove(MouseEvent e) {
        /*
        if (mTouchDown) {
            int x = getX(e);
            int y = getY(e);

            try {
                mManager.touchMove(x, y);
            } catch (IOException exp) {
                System.out.println("Error sending touch move event:" + x + " "
                        + y + " " + exp);
            }
        }
        */
    }

    public void mouseDoubleClick(MouseEvent e) {

    }

    public void mouseDown(MouseEvent e) {
        /*
        int x = getX(e);
        int y = getY(e);
        try {
            mManager.touchDown(x, y);
            //mTouchDown = true;
            if (mListener != null) {
                mListener.onClick();
            }
        } catch (IOException exp) {
            System.out.println("Error sending touch down event:" + x + " " + y
                    + " " + exp);
        }
        */
    }

    public void mouseUp(MouseEvent e) {
        int x = getX(e);
        int y = getY(e);
        try {
            //mManager.touchUp(x, y);
            mManager.tap(x, y);
            mTouchDown = false;
            if (mListener != null) {
                mListener.onClick();
            }
        } catch (IOException exp) {
            System.out.println("Error sending touch up event:" + x + " " + y
                    + " " + exp);
        }
    }

    private int getX(MouseEvent e) {
        return mScaled ? e.x * 2 : e.x;
    }

    private int getY(MouseEvent e) {
        return mScaled ? e.y * 2 : e.y;
    }

    private void executeAsyncCommand(final String command,
            final LoggingOutputReceiver logger) {
        executor.submit(new Runnable() {
            public void run() {
                try {
                    mDevice.executeShellCommand(command, logger);
                } catch (TimeoutException e) {
                    // LOG.log(Level.SEVERE, "Error starting command: " +
                    // command, e);
                    throw new RuntimeException(e);
                } catch (AdbCommandRejectedException e) {
                    // LOG.log(Level.SEVERE, "Error starting command: " +
                    // command, e);
                    throw new RuntimeException(e);
                } catch (ShellCommandUnresponsiveException e) {
                    // This happens a lot
                    // LOG.log(Level.INFO, "Error starting command: " + command,
                    // e);
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    // LOG.log(Level.SEVERE, "Error starting command: " +
                    // command, e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private MonkeyManager createManager(String address, int port) {
        try {
            mDevice.createForward(port, port);
        } catch (TimeoutException e) {
            // LOG.log(Level.SEVERE, "Timeout creating adb port forwarding", e);
            return null;
        } catch (AdbCommandRejectedException e) {
            // LOG.log(Level.SEVERE,
            // "Adb rejected adb port forwarding command: " + e.getMessage(),
            // e);
            return null;
        } catch (IOException e) {
            // LOG.log(Level.SEVERE, "Unable to create adb port forwarding: " +
            // e.getMessage(), e);
            return null;
        }

        String command = "monkey --port " + port;
        executeAsyncCommand(command, null);

        // Sleep for a second to give the command time to execute.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // LOG.log(Level.SEVERE, "Unable to sleep", e);
        }

        InetAddress addr;
        try {
            addr = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            // LOG.log(Level.SEVERE,
            // "Unable to convert address into InetAddress: " + address, e);
            return null;
        }

        Socket monkeySocket = null;
        try {
            monkeySocket = new Socket(addr, port);
        } catch (IOException e) {
            // LOG.log(Level.FINE, "Unable to connect socket", e);
            // success = false;
            // continue;
        }

        MonkeyManager mm = new MonkeyManager(monkeySocket);

        return mm;
    }
}
