package com.bleedingwolf.ratpack

import groovy.text.SimpleTemplateEngine
import javax.servlet.http.HttpServletRequest

class TemplateRenderer {

    String templateRoot

    TemplateRenderer(tr) {
        templateRoot = tr
    }

    String render(templateName, context=[:]) {
        String text
        try {
            text = loadTemplateText(templateName)
        } catch (IOException ex) {
            text = loadResource('com/bleedingwolf/ratpack/exception.html').text
            context = [
                title: 'Template Not Found',
                message: 'Template Not Found',
                metadata: ['Template Name': templateName],
                stacktrace: ""
            ]
        }

        renderTemplate(text, context)
    }

    String renderError(Map context) {
        String text = loadResource('com/bleedingwolf/ratpack/exception.html').text

        renderTemplate(text, context)
    }

    String renderException(Throwable ex, HttpServletRequest req) {
        def stackInfo = decodeStackTrace(ex)

        String text = loadResource('com/bleedingwolf/ratpack/exception.html').text
        Map context = [
            title: ex.getClass().name,
            message: ex.message,
            metadata: [
                'Request Method': req.method.toUpperCase(),
                'Request URL': req.requestURL,
                'Exception Type': ex.getClass().name,
                'Exception Location': "${stackInfo.rootCause.fileName}, line ${stackInfo.rootCause.lineNumber}",
            ],
            stacktrace: stackInfo.html
        ]

        renderTemplate(text, context)
    }

    protected String loadTemplateText(templateName) {
        File file = new File(templateRoot, templateName)
        if (file.exists()) {
            StringBuilder text = new StringBuilder()
            file.eachLine { String line -> text.append(line).append '\n' }
            return text.toString()
        }

        InputStream resource = loadResource(templateName)
        if (!resource) {
            throw new FileNotFoundException(templateName)
        }

        return resource.text
    }

    protected Map decodeStackTrace(Throwable t) {
        // FIXME
        // this doesn't really make sense, but I'm not sure
        // how to create a `firstPartyPrefixes` list.
        def thirdPartyPrefixes = ['sun', 'java', 'groovy', 'org.codehaus', 'org.mortbay']

        StringBuilder html = new StringBuilder(t.toString())
        html.append '\n'
        StackTraceElement rootCause

        for (StackTraceElement ste : t.getStackTrace()) {
            if (thirdPartyPrefixes.any { ste.className.startsWith(it) }) {
                html.append "<span class='stack-thirdparty'>        at ${ste}\n</span>"
            } else {
                html.append "        at ${ste}\n"
                if (null == rootCause) rootCause = ste
            }
        }

        return [html: html.toString(), rootCause: rootCause]
    }

    protected String renderTemplate(String text, Map context) {
        new SimpleTemplateEngine().createTemplate(text).make(context).toString()
    }

    protected InputStream loadResource(String path) {
        Thread.currentThread().contextClassLoader.getResourceAsStream(path)
    }
}
