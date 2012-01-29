package grails.plugin.ratpack

import org.springframework.context.ApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils as WACU

import com.bleedingwolf.ratpack.RatpackServlet

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class GrailsRatpackServlet extends RatpackServlet {

	@Override
	void init() {
		ApplicationContext ctx = WACU.getRequiredWebApplicationContext(servletContext)
		app = ctx.ratpackApp
		super.init()
	}
}
