package wineme.de.innotrack.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;



import de.ms.location.locationtools.ClusterMetaData;
import de.ms.location.locationtools.ClusteredLocation;
import de.ms.location.locationtools.UserLocation;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;

public class LocDataAdapter {
	// Database fields
	private SQLiteDatabase database;
	private SQLStorage dbHelper;
	private String[] allColumns = { 		SQLStorage.COLUMN_ID,
											SQLStorage.COLUMN_LAT, 
											SQLStorage.COLUMN_LNG, 
											SQLStorage.COLUMN_NAME, 
											SQLStorage.COLUMN_PLACE,
											SQLStorage.COLUMN_TYPE,
											SQLStorage.COLUMN_TIMESTAMP };
	
	private String[] allColumns_with_iid = {SQLStorage.COLUMN_ID,
											SQLStorage.COLUMN_LAT, 
											SQLStorage.COLUMN_LNG, 
											SQLStorage.COLUMN_NAME, 
											SQLStorage.COLUMN_PLACE,
											SQLStorage.COLUMN_TYPE,
											SQLStorage.COLUMN_TIMESTAMP, 
											SQLStorage.COLUMN_INTERNALID };
	
	private String[] allColumns_cluster = { SQLStorage.COLUMN_ID,
											SQLStorage.COLUMN_LAT, 
											SQLStorage.COLUMN_LNG, 
											SQLStorage.COLUMN_NAME, 
											SQLStorage.COLUMN_PLACE,
											SQLStorage.COLUMN_TYPE,
											SQLStorage.COLUMN_TIMESTAMP, 
											SQLStorage.COLUMN_INTERNALID,
											SQLStorage.COLUMN_FIRSTSEEN,
											SQLStorage.COLUMN_COUNT };
	
	private String[] allColumns_locHist = { SQLStorage.COLUMN_ID,
											SQLStorage.COLUMN_LAT, 
											SQLStorage.COLUMN_LNG, 
											SQLStorage.COLUMN_NAME, 
											SQLStorage.COLUMN_PLACE,
											SQLStorage.COLUMN_TYPE,
											SQLStorage.COLUMN_TIMESTAMP, 
											SQLStorage.COLUMN_INTERNALID,
											SQLStorage.COLUMN_CLUSTER_INTERNALID
											};

    private String[] allColumns_Meta = {    SQLStorage.COLUMN_ID,
                                            SQLStorage.COLUMN_TYPE,
                                            SQLStorage.COLUMN_DELETED,
                                            SQLStorage.COLUMN_STICKY,
                                            SQLStorage.COLUMN_META_EXTRA

    };

