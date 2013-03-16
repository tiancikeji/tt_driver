package com.findcab.driver.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CallActivity extends Activity implements OnClickListener {

	private Button back;
	private Button record;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.call);
		initView();
	}

	private void initView() {
		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(this);
		record = (Button) findViewById(R.id.record);
		record.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.record:
			// Intent intent = new
			// Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			// intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
			// RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			// intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "开始语音");
			// startActivityForResult(intent, 1);

			break;
		case R.id.back:

			finish();
			break;

		default:
			break;
		}
	}
}
