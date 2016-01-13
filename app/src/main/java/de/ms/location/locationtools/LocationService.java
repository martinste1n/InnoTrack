package de.ms.location.locationtools;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.location.Location;


import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service implements 
								GoogleApiClient.ConnectionCallbacks,
								GoogleApiClient.OnConnectionFailedListener,
								LocationListener {

	private GoogleApiClient mGoogleApiClient;

    private LocationRequest forceUpdateRequest = null;
    private final String TAG = "PTEnabler";
    private LocationRequest defaulLocRequest=null; 
    private LocationRequest current = null;
    private Handler handler = new Handler();
    private Runnable resetIntervalFallback = new Runnable() {

        public void run() {
            setNormalInterval();
          }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        connectService();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO: DExter Abfrage machen
		Dexter.checkPermission(new PermissionListener() {
			@Override
			public void onPermissionGranted(PermissionGrantedResponse response) {

			}

			@Override
			public void onPermissionDenied(PermissionDeniedResponse response) {

			}

			@Override
			public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

			}
		}, Manifest.permission.ACCESS_FINE_LOCATION);
		if(mGoogleApiClient!=null && (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting())){
            Log.d(TAG,"Service already running...");
        }else{
            connectService();
        }
		return START_STICKY;

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
        Log.d(TAG, "LocationService destroyed");
		mGoogleApiClient.disconnect();
        }


	public void onConnected(Bundle bundle) {
		Log.i(TAG, "GoogleApiClient connection has been connected");
		if(mGoogleApiClient.isConnected()){
            forceLocationUpdateSetting();
        }
	    PTNMELocationManager.setService(this);
    }

	
	public void onConnectionSuspended(int i) {
	Log.i(TAG, "GoogleApiClient connection has been suspend");
	PTNMELocationManager.setService(null);
	}
	
	
	public void onConnectionFailed(ConnectionResult result) {
	Log.i(TAG, "GoogleApiClient connection has failed");
	PTNMELocationManager.setService(null);
	}
	
	
	
	public void onLocationChanged(Location location) {
        Log.d(TAG, "Received Location: " + location.getLatitude() + "/" + location.getLongitude() + "(Accuracy: " + location.getAccuracy() + " " + location.getProvider() + ")");
        if(location.hasAccuracy() && location.getAccuracy()<300){
			Intent i = new Intent("de.ms.ptenabler.LOCATION_RECEIVED");
			i.putExtra("LOCATION", location);
			sendBroadcast(i);
		}else{
			Log.d("PTEnabler", "Dropped Location due to insufficient accuracy: " + location.getAccuracy());
		}

		if (current == forceUpdateRequest){
		Log.d("PTEnabler", "Resetting Location Request Interval");
			setNormalInterval();
		}
	}
	
	public void forceLocationUpdateSetting (){
		Log.d("PTEnabler", "Forcing Location Request");
		forceUpdateRequest = LocationRequest.create();
		forceUpdateRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		forceUpdateRequest.setFastestInterval(5000l);
		forceUpdateRequest.setInterval(1000l);
	    try {
			LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, forceUpdateRequest, this);
		}catch(SecurityException e){
			Log.e("InnoTrack", "No Permission to request locations");
		}
	    current = forceUpdateRequest;
        handler.postDelayed(resetIntervalFallback, 10000l);

	}
	public void setNormalInterval(){

		defaulLocRequest = LocationRequest.create();
	    defaulLocRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
	    defaulLocRequest.setFastestInterval(1000l);
	    defaulLocRequest.setInterval(1000l * 300l);
	    try {
			LocationServices.FusedLocationApi.requestLocationUpdates(
					mGoogleApiClient, defaulLocRequest, this);

		}catch(SecurityException e){
			Log.e("InnoTrack", "No Permission to request locations");
		}
	    current = defaulLocRequest;

	}
	
	
	
	public IBinder onBind(Intent intent) {
		
		return null;
	}

	private void connectService(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
				.addApi(ActivityRecognition.API)
				.addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        Log.d(TAG, "LocationService starting....");

    }

}
