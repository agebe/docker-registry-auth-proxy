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

import io.github.agebe.rproxy.ProxyPath;
import io.github.agebe.rproxy.RequestStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ProxyPath("v2/*/tags/*")
@ProxyPath("v2/*/manifests/*")
@ProxyPath("v2/*/blobs/*")
public class PH3RepoAccessHandler extends PHAbstractHandler {

  private static final Logger log = LoggerFactory.getLogger(PH3RepoAccessHandler.class);

  @Override
  public RequestStatus handle(HttpServletRequest request, HttpServletResponse response) {
    User user = (User)request.getAttribute("user");
    if(user == null) {
      log.warn("unauthorized request '{}', user is null", request.getRequestURI());
      return unauthorized(response);
    }
    String repo = getRepoName(request.getRequestURI());
    log.debug("request '{}', repo '{}'", request.getRequestURI(), repo);
    if(StringUtils.isBlank(repo)) {
      log.info("deny request, repository is blank, request uri '{}'", request.getRequestURI());
      return denied(response, "access denied");
    }
    if(user.canAccessRepo(repo)) {
      String url = Config.getConfiguration().getRegistry() + request.getRequestURI();
      return forwardStreamResult(url, request, response);
    } else {
      log.info("deny request, user '{}' has insufficient privilege to access repository '{}'", user.getName(), repo);
      return denied(response, "access denied, repository '%s".formatted(repo));
    }
  }

  private String getRepoName(String uri) {
    String[] split = StringUtils.split(uri, '/');
    int endIdx = findEndIndex(split);
    if(endIdx == -1) {
      return null;
    }
    return StringUtils.join(split, '/', 1, endIdx);
  }

  private int findEndIndex(String[] split) {
    for(int i=split.length-1;i>=0;i--) {
      String s = split[i];
      if(StringUtils.equalsAny(s, "tags", "blobs", "manifests")) {
        return i;
      }
    }
    return -1;
  }

}
