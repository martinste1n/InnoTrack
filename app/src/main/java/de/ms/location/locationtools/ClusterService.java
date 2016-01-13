package de.ms.location.locationtools;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.javaml.core.Dataset;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;


import de.ms.location.Utilities;
import de.schildbach.pte.dto.Location;

public class ClusterService extends IntentService {

	public final String LAST_CLUSTERED = "LAST_CLUSTERED";
	private double mintransitionprob = 0.05;
    private int timeinFuture= 30;
    private SharedPreferences prefs;
	private SharedPreferences.Editor editor;
	public ClusterService() {
		super("TMClusteringService");
		
		
	}

	
	protected void onHandleIntent(Intent intent) {
		prefs= PreferenceManager.getDefaultSharedPreferences(this);
		long now = new Date().getTime();
		
			
			Utilities.openDBConnection().clearLocationHistory(14);
			Date start = new Date((new Date().getTime()-(1000l*3600l*24l)));
			//Date start = new Date(0);
			Map<Location, Dataset> clusteredLocs = ClusterManagement.clusterLocations(start, new Date(), 1000);
            //Map<Location, Dataset> clusteredLocs = Utilities.clusterLocations(new Date(0) , new Date(), 10000);
			Log.d("InnoTrack","Done with yesterday! "+ clusteredLocs.size()+ "Locations calculated" );
            ClusterManagement.addNewClusteredLocations(clusteredLocs, ClusterMetaData.ClusterType.DAILY_CLUSTER);
			if(prefs.getBoolean("INCLUDE_NOISE", false)){
                Log.d("InnoTrack","Checking remaining noise! ");
                clusteredLocs.clear();
                clusteredLocs.putAll(ClusterManagement.clusterLocations(new Date(0), new Date(), Integer.MAX_VALUE, true));
                ClusterManagement.addNewClusteredLocations(clusteredLocs, ClusterMetaData.ClusterType.NOISE_CLUSTER);
            }

			editor = prefs.edit();
			editor.putLong(LAST_CLUSTERED, now);
			editor.commit();
			Log.d("InnoTrack","Done! "+ clusteredLocs.size()+ "Locations calculated in total" );

			ClusterManagement.clearClusters();
			List<ClusteredLocation> clocs = ClusterManagement.getClusteredLocationsFromCache(true);
			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		    if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
			    
		    	for (ClusteredLocation caller:clocs){
			    	if(caller.getLoc().place==null){

                        Log.d("InnoTrack","Reverse Geocoding Cluster "+ caller.getId());
                        double shortenX = (double)caller.getLoc().lat / 1000000;
                        double shortenY = (double)caller.getLoc().lon / 1000000;
			    		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        String place = "";
                        try {
                            List<Address> res = geocoder.getFromLocation(shortenX,shortenY,1);
                            if(res!=null && res.size()>0){
                                Address address = res.get(0);

                                for(int i=0; i<address.getMaxAddressLineIndex(); i++){
                                    place +=address.getAddressLine(i);
                                    if(i!=address.getMaxAddressLineIndex()-1)place+=", ";
                                }
                                Log.d("InnoTrack","Address: "+ place);

                            }
                        } catch (IOException e) {
                            place=null;
                            e.printStackTrace();
                        }
                        Location loc = new Location(caller.getLoc().type, caller.getLoc().id, caller.getLoc().lat, caller.getLoc().lon, place, place);
                        caller.setLoc(loc);
                    }
                    caller.getMeta().nextCIDs = caller.getProableNextCID(mintransitionprob,timeinFuture);
			    						
			    }
		    	ClusterManagement.addNewClusteredLocations(clocs);



			}
			
		
	
		
	}

}
