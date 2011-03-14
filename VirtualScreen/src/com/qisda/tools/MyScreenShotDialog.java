package com.qisda.tools;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.Log;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.TimeoutException;
import com.qisda.tools.TouchController.TouchListener;

public class MyScreenShotDialog extends Dialog {
    private Label mBusyLabel;
    private Label mImageLabel;
    private IDevice mDevice;
    private RawImage mRawImage;
    
    private int mRotateCount;
    
    private boolean mScaled = true;


    /**
     * Create with default style.
     */
    public MyScreenShotDialog(Shell parent) {
        this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    }

    /**
     * Create with app-defined style.
     */
    public MyScreenShotDialog(Shell parent, int style) {
        super(parent, style);
    }

    /**
     * Prepare and display the dialog.
     * @param device The {@link IDevice} from which to get the screenshot.
     */
    public void open(IDevice device) {
        mDevice = device;

        Shell parent = getParent();
        Shell shell = new Shell(parent, getStyle());
        shell.setText("Device Screen Capture");

        createContents(shell);
        shell.pack();
        shell.open();

        updateDeviceImage(shell);

        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

    }

    /*
     * Create the screen capture dialog contents.
     */
    private void createContents(final Shell shell) {
        GridData data;

        final int colCount = 5;

        shell.setLayout(new GridLayout(colCount, true));

        // "refresh" button
        Button refresh = new Button(shell, SWT.PUSH);
        refresh.setText("Refresh");
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        data.widthHint = 80;
        refresh.setLayoutData(data);
        refresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateDeviceImage(shell);
            }
        });

        // "rotate" button
        Button rotate = new Button(shell, SWT.PUSH);
        rotate.setText("Rotate");
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        data.widthHint = 80;
        rotate.setLayoutData(data);
        rotate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (mRawImage != null) {
                    mRawImage = mRawImage.getRotated();
                    updateImageDisplay(shell);
                    
                    mRotateCount++;
                    if (mRotateCount >= 4) mRotateCount = 0;
                }
            }
        });

        // "done" button
        Button done = new Button(shell, SWT.PUSH);
        done.setText("Done");
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        data.widthHint = 80;
        done.setLayoutData(data);
        done.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });

        // title/"capturing" label
        mBusyLabel = new Label(shell, SWT.NONE);
        mBusyLabel.setText("Preparing...");
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.horizontalSpan = colCount;
        mBusyLabel.setLayoutData(data);

        // space for the image
        mImageLabel = new Label(shell, SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        data.horizontalSpan = colCount;
        mImageLabel.setLayoutData(data);
        Display display = shell.getDisplay();
        mImageLabel.setImage(createPlaceHolderArt(
                display, 50, 50, display.getSystemColor(SWT.COLOR_BLUE)));
        TouchController tc = new TouchController(mDevice, mScaled);
        tc.setTouchListener(new TouchListener() {
            public void onClick() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                updateDeviceImage(shell);
            }
        });
        mImageLabel.addMouseListener(tc);
        mImageLabel.addMouseMoveListener(tc);


        shell.setDefaultButton(done);
    }

    /**
     * Captures a new image from the device, and display it.
     */
    private void updateDeviceImage(Shell shell) {
        mBusyLabel.setText("Capturing...");     // no effect

        shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            getDeviceImage();
            long end = System.currentTimeMillis();
            System.out.println("count " + i + " take " + (end-start) + "ms");
        }
        mRawImage = getDeviceImage();
        System.out.println("size is " + mRawImage.size);
        for (int i = 0; i < mRotateCount; i++) {
            mRawImage = mRawImage.getRotated();
        }

        updateImageDisplay(shell);
    }

    /**
     * Updates the display with {@link #mRawImage}.
     * @param shell
     */
    private void updateImageDisplay(Shell shell) {
        Image image;
        if (mRawImage == null) {
            Display display = shell.getDisplay();
            image = createPlaceHolderArt(
                    display, 320, 240, display.getSystemColor(SWT.COLOR_BLUE));

            mBusyLabel.setText("Screen not available");
        } else {
            // convert raw data to an Image.
            PaletteData palette = new PaletteData(
                    mRawImage.getRedMask(),
                    mRawImage.getGreenMask(),
                    mRawImage.getBlueMask());

            ImageData imageData = new ImageData(mRawImage.width, mRawImage.height,
                    mRawImage.bpp, palette, 1, mRawImage.data);
            if (mScaled) {
                imageData = imageData.scaledTo(mRawImage.width/2, mRawImage.height/2);
            } else {
                imageData = imageData.scaledTo(mRawImage.width, mRawImage.height);
            }
            image = new Image(getParent().getDisplay(), imageData);

            mBusyLabel.setText("Captured image:");
        }

        mImageLabel.setImage(image);
        mImageLabel.pack();
        shell.pack();

        // there's no way to restore old cursor; assume it's ARROW
        shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
    }

    /**
     * Grabs an image from an ADB-connected device and returns it as a {@link RawImage}.
     */
    private RawImage getDeviceImage() {
        try {
            return mDevice.getScreenshot();
        }
        catch (IOException ioe) {
            Log.w("ddms", "Unable to get frame buffer: " + ioe.getMessage());
            return null;
        } catch (TimeoutException e) {
            Log.w("ddms", "Unable to get frame buffer: timeout ");
            return null;
        } catch (AdbCommandRejectedException e) {
            Log.w("ddms", "Unable to get frame buffer: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Create place-holder art with the specified color.
     */
    private Image createPlaceHolderArt(Display display, int width,
            int height, Color color) {
        Image img = new Image(display, width, height);
        GC gc = new GC(img);
        gc.setForeground(color);
        gc.drawLine(0, 0, width, height);
        gc.drawLine(0, height - 1, width, -1);
        gc.dispose();
        return img;
    }
}
