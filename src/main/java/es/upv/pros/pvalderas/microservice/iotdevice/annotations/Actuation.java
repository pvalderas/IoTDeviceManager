package es.upv.pros.pvalderas.microservice.iotdevice.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Actuation {
	
	public String name();
	public Class resultType();
	public String description();

}