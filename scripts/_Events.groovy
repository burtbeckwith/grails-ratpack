eventCreateWarStart = { warName, stagingDir ->

	def conf = grailsApp.config.grails.plugin.ratpack
	if (conf.templateRoot) {
		// copy templates to the classpath so they're available in war mode
		ant.copy(todir: new File(stagingDir, 'WEB-INF/classes'), verbose: true) {
			fileset(dir: conf.templateRoot)
		}
	}
}
