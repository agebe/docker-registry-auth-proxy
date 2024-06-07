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

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

public class Password implements Predicate<String> {

  private String password;

  public Password(String password) {
    super();
    if(StringUtils.isBlank(password)) {
      throw new RuntimeException("blank password");
    }
    this.password = password;
  }

  @Override
  public boolean test(String entered) {
    if(StringUtils.startsWithAny(password, "$2a$", "$2y$")) {
      // BCrypt
      // org.mindrot:jbcrypt:0.4 can't handle 2y, replace with 2a
      // java.lang.IllegalArgumentException: Invalid salt revision
      // create the password with e.g.: htpasswd -Bn user
      return BCrypt.checkpw(entered, "$2a$" + StringUtils.substring(password, 4));
    } else {
      // plain
      return StringUtils.equals(entered, password);
    }
  }

  @Override
  public String toString() {
    return "***";
  }
}
