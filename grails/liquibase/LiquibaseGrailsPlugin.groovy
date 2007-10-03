
class LiquibaseGrailsPlugin {
	def version = 0.9
	def author = "Nathan Voxland"
	def authorEmail = "nathan@voxland.net"
	def title = "This plugin adds ActiveRecord::Migration-like functionality via the LiquiBase project."
	def documentation = "http://www.liquibase.org"

//   def version = grails.util.GrailsUtil.getGrailsVersion()
	def dependsOn = [dataSource:version]

	def doWithSpring = {
		// TODO Implement runtime spring config (optional)
	}   
	def doWithApplicationContext = { applicationContext ->
		// TODO Implement post initialization spring config (optional)		
	}
	def doWithWebDescriptor = { xml ->
		// TODO Implement additions to web.xml (optional)
	}	                                      
	def doWithDynamicMethods = { ctx ->
		// TODO Implement additions to web.xml (optional)
	}	
	def onChange = { event ->
		// TODO Implement code that is executed when this class plugin class is changed  
		// the event contains: event.application and event.applicationContext objects
	}                                                                                  
	def onApplicationChange = { event ->
		// TODO Implement code that is executed when any class in a GrailsApplication changes
		// the event contain: event.source, event.application and event.applicationContext objects
	}
}
