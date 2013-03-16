package com.findcab.driver.activity;

import android.content.Context;
import android.content.SharedPreferences;

import com.iflytek.speech.SynthesizerPlayer;
import com.iflytek.ui.SynthesizerDialog;

/**
 * 合成页面,调用SDK的SynthesizerDialog实现语音合成.
 * 
 * @author iFlytek
 * @since 20120823
 */
public class Plysounds {

	// // 合成的文本
	// private EditText mSourceText;

	// 缓存对象.
	private SharedPreferences mSharedPreferences;

	// 合成对象.
	private SynthesizerPlayer mSynthesizerPlayer;

	// 弹出提示
	// private Toast mToast;

	// 缓冲进度

	// 播放进度

	// 合成Dialog
	private SynthesizerDialog ttsDialog;
	public Context context;

	/**
	 * 合成界面入口函数
	 * 
	 * @param savedInstanceState
	 */

	public Plysounds(Context context) {
		this.context = context;
		init();
	}

	public void init() {

		// setContentView(R.layout.demo);
		//
		// ((TextView) findViewById(android.R.id.title))
		// .setGravity(Gravity.CENTER);
		//
		// Button ttsButton = (Button) findViewById(android.R.id.button1);
		// ttsButton.setOnClickListener(this);
		// ttsButton.setText(R.string.text_tts);
		// Button settingButton = (Button) findViewById(android.R.id.button2);
		// settingButton.setOnClickListener(this);
		// settingButton.setText(R.string.text_setting);
		// mSourceText = (EditText) findViewById(R.id.txt_result);
		// mSourceText.setText(R.string.text_tts_source);
		// mSourceText.setKeyListener(TextKeyListener.getInstance());

		// 设置EditText的输入方式.
		// mSourceText.setInputType(EditorInfo.TYPE_CLASS_TEXT
		// | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);

		mSharedPreferences = context.getSharedPreferences(context
				.getPackageName(), Context.MODE_PRIVATE);

		// mToast = Toast.makeText(this, String.format(
		// getString(R.string.tts_toast_format), 0, 0), Toast.LENGTH_LONG);

		// 初始化合成Dialog.
		ttsDialog = new SynthesizerDialog(context, "appid="
				+ context.getString(R.string.app_id));
	}

	/**
	 * 使用SynthesizerPlayer合成语音，不弹出合成Dialog.
	 * 
	 * @param
	 */
	public void synthetizeInSilence(String content) {
		// 创建合成对象.
		mSynthesizerPlayer = SynthesizerPlayer.createSynthesizerPlayer(context,
				"appid=50ee7791");

		mSynthesizerPlayer.replay();

		// 设置合成发音人.
		String role = mSharedPreferences.getString(context
				.getString(R.string.preference_key_tts_role), context
				.getString(R.string.preference_default_tts_role));
		mSynthesizerPlayer.setVoiceName(role);

		// 设置发音人语速
		int speed = mSharedPreferences.getInt(context
				.getString(R.string.preference_key_tts_speed), 50);
		mSynthesizerPlayer.setSpeed(speed);

		// 设置音量.
		int volume = mSharedPreferences.getInt(context
				.getString(R.string.preference_key_tts_volume), 50);
		mSynthesizerPlayer.setVolume(volume);

		// 设置背景音.
		String music = mSharedPreferences.getString(context
				.getString(R.string.preference_key_tts_music), context
				.getString(R.string.preference_default_tts_music));
		mSynthesizerPlayer.setBackgroundSound(music);

		// 进行语音合成.
		if (content != null) {

			mSynthesizerPlayer.playText(content, null, null);
		}

	}

	public SynthesizerPlayer getMySynthesizerPlayer() {
		return mSynthesizerPlayer;

	}

	/**
	 * 弹出合成Dialog，进行语音合成
	 * 
	 * @param
	 */
	public void showSynDialog(String source) {

		// 设置合成文本.
		ttsDialog.setText(source, null);

		// 设置发音人.
		String role = mSharedPreferences.getString(context
				.getString(R.string.preference_key_tts_role), context
				.getString(R.string.preference_default_tts_role));
		ttsDialog.setVoiceName(role);

		// 设置语速.
		int speed = mSharedPreferences.getInt(context
				.getString(R.string.preference_key_tts_speed), 50);
		ttsDialog.setSpeed(speed);

		// 设置音量.
		int volume = mSharedPreferences.getInt(context
				.getString(R.string.preference_key_tts_volume), 50);
		ttsDialog.setVolume(volume);

		// 设置背景音.
		String music = mSharedPreferences.getString(context
				.getString(R.string.preference_key_tts_music), context
				.getString(R.string.preference_default_tts_music));
		ttsDialog.setBackgroundSound(music);

		// 弹出合成Dialog
		ttsDialog.show();
	}
}
