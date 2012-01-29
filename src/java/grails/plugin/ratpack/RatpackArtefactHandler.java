package grails.plugin.ratpack;

import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter;

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
}
