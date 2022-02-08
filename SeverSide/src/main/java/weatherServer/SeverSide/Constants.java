package weatherServer.SeverSide;

import java.util.Date;
import java.util.logging.Logger;

public class Constants {
    public static final Logger logger = Logger.getLogger(Constants.class.getName());
    public final static String APIKey = System.getenv("MyWeatherAPIKey") ; 
    public static final String BaseURL="https://api.openweathermap.org/data/2.5/weather";
    public static final String Bean_Cache_SVC="Weather_Cache";
    public static final String ENV_REDIS_PASSWORD="Redis_Pass_Weather";

    public static String getTimeStamp(){
        
        long time = new Date().getTime();
        java.sql.Timestamp ts = new java.sql.Timestamp(time);
        String Ts=ts.toString().split("\\.")[0];
        return Ts;
    }
}
