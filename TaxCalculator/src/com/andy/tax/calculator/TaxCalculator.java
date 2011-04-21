package com.andy.tax.calculator;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class TaxCalculator extends Activity {

	private EditText mTotalIncome;
	private EditText mOtherTax;
	private EditText mNewTax;
	private EditText mOldTax;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Look up the AdView as a resource and load a request.
	    AdView adView = (AdView)this.findViewById(R.id.adView);
	    adView.loadAd(new AdRequest());

		mTotalIncome = (EditText) findViewById(R.id.total_income);
		mOtherTax = (EditText) findViewById(R.id.other_tax);
		mNewTax = (EditText) findViewById(R.id.new_tax);
		mOldTax = (EditText) findViewById(R.id.old_tax);

		((Button) findViewById(R.id.btn_clear))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						mTotalIncome.getEditableText().clear();
						mOtherTax.getEditableText().clear();
						mNewTax.getEditableText().clear();
						mOldTax.getEditableText().clear();
					}
				});

		((Button) findViewById(R.id.btn_calculate))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						long totalIncome = 0;
						long otherTax = 0;
						long actualIncom = 0;
						
						cleanInputPannel();

						try {
							totalIncome = Long.parseLong(mTotalIncome
									.getEditableText().toString());
							otherTax = Long.parseLong(mOtherTax
									.getEditableText().toString());

						} catch (NumberFormatException e) {
						}

						if (totalIncome > otherTax) {
							actualIncom = totalIncome - otherTax;
						}

						long newTax = calculateNewTax(actualIncom);
						long oldTax = calculateOldTax(actualIncom);
						mNewTax.setText(Long.toString(newTax));
						mOldTax.setText(Long.toString(oldTax));

						if (newTax < oldTax) {
							Toast.makeText(TaxCalculator.this,
									"感谢国家感谢党，为您减税" + (oldTax - newTax) + "元",
									5000).show();
						} else if (newTax > oldTax) {
							Toast.makeText(TaxCalculator.this,
									"代表国家感謝您，光荣多纳税" + (newTax - oldTax) + "元",
									5000).show();
						} else {
							Toast.makeText(TaxCalculator.this,
									"感谢国家，本次调整没有多收您的税",
									5000).show();
						}
					}
				});
	}

	private long calculateNewTax(long income) {
		double tax = 0;
		income -= 3000;

		if (income <= 0)
			tax = 0;
		else if (income <= 1500)
			tax = income * 0.05;
		else if (income <= 4500)
			tax = income * 0.1 - 75;
		else if (income <= 9000)
			tax = income * 0.2 - 525;
		else if (income <= 35000)
			tax = income * 0.25 - 975;
		else if (income <= 55000)
			tax = income * 0.3 - 2725;
		else if (income <= 80000)
			tax = income * 0.35 - 5475;
		else
			tax = income * 0.45 - 13475;

		return (long) tax;
	}

	private long calculateOldTax(long income) {
		double tax = 0;
		income -= 2000;

		if (income <= 0)
			tax = 0;
		else if (income <= 500)
			tax = income * 0.05;
		else if (income <= 2000)
			tax = income * 0.1 - 25;
		else if (income <= 5000)
			tax = income * 0.15 - 125;
		else if (income <= 20000)
			tax = income * 0.2 - 375;
		else if (income <= 40000)
			tax = income * 0.25 - 1375;
		else if (income <= 60000)
			tax = income * 0.3 - 3375;
		else if (income <= 80000)
			tax = income * 0.35 - 6375;
		else if (income <= 100000)
			tax = income * 0.40 - 10375;
		else
			tax = income * 0.45 - 15375;

		return (long) tax;
	}

	private void cleanInputPannel() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mTotalIncome.getWindowToken(), 0);
	}
}