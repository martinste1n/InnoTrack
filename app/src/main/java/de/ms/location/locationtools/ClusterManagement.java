package de.ms.location.locationtools;



import android.util.Log;

import com.google.gson.Gson;

import net.sf.javaml.clustering.DensityBasedSpatialClustering;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.ms.location.Utilities;
import de.ms.location.locationtools.trajectory.Trajectory;
import de.ms.location.locationtools.trajectory.TrajectoryElement;
import de.ms.location.locationtools.trajectory.TrajectoryNode;
import de.ms.location.locationtools.trajectory.TrajectoryPath;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;

/**
 * Created by Martin on 05.03.2015.
 */
public class ClusterManagement {
    private static double minLocsFactor = 0.1;
    private static int distance4Clustering = 25;
    private static Dataset[] resultsCluster;
    private static HashMap<Long, HashMap<Long, Double>> probresult = null;

    public static HashMap<Location, Dataset> clusterLocations(Date since, Date until, Integer max, boolean onlyUnClustered) {
        HashMap<Location, Dataset> clusterCenters = new HashMap<Location, Dataset>();

        Utilities.openDBConnection();

        Dataset data = new DefaultDataset();
        Log.d("PTEnabler", "Initializing Locations for clustering\nSince:" + since.toLocaleString() + "\nUntil:" + until.toLocaleString());
        List<UserLocation> locs;
        if (onlyUnClustered) {
            locs = Utilities.openDBConnection().getUnclusteredHistoryLocs(since.getTime(), until.getTime());
        } else {
            locs = Utilities.openDBConnection().getAllHistoryLocs(since.getTime(), until.getTime());
        }

        Log.d("PTEnabler", "Number of Locations available for clustering:" + locs.size());

        if (max != null) {
            int allLocs = locs.size();
            int every = (allLocs > max) ? allLocs / max : 1;
            int i = 0;
            for (UserLocation loc : locs) {
                if ((i++) % every == 0) {
//							double[] x= new double[]{((double)(loc.getLoc().lat))/1000000,((double)(loc.getLoc().lon))/1000000};
//							Instance instance = new DenseInstance(x);
                    data.add(loc);
                } else {
                    continue;
                }
            }
        } else {
            for (UserLocation loc : locs) {
//					double[] x= new double[]{((double)(loc.getLoc().lat))/1000000,((double)(loc.getLoc().lon))/1000000};
//					Instance instance = new DenseInstance(x);
                data.add(loc);
            }
        }

        Log.d("PTEnabler", "" + data.size() + " Locations initialized for clustering");
        int minLocs = Math.max(10, (int) (data.size() * minLocsFactor));
        Log.d("PTEnabler", "" + minLocs + " Locations required for clustering");
        DensityBasedSpatialClustering clusterer = new DensityBasedSpatialClustering(distance4Clustering, minLocs, new MyDistanceMeasure());
        Log.d("PTEnabler", "Starting Clustering");

        resultsCluster = clusterer.cluster(data);


        Log.d("PTEnabler", "Finished Clustering");
        for (Dataset y : resultsCluster) {
            double sumLat = 0.0;
            double sumLon = 0.0;
            Iterator<Instance> it = y.listIterator();
            while (it.hasNext()) {
                Instance i = it.next();
                if (i instanceof UserLocation) {
                    UserLocation uloc = (UserLocation) i;
                    sumLat += ((double) uloc.getLoc().lat) / 1000000.0;
                    sumLon += ((double) uloc.getLoc().lon) / 1000000.0;
                }


            }
            int lat = (int) ((sumLat / (double) y.size()) * 1000000.0);
            int lon = (int) ((sumLon / (double) y.size()) * 1000000.0);
            clusterCenters.put(Location.coord(lat, lon), y);
        }

        return clusterCenters;
    }

    public static HashMap<Location, Dataset> clusterLocations(Date since, Date until, Integer max) {
        return clusterLocations(since, until, max, false);
    }

    public static List<ClusteredLocation> getClusteredLocationsFromCache(boolean includeDeleted) {
        List<ClusteredLocation> locs=Utilities.openDBConnection().getAllClusterLocs();
        if(!includeDeleted){
            Iterator<ClusteredLocation> it = locs.iterator();
            while(it.hasNext()){
                ClusteredLocation loc = it.next();
                if(loc.getMeta().deleted){
                    it.remove();
                }
            }
        }

        return locs;
    }
    public static List<ClusteredLocation> getClusteredLocationWithIDs(Collection<Long> ids){
        List<ClusteredLocation> res = getClusteredLocationsFromCache(true);
        Iterator<ClusteredLocation> it = res.iterator();
        while(it.hasNext()){
            ClusteredLocation cloc = it.next();
            if(!ids.contains(cloc.getId())){
                it.remove();
            }
        }
    return res;
    }

