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

import io.github.agebe.rproxy.AbstractHttpRequestHandler;
import io.github.agebe.rproxy.MatchType;
import io.github.agebe.rproxy.ProxyPath;
import io.github.agebe.rproxy.RequestStatus;
import io.github.agebe.rproxy.ReverseProxyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ProxyPath(value = "v2", type = MatchType.EQUALS)
public class PH3BaseRedirectHandler extends AbstractHttpRequestHandler {

  @Override
  public RequestStatus handle(HttpServletRequest request, HttpServletResponse response) {
    try {
      response.sendRedirect("/v2/");
      return RequestStatus.COMPLETED;
    } catch(Exception e) {
      throw new ReverseProxyException(e);
    }
  }

}

