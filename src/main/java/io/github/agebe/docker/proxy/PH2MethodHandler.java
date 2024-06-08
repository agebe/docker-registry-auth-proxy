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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.agebe.rproxy.RequestStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PH2MethodHandler extends PHAbstractHandler {

  private static final Logger log = LoggerFactory.getLogger(PH2MethodHandler.class);

  @Override
  public RequestStatus handle(HttpServletRequest request, HttpServletResponse response) {
    User user = (User)request.getAttribute("user");
    if(user == null) {
      log.warn("unauthorized request '{}', user is null", request.getRequestURI());
      return unauthorized(response);
    }
    Role role = user.getRole();
    if(role == null) {
      log.warn("deny request '{}', role is null for user '{}'", request.getRequestURI(), user.getName());
      return denied(response, "access denied");
    } else if(Role.READER.equals(role)) {
      String method = request.getMethod();
      if(StringUtils.equalsAnyIgnoreCase(method, "get", "head")) {
        return RequestStatus.CONTINUE;
      } else {
        log.info("deny request '{}' '{}', user '{}'", method, request.getRequestURI(), user.getName());
        return denied(response, "read-only access");
      }
    } else if(Role.WRITER.equals(role)) {
      String method = request.getMethod();
      if(StringUtils.equalsIgnoreCase(method, "delete")) {
        log.info("deny request '{}' '{}', user '{}'", method, request.getRequestURI(), user.getName());
        return denied(response, "delete access denied");
      } else {
        return RequestStatus.CONTINUE; 
      }
    } else if(Role.ADMIN.equals(role)) {
      return RequestStatus.CONTINUE;
    } else {
      log.warn("deny request '{}', unknown role '{}' for user '{}'", request.getRequestURI(), role, user.getName());
      return denied(response, "access denied");
    }
  }

}
