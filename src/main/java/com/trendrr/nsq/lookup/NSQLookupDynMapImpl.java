/**
 * 
 */
package com.trendrr.nsq.lookup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trendrr.nsq.ConnectionAddress;
import com.trendrr.nsq.NSQLookup;


/**
 * Lookup implementation based on trendrr-oss DynMap
 * 
 * 
 * @author Dustin Norlander
 * @created Jan 23, 2013
 * 
 */
public class NSQLookupDynMapImpl implements NSQLookup {

	protected static Logger log = LoggerFactory.getLogger(NSQLookupDynMapImpl.class);
	
	Set<String> addresses = new HashSet<String> ();
	
	
	public void addAddr(String addr, int port) {
		if (!addr.startsWith("http")) {
			addr = "http://" + addr;
		}
		addr = addr + ":" + port;
		this.addresses.add(addr);
	}
	
	public List<ConnectionAddress> lookup(String topic) {
		HashMap<String, ConnectionAddress> addresses = new HashMap<String, ConnectionAddress>();
		
		ObjectMapper mapper = new ObjectMapper();
		
		for (String addr : this.addresses) {
			
			try {
				JsonNode rootNode = mapper.readTree(this.getHTML(addr + "/lookup?topic=" + topic));
				JsonNode producers = rootNode.path("data").path("producers");
				Iterator<JsonNode> prodItr = producers.getElements();
				while (prodItr.hasNext()) {
					JsonNode producer = prodItr.next();
					String host = producer.path("broadcast_address").getTextValue();
					int tcpPort = producer.path("tcp_port").getIntValue();
					String key =  host + ":" + tcpPort;
					ConnectionAddress address = new ConnectionAddress();
					address.setHost(host);
					address.setPort(tcpPort);
					addresses.put(key, address);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ArrayList<ConnectionAddress>(addresses.values());
	}
	
	public String getHTML(String url) {
	  URL u;
	  HttpURLConnection conn = null;
	  BufferedReader rd = null;
	  String line;
	  String result = "";
	  try {
	     u = new URL(url);
	     conn = (HttpURLConnection) u.openConnection();
	     conn.setRequestMethod("GET");
	         rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	         while ((line = rd.readLine()) != null) {
	            result += line;
	         }
	         
	      } catch (Exception e) {
	    	  log.error("Caught", e);
	      } finally {
	    	  try {
    			  if (rd != null){
    		  	  	rd.close();
    		  	  }	    			
				} catch (Exception e) {
					log.error("Caught", e);
				}

				// Release memory and underlying resources on the HttpURLConnection otherwise we may run out of file descriptors and leak memory
				if (conn != null){
					conn.disconnect();
				}
	      }
	      return result;
	   } 
	
}
