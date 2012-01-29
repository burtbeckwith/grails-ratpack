package com.bleedingwolf.ratpack.routing

import java.util.regex.Pattern

class Route {

	String path = ""
	String regex = ""
	def names = []

	Route(p) {
		path = p
		parsePath()
	}

	def parsePath() {
		regex = path

		Pattern.compile("(:\\w+)").matcher(path).each {
			def name = it[1][1..-1]
			regex = regex.replaceFirst(it[0], "([^/?&#]+)")
			names << name
		}
	}

	def match(url) {
		def params = [:]
		def matcher = Pattern.compile(regex).matcher(url)
		if (matcher.matches()) {
			names.eachWithIndex { it, i ->
				params[it] = matcher[0][i+1]
			}
			return params
		}

		return null
	}
}
