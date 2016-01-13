package de.ms.location.locationtools.trajectory;

import com.google.gson.Gson;

import java.util.Date;
import java.util.TreeSet;

import de.ms.location.locationtools.ClusteredLocation;
import de.ms.location.locationtools.UserLocation;

/**
 * Created by Martin on 30.10.2015.
 */
public class TrajectoryNode implements TrajectoryElement, Comparable<TrajectoryElement>{
    public ClusteredLocation nodeLocation;
    public long start_time;
    public long end_time;
    public TreeSet<UserLocation> locations;
    public TrajectoryNode(ClusteredLocation loc, long start, long end){
        this.nodeLocation = loc;
        this.start_time = start;
        this.end_time = end;
        this.locations = new TreeSet<UserLocation>();
    }
    public TrajectoryNode(ClusteredLocation loc){
        this.nodeLocation = loc;
        this.start_time = new Date().getTime();
        this.end_time = 0l;
        this.locations = new TreeSet<UserLocation>();
    }

    public void addLocationSample(UserLocation loc){
        this.start_time = Math.min(this.start_time, loc.getDate());
        this.end_time = Math.max(this.end_time, loc.getDate());
        locations.add(loc);
    }
    @Override
    public int compareTo(TrajectoryElement another) {
        long res = this.start_time-another.getStart();
        if(res==0) return 0;
        return (res<0) ? -1:1;
    }

    @Override
    public long getStart() {
        return start_time;
    }

    @Override
    public long getEnd() {
        return end_time;
    }

    @Override
    public long getDuration() {
        return getEnd()-getStart();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof TrajectoryNode){
           return this.compareTo((TrajectoryElement)o) ==0;
        }
        return false;
    }

    @Override
    public String toString() {
        return ""+getStart()+"\t"+ new Gson().toJson(nodeLocation);
    }

    public String toReadableString(){
        return ""+new Date(getStart()).toLocaleString()+": " + nodeLocation.getLoc().place;
    }
}
