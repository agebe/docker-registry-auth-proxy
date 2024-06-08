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

import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.agebe.rproxy.AbstractHttpRequestHandler;
import io.github.agebe.rproxy.RequestStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PH1AuthenticationHandler extends AbstractHttpRequestHandler {

  private static final Logger log = LoggerFactory.getLogger(PH1AuthenticationHandler.class);

  @Override
  public RequestStatus handle(HttpServletRequest request, HttpServletResponse response) {
    try {
      traceRequest(request);
      String auth = request.getHeader("authorization");
      if(StringUtils.isBlank(auth)) {
        log.info("deny request, no authorization header");
        return deny(response);
      }
      String method = StringUtils.substringBefore(auth, " ");
      String base64 = StringUtils.substringAfter(auth, " ");
      if(!"basic".equalsIgnoreCase(method)) {
        log.info("deny request, only basic authorization method supported, method '{}'", method);
        return deny(response);
      }
      String s = new String(Base64.getDecoder().decode(base64));
      String name = StringUtils.substringBefore(s, ":");
      String password = StringUtils.substringAfter(s, ":");
      User user = Config.getConfiguration()
          .getUsers()
          .stream()
          .filter(u -> u.getName().equals(name))
          .findFirst()
          .orElse(null);
      if((user == null) || !(user.getPassword().test(password))) {
        log.info("deny request, unknown user or wrong password, user '{}'", user);
        return deny(response);
      } else {
        request.setAttribute("user", user);
        return RequestStatus.CONTINUE;
      }
    } catch(Exception e) {
      log.warn("deny request, failed to process authentication", e);
      return deny(response);
    }
  }

  private void traceRequest(HttpServletRequest req) {
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
