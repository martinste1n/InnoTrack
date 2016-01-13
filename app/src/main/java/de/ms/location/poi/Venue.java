package de.ms.location.poi;

import android.graphics.Bitmap;

import java.io.Serializable;



public class Venue implements Serializable{
/**
	 * 
	 */
	private static final long serialVersionUID = 4969403086494214684L;
public int x; //Mercator
public int y;//Mercator
private double x_coord;//lat
private double y_coord;//lon
private boolean reverseGeoCoded = false;
private String id;
private String street;
private String zip;
private String city;
private String c_part;
private String housenmb;
private String land;
private String featureName;
private String query;
private String iconUrl;
private String type;
private String tel;
private String web;
private String gformattedAdress;
private double rating;
private double max_rating;
private String rating_img_url;
private double initialDistance;
private String description;
private boolean closed;
private String [] imageUrls;
private String [] categories;
private static final int X = 0;
private static final int Y = 1;
transient private Bitmap iconBM;

private static final double MERCATOR_A = 6378137.0;
private static final double LONGITUDE_FACTOR = Math.PI / 180.0 * 6378137.0;
private static final double PI_OVER_2 = Math.PI / 2.0;
private static final double PI_OVER_180 = Math.PI / 180.0;
public Venue(){
		x=-1;
		y=-1;
	}
	
	public Venue(int x, int y){
		this.x = x;
		this.y =y;
	}

	public Venue(double x_coord, double y_coord, String street, String zip, String city, String c_part, String housenmb, String land, String featureName, String query){
		this.x_coord = x_coord;
		this.y_coord = y_coord;
		this.street = street;
		this.zip = zip;
		this.city= city;
		this.c_part = c_part;
		this.housenmb= housenmb;
		this.land = land;
		this.featureName = featureName;
		this.query = query;
		this.reverseGeoCoded = false;
	}
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getStreet() {
		if(street==null ||street.equals(" ")||street.equals(""))return null;
		return street;
	}

	public void setStreet(String street) {
		if(!street.equals(""))
		this.street = street;
	}

	public String getZip() {
		if(zip==null ||zip.equals(" ")||zip.equals(""))return null;
			return zip;
	}

	public void setZip(String zip) {
		if(!zip.equals(""))
		this.zip = zip;
	}

