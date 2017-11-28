/*
 * Aipo is a groupware program developed by TOWN, Inc.
 * Copyright (C) 2004-2015 TOWN, Inc.
 * http://www.aipo.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aipo.social.core.model;

import com.aipo.social.opensocial.model.ALMobileNotificationSettings;

/**
 *
 */
public class ALMobileNotificationSettingsImpl implements
    ALMobileNotificationSettings {

  private String userId;

  private String mobileNotification;

  private String whileBrowse;

  /**
   * @return
   */
  @Override
  public String getUserId() {
    return userId;
  }

  /**
   * @param paramString
   */
  @Override
  public void setUserId(String paramString) {
    this.userId = paramString;
  }

  /**
   * @return mobileNotification
   */
  @Override
  public String getMobileNotification() {
    return mobileNotification;
  }

  /**
   * @param mobileNotification
   */
  @Override
  public void setMobileNotification(String mobileNotification) {
    this.mobileNotification = mobileNotification;
  }

  /**
   * @return whileBrowse
   */
  @Override
  public String getWhileBrowse() {
    return whileBrowse;
  }

  /**
   * @param whileBrowse
   */
  @Override
  public void setWhileBrowse(String whileBrowse) {
    this.whileBrowse = whileBrowse;
  }
}
