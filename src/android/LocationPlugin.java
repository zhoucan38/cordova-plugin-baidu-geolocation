package com.baidu.location;

import android.util.Log;

import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationPlugin extends CordovaPlugin {

	private CallbackContext callbackContext = null;
	private LocationClient locationClient = null;
	private LocationListener locationListener = new LocationListener();

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		// 隐私政策设置同意
		LocationClient.setAgreePrivacy(true);
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
		if ("getLocation".equals(action)) {
			locationClient.start();
			PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
			r.setKeepCallback(true);
			callbackContext.sendPluginResult(r);
			return true;
		}
		if ("getDistance".equals(action)) {
			Double latitudeFrom = args.getDouble(0);
			Double longitudeFrom = args.getDouble(1);
			Double latitudeTo = args.getDouble(2);
			Double longitudeTo = args.getDouble(3);
			double distance = 0;
			try {
				// TODO: DistanceUtil报错
				distance = DistanceUtil.getDistance(new LatLng(latitudeFrom, longitudeFrom), new LatLng(latitudeTo, longitudeTo));
				callbackContext.success(Double.toString(distance));
			} catch (Exception e) {
				e.printStackTrace();
				callbackContext.error(e.getMessage());
				return true;
			}
			callbackContext.success(Double.toString(distance));
			return true;
		}
		return false;
	}

	private class LocationListener extends BDAbstractLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (locationClient.isStarted()) {
				locationClient.stop();
			}
			JSONObject jo = new JSONObject();
			try {
				jo.put("longitude",  location.getLongitude());
				jo.put("latitude",  location.getLatitude());
				jo.put("addr", location.getAddrStr());
				jo.put("country", location.getCountry());
				jo.put("province", location.getProvince());
				jo.put("city", location.getCity());
				jo.put("district", location.getDistrict());
				jo.put("adcode", location.getAdCode());
				jo.put("town", location.getTown());
				jo.put("hasRadius", location.hasRadius());
				jo.put("radius", location.getRadius());
				jo.put("errorCode", location.getLocType());
				int locType = location.getLocType();
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