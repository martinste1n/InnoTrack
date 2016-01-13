package de.ms.location.locationtools;

import java.util.Vector;

/**
 * Created by Martin on 20.04.2015.
 */
public interface ProbLocationCalculationListener {
public void onCalculationCompleted(Vector<ClusteredLocation> results);
}
