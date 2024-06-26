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

import java.io.File;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class Init implements ServletContextListener {

  private static final Logger log = LoggerFactory.getLogger(Init.class);

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      ServletContext ctx = sce.getServletContext();
      Manifest manifest = new Manifest(ctx.getResourceAsStream("/META-INF/MANIFEST.MF"));
      Attributes attributes = manifest.getMainAttributes();
      log.info("starting {}-{}, git version {}, git hash '{}'",
          attributes.getValue("Implementation-Title"),
          attributes.getValue("Implementation-Version"),
          attributes.getValue("git-version"),
          attributes.getValue("git-hash"));
      String cfg = System.getenv().get("DOCKER_PROXY_CONFIG");
      if(cfg == null) {
        log.warn("DOCKER_PROXY_CONFIG environment variable not set");
      } else {
        File f = new File(cfg);
        if(f.exists()) {
          log.info("using configuration from '{}'", f.getAbsolutePath());
          Config.setConfFile(f);
          Config config = Config.getConfiguration();
          log.info("configuration '{}'", config);
        } else {
          log.warn("configuration file '{}' not found", f.getAbsolutePath());
        }
      }
    } catch(Exception e) {
      throw new DockerProxyException("failed to initialize", e);
    }
  }

}
