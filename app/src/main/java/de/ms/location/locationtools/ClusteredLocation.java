package de.ms.location.locationtools;
import android.os.AsyncTask;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import de.schildbach.pte.dto.Location;


public class ClusteredLocation implements Comparable<ClusteredLocation>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Location loc;
	private long date;
	private long firstseen;
	private int count;
	private long id;
    private ClusterMetaData meta;
	
	public ClusteredLocation(Location loc, long date, long  id, ClusterMetaData.ClusterType type){

		this.loc = loc;
		this.date = date;
		this.id = id;
		this.count = 1;
		this.firstseen = new Date().getTime();
        this.meta = new ClusterMetaData(type);
	}
	public ClusteredLocation(Location loc, long date, long  id, int count, long firstseen, String meta){

		this.loc = loc;
		this.date = date;
		this.id = id;
		this.count = count;
		this.firstseen = firstseen;
        this.meta = new ClusterMetaData(meta);
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getFirstseen() {
		return firstseen;
	}

	public void setFirstseen(long firstseen) {
		this.firstseen = firstseen;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

    public ClusterMetaData getMeta() {
        return meta;
    }

    public void setMeta(ClusterMetaData meta) {
        this.meta = meta;
    }
	private static double[] getDoubleArray(Location loc){
		double [] tmp = new double[2];
		tmp[0] = (double)loc.lat/1000000.0;
		tmp[1] = (double)loc.lon/1000000.0;
		return tmp;
	}
	public int compareTo(ClusteredLocation cl2) {
		
		return new Long(getDate()).compareTo(new Long(cl2.getDate()));
	
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof ClusteredLocation){
			return ((ClusteredLocation)obj).compareTo(this)==0;
		}
		return false;
	}
    public Vector<ClusteredLocation> getProbableNextLocations(double minTransitionProb, long timeInFutureInMin){

            Set<PropableNextLocationResult> props;
            Vector<PropableNextLocationResult> test = new Vector<PropableNextLocationResult>();
            if(minTransitionProb>0){
                props= ClusterManagement.getProbableDestinations(this.getId());
                if(props!=null && props.size()>0)test.addAll(props);
            }else{
                for(ClusteredLocation cl: ClusterManagement.getClusteredLocationsFromCache(false)){
                    PropableNextLocationResult temp = new PropableNextLocationResult(0,cl);
                    test.add(temp);
                }
            }

        props= ClusterManagement.getPropableLocationForDate(new Date(new Date().getTime() + 1000l * 60l * timeInFutureInMin), false);
            HashSet<PropableNextLocationResult> unique = new HashSet<PropableNextLocationResult>();
            Vector<ClusteredLocation> probdata = new Vector<ClusteredLocation>();
            if(props!=null && props.size()>0){
                for(PropableNextLocationResult pnlr:props){
                    if(test.contains(pnlr)){
                      //TODO Parameter für Gewichtung
						pnlr.addPropability(test.get(test.indexOf(pnlr)),3);
                    }
                    if(pnlr.getProb()>=minTransitionProb)unique.add(pnlr);
                }
                for(PropableNextLocationResult pnlr:test){
                    if(pnlr.getProb()>=minTransitionProb && !unique.contains(pnlr)){
                       unique.add(pnlr);
                    }
                }
                TreeSet<PropableNextLocationResult> temp = new TreeSet<PropableNextLocationResult>();
                temp.addAll(unique);
                for(PropableNextLocationResult pnlr: temp){
                    probdata.add(pnlr.getCloc());
                }
            }else{
               List<ClusteredLocation> locs = ClusterManagement.getCloseByClusteredLocationsFromCache(5000);

                for(ClusteredLocation pnlr:locs){
                    probdata.add(pnlr);
                }

            }


            return probdata;
    }
    public Vector<Long> getProableNextCID(double minTransitionProb, long timeInFutureInMin){
        Vector<ClusteredLocation> clocs= this.getProbableNextLocations(minTransitionProb,timeInFutureInMin);
        Vector<Long> results = new Vector<Long>();
        for(ClusteredLocation loc : clocs){
            results.add(loc.getId());
        }
    return results;
    }

    public void getProbableNextLocationsAsync(double minTransitionProb, long timeInFutureInMin, ProbLocationCalculationListener listener){
        ProbNextLocationTask pnlt = new ProbNextLocationTask(this,minTransitionProb,timeInFutureInMin,listener);
        pnlt.execute();
    }


    public class ProbNextLocationTask extends AsyncTask<Void,Void,Vector<ClusteredLocation>>{
        private ClusteredLocation cl;
        private double minTransitionProb;
        private long timeInFutureInMin;
        private ProbLocationCalculationListener listener;

        public ProbNextLocationTask(ClusteredLocation cl, double minTransitionProb, long timeInFutureInMin, ProbLocationCalculationListener listener){
            this.cl = cl;
            this.minTransitionProb = minTransitionProb;
            this.timeInFutureInMin = timeInFutureInMin;
            this.listener = listener;
        }
        @Override
        protected Vector<ClusteredLocation> doInBackground(Void... params) {

            return cl.getProbableNextLocations(minTransitionProb,timeInFutureInMin);
        }

        @Override
        protected void onPostExecute(Vector<ClusteredLocation> clusteredLocations) {
            listener.onCalculationCompleted(clusteredLocations);
        }
    }


}

