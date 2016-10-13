package tool;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Tool {

	public static Double getDirection(Double[] lonLat1, Double[] lonLat2){
		double lon1 = lonLat1[0];
		double lat1 = lonLat1[1];
		double lon2 = lonLat2[0];
		double lat2 = lonLat2[1];
		double deltaLon = lon2 - lon1;
		double deltaLat = lat2 - lat1;
		return Math.atan2(deltaLat, deltaLon)/Math.PI*180;
	}
	
	public static double getAngle(double direction1, double direction2){
		double angle = Math.abs(direction2 - direction1);
		if(angle > 180) angle = 360 - angle;
		return angle;
	}
	
    /**   
     * google maps algorithm   
     */      
    private static double EARTH_RADIUS = 6378.137;   
    private static double rad(double d)   
    {   
         return d * Math.PI / 180.0;   
    }    
      
    /**   
     * distance return in double
     * unit is meter   
     */   
    public static double getDistance(double lat1, double lng1, double lat2, double lng2)   
    {   
        double radLat1 = rad(lat1);   
        double radLat2 = rad(lat2);   
        double a = radLat1 - radLat2;   
        double b = rad(lng1) - rad(lng2);   
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +   
        Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));   
        s = s * EARTH_RADIUS;   
        s = Math.round(s * 1000) ;   
        return s;   
    } 
    
    public static double getDistance(Double[] location1, Double[] location2)   
    {   
    	double lat1 = location1[1];
    	double lng1 = location1[0];
    	double lat2 = location2[1];
    	double lng2 = location2[0];
        double radLat1 = rad(lat1);   
        double radLat2 = rad(lat2);   
        double a = radLat1 - radLat2;   
        double b = rad(lng1) - rad(lng2);   
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +   
        Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));   
        s = s * EARTH_RADIUS;   
        s = Math.round(s * 1000) ;   
        return s;   
    }
    public static double getDistance(double[] location1, double[] location2)   
    {   
    	double lat1 = location1[1];
    	double lng1 = location1[0];
    	double lat2 = location2[1];
    	double lng2 = location2[0];
        double radLat1 = rad(lat1);   
        double radLat2 = rad(lat2);   
        double a = radLat1 - radLat2;   
        double b = rad(lng1) - rad(lng2);   
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +   
        Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));   
        s = s * EARTH_RADIUS;   
        s = Math.round(s * 1000) ;   
        return s;   
    }  
    
    public static double save2Decimal(double scalar){
    	return ((Long) Math.round(scalar*100)) / 100d;
    }
    
    public static void log(Object s){
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	System.out.println("[" +sdf.format(new Date()) + "] " + s);
    }
    
    public static Process callShell(String[] cmd) throws IOException{
    	return Runtime.getRuntime().exec(cmd);
    }
    
    public static Process callShell(String cmd) throws IOException{
    	return Runtime.getRuntime().exec(cmd);
    }
    
    public static String toCsv(Object... objArr){
    	String sCsv = "";
    	for(int index = 0; index < objArr.length - 1; index++){
    		if(objArr[index] == null) continue;
    		sCsv += objArr[index].toString() + ",";
    	}
    	if(objArr[objArr.length - 1] == null) 
    		return sCsv + "\n";
    	else 
    		return sCsv + objArr[objArr.length - 1].toString() + "\n";
    }
    
    public static double findMax(Object[] list){
    	if(!(list[0] instanceof Double)) return Double.MIN_VALUE;
    	double max = Double.MIN_VALUE;
    	for(int index = 0; index < list.length; index++){
    		max = (max > (Double) list[index])? max:((Double) list[index]);
    	}
    	return max;
    }
    
    public static double findMin(Object[] list){
    	if(!(list[0] instanceof Double)) return Double.MAX_VALUE;
    	double min = Double.MAX_VALUE;
    	for(int index = 0; index < list.length; index++){
    		min = (min < (Double) list[index])? min:((Double) list[index]);
    	}
    	return min;
    }
    
    public static Extremum findMin(Integer[] list){
    	int min = Integer.MAX_VALUE;
    	int minIndex = -1;
    	for(int index = 0; index < list.length; index++){
    		if(list[index] == null) continue;
    		if(list[index] < min){
    			min = list[index];
    			minIndex = index;
    		}    		
    	}
    	return new Extremum(min, minIndex);
    }
    
    public static void output(String[] strArr, String pathfile) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(pathfile));
		for(int j = 0; j < strArr.length; j++){
			if(strArr[j] == null)
				break;
			bw.write(strArr[j]);
			bw.newLine();
		}
		bw.flush();
		bw.close();
    }
    
    public static void output(String[] strArr, String pathname, String filename, boolean append) throws IOException{
        File file = new File(pathname, filename);
    	if(!file.exists())
    		try {
    			file.createNewFile();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	
    	FileWriter fw = new FileWriter(pathname+filename, append);
		for(int j = 0; j < strArr.length; j++){
			if(strArr[j] == null)
				break;
			fw.write(strArr[j] + "\n");
		}
		fw.flush();
		fw.close();
    }
}
