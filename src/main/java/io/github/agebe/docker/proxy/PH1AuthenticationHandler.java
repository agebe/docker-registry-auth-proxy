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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.agebe.rproxy.RequestStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PH1AuthenticationHandler extends PHAbstractHandler {

  private static final Logger log = LoggerFactory.getLogger(PH1AuthenticationHandler.class);

  @Override
  public RequestStatus handle(HttpServletRequest request, HttpServletResponse response) {
    try {
      traceRequest(request);
      String auth = request.getHeader("authorization");
      if(StringUtils.isBlank(auth)) {
        log.info("unauthorized request '{}', no authorization header", request.getRequestURI());
        return unauthorized(response);
      }
      String method = StringUtils.substringBefore(auth, " ");
      String base64 = StringUtils.substringAfter(auth, " ");
      if(!"basic".equalsIgnoreCase(method)) {
        log.info("unauthorized request '{}', only basic authorization method supported, method '{}'",
            request.getRequestURI(), method);
        return unauthorized(response);
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
        log.info("unauthorized request '{}', unknown user or wrong password, user '{}'", request.getRequestURI(), name);
        return unauthorized(response);
      } else {
        request.setAttribute("user", user);
        return RequestStatus.CONTINUE;
      }
    } catch(Exception e) {
      throw new DockerProxyException("failed to process authentication", e);
    }
  }

}
