package open.source.justdial.configuration;

import open.source.justdial.enumeration.City;
import open.source.justdial.enumeration.Service;

public interface Properties {
	
	public static byte MAX_BROWSER_INSTANCES = 1;
	
	public static String ABSOLUTE_LOCAL_PATH_DATASTORE = "/space/workspace/eclipse/justdial/datastore";
	
	public static City[] CITIES = City.values();
	// public static City[] CITIES = { City.Amritsar, City.Ludhiana };
	
	public static Service[] SERVICES = Service.values();
	// public static Service[] SERVICES = { Service.Holiday };
	
	public static int PAGE_LOAD_TIMEOUT_SECONDS = 10;
	
	public static int ELEMENT_FIND_TIMEOUT_SECONDS = 1;
	
	public static int MAX_SEARCH_PAGES = 10;
	// public static int MAX_SEARCH_PAGES = Integer.MAX_VALUE;
	
	public static int MAX_SEARCH_PAGES_BAD_LOAD = 3;
	
	public static int RETRY_BAD_LOAD = 4;
	// public static int RETRY_BAD_LOAD = Integer.MAX_VALUE;
	
}
