package open.source.justdial.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import open.source.justdial.configuration.Properties;
import open.source.justdial.enumeration.City;
import open.source.justdial.enumeration.Service;
import open.source.justdial.model.Data;
import open.source.justdial.utility.PhoneNumberDecoder;

public class Search implements Runnable {
	
	private final static String baseUrl = "http://www.justdial.com";
	
	private City city;
	private Service service;
	
	private String searchUrl;
	
	protected WebDriver webDriver;
	
	private Set<Data> allData = new TreeSet<Data>();
	
	int currentPageNumber = 1;
	private List<Data> data;
	
	public Search(City city, Service service) {
		this.city = city;
		this.service = service;
	}
	
	protected void init() {
		File file = new File("/home/pm/.mozilla/firefox/n34w7dcm.user/");
		file.mkdirs();
		
		/*
		ProfilesIni profileIni = new ProfilesIni();
		
		FirefoxProfile profile = profileIni.getProfile("default");
		
		// FirefoxProfile profile = new FirefoxProfile(file);
		profile.setPreference("general.useragent.override","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
		profile.setPreference("media.peerconnection.enabled", true);
		profile.setPreference("plugin.state.flash", 1);
		profile.setPreference("general.useragent.locale","en");
		profile.setPreference("places.history.enabled", true);
		profile.setPreference("privacy.clearOnShutdown.offlineApps", true);
		profile.setPreference("privacy.clearOnShutdown.passwords", true);
		profile.setPreference("privacy.clearOnShutdown.siteSettings", true);
		profile.setPreference("privacy.sanitize.sanitizeOnShutdown", true);
		
		FirefoxOptions options = new FirefoxOptions();
        options.setProfile(profile);
        
        // applicable for chrome
        options.add_experimental_option("excludeSwitches", ["ignore-certificate-errors", "safebrowsing-disable-download-protection", "safebrowsing-disable-auto-update", "disable-client-side-phishing-detection"]);
        
        webDriver = new FirefoxDriver(options);
        */
		
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.setExperimentalOption("useAutomationExtension", false);
		//chromeOptions.addArguments("--headless", "--window-size=1920,1200", "--ignore-certificate-errors");
		chromeOptions.addArguments("--window-size=1920,1200", "--ignore-certificate-errors", "safebrowsing-disable-download-protection", "safebrowsing-disable-auto-update", "disable-client-side-phishing-detection");
		chromeOptions.setAcceptInsecureCerts(true);
	    chromeOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
	    chromeOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
		
		webDriver = new ChromeDriver(chromeOptions);
		
		// webDriver = new FirefoxDriver();
		// webDriver = ChromeDriver.builder().withDriverService(driverService).build();
		
		webDriver.manage().timeouts().implicitlyWait(Properties.PAGE_LOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		
		searchUrl = baseUrl + "/" + city.name() + "/" + service.name();
		
		System.out.println("-> (expectation) " + searchUrl);
		
		webDriver.get(searchUrl);
		
		System.out.println("-> (reality) " + webDriver.getCurrentUrl());
		
		webDriver.switchTo().parentFrame();
		
	}
	
	private void extractDataAndInsertInList(WebElement item) {
		
		WebElement one = item.findElement(By.tagName("section"))
			.findElement(By.className("colsp"))
			.findElement(By.tagName("section"))
			.findElement(By.className("store-details"));
		
		Data d = new Data();
		
		try {
			WebElement anchor = one.findElement(By.tagName("h2")).findElement(By.tagName("span")).findElement(By.tagName("a"));
			String titleText = anchor.getAttribute("title");
			String[] parts = titleText.split(" in ");
			String name = parts[0];
			d.setName(name);
			if (parts.length > 1) {
				String[] addressParts = parts[1].split(", ");
				String locality = addressParts[0];
				d.setLocality(locality);
				String cityTmp = addressParts[1];
				d.setCity(cityTmp);
			} else {
				d.setLocality("");
				d.setCity("");
			}
		} catch(Exception e) {
			d.setName("");
		}
		
		try {
			d.setRating(one.findElement(By.className("newrtings")).findElement(By.className("green-box")).getText());
		} catch(Exception e) {
			d.setRating("");
		}
		
		try {
			String voteStr = one.findElement(By.className("newrtings")).findElement(By.className("lng_vote")).getText();
			if (null != voteStr) {
				voteStr = voteStr.trim();
				voteStr = voteStr.split(" ")[0];
			}
			d.setVotes(voteStr);
		} catch(Exception e) {
			d.setVotes("");
		}
		
		try {
			List<WebElement> spanElements = one.findElements(By.className("mobilesv"));
			String phn = new String();
			for (WebElement spanElement : spanElements) {
				String classAttributeValue = spanElement.getAttribute("class");
				String digit = PhoneNumberDecoder.identify(classAttributeValue);
				phn = phn + digit;
			}
			d.setPhone(phn);
		} catch (Exception e) {
			e.printStackTrace();
			d.setPhone("");
		}
		
		data.add(d);
	
	}
	
	private List<Data> extractData() {
		
		System.out.println("Started extracting data : (city) " + city.name() + " (service) " + service.name() + " (page) " + currentPageNumber);
		
		data = new ArrayList<Data>();
		
		List<WebElement> listItems = webDriver.findElements(By.className("cntanr"));
		
		listItems.parallelStream()
				.forEach(item -> extractDataAndInsertInList(item));
		
		return data;
		
	}
	
	private void saveDataInExcel(Collection<Data> collection) {
		
		System.out.println("Started saving data : (city) " + city.name() + " (service) " + service.name() + " (records) " + collection.size());
		
		File dir = new File(Properties.ABSOLUTE_LOCAL_PATH_DATASTORE + "/" + city.name() + "/");
		File file = new File(Properties.ABSOLUTE_LOCAL_PATH_DATASTORE + "/" + city.name() + "/" + service.name() + ".csv");
		
		try {
			dir.mkdirs();
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("<- Could not Create File in Location : " + file.getAbsolutePath());
		}
		
		FileWriter outputFile = null;
		
		try {
			outputFile = new FileWriter(file);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("<- Could not Open File in Location : " + file.getAbsolutePath());
		}
		
		try {
			outputFile.write( Data.firstLineForExcel );
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("<- Could not Write firstLine of Excel File in Location : " + file.getAbsolutePath());
		}
		
		for(Data entry : collection) {
			try {
				outputFile.write( entry.toStringForExcel() );
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("<- Could not Write File in Location : " + file.getAbsolutePath());
			}
		}
		
		try {
			outputFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("<- Could not Close File in Location : " + file.getAbsolutePath());
		}
		
		System.out.println("Successfully saved : " + file.getAbsolutePath());
		
	}
	
	private void searchInJustDial() {
		
		init();
		
		webDriver.manage().timeouts().implicitlyWait(Properties.ELEMENT_FIND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		
		int LIMIT = Properties.MAX_SEARCH_PAGES;
		
		if (webDriver.getCurrentUrl().contains("/search")) {
			// TODO
			LIMIT = Properties.MAX_SEARCH_PAGES_BAD_LOAD;
		}
		
		List<Data> currentData = new ArrayList<Data>();
		
		do {
			
			if (currentPageNumber > 1) {
				
				int retryCount = -1;
				
				String nextUrl = searchUrl + "/page-" + currentPageNumber;
				System.out.println("-> (expectation) " + nextUrl + " (limit) " + LIMIT);
				
				String currentUrl = null;
				
				while (!nextUrl.equals(currentUrl) && retryCount < Properties.RETRY_BAD_LOAD) {
					webDriver.get(nextUrl);
					++retryCount;
					currentUrl = webDriver.getCurrentUrl();
					
					String log = "-> (reality) " + currentUrl + " (limit) " + LIMIT;
					if (retryCount > 0) {
						log += " (retry) " + retryCount;
					}
					System.out.println(log);
				}
				
			}
			
			currentData = extractData();
			
			allData.addAll(currentData);
			
			++currentPageNumber;
			
		} while (currentData.size() > 0 && currentPageNumber <= LIMIT);
		
	}
	
	public void run() {
		
		System.out.println("Thread Starts : (city) " + city.name() + " (service) " + service.name());
		try {
			searchInJustDial();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("<- Failed for (city) " + city.name() + " (service) " + service.name() + " (page) " + currentPageNumber);
		} finally {
			try {
				webDriver.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (allData.size() > 0) {
				saveDataInExcel(allData);
			} else {
				System.out.println("<- No results for (city) " + city.name() + " (service) " + service.name() + " (page) " + currentPageNumber);
			}
		}
		System.out.println("Thread Ends : (city) " + city.name() + " (service) " + service.name() + " (pages) " + currentPageNumber);
		
	}

}
