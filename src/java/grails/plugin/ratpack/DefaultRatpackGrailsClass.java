package grails.plugin.ratpack;

import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;

import org.codehaus.groovy.grails.commons.AbstractInjectableGrailsClass;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Default implementation of <code>RatpackGrailsClass</code>.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class DefaultRatpackGrailsClass extends AbstractInjectableGrailsClass
       implements RatpackGrailsClass {

	/**
	 * Default constructor.
	 * @param wrappedClass
	 */
	public DefaultRatpackGrailsClass(Class<?> wrappedClass) {
		super(wrappedClass, RatpackArtefactHandler.TYPE);
	}

	@Override
	public MetaClass getMetaClass() {
		// Workaround for http://jira.codehaus.org/browse/GRAILS-4542
		return GroovySystem.getMetaClassRegistry().getMetaClass(DefaultRatpackGrailsClass.class);
	}

	@Override
	public Object newInstance() {
		Object instance = super.newInstance();
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext)grailsApplication.getMainContext();
		ctx.getBeanFactory().autowireBeanProperties(instance,
				AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
		return instance;
	}
}
