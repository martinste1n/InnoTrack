package de.ms.location.locationtools.trajectory;

import android.util.Log;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import de.ms.location.locationtools.PTNMELocationManager;
import de.ms.location.locationtools.UserLocation;
import de.schildbach.pte.dto.Location;

/**
 * Created by Martin on 30.10.2015.
 */
public class Trajectory {
    public long enter_time;
    public long exit_time;
    public TreeSet<TrajectoryElement> elements;
    public Trajectory(){
        this.enter_time=new Date().getTime();
        this.exit_time = 0l;
        elements = new TreeSet<TrajectoryElement>();
    }
    public void addElement(TrajectoryElement element){
        this.enter_time = Math.min(this.enter_time, element.getStart());
        this.exit_time = Math.max(this.exit_time, element.getEnd());
        elements.add(element);
    }
    public void addMultipleElements(Collection<TrajectoryElement> toAdd){
        for(TrajectoryElement element : toAdd){
            addElement(element);
        }
    }

    /**
     *
     * @param timeThresholdInMillis the duration a path at least needs to last in order to be valid
     * @return A cleaned Trajectory, which is created by copying all elements of the current Trajectory and filter paths based on the provided criteria and internal reasoning.
     */
    public Trajectory cleanTrajectory(long timeThresholdInMillis){
        Trajectory result = new Trajectory();
        Iterator<TrajectoryElement> it = this.elements.iterator();
        TrajectoryElement [] array = mergeElements(this.elements.toArray(new TrajectoryElement[this.elements.size()]), timeThresholdInMillis);

        /*
        while (it.hasNext()){
        TrajectoryElement first = it.next();
            if(first instanceof TrajectoryNode && it.hasNext()){
                    TrajectoryNode node = (TrajectoryNode)first;
                    TrajectoryElement second = it.next();
                    if (second instanceof TrajectoryPath && it.hasNext()) {
                        TrajectoryPath path = (TrajectoryPath)second;
                        TrajectoryElement third = it.next();
                        if(third instanceof TrajectoryNode && ((TrajectoryNode)third).nodeLocation.equals(node.nodeLocation)){
                            if(path.getDuration()<timeThresholdInMillis){
                                for(UserLocation loc: path.locations){
                                    node.addLocationSample(loc);
                                }
                                result.addElement(node);
                            }else{
                                Log.d("PTEnabler", "Start: " + node.nodeLocation.getId() + "\t End:" + ((TrajectoryNode)third).nodeLocation.getId());
                                if(testForRealPath(new UserLocation(node.nodeLocation.getLoc(),node.getEnd(),node.nodeLocation.getId()),path.locations)>0.8){
                                    result.addElement(node);
                                    result.addElement(path);
                                    result.addElement(third);
                                }
                            }
                        }else{
                            Log.d("PTEnabler", "Start: " + node.nodeLocation.getId() + "\t End:" + ((TrajectoryNode)third).nodeLocation.getId());
                            testForRealPath(new UserLocation(node.nodeLocation.getLoc(), node.getEnd(), node.nodeLocation.getId()), path.locations);
                            result.addElement(node);
                            result.addElement(path);
                            result.addElement(third);

                        }
                    }else{
                        result.addElement(node);
                        result.addElement(second);
                    }
            }else{
                result.addElement(first);
            }
        }
        // Merge duplicates
        it = result.elements.iterator();
        while(it.hasNext()) {
            TrajectoryElement first = it.next();
            // Two paths in a row
            if (first instanceof TrajectoryPath && it.hasNext()) {
                TrajectoryPath path = (TrajectoryPath) first;
                TrajectoryElement second = it.next();
                if (second instanceof TrajectoryPath) {
                    path.addMultipleElements(((TrajectoryPath) second).locations);
                    it.remove();
                }
            }
            //Same node twice in row
            if(first instanceof TrajectoryNode && it.hasNext()){
                TrajectoryNode node= (TrajectoryNode) first;
                TrajectoryElement second = it.next();
                if(second instanceof TrajectoryNode && ((TrajectoryNode)second).nodeLocation.equals(node.nodeLocation)){
                    for(UserLocation sample :((TrajectoryNode) second).locations){
                        node.addLocationSample(sample);
                    }
                    it.remove();
                }
            }
        }
        */
        for(TrajectoryElement arrayMent: array){
            result.addElement(arrayMent);
        }
        return result;
    }

