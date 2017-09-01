/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.har.mock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;
import org.apache.commons.lang3.StringUtils;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static io.neocdtv.har.mock.Main.HAR_PATH;

/**
 * @author xix
 */
@Path(MockResource.PATH + "/{path: .*}")
public class MockResource {

  // TODO: at some point add other http methods
  final static String PATH = "/mocks";
  private Har har;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @GET
  public Response get(@Context UriInfo uriInfo) throws HarReaderException {
    loadHar(HAR_PATH);
    final HarEntry entry = findEntry(har, HttpMethod.GET, "/" + uriInfo.getPath());
    final Response.ResponseBuilder entity =
        Response.status(entry.getResponse().getStatus()).
            entity(entry.getResponse().getContent().getText()).type(entry.getResponse().getContent().getMimeType());
    return entity.build();
  }

  @POST
  public Response post(@Context UriInfo uriInfo, final String payload) throws HarReaderException {
    return otherCall(uriInfo, payload, HttpMethod.POST);
  }

  @DELETE
  public Response delete(@Context UriInfo uriInfo, final String payload) throws HarReaderException {
    return otherCall(uriInfo, payload, HttpMethod.DELETE);
  }

  public Response otherCall(@Context UriInfo uriInfo, final String payload, final String method) throws HarReaderException {
    loadHar(HAR_PATH);
    final HarEntry entry = findEntry(har, method, "/" + uriInfo.getPath(), payload);
    final Response.ResponseBuilder entity
        = Response.status(entry.getResponse().getStatus()).
        entity(entry.getResponse().getContent().getText()).type(entry.getResponse().getContent().getMimeType());
    return entity.build();
  }

  private boolean isPayload(String payload, HarEntry entry) {
    try {
      final JsonNode source = objectMapper.readTree(payload);
      final JsonNode target = objectMapper.readTree(entry.getRequest().getPostData().getText());
      JsonNode patch = JsonDiff.asJson(source, target);
      return !patch.iterator().hasNext();
    } catch (IOException e) {
      return false;
    }
  }

  @Deprecated
  private boolean isPayloadOld(String payload, HarEntry entry) {
    try {
      JSONAssert.assertEquals(payload, entry.getRequest().getPostData().getText(), false);
      return true;
    } catch (AssertionError ae) {
      return false;
    } catch (Exception e) {
      return false;
    }
  }

  private HarEntry findEntry(final Har har, final String method, final String path, final String payload) {
    return har.getLog().getEntries().stream()
        .filter(entry
            -> entry.getRequest().getMethod().toString().equals(method)
            && extractPath(entry.getRequest().getUrl()).equals(removeResourcePathFromRequestedPath(path))
            && isPayload(payload, entry)
        ).findFirst().get();
  }

  private HarEntry findEntry(final Har har, final String method, final String path) {
    return har.getLog().getEntries().stream()
        .filter(entry
            -> entry.getRequest().getMethod().toString().equals(method) && extractPath(entry.getRequest().getUrl()).equals(removeResourcePathFromRequestedPath(path))
        ).findFirst().get();
  }

  private String removeResourcePathFromRequestedPath(final String path) {
    return path.replace(PATH, StringUtils.EMPTY);
  }

  private String extractPath(final String url) {
    try {
      return new URL(url).getPath();
    } catch (MalformedURLException e) {
      return null;
    }
  }

  private void loadHar(final String path) throws HarReaderException {
    HarReader harReader = new HarReader();
    if (har == null) {
      har = harReader.readFromFile(new File(path));
    }
  }
}