    public static List<ClusteredLocation> getCloseByClusteredLocationsFromCache(double distanceInMeter) {
        List<ClusteredLocation> all = getClusteredLocationsFromCache(false);
        Iterator<ClusteredLocation> it = all.iterator();
        while (it.hasNext()) {
            ClusteredLocation cl = it.next();
            double[] currentLoc = PTNMELocationManager.getLocationLatLng();
            if (currentLoc != null) {
                double distance = PTNMELocationManager.computeDistance(cl.getLoc(), currentLoc[0], currentLoc[1]);
                if (distance > distanceInMeter || distance < distance4Clustering) {
                    it.remove();
                }
            }

        }
        return all;

    }

    public static ClusteredLocation getClosestClusteredLocationsFromCache(boolean includeDeleted) {
        List<ClusteredLocation> all = getClusteredLocationsFromCache(includeDeleted);
        ClusteredLocation res = null;
        double[] locations = PTNMELocationManager.getLocationLatLng();
        if (locations != null) {
            double x = PTNMELocationManager.getLocationLatLng()[0];
            double y = PTNMELocationManager.getLocationLatLng()[1];
            double mindistance = Double.MAX_VALUE;
            Iterator<ClusteredLocation> it = all.iterator();
            while (it.hasNext()) {
                ClusteredLocation cl = it.next();
                double distance = PTNMELocationManager.computeDistance(cl.getLoc(), x, y);
                if (distance < mindistance) {
                    res = cl;
                    mindistance = distance;
                }
            }
            return res;
        } else {
            return null;
        }

    }

    public static void addNewClusteredLocations(Map<Location, Dataset> locs, ClusterMetaData.ClusterType type) {
        boolean wasOpen = Utilities.isDbOpen();

        for (Location loc : locs.keySet()) {
            ClusteredLocation toAdd = null;
            List<ClusteredLocation> oldClusteredLocs = getClusteredLocationsFromCache(true);
            for (ClusteredLocation oldLoc : oldClusteredLocs) {
                double distance = PTNMELocationManager.computeDistance(loc, oldLoc.getLoc());
                if (distance < (2*distance4Clustering)) {
                    toAdd = oldLoc;
                    break;
                }
            }
            if (toAdd == null) {
                Utilities.openDBConnection().saveClusterLocation(loc, locs.get(loc), type);
            } else {
                Log.d("PTEnabler", "Updating Clustered Location: ID " + toAdd.getId() + " Last Clustered: " + new Date(toAdd.getDate()).toLocaleString());
                int lat = (loc.lat + toAdd.getLoc().lat) / 2;
                int lon = (loc.lon + toAdd.getLoc().lon) / 2;
                Location upatedLL;
                if (toAdd.getLoc().place != null) {
                    upatedLL = new Location(LocationType.ADDRESS, toAdd.getLoc().id, lat, lon, toAdd.getLoc().place, toAdd.getLoc().name);
                } else {
                    upatedLL = Location.coord(lat, lon);
                }

                toAdd.setDate(new Date().getTime());
                toAdd.setLoc(upatedLL);
                toAdd.setCount(toAdd.getCount() + 1);
                Utilities.openDBConnection().updateClusteredLocation(toAdd, locs.get(loc));

            }
        }


        if (!wasOpen) {
            Utilities.closeDBConnection();
        }
    }

    public static void addNewClusteredLocations(List<ClusteredLocation> clocs) {

        for (ClusteredLocation loc : clocs) {

            Utilities.openDBConnection().updateClusteredLocation(loc, null);

        }
    }

    public static void clearClusters() {
        clearClusters(14);
    }

    public static void clearClusters(int olderThanXdays) {
        Utilities.openDBConnection().clearClusteredLocations(olderThanXdays);
    }

