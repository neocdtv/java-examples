/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.dynamic;

import io.swagger.models.Operation;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.spi.Container;

/**
 *
 * @author xix
 */
@Path("swagger.json")
@Produces(MediaType.APPLICATION_JSON)
public class SwaggerResource {

  @Context
  private ServletContext servletContext;

  private static Container container;

  @GET
  public Swagger generateSwagger() {
    Swagger swagger = new Swagger();
    final Set<Resource> resources = container.getConfiguration().getResources();

    final String contextPath = servletContext.getContextPath();
    swagger.setBasePath(contextPath);
    swagger.addScheme(Scheme.HTTP);

    resources.forEach(resource -> {
      final String path = resource.getPath();

      final List<ResourceMethod> resourceMethods = resource.getResourceMethods();
      resourceMethods.forEach(method -> {
        final String httpMethod = method.getHttpMethod();

        final io.swagger.models.Path swaggerPath = new io.swagger.models.Path();
        final Operation operation = new Operation();

        final List<MediaType> consumedTypes = method.getConsumedTypes();
        consumedTypes.forEach(consumedType -> {
          operation.addConsumes(consumedType.toString());
        });

        final List<MediaType> producedTypes = method.getProducedTypes();
        producedTypes.forEach(producedType -> {
          operation.addConsumes(producedType.toString());
        });
        addOperation(swaggerPath, operation, httpMethod);
        swagger.path(path, swaggerPath);
      });
    });
    return swagger;
  }

  public static void setContainer(Container container) {
    SwaggerResource.container = container;
  }

  public void addOperation(final io.swagger.models.Path swaggerPath,
          final Operation operation, final String httpMethod) {
    switch (httpMethod) {
      case HttpMethod.GET: {
        swaggerPath.get(operation);
        return;
      }
      case HttpMethod.POST: {
        swaggerPath.post(operation);
        return;
      }
    }
    throw new RuntimeException("HttpMethod " + httpMethod + " not supported");
  }
}
