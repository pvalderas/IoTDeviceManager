package es.upv.pros.pvalderas.microservice.iotdevice;

import org.json.JSONArray;

public class ObservationList {

	private static JSONArray observationList;
	
	public static void set(JSONArray list){
		observationList=list;
	}
	
	public static JSONArray get(){
		return observationList;
	}
	
	public static String getAsString(){
		return observationList.toString();
	}
}