    public static Set<PropableNextLocationResult> getProbableDestinations(long cid, boolean includeCurrent) {
        List<ClusteredLocation> clocations = getClusteredLocationsFromCache(false);
        HashMap<Integer, ClusteredLocation> probLoc = new HashMap<Integer, ClusteredLocation>();
        TreeSet<UserLocation> ulocs = new TreeSet<UserLocation>();
        ulocs.addAll(Utilities.openDBConnection().getAllHistoryLocs(0, new Date().getTime(), false));
        Trajectory[] trajects = getTrajectoriesUntil(Calendar.getInstance(),14,10000*60);
        HashMap<Long, HashMap<Long, Integer>> sums = new HashMap<Long, HashMap<Long, Integer>>();

        for(int i=0; i<trajects.length; i++){
            TreeSet<TrajectoryElement> elements=trajects[i].elements;
            Iterator<TrajectoryElement> it = elements.iterator();
            TrajectoryNode last =null;
            while (it.hasNext()){
                TrajectoryElement toCheck =it.next();
                if(toCheck instanceof TrajectoryPath){
                    continue;
                }
                TrajectoryNode current = (TrajectoryNode)toCheck;
                if(last==null || (last!=null&&current.nodeLocation.getId()==last.nodeLocation.getId())){
                    last = current;
                    continue;
                }
                HashMap<Long, Integer> c1Scores = sums.get(last.nodeLocation.getId());
                if (c1Scores != null) {
                    Integer c1c2score = c1Scores.get(current.nodeLocation.getId());
                    if (c1c2score == null) {
                        c1Scores.put(current.nodeLocation.getId(), 1);
                    } else {
                        c1Scores.put(current.nodeLocation.getId(), (c1c2score.intValue() + 1));
                    }
                } else {
                    c1Scores = new HashMap<Long, Integer>();
                    c1Scores.put(current.nodeLocation.getId(), 1);
                    sums.put(last.nodeLocation.getId(), c1Scores);
                }
                last = current;
            }
        }
        probresult = calculateProbabilityForDestinations(sums);
         /*
        // All IDs of currently available cluster
            Vector<Long> cids = new Vector<Long>();
            for (ClusteredLocation cl : clocations) {
                cids.add(cl.getId());
            }
            HashMap<Long, HashMap<Long, Integer>> sums = new HashMap<Long, HashMap<Long, Integer>>();
            Iterator<UserLocation> it = ulocs.iterator();
            UserLocation lastparentCluster = null;
            int tempCount = 0;
            while (it.hasNext()) {
                UserLocation current = it.next();
                if (cids.contains(current.getParentCluster())||current.getParentCluster()==0) {
                    if ((lastparentCluster!=null&&lastparentCluster.getParentCluster() == current.getParentCluster()) || current.getParentCluster() == 0) {
                        // Iteration durch einen Cluster oder durch noise

                        //tempCount auf 0, um sicherzustellen, dass das vorbeilaufen an einem Cluster nicht gezählt wird.
                        // Auf 0 stellt sicher, das wenn ein Cluster passiert wurde und der Counter hochging jetzt im Noise wieder von 0 angefangen wird
                        tempCount = 0;
                        continue;
                    } else {
                        // Eintritt in neuen Cluster der von altem Abweicht
                        if (lastparentCluster == null) {
                            // Eintritt in ersten Cluster
                            // Setzen und weiter
                            lastparentCluster = current;
                            continue;
                        }


                        if (tempCount < 5) {
                        // Vorbeilaufen an Clustern ausschließen
                        // Vergleichswert heißt, dass 5 Locations im selben Cluster gefunden werden müssen mit jeweils 30 Sekunden differenz
                            tempCount++;
                            continue;
                        }
                        // Clusterwechsel (am Ende von Noise oder direkt)
                        HashMap<Long, Integer> c1Scores = sums.get(lastparentCluster);
                        if (c1Scores != null) {
                            Integer c1c2score = c1Scores.get(current.getParentCluster());
                            if (c1c2score == null) {
                                c1Scores.put(current.getParentCluster(), 1);
                            } else {
                                c1Scores.put(current.getParentCluster(), (c1c2score.intValue() + 1));
                            }
                        } else {
                            c1Scores = new HashMap<Long, Integer>();
                            c1Scores.put(current.getParentCluster(), 1);
                            sums.put(lastparentCluster.getParentCluster(), c1Scores);
                        }

                        tempCount = 0;
                        lastparentCluster = current;
                    }
                }
            }
            */


    //    }
        HashMap<Long, Double> currentCluster = probresult.get(cid);
        if (currentCluster != null && currentCluster.size() != 0) {
            TreeSet<PropableNextLocationResult> results = new TreeSet<PropableNextLocationResult>();
            for (ClusteredLocation cloc : clocations) {
                if (currentCluster.keySet().contains(cloc.getId())) {
                    Location currentGeoPos = PTNMELocationManager.getLocation();
                    if ((currentGeoPos != null && PTNMELocationManager.computeDistance(cloc.getLoc(), currentGeoPos) > distance4Clustering) || includeCurrent)
                        results.add(new PropableNextLocationResult(currentCluster.get(cloc.getId()), cloc));
                }
            }
            return results;
        }

        return null;

    }

