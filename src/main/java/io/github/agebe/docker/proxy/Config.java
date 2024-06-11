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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Config {

  private static File confFile;

  private String registry;

  private List<User> users;

  public Config() {
    super();
  }

  public Config(String registry, List<User> users) {
    super();
    this.registry = registry;
    this.users = users;
  }

  public String getRegistry() {
    return registry;
  }

  public List<User> getUsers() {
    return users;
  }

  @Override
  public String toString() {
    return "Config [registry=" + registry + ", users=" + users + "]";
  }

  public static Config parse(File f) {
    try {
      InputStream inputStream = new FileInputStream(f);
      Yaml yaml = new Yaml();
      Map<String, Object> data = yaml.load(inputStream);
      Gson gson = new GsonBuilder()
          .disableHtmlEscaping()
          .disableJdkUnsafe()
          .registerTypeAdapter(Password.class, new PasswordTypeAdapter())
          .registerTypeAdapter(Role.class, new RoleTypeAdapter())
          .create();
      Config config = gson.fromJson(gson.toJson(data), Config.class);
      for(User user : config.getUsers()) {
        long count = config.getUsers().stream()
            .filter(u -> u.getName().equals(user.getName()))
            .count();
        if(count > 1) {
          throw new DockerProxyException("check configuration, user '%s' duplicated".formatted(user.getName()));
        }
      }
      return config;
    } catch(Exception e) {
      throw new DockerProxyException("failed to parse configuration", e);
    }
 }

  public static Config getConfiguration() {
    if((confFile != null) && confFile.canRead()) {
      return Config.parse(confFile);
    } else {
      return new Config(null, List.of());
    }
  }

  public static void setConfFile(File file) {
    confFile = file;
  }

}
