package com.andy.dpi;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Calculator extends Activity {

    private EditText mScreenWidth;
    private EditText mScreenHeight;
    private EditText mScreenSize;

    private EditText mDpiResult;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculator);

        initUI();
    }

    private void initUI() {
        mScreenWidth = ((EditText) findViewById(R.id.screen_width));
        mScreenHeight = (EditText) findViewById(R.id.screen_height);
        mScreenSize = (EditText) findViewById(R.id.screen_size);
        mDpiResult = (EditText) findViewById(R.id.dpi_result);

        ((Button) findViewById(R.id.btn_clear))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        mScreenWidth.getEditableText().clear();
                        mScreenHeight.getEditableText().clear();
                        mScreenSize.getEditableText().clear();
                        mDpiResult.getEditableText().clear();
                    }
                });

        ((Button) findViewById(R.id.btn_calculate))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (isUserInputValid()) {
                            int w = getUserInput(mScreenWidth);
                            int h = getUserInput(mScreenHeight);
                            int s = getUserInput(mScreenSize);

                            double dpi = calculateDpi(w, h, s);
                            mDpiResult.setText(String.format("%.1f", dpi));
                        } else {
                            Toast.makeText(Calculator.this, "Input invalid",
                                    3000).show();
                        }
                    }
                });
    }

    private int getUserInput(EditText et) {
        return Integer.parseInt(et.getText().toString());
    }

    private boolean isUserInputValid() {
        if (mScreenWidth.getEditableText().length() == 0)
            return false;
        if (mScreenHeight.getEditableText().length() == 0)
            return false;
        if (mScreenSize.getEditableText().length() == 0)
            return false;
        return true;
    }

    private double calculateDpi(int w, int h, int s) {
        return Math.sqrt(h*h + w*w) / s;
    }
}