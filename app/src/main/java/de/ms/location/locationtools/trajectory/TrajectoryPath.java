package de.ms.location.locationtools.trajectory;

import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import de.ms.location.locationtools.UserLocation;

/**
 * Created by Martin on 30.10.2015.
 */
public class TrajectoryPath implements TrajectoryElement, Comparable<TrajectoryElement>{
    public long start_time;
    public long end_time;
    public Vector<UserLocation> locations;

    public TrajectoryPath(){
        start_time = new Date().getTime();
        end_time = 0l;
        locations = new Vector<UserLocation>();
    }

    @Override
    public int compareTo(TrajectoryElement another) {
        long res = this.start_time-another.getStart();
        if(res==0) return 0;
        return (res<0) ? -1:1;
    }


    @Override
    public boolean equals(Object o) {
        if(o instanceof TrajectoryElement){
            return this.getStart() == ((TrajectoryElement)o).getStart();
        }
        return false;
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

    public void addElement(UserLocation element){
        this.start_time = Math.min(this.start_time, element.getDate());
        this.end_time = Math.max(this.end_time, element.getDate());
        locations.add(element);
    }
    public void addMultipleElements(Collection<UserLocation> toAdd){
        for(UserLocation element : toAdd){
            addElement(element);
        }
    }
    public String toReadableString(){
        return ""+new Date(getStart()).toLocaleString()+": " +(end_time-start_time)/(1000l*60l) + "Minuten";
    }
}