    /**
     *
     * @param start The date which will serve as the End Date of the Interval. Typically TODAY.
     * @param daysBefore The amount of days before Start.
     * @param minDurationThreshold The minimal duration of a Path or V
     * @return
     */
    public static Trajectory[] getTrajectoriesUntil(Calendar start, int daysBefore, long minDurationThreshold){
        int daysToTakeIntoAccount = daysBefore;
        Calendar[] dates = new Calendar[daysToTakeIntoAccount];
        for(int i= 0; i< daysToTakeIntoAccount; i++){
            dates[i] = (Calendar) start.clone();
            dates[i].set(Calendar.HOUR_OF_DAY,0);
            dates[i].set(Calendar.MINUTE,0);
            dates[i].set(Calendar.SECOND, 0);
            dates[i].add(Calendar.DATE,-i);
        }
        Trajectory[] trajects = new Trajectory[daysToTakeIntoAccount-1];
        for(int i= 0; i< trajects.length; i++){
            trajects[i] = getTrajectory(dates[trajects.length-i].getTime().getTime(),dates[(trajects.length-1)-i].getTime().getTime()).cleanTrajectory(minDurationThreshold);
        }
        return trajects;
    }

    public static Set<PropableNextLocationResult> getProbableDestinations(long cid) {
        return getProbableDestinations(cid, false);
    }

    public static Set<PropableNextLocationResult> getPropableLocationForDate(Date x, boolean includeCurPosCluster) {
        List<UserLocation> all = Utilities.openDBConnection().getAllHistoryLocs(0, new Date().getTime());
        Calendar toLookFor = Calendar.getInstance();
        toLookFor.setTimeInMillis(x.getTime());
        Calendar loccal = Calendar.getInstance();
        HashMap<Long, Double> props = new HashMap<Long, Double>();
        HashMap<Long, Integer> hourOfDaytoUse = new HashMap<Long, Integer>();
        for (UserLocation loc : all) {
            loccal.setTimeInMillis(loc.getDate());
            if (loc.getParentCluster() != 0
                    && loccal.get(Calendar.HOUR_OF_DAY) == toLookFor.get(Calendar.HOUR_OF_DAY)) {


                if (hourOfDaytoUse.get(loc.getParentCluster()) != null) {
                    if (loccal.get(Calendar.DAY_OF_WEEK) == toLookFor.get(Calendar.DAY_OF_WEEK)) {
                        hourOfDaytoUse.put(loc.getParentCluster(), hourOfDaytoUse.get(loc.getParentCluster()) + 4);
                    } else {
                        hourOfDaytoUse.put(loc.getParentCluster(), hourOfDaytoUse.get(loc.getParentCluster()) + 1);
                    }

                } else {
                    if (loccal.get(Calendar.DAY_OF_WEEK) == toLookFor.get(Calendar.DAY_OF_WEEK)) {
                        hourOfDaytoUse.put(loc.getParentCluster(), 4);
                    } else {
                        hourOfDaytoUse.put(loc.getParentCluster(), 1);
                    }

                }


            }
        }
        double sum = 0;
        for (int count : hourOfDaytoUse.values()) {
            sum += count;
        }

        for (long cid : hourOfDaytoUse.keySet()) {
            props.put(cid, ((double) hourOfDaytoUse.get(cid)) / sum);
        }
        TreeSet<PropableNextLocationResult> res = new TreeSet<PropableNextLocationResult>();
        for (ClusteredLocation cloc : getClusteredLocationsFromCache(false)) {
            if (props.keySet().contains(cloc.getId())) {
                double[] currentLoc = PTNMELocationManager.getLocationLatLng();
                double dist = Double.MAX_VALUE;
                if (currentLoc != null) {
                    dist = PTNMELocationManager.computeDistance(cloc.getLoc(), currentLoc[0], currentLoc[1]);
                }

                if (dist > distance4Clustering || includeCurPosCluster) {
                    res.add(new PropableNextLocationResult(props.get(cloc.getId()), cloc));
                }
            }
        }

        return res;
    }

