import grails.util.Holder
import grails.util.Holders
import org.twitter4j.grails.plugin.TwitterUserStreamFactoryBean
import twitter4j.TwitterStream

class Twitter4jGrailsPlugin {

    // the plugin version (Follows Twitter4j library version + number for plugin revision)
    def version = "4.0.4.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.3 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def title = "Twitter4j for Grails"
    def author = "Soeren Berg Glasius, Arthur Neves, Rubén Salinas"
    def authorEmail = "soeren@glasius.dk, arthurnn@gmail.com, rubensalinasgarcia@gmail.com"
    def description = 'Wraps the Twitter4j API by Groovy delegation (see http://www.twitter4j.org for API documentation and examples)'

    def documentation =  "http://sbglasius.github.io/grails-twitter4j/"

    def license = "APACHE"

    def developers = [
            [name: 'Arthur Neves', email: 'arthurnn@gmail.com'],
            [name: "Daniel Gerbaudo", email: "info@danielgerbaudo.com"],
            [name: "Ricardo Vilella", email: "vilellaricardo@gmail.com"]
    ]

    def issueManagement = [ system: "GitHub", url: "https://github.com/sbglasius/grails-twitter4j" ]
    def scm = [ url: "https://github.com/sbglasius/grails-twitter4j/settings" ]

    def doWithWebDescriptor = { xml ->
    }

    def doWithSpring = {

        def prop = application.config.twitter.userListenerClass
        if (prop) {
            def clazz = application.classLoader.loadClass(prop)
            log.debug "Register twitter user listenet[${clazz}]"
            twitterUserListener(clazz)
        } else {
            log.debug "There is no Tweeter User Listener to register."
        }

        twitterStream(TwitterUserStreamFactoryBean) {
            configuration = application.config.twitter.'default'
        }
    }

    def doWithDynamicMethods = { ctx ->
    }

    def doWithApplicationContext = { applicationContext ->
        scheduleStream(applicationContext)
    }

    def onChange = { event ->
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        def ctx = event.ctx
        if (!ctx) {
            log.error("Application context not found. Cannot execute shutdown code.")
            return
        }
        def twitterStream = ctx.getBean("twitterStream")
        if (twitterStream) {
            twitterStream.shutdown()
        }
    }

    private scheduleStream = { ctx ->
        if (ctx.containsBean('twitterUserListener')) {
            def twitterUserListener = ctx.getBean('twitterUserListener')
            TwitterStream twitterStream = ctx.getBean('twitterStream')
            twitterStream.addListener(twitterUserListener);
            twitterStream.user();

            log.debug "Twitter User Stream Created.."
        }
    }

}