    public LocDataAdapter(Context context) {
		dbHelper = new SQLStorage(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}



	public Location save2LocationHistory(Location loc,long timeStamp) {

		ContentValues values = new ContentValues();
		values.put(SQLStorage.COLUMN_ID, loc.id);
		values.put(SQLStorage.COLUMN_LAT, loc.lat);
		values.put(SQLStorage.COLUMN_LNG, loc.lon);
		values.put(SQLStorage.COLUMN_NAME, loc.name);
		values.put(SQLStorage.COLUMN_PLACE, loc.place);
		values.put(SQLStorage.COLUMN_TYPE, loc.type.toString());
		values.put(SQLStorage.COLUMN_TIMESTAMP, timeStamp);
		values.put(SQLStorage.COLUMN_CLUSTER_INTERNALID, 0);
		database.insert(SQLStorage.TABLE_LOCATION_HISTORY, null,
                values);
		Cursor cursor = database.query(SQLStorage.TABLE_LOCATION_HISTORY,
				allColumns_locHist, SQLStorage.COLUMN_TIMESTAMP+ "=" +timeStamp, null,
				null, null, null);
		cursor.moveToFirst();
		Location newLoc = cursorToUserLoc(cursor).getLoc();
		cursor.close();
		Log.d("PtEnabler", "Location saved to Location History: " +newLoc.lat+"//"+newLoc.lon);
		return newLoc;
	}
  public Location save2LocationHistory(Location loc ) {
    return save2LocationHistory(loc, new Date().getTime());
  }

	public ClusteredLocation saveClusterLocation(Location loc, ClusterMetaData.ClusterType type) {
		ContentValues values = new ContentValues();
		values.put(SQLStorage.COLUMN_ID, loc.id);
		values.put(SQLStorage.COLUMN_LAT, loc.lat);
		values.put(SQLStorage.COLUMN_LNG, loc.lon);
		values.put(SQLStorage.COLUMN_NAME, loc.name);
		values.put(SQLStorage.COLUMN_PLACE, loc.place);
		values.put(SQLStorage.COLUMN_TYPE, new ClusterMetaData(type).toString());
		values.put(SQLStorage.COLUMN_TIMESTAMP, new Date().getTime());
		values.put(SQLStorage.COLUMN_FIRSTSEEN, new Date().getTime());
		values.put(SQLStorage.COLUMN_COUNT, 1);
		
		long iid = database.insert(SQLStorage.TABLE_LOCATION_CLUSTER, null,
				values);
		Cursor cursor = database.query(SQLStorage.TABLE_LOCATION_CLUSTER,
				allColumns_cluster, SQLStorage.COLUMN_INTERNALID +" = "+iid, null,
				null, null, null);
		cursor.moveToFirst();
		ClusteredLocation newLoc = cursorToClusterLoc(cursor);
		cursor.close();
		Log.d("PtEnabler", "Location "+iid+"("+newLoc.getId()+")"+" saved to clustered Locations : " +newLoc.getLoc().lat+"//"+newLoc.getLoc().lon);
		return newLoc;
	}
	public ClusteredLocation saveClusterLocation(Location loc, Dataset ds, ClusterMetaData.ClusterType type) {
		ClusteredLocation cl =saveClusterLocation(loc, type);
		setClusterIDOfLocations(ds, cl.getId());
		return cl;
	}
	
	public ClusteredLocation updateClusteredLocation(ClusteredLocation updatedLoc){
		ContentValues values = new ContentValues();
        String metainfo = updatedLoc.getMeta().toString();
		values.put(SQLStorage.COLUMN_ID, updatedLoc.getLoc().id);
		values.put(SQLStorage.COLUMN_LAT, updatedLoc.getLoc().lat);
		values.put(SQLStorage.COLUMN_LNG, updatedLoc.getLoc().lon);
		values.put(SQLStorage.COLUMN_NAME, updatedLoc.getLoc().name);
		values.put(SQLStorage.COLUMN_PLACE, updatedLoc.getLoc().place);
		values.put(SQLStorage.COLUMN_TYPE,metainfo );
		values.put(SQLStorage.COLUMN_TIMESTAMP, updatedLoc.getDate());
		values.put(SQLStorage.COLUMN_COUNT, updatedLoc.getCount());
		values.put(SQLStorage.COLUMN_FIRSTSEEN, updatedLoc.getFirstseen());
		
		int i = database.update(SQLStorage.TABLE_LOCATION_CLUSTER, values, SQLStorage.COLUMN_INTERNALID +" = "+updatedLoc.getId(), null);
		Cursor cursor = database.query(SQLStorage.TABLE_LOCATION_CLUSTER,
				allColumns_cluster, SQLStorage.COLUMN_INTERNALID +" = "+updatedLoc.getId(), null,
				null, null, null);
		cursor.moveToFirst();
		ClusteredLocation newLoc = cursorToClusterLoc(cursor);
		cursor.close();
		Log.d("PTEnabler", "Clustered Location count:" + newLoc.getCount() + " ID " + newLoc.getId() + " New Timestamp: " + new Date(newLoc.getDate()).toLocaleString());
		return newLoc;
	}
	public ClusteredLocation updateClusteredLocation(ClusteredLocation updatedLoc, Dataset ds){
		if(ds!=null)setClusterIDOfLocations(ds, updatedLoc.getId());
		return updateClusteredLocation(updatedLoc);
	}
	public boolean setClusterIDOfLocations(Dataset ds, long id){
		String where = "";
		int count = 0;
		Log.d("InnoLog", "Setting CID to " +id + " of " + ds.size()+" Locations");
		for(Instance i: ds){
			if(i instanceof UserLocation){
				UserLocation uloc = (UserLocation)i;
				ContentValues cv = new ContentValues();
				cv.put(SQLStorage.COLUMN_CLUSTER_INTERNALID, id);
				if(!database.isOpen())open();
				int affectedLines = database.update(SQLStorage.TABLE_LOCATION_HISTORY, cv, SQLStorage.COLUMN_TIMESTAMP +"="+uloc.getDate(), null);
				if(affectedLines!=1)Log.d("PTEnabler", "ID of Cluster not assigned: Location not found in DB");
			}else{
				Log.d("PTEnabler", "Location Datatype conversion failed");
				continue;
			}
		}
		return true;
		
//		for(Instance i : ds){
//			UserLocation di = (UserLocation)i;
//			int lat =  (int) Math.round(di.value(0) *1000000);
//			int lng = (int) Math.round(di.value(1) *1000000);
//			where+="("+SQLStorage.COLUMN_LAT +"="+lat+" AND "+ SQLStorage.COLUMN_LNG + "=" + lng +") "; 
//			if (++count < ds.size()){
//				where += "OR ";
//			}
//			
//		}
//		ContentValues cv = new ContentValues();
//		cv.put(SQLStorage.COLUMN_CLUSTER_INTERNALID, id);
//		int affectedLines = database.update(SQLStorage.TABLE_LOCATION_HISTORY, cv, where, null);
//		if (affectedLines>= ds.size()) return true;
//		return false;
	}
	public void clearLocationHistory(int locationsOlderThanXDays){
		Date x = new Date();
		long millis =x.getTime()- ((long)locationsOlderThanXDays *1000L*3600L*24L);
		
		int deletes = database.delete(SQLStorage.TABLE_LOCATION_HISTORY, SQLStorage.COLUMN_TIMESTAMP+" < "+millis , null);
		Log.d("PTEnabler", "Location(s) removed from Location History: " + deletes+ " Locations");
	}
	public void clearClusteredLocations(int locationsOlderThanXDays){
		Date x = new Date();
		long millis =x.getTime()-((long)locationsOlderThanXDays *1000L*3600L*24L);
		
		int deletes = database.delete(SQLStorage.TABLE_LOCATION_CLUSTER, SQLStorage.COLUMN_TIMESTAMP+" < "+millis , null);
		Log.d("PTEnabler", "Clusters removed from Location History: " + deletes + " clustered Locations");
		ContentValues values = new ContentValues();
		values.put(SQLStorage.COLUMN_CLUSTER_INTERNALID, 0);
		String ids = "";
		boolean first = true;
		for(ClusteredLocation cl:getAllClusterLocs()){
			if(first){
				ids+=cl.getId();
				first = false;
			}else{
				ids+=", "+cl.getId();
			}
		}

		int affectedLocations = database.update(SQLStorage.TABLE_LOCATION_HISTORY, values, SQLStorage.COLUMN_CLUSTER_INTERNALID + " not in (SELECT "+ SQLStorage.COLUMN_CLUSTER_INTERNALID+" from "+SQLStorage.TABLE_LOCATION_CLUSTER+")",
				null);
		Log.d("PTEnabler", "Removed cluster reference from  " + affectedLocations + " locations. \nExisting CIDs: " + ids);
	}

	public List<ClusteredLocation> getAllClusterLocs() {
		List<ClusteredLocation> locs = new ArrayList<ClusteredLocation>();

		Cursor cursor = database.query(SQLStorage.TABLE_LOCATION_CLUSTER,
				allColumns_cluster, null, null, null, null, SQLStorage.COLUMN_TIMESTAMP+" DESC");

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ClusteredLocation loc = cursorToClusterLoc(cursor);
			locs.add(loc);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return locs;
	}
	public List<UserLocation> getAllHistoryLocs(long since, long until, boolean descending) {
		List<UserLocation> locs = new ArrayList<UserLocation>();
		String order = "DESC";
		if(!descending){
			order = "ASC";
		}
		
		Cursor cursor = database.query(SQLStorage.TABLE_LOCATION_HISTORY,
				allColumns_locHist, SQLStorage.COLUMN_TIMESTAMP+" > "+since + " and " +SQLStorage.COLUMN_TIMESTAMP+ " < "+until, null, null, null, SQLStorage.COLUMN_TIMESTAMP+" "+order);
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			UserLocation loc = cursorToUserLoc(cursor);
			locs.add(loc);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return locs;
	}
	public List<UserLocation> getAllHistoryLocs(long since, long until){
		return getAllHistoryLocs(since, until, true);
	}
	public List<UserLocation> getUnclusteredHistoryLocs(long since, long until) {
		List<UserLocation> locs = new ArrayList<UserLocation>();

		
		Cursor cursor = database.query(SQLStorage.TABLE_LOCATION_HISTORY,
				allColumns_locHist, SQLStorage.COLUMN_TIMESTAMP+" > "+since + " and " +SQLStorage.COLUMN_TIMESTAMP+ " < "+until + " and " + SQLStorage.COLUMN_CLUSTER_INTERNALID +" is null", null, null, null, SQLStorage.COLUMN_TIMESTAMP+" DESC");
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			UserLocation loc = cursorToUserLoc(cursor);
			locs.add(loc);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return locs;
	}

	private UserLocation cursorToUserLoc(Cursor cursor) {
//		SQLStorage.COLUMN_ID,				0
//		SQLStorage.COLUMN_LAT, 				1
//		SQLStorage.COLUMN_LNG, 				2
//		SQLStorage.COLUMN_NAME, 			3
//		SQLStorage.COLUMN_PLACE,			4
//		SQLStorage.COLUMN_TYPE,				5
//		SQLStorage.COLUMN_TIMESTAMP, 		6
//		SQLStorage.COLUMN_INTERNALID,		7
//		SQLStorage.COLUMN_CLUSTER_INTERNALID8
		LocationType type = null;
		if(cursor.getString(5).equals(LocationType.ADDRESS.toString()))type = LocationType.ADDRESS;
		if(cursor.getString(5).equals(LocationType.ANY.toString()))type = LocationType.ANY;
		if(cursor.getString(5).equals(LocationType.STATION.toString()))type = LocationType.STATION;
		if(cursor.getString(5).equals(LocationType.POI.toString()))type = LocationType.POI;
        if(cursor.getString(5).equals(LocationType.COORD.toString()))type = LocationType.COORD;


		Location loc = new Location(	type, 	
										""+(int)cursor.getLong(0), 
										(int)cursor.getLong(1), 
										(int)cursor.getLong(2), 
										cursor.getString(4), 
										cursor.getString(3));
		
		UserLocation ul = new UserLocation(loc, cursor.getLong(6), cursor.getInt(8));
		return ul;
	}
	private Location cursorToLoc(Cursor cursor) {
//		SQLStorage.COLUMN_ID,				0
//		SQLStorage.COLUMN_LAT, 				1
//		SQLStorage.COLUMN_LNG, 				2
//		SQLStorage.COLUMN_NAME, 			3
//		SQLStorage.COLUMN_PLACE,			4
//		SQLStorage.COLUMN_TYPE,				5
//		SQLStorage.COLUMN_TIMESTAMP, 		6

		LocationType type = null;
		if(cursor.getString(5).equals(LocationType.ADDRESS.toString()))type = LocationType.ADDRESS;
		if(cursor.getString(5).equals(LocationType.ANY.toString()))type = LocationType.ANY;
		if(cursor.getString(5).equals(LocationType.STATION.toString()))type = LocationType.STATION;
		if(cursor.getString(5).equals(LocationType.POI.toString()))type = LocationType.POI;
        if(cursor.getString(5).equals(LocationType.COORD.toString()))type = LocationType.COORD;

		
		Location loc = new Location(	type, 	
										""+(int)cursor.getLong(0), 
										(int)cursor.getLong(1), 
										(int)cursor.getLong(2), 
										cursor.getString(4), 
										cursor.getString(3));
		
		
		return loc;
	}
	private ClusteredLocation cursorToClusterLoc(Cursor cursor) {
//		0 SQLStorage.COLUMN_ID,
//		1 SQLStorage.COLUMN_LAT, 
//		2 SQLStorage.COLUMN_LNG, 
//		3 SQLStorage.COLUMN_NAME, 
//		4 SQLStorage.COLUMN_PLACE,
//		5 SQLStorage.COLUMN_TYPE,
//		6 SQLStorage.COLUMN_TIMESTAMP, 
//		7 SQLStorage.COLUMN_INTERNALID,
//		8 SQLStorage.COLUMN_FIRSTSEEN,
//		9 SQLStorage.COLUMN_COUNT };
		Location loc = new Location(LocationType.COORD, ""+cursor.getLong(0), (int)cursor.getLong(1), (int)cursor.getLong(2), cursor.getString(4), cursor.getString(3));
		ClusteredLocation cloc = new ClusteredLocation(loc, cursor.getLong(6), cursor.getLong(7), cursor.getInt(9),cursor.getLong(8), cursor.getString(5));
		

        return cloc;
		
	}
	private Object fromString( String s ) throws IOException ,
	ClassNotFoundException {
		byte [] data = Base64.decode(s.getBytes(),Base64.DEFAULT);
		ObjectInputStream ois = new ObjectInputStream( 
				new ByteArrayInputStream(  data ) );
		Object o  = ois.readObject();
		ois.close();
		return o;
	}

	/** Write the object to a Base64 string. */
	private String toSerializeString( Serializable o ) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream( baos );
		oos.writeObject( o );
		oos.close();
		return new String( Base64.encode( baos.toByteArray(), Base64.DEFAULT ) );
	}
}
