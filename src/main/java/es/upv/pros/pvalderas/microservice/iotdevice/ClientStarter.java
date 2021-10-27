package es.upv.pros.pvalderas.microservice.iotdevice;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.appinfo.ApplicationInfoManager;

import es.upv.pros.pvalderas.http.HTTPClient;
import es.upv.pros.pvalderas.microservice.iotdevice.annotations.Actuation;
import es.upv.pros.pvalderas.microservice.iotdevice.annotations.IoTDeviceManager;
import es.upv.pros.pvalderas.microservice.iotdevice.annotations.Observation;
import es.upv.pros.pvalderas.microservice.iotdevice.annotations.Sensor;

@Component
public class ClientStarter implements ApplicationRunner {
	 
	 
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private ApplicationInfoManager eurekaManager;
	
	 
    @Override
    public void run(ApplicationArguments args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, JSONException, IOException {
    	
		Class mainClass=context.getBeansWithAnnotation(IoTDeviceManager.class).values().iterator().next().getClass().getSuperclass();
		 
		if(mainClass!=null){
			Annotation classAnnotation= mainClass.getDeclaredAnnotation(IoTDeviceManager.class);
		    String microservice =(String)classAnnotation.annotationType().getMethod("name").invoke(classAnnotation);
			
			System.out.println("Setting up IoT Device Manager for "+microservice);
				        		
			System.out.println("Setting up OK");
			
			
		}
         
    }
    
    @PostConstruct
    private void registerIoTDevice() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, JSONException, IOException {
        
    	
    	Properties props=getProperties();
    	
    	JSONObject device=new JSONObject();
    	device.put("name", props.getProperty("spring.application.name"));
    	device.put("iotDevice", props.getProperty("spring.application.iotDevice"));
    	device.put("system", props.getProperty("spring.application.system"));
    	
    	Map<String,Object> actuator=context.getBeansWithAnnotation(RestController.class);
    	if(actuator.size()>0){
    			device.put("operations", getActuations(actuator));
    	}
    	
    	Map<String,Object> sensor=context.getBeansWithAnnotation(Sensor.class);
    	if(sensor.size()>0){
    		device.put("observations", getObservatiosn(sensor));
    	}
    	
    	if(props.getProperty("microserviceEmu.url")!=null)
    		System.out.println(HTTPClient.post(props.getProperty("microserviceEmu.url"), device.toString(), true, "application/json"));
    	else{
	    	Map<String, String> map = eurekaManager.getEurekaInstanceConfig().getMetadataMap();
	    	map.put("microservices", device.toString());
	    	eurekaManager.initComponent(eurekaManager.getEurekaInstanceConfig());
    	}
   }
    
    private JSONArray getActuations(Map<String,Object> actuator) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, JSONException{
	        
    	 Method methods[]=actuator.values().iterator().next().getClass().getMethods(); 
    	
    	JSONArray list=new JSONArray();
	    
	        for(Method m:methods){
		       	Annotation request=m.getDeclaredAnnotation(RequestMapping.class);
		       	Annotation actuation = m.getDeclaredAnnotation(Actuation.class);
		       	
		       	if(request!=null &&  actuation!=null){
		       		String operationName=(String) actuation.annotationType().getMethod("name").invoke(actuation);
		       		String operationDesc=(String) actuation.annotationType().getMethod("description").invoke(actuation);
		       		Class resultType=(Class) actuation.annotationType().getMethod("resultType").invoke(actuation);
		       	
		        
		        	JSONObject op=new JSONObject();
		       		String[] paths=(String[])request.annotationType().getMethod("value").invoke(request);
		       		RequestMethod[] httpMethods=(RequestMethod[])request.annotationType().getMethod("method").invoke(request);        		
		       	
		       		op.put("description", operationDesc);
		       		op.put("name", operationName);
		       		op.put("path", paths[0]);
		       		op.put("method", httpMethods[0]);
		       		
		       		list.put(op);
		        }
		    
	        }
	        
	        OperationList.set(list);
	        return list;
    }
    
    private JSONArray getObservatiosn(Map<String,Object> sensor) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, JSONException{
    	JSONArray list=new JSONArray();
	        
        Method methods[]=sensor.values().iterator().next().getClass().getMethods();
    
        for(Method m:methods){
	       	Annotation observation = m.getDeclaredAnnotation(Observation.class);
	       	
	       	if(observation!=null){
	       		String observationName=(String) observation.annotationType().getMethod("name").invoke(observation);
	       		String observationProperty=(String) observation.annotationType().getMethod("property").invoke(observation);
	       		String featureOfInterest=(String) observation.annotationType().getMethod("featureOfInterest").invoke(observation);
	       		String observationDesc=(String) observation.annotationType().getMethod("description").invoke(observation);
	       		Class observationResultType=(Class) observation.annotationType().getMethod("resultType").invoke(observation);
	       	
	        
	        	JSONObject ob=new JSONObject();  		
	       	
	       		ob.put("description", observationDesc);
	       		ob.put("property", observationProperty);
	       		ob.put("name", observationName);
	       		ob.put("featureOfInterest", featureOfInterest);
	       		ob.put("resultName", observationResultType.getSimpleName());
	       		
	       		JSONArray atts=new JSONArray();
	       		for(Field f:observationResultType.getDeclaredFields()){
		       		JSONObject att=new JSONObject(); 
		       		att.put("name", f.getName());
		       		att.put("type", f.getType().getSimpleName().toLowerCase());
		       		atts.put(att);
		       	}
	       		
	       		ob.put("resultProps", atts);
	       		
	       		list.put(ob);
	        }
	    
        }
        
       ObservationList.set(list);
       return list;
    }
	
    private  Properties getProperties(){
		YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
	    yamlFactory.setResources(new ClassPathResource("application.yml"));
	    Properties props=yamlFactory.getObject();
	    return props;
    }
    
}
