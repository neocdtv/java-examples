package io.neocdtv.dynamic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.ext.ContextResolver;

/**
 * TODO javadoc.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class JacksonProvider implements ContextResolver<ObjectMapper> {

  public static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

  static {
    
    DEFAULT_OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    DEFAULT_OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
  }

  public JacksonProvider() {
  }

  @Override
  public ObjectMapper getContext(final Class<?> type) {
    return DEFAULT_OBJECT_MAPPER;
  }
}
