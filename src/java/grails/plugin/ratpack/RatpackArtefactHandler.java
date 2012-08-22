package grails.plugin.ratpack;

import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;

import org.codehaus.groovy.grails.commons.AbstractInjectableGrailsClass;
import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.InjectableGrailsClass;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Ratpack handler.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class RatpackArtefactHandler extends ArtefactHandlerAdapter {

	/** The artefact type. */
	public static final String TYPE = "Ratpack";

	/**
	 * Default constructor.
	 */
	public RatpackArtefactHandler() {
		super(TYPE, RatpackGrailsClass.class, DefaultRatpackGrailsClass.class, TYPE);
	}

	/**
	 * GrailsClass interface for Ratpack definitions.
	 */
	public static interface RatpackGrailsClass extends InjectableGrailsClass {
		void setGrailsApplication(GrailsApplication application);
	}

	/**
	 * Default implementation of <code>RatpackGrailsClass</code>.
	 *
	 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
	 */
	public static class DefaultRatpackGrailsClass extends AbstractInjectableGrailsClass
	       implements RatpackGrailsClass {

		// shadows base class field in 2.0, necessary for pre-2.0
		@SuppressWarnings("hiding")
		private GrailsApplication grailsApplication;

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
			autowireBeanProperties(instance);
			return instance;
		}

		protected void autowireBeanProperties(Object instance) {
			ConfigurableApplicationContext ctx = (ConfigurableApplicationContext)grailsApplication.getMainContext();
			ctx.getBeanFactory().autowireBeanProperties(instance,
					AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
		}

		public void setGrailsApplication(GrailsApplication application) {
			grailsApplication = application;
		}
	}
}
