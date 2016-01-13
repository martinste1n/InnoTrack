package de.ms.location.locationtools.trajectory;

/**
 * Created by Martin on 30.10.2015.
 */
public interface TrajectoryElement{
    public long getStart();
    public long getEnd();
    public long getDuration();
    public String toReadableString();
}
