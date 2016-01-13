package de.ms.location.locationtools;



import java.util.Date;
import java.util.List;

import de.ms.location.Utilities;

/**
 * Created by Martin on 12.07.2015.
 */
public class ClusterReport{
    public Date begins;
    public Date ends;
    public List<ClusteredLocation> clusters;
    public List<UserLocation> rawLocations;
    public int clusterCount;
    public int rawLocationCount;

    private ClusterReport(boolean includeRawLocations){
        clusters= ClusterManagement.getClusteredLocationsFromCache(true);
        rawLocations = Utilities.openDBConnection().getAllHistoryLocs(0, new Date().getTime());
        begins = new Date(rawLocations.get(rawLocations.size()-1).getDate());
        ends = new Date(rawLocations.get(0).getDate());
        clusterCount = clusters.size();
        rawLocationCount = rawLocations.size();
        if(!includeRawLocations)rawLocations.clear();
    }
    public static ClusterReport generateReport(){
        return new ClusterReport(false);
    }

    public static ClusterReport generateReport(boolean include){
        return new ClusterReport(include);
    }
}

