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

import com.hrakaroo.glob.GlobPattern;
import com.hrakaroo.glob.MatchingEngine;

public class User {

  private String name;

  private Password password;

  private Role role;

  private List<String> repos;

  public String getName() {
    return name;
  }

  public Password getPassword() {
    return password;
  }

  public Role getRole() {
    return role;
  }

  public List<String> getRepos() {
    return repos;
  }

  public boolean canAccessRepo(String repo) {
    if((repos == null) || repos.isEmpty()) {
      return true;
    } else {
      for(String pattern : repos) {
        MatchingEngine matcher = GlobPattern.compile(pattern);
        if(matcher.matches(repo)) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public String toString() {
    return "User [name=" + name + ", password=" + password + ", role=" + role + ", repos=" + repos + "]";
  }

}
