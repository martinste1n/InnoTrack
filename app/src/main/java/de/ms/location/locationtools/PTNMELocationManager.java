package de.ms.location.locationtools;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.LatLng;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import de.ms.location.Utilities;
import de.schildbach.pte.LocationUtils;
import de.schildbach.pte.dto.Location;

public class PTNMELocationManager {

    private static LocationService service;
    static boolean useFallBackLocation = false;
    static Location lastLocation = null;
    static long	lastLocationReceivedAt = 0;
    static Vector<LocationListener> listener;
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;
    private static android.os.Handler handler = new android.os.Handler();
    private static Runnable updateTimeOutRunnable = new Runnable() {
        public void run() {
            notifyListener(null);
        }
    };
    public static int LocationServiceStatusCode = ConnectionResult.SUCCESS;

    public static LocationService getService() {
        return service;
    }
    public static void setService(LocationService svc) {
        if(svc ==null && service !=null) Toast.makeText(service, "TNME Service stopped", Toast.LENGTH_SHORT).show();

        service = svc;
    }

    public static void startService(){
        Dexter.checkPermissions(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if(report.areAllPermissionsGranted()){
                    Utilities.getContext().startService(new Intent(Utilities.getContext(), LocationService.class));
                }else{
                    setService(null);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

            }
        }, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION);

    }

    public static void forceLocationUpdate(boolean enable) {
        if (service != null) {
            if (enable) {
                service.forceLocationUpdateSetting();

            } else {
                service.setNormalInterval();
            }

        }
    }


    public static Location getPTLocation(double lat, double lng){

        Location loc1;

        loc1 = Location.coord((int)(lat*1000000), (int)(lng*1000000));

        return loc1;
    }

    public static Location getLocation(){
        if(prefs==null){
            prefs= PreferenceManager.getDefaultSharedPreferences(Utilities.getContext());
        }
        if(lastLocation==null){
            lastLocationReceivedAt = prefs.getLong(LocationReceiver.LAST_POISITION_UPDATE, 0);
            lastLocation = createNowLocation(null);
        }
        if(lastLocation==null || (lastLocation!=null && (lastLocation.lat ==0 && lastLocation.lon==0))){
            return null;
        }
        return lastLocation;

    }

    public static void getLocationAsync(LocationListener locListener){
        if(listener==null)listener= new Vector<LocationListener>();
        if(locListener!=null)listener.add(locListener);
        if(service!=null){
            forceLocationUpdate(true);
            handler.postDelayed(updateTimeOutRunnable,10000);
        }else{
            notifyListener(null);
        }



    }


    public static double[] getLocationLatLng(){
        Location x;
        x = getLocation();
        if(x !=null)return new double[]{((double)x.lat)/1000000.0,((double)x.lon)/1000000.0};
        return null;

    }
    public static LatLng getLatLng(Location loc){
        return new LatLng(((double)loc.lat)/1000000.0,((double)loc.lon)/1000000.0);
    }
    public static boolean isUseFallBackLocation() {
        return useFallBackLocation;
    }

    public static void setUseFallBackLocation(boolean useFallBackLocation) {
        PTNMELocationManager.useFallBackLocation = useFallBackLocation;
    }

    public static void registerListener(LocationListener x){
        if (listener == null) listener = new Vector<LocationListener>();
        listener.add(x);
    }

    public static void unregisterListener(LocationListener x){
        if (listener != null){
            listener.remove(x);
        }
    }


    public static double computeDistance(Location loc1, Location loc2){
        return computeDistance(((double)(loc1.lat))/1000000,((double)(loc1.lon))/1000000, ((double)(loc2.lat))/1000000,((double)(loc2.lon))/1000000);
    }

    public static double computeDistance(Location loc1, double lat2, double lon2){
        return computeDistance(((double)(loc1.lat))/1000000,((double)(loc1.lon))/1000000, lat2,lon2);
    }

    public static double computeDistance(double lat1,double lon1, double lat2, double lon2){
        return LocationUtils.computeDistance(lat1, lon1, lat2, lon2);
    }

    public static double computeDistance(android.location.Location loc1,android.location.Location loc2){
        return computeDistance(loc1.getLatitude(), loc1.getLongitude(), loc2.getLatitude(), loc2.getLongitude());
    }

    public static long getLastLocationReceivedAt() {
        return lastLocationReceivedAt;
    }

    public static void setLastLocationReceivedAt(long lastLocationReceivedAt) {
        PTNMELocationManager.lastLocationReceivedAt = lastLocationReceivedAt;
    }

    public static void notifyListener(Location loc){
        handler.removeCallbacksAndMessages(null);
        if(loc!=null){
            lastLocationReceivedAt = new Date().getTime();
            lastLocation = createNowLocation(loc);
            if(listener !=null){
                for (LocationListener x: listener){
                    x.onNewLocationFound(lastLocation);
                }
            }
        }else{
            if(listener !=null){
                for (LocationListener x: listener){
                    x.onLocationError("Timeout");
                }
            }
        }

    }
    public static interface LocationListener{
        public void onNewLocationFound(Location x);
        public void onLocationError(String cause);
    }
    private static Location createNowLocation(Location loc){
        if(loc ==null){
            try{
                return Location.coord(prefs.getInt(LocationReceiver.LAST_LAT, 0), prefs.getInt(LocationReceiver.LAST_LON, 0));
            }catch(RuntimeException e){
                return null;
            }

        }
        return loc;

    }
}
