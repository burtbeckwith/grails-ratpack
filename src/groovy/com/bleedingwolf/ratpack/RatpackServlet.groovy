package com.bleedingwolf.ratpack

import javax.activation.MimetypesFileTypeMap
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RatpackServlet extends HttpServlet {

	protected final Logger logger = LoggerFactory.getLogger(getClass())

	protected int majorServletVersion

	RatpackApp app
	MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap()

	void init() {

		majorServletVersion = servletContext.majorVersion

		if (app == null) {
			String appScriptName = getServletConfig().getInitParameter("app-script-filename")
			String fullScriptPath = getServletContext().getRealPath("WEB-INF/lib/${appScriptName}")

			logger.info('Loading app from script "{}"', appScriptName)
			loadAppFromScript(fullScriptPath)
		}
		mimetypesFileTypeMap.addMimeTypes(
			Thread.currentThread().contextClassLoader.getResourceAsStream(
				'com/bleedingwolf/ratpack/mime.types').text)
	}

	protected void loadAppFromScript(String filename) {
		app = new RatpackApp()
		app.prepareScriptForExecutionOnApp(filename)
	}

	void service(HttpServletRequest req, HttpServletResponse res) {

		String verb = req.method
		String path = req.pathInfo ?: '/'

		def renderer = new TemplateRenderer(app.config.templateRoot)

		def handler = app.getHandler(verb, path)
		def output = ''

		if (handler) {

			handler.delegate.renderer = renderer
			handler.delegate.request = req
			handler.delegate.response = res

			try {
				output = handler.call()
			}
			catch(RuntimeException ex) {

				logger.error('Handling {} {}', [ verb, path, ex] as Object[])

				res.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
				output = renderer.renderException(ex, req)
			}

		} else if (app.config.public && staticFileExists(path)) {

			output = serveStaticFile(res, path)

		} else {
			res.status = HttpServletResponse.SC_NOT_FOUND
			output = renderer.renderError(
				title: 'Page Not Found',
				message: 'Page Not Found',
				metadata: [
					'Request Method': req.method.toUpperCase(),
					'Request URL': req.requestURL,
				]
			)
		}

		output = convertOutputToByteArray(output)

		int contentLength = output.length
		res.setHeader('Content-Length', contentLength.toString())

		def stream = res.getOutputStream()
		stream.write(output)
		stream.flush()
		stream.close()

		if (majorServletVersion > 2) {
			logger.info("[   ${res.status}] ${verb} ${path}")
		}
		else {
			logger.info("${verb} ${path}")
		}
	}

	protected boolean staticFileExists(path) {
		!path.endsWith('/') && staticFileFrom(path) != null
	}

	protected byte[] serveStaticFile(response, path) {
		URL url = staticFileFrom(path)
		response.setHeader('Content-Type', mimetypesFileTypeMap.getContentType(url.toString()))
		url.openStream().bytes
	}

	protected URL staticFileFrom(path) {
		def publicDir = app.config.public
		def fullPath = [publicDir, path].join(File.separator)
		def file = new File(fullPath)

		if (file.exists()) return file.toURI().toURL()

		try {
			return Thread.currentThread().contextClassLoader.getResource([publicDir, path].join('/'))
		} catch(Exception e) {
			return null
		}
	}

	private byte[] convertOutputToByteArray(output) {
		if (output instanceof CharSequence) {
			output = output.toString().getBytes()
		}
		return output
	}

	static void serve(theApp) {
		// Runs this RatpackApp in a Jetty container
		def servlet = new RatpackServlet()
		servlet.app = theApp

		theApp.logger.info('Starting Ratpack app with config:\n{}', theApp.config)

		def forName = { String className -> Class.forName(className, true, Thread.currentThread().contextClassLoader) }
		def Server = forName('org.mortbay.jetty.Server')
		def Context = forName('org.mortbay.jetty.servlet.Context')
		def ServletHolder = forName('org.mortbay.jetty.servlet.ServletHolder')

		def jettyServer = Server.newInstance(theApp.config.port)
		def root = Context.newInstance(jettyServer, "/", Context.SESSIONS)
		root.addServlet(ServletHolder.newInstance(servlet), "/*")
		jettyServer.start()
	}
}