    private static HashMap<Long, HashMap<Long, Double>> calculateProbabilityForDestinations(HashMap<Long, HashMap<Long, Integer>> inputs) {
        HashMap<Long, HashMap<Long, Double>> res = new HashMap<Long, HashMap<Long, Double>>();
        for (long c1 : inputs.keySet()) {
            HashMap<Long, Integer> c1scores = inputs.get(c1);
            double sum = 0;
            for (int count : c1scores.values()) {
                sum += count;
            }
            for (long c1c2 : c1scores.keySet()) {
                HashMap<Long, Double> c1c2prob = res.get(c1);
                if (c1c2prob == null) c1c2prob = new HashMap<Long, Double>();
                if (sum != 0) c1c2prob.put(c1c2, ((double) c1scores.get(c1c2)) / sum);
                res.put(c1, c1c2prob);
            }
        }

        return res;


    }

    public static String exportClusters(){
        Gson gson = new Gson();
        ClusterReport report = ClusterReport.generateReport(false);
        String test = gson.toJson(report);
        return test;

    }

    public static Trajectory getTrajectory(long start, long end){
        Trajectory result = new Trajectory();
        List<ClusteredLocation> clocations = getClusteredLocationsFromCache(false);
        HashMap<Integer, ClusteredLocation> probLoc = new HashMap<Integer, ClusteredLocation>();
        TreeSet<UserLocation> ulocs = new TreeSet<UserLocation>();
        ulocs.addAll(Utilities.openDBConnection().getAllHistoryLocs(start, end, false));

        HashMap<Long, HashMap<Long, Integer>> sums = new HashMap<Long, HashMap<Long, Integer>>();
        Iterator<UserLocation> it = ulocs.iterator();
        UserLocation previousLocation = null;
        int tempCount = 0;
        TrajectoryPath path = null;
        TrajectoryNode node=null;
        while (it.hasNext()) {
            UserLocation current = it.next();
            if (previousLocation != null) {
                // Sind nicht mehr bei der ersten Location
                if (previousLocation.getParentCluster() != current.getParentCluster()) {
                    // Wechsel von Cluster auf Weg oder andersherum oder von Cluster zu anderem Cluster
                    if (previousLocation.getParentCluster() > 0) {
                        //Wechsel von Cluster->?: Alten Cluster als Node hinzufügen
                        result.addElement(node);
                        if (current.getParentCluster() > 0) {
                            //Nachfolger von Cluster ist auch Cluster: node neu initialisieren
                            ClusteredLocation cl = getClusteredLocation(current.getParentCluster());
                            if (cl == null || cl.getMeta().deleted) {
                                continue;
                            }
                            // Parent cluster existiert und ist nicht gelöscht
                                node = new TrajectoryNode(cl);
                                node.addLocationSample(current);

                        } else {
                            //Nachfolger ist Path: Patch initialsieren
                            path = new TrajectoryPath();
                            path.addElement(current);
                        }

                    } else {
                        //Wechsel von Path in Cluster: Path als Element hinzugügen
                        ClusteredLocation cl = getClusteredLocation(current.getParentCluster());
                        if (cl == null || cl.getMeta().deleted) {
                            continue;
                        }
                        result.addElement(path);
                        // Parent cluster existiert und ist nicht gelöscht
                        node = new TrajectoryNode(cl);
                        node.addLocationSample(current);
                    }
                } else {
                    // Bleiben in aktuellen Cluster oder in Path
                    if (previousLocation.getParentCluster() > 0) {
                        //Bleiben in Cluster: Sample hinzufügen
                        if(node==null){
                            Log.d("PTEnabler", "Adding Sample for Cluster " + current.getParentCluster());
                        }
                        node.addLocationSample(current);
                    } else {
                        //Bleiben auf Path: Sample zu Strecke hinzufügen
                        path.addElement(current);
                    }
                }

            } else {
                //Erste Location
                if(current.getParentCluster()>0){
                    //Erste Location gehört zu Cluster
                    ClusteredLocation cl = getClusteredLocation(current.getParentCluster());
                    if (cl == null || cl.getMeta().deleted) {
                        continue;

                    }
                    // Parent cluster existiert
                    node = new TrajectoryNode(cl);
                    node.addLocationSample(current);
                }else{
                    path = new TrajectoryPath();
                    path.addElement(current);
                }
            }


        previousLocation = current;
        }

        return result;
    }
    public static ClusteredLocation getClusteredLocation(long id){
       for(ClusteredLocation cl: getClusteredLocationsFromCache(true)){
           if(cl.getId() == id){
               return cl;
           }
       }
    return null;
    }
}
