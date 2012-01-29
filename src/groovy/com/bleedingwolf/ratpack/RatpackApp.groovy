package com.bleedingwolf.ratpack

import com.bleedingwolf.ratpack.routing.Route
import com.bleedingwolf.ratpack.routing.RoutingTable
import org.slf4j.LoggerFactory

class RatpackApp {

	final logger = LoggerFactory.getLogger(getClass())

    Map<String, RoutingTable> handlers = [
        'GET': new RoutingTable(),
        'POST': new RoutingTable(),
    ]

    def config = [
        port: 5000
    ]

    def set = { setting, value ->
        config[setting] = value
    }

    void register(List methods, path, handler) {
        methods.each {
            register(it, path, handler)
        }
    }

    void register(String method, path, handler) {
        method = method.toUpperCase()

        if (path instanceof String) {
            path = new Route(path)
        }

        def routingTable = handlers[method]
        if (routingTable == null) {
            routingTable = new RoutingTable()
            handlers[method] = routingTable
        }
        routingTable.attachRoute path, handler
    }

    Closure getHandler(method, subject) {
        return handlers[method.toUpperCase()]?.route(subject)
    }

    def head = { path, handler ->
        register('HEAD', path, handler)
    }

    def get = { path, handler ->
        register('GET', path, handler)
    }

    def post = { path, handler ->
        register('POST', path, handler)
    }

	  def put = { path, handler ->
        register('PUT', path, handler)
    }

    def delete = { path, handler ->
        register('DELETE', path, handler)
    }

	def register = { method, path, handler ->
		if (method instanceof List) {
			register(method as List, path, handler)
		} else {
			register(method as String, path, handler)
		}
	}

    void prepareScriptForExecutionOnApp(String scriptName){
      prepareScriptForExecutionOnApp(new File(scriptName))
    }

    void prepareScriptForExecutionOnApp(File scriptFile){
        GroovyScriptEngine gse = new GroovyScriptEngine(scriptFile.canonicalPath.replace(scriptFile.name,''))
        def app = this
        Binding binding = new Binding()
        binding.setVariable('head', app.head)
        binding.setVariable('get', app.get)
        binding.setVariable('post', app.post)
        binding.setVariable('put', app.put)
        binding.setVariable('delete', app.delete)
        binding.setVariable('set', app.set)
        binding.setVariable('register', app.register)
        gse.run scriptFile.name, binding
    }
}
