package com.bleedingwolf.ratpack

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RatpackRequestDelegate {

	def renderer

	def params = [:]
	def urlparams = [:]
	def headers = [:]

	def request
	def response
	def requestParamReader = new RatpackRequestParamReader()
	final Logger logger = LoggerFactory.getLogger(getClass())

	void setHeader(name, value) {
		response.setHeader(name.toString(), value.toString())
	}

	void setRequest(req) {
		request = req
		params.putAll(requestParamReader.readRequestParams(req))

		req.headerNames.each { header ->
			def values = []
			req.getHeaders(header).each { values << it }
			if (values.size == 1) {
				values = values.get(0)
			}
			headers[header.toLowerCase()] = values
		}
	}

	String render(templateName, context=[:]) {
		if (!response.containsHeader('Content-Type')) {
			setHeader('Content-Type', 'text/html')
		}
		renderer.render(templateName, context)
	}

	void contentType(String contentType) {
		setHeader("Content-Type", contentType)
	}

	String renderJson(o) {
		if (!response.containsHeader("Content-Type")) {
			contentType("application/json")
		}

		def JSONObject = Class.forName('org.json.JSONObject', true, Thread.currentThread().contextClassLoader)
		JSONObject.newInstance(o).toString()
	}
}
