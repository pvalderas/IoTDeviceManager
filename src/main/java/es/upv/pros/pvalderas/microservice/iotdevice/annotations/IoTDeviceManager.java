package es.upv.pros.pvalderas.microservice.iotdevice.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ComponentScan;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IoTDeviceManager {

	public String name();
	
}
