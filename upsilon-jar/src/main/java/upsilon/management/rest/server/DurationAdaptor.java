package upsilon.management.rest.server;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.Duration;

public class DurationAdaptor extends XmlAdapter<String, Duration> {
       
	@Override  
	public String marshal(Duration arg0) throws Exception { 
		return arg0.toString();
	}  
  
	@Override
	public Duration unmarshal(String arg0) throws Exception { 
		return Duration.parse(arg0); 
	}
}
 