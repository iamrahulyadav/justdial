package open.source.justdial;

import open.source.justdial.configuration.Properties;
import open.source.justdial.enumeration.City;
import open.source.justdial.enumeration.Service;
import open.source.justdial.service.Search;
import open.source.justdial.utility.ThreadsControl;

public class Launch {
	
	private static ThreadsControl threadsControl;
	
	private static void process(City city, Service service) {
		
		Search search = new Search(city, service);
		threadsControl.add(search);
		
	}
	
	private static void processItsServices(City city) {
		
		for (Service service : Properties.SERVICES) {
			process(city, service);
		}
		
	}
	
	public static void main(String[] args) {
		
		threadsControl = new ThreadsControl(Properties.MAX_BROWSER_INSTANCES);
		
		for (City city : Properties.CITIES) {
			processItsServices(city);
		}
		
		threadsControl.start();
		
	}
	
}
