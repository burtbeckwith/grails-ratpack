import com.bleedingwolf.ratpack.RatpackApp

import grails.plugin.ratpack.GrailsRatpackServlet
import grails.plugin.ratpack.RatpackArtefactHandler
import grails.plugin.ratpack.RatpackGrailsClass
import grails.util.GrailsUtil

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class RatpackGrailsPlugin {

	String version = '1.0.1'
	String grailsVersion = '1.3.3 > *'
	String title = 'Ratpack'
	String author = 'Burt Beckwith'
	String authorEmail = 'beckwithb@vmware.com'
	String description = 'Ratpack Integration'
	String documentation = 'http://grails.org/plugin/ratpack'

	def pluginExcludes = [
		'docs/**',
		'src/docs/**'
	]

	def artefacts = [RatpackArtefactHandler]
	def watchedResources = [
		'file:./grails-app/ratpack/**/*Ratpack.groovy',
		'file:./plugins/*/grails-app/ratpack/**/*Ratpack.groovy']

	String license = 'APACHE'
	def issueManagement = [system: 'JIRA', url: 'http://jira.grails.org/browse/GPRATPACK']
	def scm = [url: 'https://github.com/burtbeckwith/grails-ratpack']

	private Logger log = Logger.getLogger('grails.plugin.ratpack.RatpackGrailsPlugin')

	def doWithWebDescriptor = { xml ->

		def conf = application.config.grails.plugin.ratpack

		def listeners = xml.'listener'

		listeners[listeners.size() - 1] + {
			'servlet' {
				'servlet-name'('GrailsRatpackServlet')
				'servlet-class'(GrailsRatpackServlet.name)
			}

			String urlPattern = conf.urlPattern ?: '/ratpack/*'
			'servlet-mapping' {
				'servlet-name'('GrailsRatpackServlet')
				'url-pattern'(urlPattern)
			}
		}
	}

	def doWithSpring = {
		ratpackApp(RatpackApp)
	}

	def doWithApplicationContext = { ctx ->
		rebuildUrlMappings ctx.ratpackApp, ctx.grailsApplication
	}

	def onChange = { event ->

		if (!event.source || !event.application.isRatpackClass(event.source)) {
			return
		}

		RatpackApp ratpackApp = event.ctx.ratpackApp
		rebuildUrlMappings ratpackApp, event.application
	}

	private void rebuildUrlMappings(RatpackApp ratpackApp, GrailsApplication application) {
		ratpackApp.config.clear()

		def conf = application.config.grails.plugin.ratpack
		if (conf.templateRoot) {
			ratpackApp.config.templateRoot = conf.templateRoot
		}

		ratpackApp.handlers.clear()
		for (RatpackGrailsClass rgc in application.ratpackClasses) {
			try {
				rgc.grailsApplication = application
				if (log.debugEnabled) {
					log.debug "Processing $rgc"
				}
				Closure closure = rgc.newInstance().urls
				closure.delegate = ratpackApp
				closure.call()
			}
			catch (e) {
				GrailsUtil.deepSanitize e
				log.error "Problem processing $rgc.clazz.name: $e.message", e
			}
		}
	}
}
