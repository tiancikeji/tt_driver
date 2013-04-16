package com.findcab.jpush;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

public class MyJpushTools {
	/**
	 *设置Alias
	 */
	public static void setAlias(Context context,String alias){//这里不适用手机imei来识别用户，有可能获取不到
		if (TextUtils.isEmpty(alias)) {
			Toast.makeText(context,"alias不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if (!isValidTagAndAlias(alias)) {
			Toast.makeText(context,"格式不对", Toast.LENGTH_SHORT).show();
			return;
		}
		
		//调用JPush API设置Alias
		JPushInterface.setAliasAndTags(context.getApplicationContext(), alias, null);
		Toast.makeText(context,"设置成功", Toast.LENGTH_SHORT).show();

	}

	// 校验Tag Alias 只能是数字,英文字母和中文
    public static boolean isValidTagAndAlias(String s) {
        Pattern p = Pattern.compile("^[\u4E00-\u9FA50-9a-zA-Z_-]{0,}$");
        Matcher m = p.matcher(s);
        return m.matches();
    }
}
