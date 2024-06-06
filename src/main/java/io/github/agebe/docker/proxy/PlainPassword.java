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

public class PlainPassword implements Password {

  private String password;

  public PlainPassword(String password) {
    super();
    if(StringUtils.isBlank(password)) {
      throw new RuntimeException("blank password");
    }
    this.password = password;
  }

  @Override
  public boolean test(String entered) {
    return StringUtils.equals(entered, password);
  }

  @Override
  public String toString() {
    return "***";
  }

}
