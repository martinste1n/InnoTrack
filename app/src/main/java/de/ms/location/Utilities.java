package de.ms.location;



import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import de.ms.location.locationtools.LocationService;
import de.ms.location.locationtools.PTNMELocationManager;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Leg;
import de.schildbach.pte.dto.Trip.Public;
import wineme.de.innotrack.storage.LocDataAdapter;

public class Utilities{
    public static final int FONT_LIGHT =0;
    public static final int FONT_REGULAR =1;
    public static final int FONT_BOLD =2;
    public static final int FONT_ICONS =3;

	private static Activity imageContext;
	private static Context context;
    private static Activity parentActivity;

    private static LocDataAdapter lda;
	private static boolean dbOpen = false;
    public static Typeface light ;
    public static Typeface bold ;
    public static Typeface regular ;
    public static Typeface icons ;
    public static int PlayServiceReqCode = 252;

  private LocationService mLocationService;
	

	

	public static String formatTime(int minutes){
		String minuteLbl="";
		if(minutes<10){
			minuteLbl="0"+minutes;
		}else{
			minuteLbl=""+minutes;
		}
		return minuteLbl;
	}
	public static int convertToPx(Activity con, int dp) {
	    // Get the screen's density scale
	    final float scale = con.getResources().getDisplayMetrics().density;
	    // Convert the dps to pixels, based on density scale
	    return (int) (dp * scale + 0.5f);
	}



	public static int[] getDisplaySize(Activity context){
		Display display = context.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		return new int[]{width,height};
	}
	public static Context getContext() {
		return context;
	}
	public static void setContext(Context context) {
		Utilities.context = context;
	}

    public static Typeface getTypeFace(int type){

        switch(type){
        case FONT_BOLD: return bold;
        case FONT_ICONS: return icons;
        case FONT_LIGHT: return light;
        case FONT_REGULAR: return regular;
        default: return null;
        }

    }

    public static LocDataAdapter getDataAdapter(){
        return openDBConnection();
    }

    public static void showMessage(String text){
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void onReceive(Context context, Intent intent) {
		
	}
	public static void closeDBConnection(){
		if(lda!= null ){
			lda.close();
			dbOpen = false;
		}
	}
	public static LocDataAdapter openDBConnection(){
		if(lda== null ){
		lda = new LocDataAdapter(context);
		}
		lda.open();
		dbOpen= true;
		return lda;
	}





	public static String printTripLog(Trip x){
		String res = "";
		for(Leg y :x.legs){
			
			if(y instanceof Public){
				Public pub = ((Public)y);
				res+="\n"+ printTime(y.getDepartureTime())+"| "+pub.line.label+ " | " + y.departure.name;
				List<Stop> inters = pub.intermediateStops;
				if(inters !=null){
					for(Stop intermediate : inters){
						res+="\n\t-"+ printTime(intermediate.plannedArrivalTime)+" "+ intermediate.location.name;
					}	
				}
				
			}else{
				res+="\n"+ printTime(y.getDepartureTime())+"| "+ y.arrival.name;
			}
		}
		return res;
	}
	public static String printTime(Date date){
		return ""+formatTime(date.getHours())+ ":" + formatTime(date.getMinutes());
	}


    public static boolean isDbOpen() {
		return dbOpen;
	}
	public static void setDbOpen(boolean dbOpen) {
		Utilities.dbOpen = dbOpen;
	}



    public static int getNavBarDimen(String resourceString) {
        Resources r = context.getResources();
        int id = r.getIdentifier(resourceString, "dimen", "android");
        if (id > 0) {
            return r.getDimensionPixelSize(id);
        } else {
            return 0;
        }
    }
    public static void initFontFaces(){
        light =Typeface.createFromAsset(context.getAssets(), "fonts/roboto_light.ttf");
        regular =Typeface.createFromAsset(context.getAssets(), "fonts/roboto_regular.ttf");
        bold =Typeface.createFromAsset(context.getAssets(), "fonts/roboto_bold.ttf");
        icons =Typeface.createFromAsset(context.getAssets(), "fonts/icons_regular.ttf");
    }
    public static Activity getParentActivity() {
        return parentActivity;
    }

    public static void setParentActivity(Activity parentActivity) {
        Utilities.parentActivity = parentActivity;
    }

    public static void checkAndInitLocationService() {
        if(!isMyServiceRunning(LocationService.class)){
            int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
            if(result == ConnectionResult.SUCCESS){
                Log.d("PTEnabler", "Play Services up to date! Starting service");
                PTNMELocationManager.startService();
            }else{
                Log.e("PTEnabler", "Play Services error!" + GoogleApiAvailability.getInstance().getErrorString(result));
                PTNMELocationManager.LocationServiceStatusCode = result;
            }
        }

    }
    public static String getDataFromUrl(String url) {
        String res = "";
        InputStream is = null;

        // Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(httpget);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            res = sb.toString();
            return res;
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
            return null;
        }
    }
    private static boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

