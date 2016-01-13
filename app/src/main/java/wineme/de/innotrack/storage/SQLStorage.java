package wineme.de.innotrack.storage;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLStorage extends SQLiteOpenHelper{



	public static final String TABLE_LOCATION_HISTORY = "lochistory";
	public static final String TABLE_LOCATION_CLUSTER = "locclusters";
    public static final String TABLE_CLUSTER_META = "clustermeta";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_LAT = "lat";
	public static final String COLUMN_LNG = "lng";
	public static final String COLUMN_PLACE = "place";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_TRIP = "trip";
	public static final String COLUMN_TIMESTAMP = "timestamp";
	public static final String COLUMN_INTERNALID = "iid";
    public static final String COLUMN_DELETED = "deleted";
    public static final String COLUMN_STICKY = "sticky";
    public static final String COLUMN_META_EXTRA = "extrainfo";
	public static final String COLUMN_CLUSTER_INTERNALID = "cid";
	public static final String COLUMN_COUNT = "count";
	public static final String COLUMN_FIRSTSEEN = "firstseen";

	private static final String DATABASE_NAME = "locationTacking.db";
	private static final int DATABASE_VERSION = 1;


	private static final String DATABASE_CREATE_LOCATION_HISTORY = "create table "
			+ TABLE_LOCATION_HISTORY + " (" 
			+ COLUMN_ID + " integer , " 
			+ COLUMN_LAT + " integer , "
			+ COLUMN_LNG + " integer, " 
			+ COLUMN_PLACE +" text, " 
			+ COLUMN_NAME +" text, " 
			+ COLUMN_TYPE +" text, "
			+ COLUMN_TIMESTAMP +" integer not null,"
			+ COLUMN_INTERNALID +" INTEGER PRIMARY KEY, "
			+ COLUMN_CLUSTER_INTERNALID +" INTEGER "
			+ ") ;";
	private static final String DATABASE_CREATE_LOCATION_CLUSTER = "create table "
			+ TABLE_LOCATION_CLUSTER + " (" 
			+ COLUMN_ID + " integer , " 
			+ COLUMN_LAT + " integer , "
			+ COLUMN_LNG + " integer, " 
			+ COLUMN_PLACE +" text, " 
			+ COLUMN_NAME +" text, " 
			+ COLUMN_TYPE +" text, "
			+ COLUMN_TIMESTAMP +" integer not null, "
			+ COLUMN_INTERNALID +" INTEGER PRIMARY KEY, "
			+ COLUMN_FIRSTSEEN +" integer, "
			+ COLUMN_COUNT +" integer "
			+ ") ;";
    private static final String DATABASE_CREATE_CLUSTER_META = "create table "
            + TABLE_CLUSTER_META + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY,"
            + COLUMN_TYPE +" text, "
            + COLUMN_DELETED +" integer DEFAULT 0, "
            + COLUMN_STICKY +" integer DEFAULT 0, "
            + COLUMN_META_EXTRA +" text "
            + ") ;";


	public SQLStorage(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}


	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE_LOCATION_HISTORY);
		database.execSQL(DATABASE_CREATE_LOCATION_CLUSTER);
        database.execSQL(DATABASE_CREATE_CLUSTER_META);
	}


	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
} 