    /**
     *
     * @param start Center of cluster or start of path to be used as benchmark
     * @param path List containing all Locations that describe the path
     * @param threshold min duration of the path
     * @return The return value is the quotient of the sum of absolute distances from each point of the path divided by the length of the path.
     * All values lower than 1 indicate that the path is circling around the start location, which suggests GPS errors
     */
    private double testForRealPath(UserLocation start, List<UserLocation> path, long threshold){
        Location root =null;
        double distanceSum =0;
        double rootDistanceSum = 0;
        double vstart =0;
        double vmean= 0;
        for(int i = 0; i<path.size(); i++){
            if(i==0){
                root= path.get(0).getLoc();
                distanceSum+= PTNMELocationManager.computeDistance(root,path.get(0).getLoc());
            }else{
                distanceSum+= PTNMELocationManager.computeDistance(path.get(i-1).getLoc(),path.get(i).getLoc());
            }
            rootDistanceSum+=PTNMELocationManager.computeDistance(root,path.get(i).getLoc());
        }
        vstart = PTNMELocationManager.computeDistance(start.getLoc(),root)/((path.get(0).getDate()-start.getDate())/1000l);
        vmean = distanceSum/(((path.get(path.size()-1).getDate()-path.get(0).getDate()))/1000l);
        Log.d("PtEnabler", "VStart: " + vstart + "\t VMean: "+ vmean + "\t RootDistance: "+ rootDistanceSum +" \t DistanceSum: " + distanceSum );
        //return rootDistanceSum/distanceSum;
        if(distanceSum>250 || (path.get(0).getDate()-path.get(path.size()-1).getDate()<threshold)){
            return 0;
        }else{
            if(vmean>10 || rootDistanceSum>distanceSum){
                return 1;
            }
            return 1/(vstart/vmean);
        }

    }
    private TrajectoryElement[] mergeElements(TrajectoryElement[] array, long threshold){
        Vector<TrajectoryElement> vector = new Vector<TrajectoryElement>();
        for(TrajectoryElement element :array){
            vector.add(element);
        }
        for(int i=0; i<vector.size()-2; i++){
            TrajectoryElement first = vector.get(i);
            TrajectoryElement second = vector.get(i+1);
            TrajectoryElement third = vector.get(i+2);
            if(first instanceof TrajectoryNode && third instanceof TrajectoryNode && second instanceof TrajectoryPath){
                if(((TrajectoryNode) first).nodeLocation.equals(((TrajectoryNode) third).nodeLocation)){
                    if(testForRealPath(new UserLocation(((TrajectoryNode) first).nodeLocation.getLoc(),
                            first.getStart(),
                            ((TrajectoryNode) first).nodeLocation.getId()),
                            ((TrajectoryPath) second).locations,
                            threshold)>0.8){
                        continue;
                    }else{
                        vector.remove(second);
                        for(UserLocation loc:((TrajectoryNode) third).locations){
                            ((TrajectoryNode) first).addLocationSample(loc);
                        }
                        vector.remove(third);
                        i=0;
                        continue;

                    }
                }
            }
            if(first instanceof TrajectoryNode && second instanceof TrajectoryNode){
                if(((TrajectoryNode) first).nodeLocation.equals(((TrajectoryNode) second).nodeLocation)){
                    for(UserLocation loc:((TrajectoryNode) second).locations){
                        ((TrajectoryNode) first).addLocationSample(loc);
                    }
                    vector.remove(second);
                    i=0;
                }
            }
            if(first instanceof TrajectoryPath && second instanceof TrajectoryPath){
                ((TrajectoryPath) first).addMultipleElements(((TrajectoryPath) second).locations);
                vector.remove(second);
                i=0;
            }
        }
        return vector.toArray(new TrajectoryElement[vector.size()]);
    }


}