	public String getCity() {
		if(city==null ||city.equals(" ")||city.equals(""))return null;
		return city;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setCity(String city) {
		if(!city.equals(""))
		this.city = city;
	}

	public String getC_part() {
		if(c_part==null ||c_part.equals(" ")||c_part.equals(""))return null;
		return c_part;
	}

	public void setC_part(String c_part) {
		if(!c_part.equals(""))
		this.c_part = c_part;
	}

	public String getHousenmb() {
		if(housenmb==null || housenmb.equals(" ")||housenmb.equals(""))return null;
		return housenmb;
	}

	public void setHousenmb(String housenmb) {
		if(!housenmb.equals(""))
		this.housenmb = housenmb;
	}

	public String getLand() {
		if(land==null ||land.equals(" ")||land.equals(""))return null;
		return land;
	}

	public void setLand(String land) {
		if(!land.equals(""))
		this.land = land;
	}

	public double getX_coord() {
		return x_coord;
	}

	public double getInitialDistance() {
		return initialDistance;
	}

	public void setInitialDistance(double initialDistance) {
		this.initialDistance = initialDistance;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getWeb() {
		return web;
	}

	public void setWeb(String web) {
		this.web = web;
	}

	public void setX_coord(double x_coord) {
		this.x_coord = x_coord;
	}

	public double getY_coord() {
		return y_coord;
	}

	public void setY_coord(double y_coord) {
		this.y_coord = y_coord;
	}
	
	public String getFeatureName() {
		if (featureName!=null){
			return featureName;	
		}
		else{
			return this.toString();
		}
		
	}

	public void setFeatureName(String featureName) {
		if(!featureName.equals(""))
		this.featureName = featureName;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		if(!query.equals(""))
		this.query = query;
	}

	public String toString(){
		return (featureName!=null?featureName+"\n":"")+
				(getStreet()!=null?getStreet()+((getHousenmb()!=null?" "+getHousenmb():"")):"")+
				(getZip()!=null?"\n"+getZip():"")+
				(getCity()!=null?" "+getCity():"");	
	}
	
	public String getFullAddress(){
		return (getStreet()!=null?getStreet():"")+(getHousenmb()!=null?","+getHousenmb():"")+(getZip()!=null?","+getZip():"")+(getCity()!=null?","+getCity():"");
	}
	
	public String[] getImageUrls() {
		return imageUrls;
	}

	public void setImageUrls(String[] imageUrls) {
		this.imageUrls = imageUrls;
	}

	public String getIconUrl() {
		if(iconUrl!=null && !iconUrl.equals("")){
			return iconUrl;	
		}
		return null;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	public String getRealFeatureName(){
		return featureName;
	}

	public Bitmap getIconBM() {
		return iconBM;
	}

	public void setIconBM(Bitmap iconBM) {
		this.iconBM = iconBM;
	}


//	public boolean equals (Object o2){
//		if(o2 instanceof Venue){
//			Venue p2 = (Venue)o2;
//			if((getRealFeatureName()!=null&&p2.getRealFeatureName()!=null) ||(getRealFeatureName()==null&&p2.getRealFeatureName()==null) ){
//				if (getFeatureName()!=null && getFeatureName().equals(p2.getFeatureName())){
//					if((getStreet()!=null && p2.getStreet()!=null)||(getStreet()==null && p2.getStreet()==null)){
//						if(getStreet()!=null &&getStreet().equals(p2.getStreet())){
//							if(getType()!=null&&getType().equals(p2.getType())){
//									return true;
//							}
//						}
//					}
//				}
//			}
//					
//			
//			
//		}
//		return false;
//		
//	}



	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	
	public static double[] coordToLatLng(double x, double y)
	{
		double[] coord = { x, y }; 
		return coordToLatLng(coord);
	}
	
	public static double[] coordToLatLng(double[] coord)
	{
		double lat = (2 * Math.atan(Math.exp(coord[Y] / MERCATOR_A)) - PI_OVER_2) / PI_OVER_180;
		double lng = coord[X] / LONGITUDE_FACTOR;
		
		return new double[]{lat, lng};
	}
	
	public static double[] latLngToCoord(double[] latLng)
	{
		double[] result = new double[2];
		
		double sinLatitude = Math.sin(latLng[0] * PI_OVER_180);
		
		result[X] = latLng[1] * LONGITUDE_FACTOR;
		result[Y] = 0.5 * Math.log((1.0 + sinLatitude) / (1.0 - sinLatitude )) * MERCATOR_A;
		
		return result;
	}

	public String getGformattedAdress() {
		return gformattedAdress;
	}

	public void setGformattedAdress(String gformattedAdress) {
		this.gformattedAdress = gformattedAdress;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public double getMax_rating() {
		return max_rating;
	}

	public void setMax_rating(double max_rating) {
		this.max_rating = max_rating;
	}

	public String getRating_img_url() {
		return rating_img_url;
	}

	public void setRating_img_url(String rating_img_url) {
		this.rating_img_url = rating_img_url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public String[] getCategories() {
		return categories;
	}

	public void setCategories(String[] categories) {
		this.categories = categories;
	
	}
	public boolean equals(Object o2){
		if (o2 instanceof Venue){
			Venue v2 = (Venue) o2;
			if (this == v2)return true;
			if (v2.getX_coord() == getX_coord() && v2.getY_coord() == getY_coord()&& getFeatureName().equals(v2.getFeatureName())) return true;
			
		}
		return false;
	}
	
	
}




