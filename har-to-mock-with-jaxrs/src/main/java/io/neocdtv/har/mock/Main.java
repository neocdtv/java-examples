package io.neocdtv.har.mock;


import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.DefaultServlet;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.grizzly.utils.ArraySet;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletRegistration;
import javax.servlet.filter.logging.LoggingFilter;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;

/**
 * @author xix
 */
public class Main {

	// TODO: switch from payara maven depedency to pure grizzly with jersey?
	// TODO: move logger config to log4j.properties, maybe move to logback
	// TODO: update request respones logging format?
	//  - why: human readable
	//  - would be nice to have a format which is human and machine readable
	private static final int HTTP_PORT = 9998;
	private static final String HTTP_ADDRESS = "http://localhost";
	private static final String APP_PATH = "/har-to-mock-with-jaxrs";
	private static final String REST_PATH = "/api";
	private static final String ROOT_PATH = "/*";

	public static final String PROPERTY_HAR_PATH = "harPath";
	public static String HAR_PATH = "C:\\Temp\\ucp-har.json"; // hardcoded default

	public static void main(String[] args) throws IOException {

		configureLogger();

		final String harPath = System.getProperty(PROPERTY_HAR_PATH);
		if (harPath != null) {
			HAR_PATH = harPath;
		}

		WebappContext webappContext = new WebappContext("grizzly web context", APP_PATH);

		registerLoggingFilter(webappContext);
		registerJerseyServlet(webappContext);
		// TODO: fix static files. Working when started from IDE, not working if packed into jar
		//registerStaticFileHandler(webappContext);

		HttpServer httpServer = createHttpServer();
		webappContext.deploy(httpServer);
		httpServer.start();
		System.in.read();
	}

	private static HttpServer createHttpServer() {
		URI baseUri = UriBuilder.fromUri(HTTP_ADDRESS).port(HTTP_PORT).build();
		HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(baseUri, false);
		// this is necessary to allow not specified (specified where?) requests e.g. DELETE with payload
		httpServer.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
		return httpServer;
	}

	private static void configureLogger() {
		ConsoleAppender console = new ConsoleAppender(); //create appender
		//configure the appender
		String PATTERN = "%d [%p|%c|%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.activateOptions();
		Logger.getRootLogger().addAppender(console);
	}

	private static void registerLoggingFilter(final WebappContext webappContext) {
		FilterRegistration loggingFilterReg = webappContext.addFilter("LoggingFilter", LoggingFilter.class);
		loggingFilterReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, ROOT_PATH);
	}

	private static void registerJerseyServlet(WebappContext webappContext) {
		ServletRegistration servletRegistration = webappContext.addServlet("Jersey", ServletContainer.class);
		servletRegistration.addMapping(REST_PATH + "/*");
		servletRegistration.setInitParameter("jersey.config.server.provider.packages", Main.class.getPackage().getName());
	}

	private static void registerStaticFileHandler(WebappContext webappContext) {
	      /*
	          the standard way to do it is httpServer.getServerConfiguration().addHttpHandler(new CLStaticHttpHandler(Main.class.getClassLoader()), APP_PATH);
            but the standard way won't allow you to use the logging filter with static files
         */
		ArraySet<File> set = new ArraySet<>(File.class);

		final String pathToTargetDirectory = Main.class.getClassLoader().getResource(".").getPath();
		Logger.getLogger(Main.class).info(pathToTargetDirectory);
		final File docRoot = new File(pathToTargetDirectory + "/static");
		set.add(docRoot);
		ServletRegistration defaultServletReg = webappContext.addServlet(DefaultServlet.class.getName(), new DefaultServlet(set) {
		});
		defaultServletReg.addMapping(ROOT_PATH);
	}
}