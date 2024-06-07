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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.github.agebe.rproxy.AbstractHttpRequestHandler;
import io.github.agebe.rproxy.MatchType;
import io.github.agebe.rproxy.ProxyPath;
import io.github.agebe.rproxy.RequestStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ProxyPath(value = "v2/_catalog", type = MatchType.EQUALS)
public class PH3CatalogHandler extends AbstractHttpRequestHandler {

  private static final Logger log = LoggerFactory.getLogger(PH3CatalogHandler.class);

  @Override
  public RequestStatus handle(HttpServletRequest request, HttpServletResponse response) {
    User user = (User)request.getAttribute("user");
    if(user == null) {
      log.warn("user is null");
      return deny(response);
    }
    String url = Config.getConfiguration().getRegistry()+"/v2/_catalog";
    if((user.getRepos() == null) || user.getRepos().isEmpty()) {
      return forwardStreamResult(url, request, response);
    } else {
      return forwardModifyResult(url, request, response, null, null, content -> userCatalog(user, content));
    }
  }

  private byte[] userCatalog(User user, byte[] content) {
    if((content == null) || (content.length == 0)) {
      return content;
    }
    String json = new String(content);
    log.trace(json);
    Gson gson = new Gson();
    Catalog catalog = gson.fromJson(json, Catalog.class);
    List<String> repos = catalog.getRepositories();
    if(repos == null) {
      return content;
    }
    return gson.toJson(new Catalog(repos.stream().filter(user::canAccessRepo).toList())).getBytes();
  }

}
