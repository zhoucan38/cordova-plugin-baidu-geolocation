package com.baidu.location;

import java.util.Locale;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

public class LocationPlugin extends CordovaPlugin {

	private static final String ACTION_GETLOCATION = "getlocation";

	private CallbackContext callbackContext = null;
	private LocationClient locationClient = null;
	private LocationListener locationListener = new LocationListener();

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		if (locationClient == null) {
			LocationClientOption option = new LocationClientOption();
			option.setCoorType( "bd09ll" ); // 可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
			option.setScanSpan(3000); // 可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
			option.setIsNeedAddress(true); // 可选，设置是否需要地址信息，默认不需要
			option.setIsNeedLocationDescribe(true); // 可选，设置是否需要地址描述
			option.setNeedDeviceDirect(false); // 可选，设置是否需要设备方向结果
			option.setLocationNotify(false); // 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
			option.setIgnoreKillProcess(true); // 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop
			option.setIsNeedLocationDescribe(true); // 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation
			option.setIsNeedLocationPoiList(true); // 可选，默认false，设置是否需要POI结果，可以在BDLocation
			option.SetIgnoreCacheException(false); // 可选，默认false，设置是否收集CRASH信息，默认收集
			option.setLocationMode(LocationMode.Hight_Accuracy); // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备，模糊
			option.setIsNeedAltitude(false); // 可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
			// 可选，设置首次定位时选择定位速度优先还是定位准确性优先，默认为速度优先
			option.setFirstLocType(LocationClientOption.FirstLocType.SPEED_IN_FIRST_LOC);
			try {
				locationClient = new LocationClient(cordova.getActivity().getApplicationContext());
			} catch (Exception e) {
				e.printStackTrace();
			}
			locationClient.setLocOption(option);
			locationClient.registerLocationListener(locationListener);
		}
		super.initialize(cordova, webView);
	}

	@Override
	public void onDestroy() {
		if (locationClient != null) {
			locationClient.unRegisterLocationListener(locationListener);
			if (locationClient.isStarted()) {
				locationClient.stop();
			}
			locationClient = null;
		}
		super.onDestroy();
	}

	@Override
	public boolean execute(String action, JSONArray args,	CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
		if (ACTION_GETLOCATION.equals(action.toLowerCase(Locale.CHINA))) {
			locationClient.start();
			PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
			r.setKeepCallback(true);
			callbackContext.sendPluginResult(r);
			return true;
		}
		return false;
	}

	private class LocationListener extends BDAbstractLocationListener {
		@Override
		public void onReceiveLocation(BDLocation loc) {
			if (locationClient.isStarted()) {
				locationClient.stop();
			}
			JSONObject jo = new JSONObject();
			try {
				jo.put("longitude",  loc.getLongitude());
				jo.put("latitude",  loc.getLatitude());
				jo.put("address", loc.getAddrStr());
				jo.put("hasRadius ", loc.hasRadius());
				jo.put("radius", loc.getRadius());
				int type = loc.getLocType();
				String typeStr = (type == BDLocation.TypeGpsLocation ? "gps" : (type==BDLocation.TypeNetWorkLocation ? "网络" : "其它"));
				jo.put("type",typeStr);
			} catch (JSONException e) {
				jo = null;
				e.printStackTrace();
			}
			if(jo!=null) {
				Log.d("LocationPlugin", "location:"+jo.toString());
				callbackContext.success(jo);
			}
		}
	}
}