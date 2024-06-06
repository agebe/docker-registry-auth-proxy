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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Config {

  private static final Logger log = LoggerFactory.getLogger(Config.class);

  private static Config instance;

  private String registry;

  private List<User> users;

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
          .create();
      Config config = gson.fromJson(gson.toJson(data), Config.class);
      log.info("configuration '{}'", config);
      return config;
    } catch(Exception e) {
      throw new RuntimeException("failed to parse configuration", e);
    }
 }

  public static Config getInstance() {
    return instance;
  }

  public static void setInstance(Config config) {
    instance = config;
  }

}
