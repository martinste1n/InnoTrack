package de.ms.location.locationtools;

import java.util.Comparator;

import java.util.List;

import de.ms.location.Utilities;
import de.schildbach.pte.LocationUtils;
import de.schildbach.pte.dto.Location;

public class LocationComparator implements  Comparator<Location>{
	double lat;
	double lng;
	public LocationComparator(double lat, double lng){
		this.lat = lat;
		this.lng = lng;
	}
	
	public int compare(Location lhs, Location rhs) {

		double distance = LocationUtils.computeDistance(lat, lng, ((double)lhs.lat/100000.0), ((double)lhs.lon/1000000.0)) - LocationUtils.computeDistance(lat, lng, ((double)rhs.lat/1000000.0), ((double)rhs.lon/100000.0));
		if(distance >0){
			return -1;
		}else{
			return 1;
		}
	}
	

}
