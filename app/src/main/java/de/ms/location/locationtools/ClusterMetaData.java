package de.ms.location.locationtools;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Vector;



/**
 * Created by Martin on 19.05.2015.
 */
public class ClusterMetaData implements Serializable{
    public enum ClusterType{
        NOISE_CLUSTER,
        DAILY_CLUSTER
    }

    public boolean deleted;
    public boolean modified;
    public ClusterType type;
    public Vector<Long> nextCIDs;
    public Vector<Double> nextCIDprobs;
    public String poiCategory;
    public static Gson gson;
    public ClusterMetaData(String json){
       if(gson==null) gson = new Gson();
            ClusterMetaData temp = gson.fromJson(json, ClusterMetaData.class);
            if(temp!=null){
                this.deleted = temp.deleted;
                this.modified = temp.modified;
                this.nextCIDs = temp.nextCIDs;
                this.nextCIDprobs = temp.nextCIDprobs;
                this.poiCategory = temp.poiCategory;
                this.type = temp.type;
            }else{
                initDefaults(type);
            }




    }
    public ClusterMetaData(ClusterType type){
        if(gson==null) gson = new Gson();
        initDefaults(type);
        }

    private void initDefaults(ClusterType type){
        this.deleted = false;
        this.modified = false;
        this.nextCIDs = new Vector<Long>();
        this.nextCIDprobs = new Vector<Double>();
        this.type = type;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
