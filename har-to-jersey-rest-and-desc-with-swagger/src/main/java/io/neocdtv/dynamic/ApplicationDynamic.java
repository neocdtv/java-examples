/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.dynamic;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarRequest;
import de.sstoehr.harreader.model.HarResponse;
import de.sstoehr.harreader.model.HttpMethod;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.container.ContainerRequestContext;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

public class ApplicationDynamic extends ResourceConfig {

  //private final String DEFAULT_HAR_PATH = "C:\\Temp\\ucp-har.json";
  private final String DEFAULT_HAR_PATH = "/home/xix/Desktop/ucp-har.json";

  public ApplicationDynamic() throws HarReaderException {

    
    registerInstances(new ContainerLifecycleListener() {
      @Override
      public void onStartup(Container container) {
        SwaggerResource.setContainer(container);
      }

      @Override
      public void onReload(Container container) {
      }

      @Override
      public void onShutdown(Container container) {
      }
      
    });
    
    final String property = System.getProperty("harpath");
    System.out.println("using har: " + property);
    HarReader harReader = new HarReader();
    String harpath;
    Har har;
    if (property == null) {
      harpath = DEFAULT_HAR_PATH;
    } else {
      harpath = property;
    }
    
    har = harReader.readFromFile(new File(harpath));

    har.getLog().getEntries().forEach(entry -> {
      try {
        final HarRequest request = entry.getRequest();
        final HarResponse response = entry.getResponse();

        final Resource.Builder resourceBuilder = Resource.builder();
        final URL url = new URL(request.getUrl());
        printMethod(request.getMethod().toString(), url.getPath());
        resourceBuilder.path(url.getPath());
        final HttpMethod method = request.getMethod();
        final ResourceMethod.Builder methodBuilder
                = resourceBuilder.addMethod(method.name());

        if (method.equals(HttpMethod.GET)) {
          methodBuilder.produces(response.getContent().getMimeType())
                  .handledBy(new Inflector<ContainerRequestContext, String>() {

                    @Override
                    public String apply(ContainerRequestContext containerRequestContext) {
                      return response.getContent().getText();
                    }
                  });

          final Resource resource = resourceBuilder.build();
          registerResources(resource);
        }

        if (method.equals(HttpMethod.POST)) {
          methodBuilder
                  .consumes(request.getPostData().getMimeType())
                  .produces(response.getContent().getMimeType())
                  // if you change this line to a lamba expression
                  // you will get a 415 respons code from jersey
                  // even when content-type is correct
                  .handledBy(new Inflector<ContainerRequestContext, String>() {

                    @Override
                    public String apply(ContainerRequestContext containerRequestContext) {
                      return response.getContent().getText();
                    }
                  });

          final Resource resource = resourceBuilder.build();
          registerResources(resource);
        }
      } catch (MalformedURLException ex) {
        Logger.getLogger(ApplicationDynamic.class.getName()).log(Level.SEVERE, null, ex);
      }
    });

    register(SwaggerResource.class);
    register(JacksonFeature.class);
    register(JacksonProvider.class);
  }

  private void printMethod(final String method, final String url) {
    System.out.println("Register REST resource for " + method + " " + url);
  }
}
