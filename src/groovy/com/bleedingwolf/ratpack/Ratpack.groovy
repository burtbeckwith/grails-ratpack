package com.bleedingwolf.ratpack

class Ratpack {

    static RatpackApp app(Closure closure) {
        def theApp = new RatpackApp()
        closure.delegate = theApp
        closure.call()
        return theApp
    }
}
