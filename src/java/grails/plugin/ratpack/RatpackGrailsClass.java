package grails.plugin.ratpack;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.InjectableGrailsClass;

/**
 * GrailsClass interface for Ratpack definitions.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public interface RatpackGrailsClass extends InjectableGrailsClass {
	void setGrailsApplication(GrailsApplication application);
}
