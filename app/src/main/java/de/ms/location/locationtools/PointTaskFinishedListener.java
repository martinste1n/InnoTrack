package de.ms.location.locationtools;

import de.ms.location.poi.Venue;

public interface PointTaskFinishedListener {
	
	public void onVenueTaskFinished(Venue x);
	public void onLocationTaskFinished(ClusteredLocation x);
}
