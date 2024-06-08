/*
 * Copyright 2024 Andre Gebers
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.agebe.docker.proxy;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.agebe.rproxy.AbstractHttpRequestHandler;
import io.github.agebe.rproxy.RequestStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class PHAbstractHandler extends AbstractHttpRequestHandler {

  private static final Logger log = LoggerFactory.getLogger(PHAbstractHandler.class);

  public static record DockerRegistryError(String code, String message, String detail) {};
  public static record DockerRegistryErrors(List<DockerRegistryError> errors) {};

  private Gson gson() {
    return new GsonBuilder()
        .disableHtmlEscaping()
        .disableJdkUnsafe()
        .serializeNulls()
        .create();
  }

  protected RequestStatus unauthorized(HttpServletResponse response) {
    try {
      response.setHeader("Content-Type", "application/json; charset=utf-8");
      response.setHeader("Docker-Distribution-Api-Version", "registry/2.0");
      response.setHeader("WWW-Authenticate", "Basic realm=\"Registry Realm\"");
      response.setHeader("X-Content-Type-Options", "nosniff");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().println(gson().toJson(new DockerRegistryErrors(
          List.of(new DockerRegistryError("UNAUTHORIZED", "authentication required", null)))));
      return RequestStatus.COMPLETED;
    } catch(Exception e) {
      throw new DockerProxyException("failed to send unauthorized", e);
    }
  }

  protected RequestStatus denied(HttpServletResponse response, String message) {
    try {
      response.setHeader("Content-Type", "application/json; charset=utf-8");
      response.setHeader("Docker-Distribution-Api-Version", "registry/2.0");
      response.setHeader("X-Content-Type-Options", "nosniff");
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.getWriter().println(gson().toJson(new DockerRegistryErrors(
          List.of(new DockerRegistryError("DENIED", message, null)))));
      return RequestStatus.COMPLETED;
    } catch(Exception e) {
      throw new DockerProxyException("failed to send denied", e);
    }
  }

  protected void traceRequest(HttpServletRequest req) {
    if(log.isTraceEnabled()) {
      log.trace("content length '{}'", req.getContentType());
      log.trace("content type '{}'", req.getContentType());
      log.trace("context path '{}'", req.getContextPath());
      log.trace("method '{}'", req.getMethod());
      log.trace("path info '{}'", req.getPathInfo());
      log.trace("path translated '{}'", req.getPathTranslated());
      log.trace("protocol '{}'", req.getProtocol());
      log.trace("query string '{}'", req.getQueryString());
      log.trace("request URI '{}'", req.getRequestURI());
      log.trace("scheme '{}'", req.getScheme());
      log.trace("servlet path '{}'", req.getServletPath());
      log.trace("isSecure '{}'", req.isSecure());
      sorted(req.getHeaderNames())
      .sorted()
      .forEachOrdered(h -> {
        String values = sorted(req.getHeaders(h)).collect(Collectors.joining(", "));
        log.trace("header '{}', values '{}'", h, values);
      });
      sorted(req.getAttributeNames())
      .sorted()
      .forEachOrdered(attr -> {
        log.trace("attribute '{}', value '{}'", attr, req.getAttribute(attr));
      });
    }
  }

  private <T> Stream<T> sorted(Enumeration<T> enumeration) {
    return Collections.list(enumeration).stream().sorted();
  }

}
