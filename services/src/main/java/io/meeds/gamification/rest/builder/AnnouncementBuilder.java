/**
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2022 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package io.meeds.gamification.rest.builder;

import io.meeds.gamification.model.Announcement;
import io.meeds.gamification.model.AnnouncementActivity;

public class AnnouncementBuilder {

  private AnnouncementBuilder() {
    // Static methods
  }

  public static Announcement fromAnnouncementActivity(AnnouncementActivity announcementActivity) {
    return new Announcement(announcementActivity.getId(),
                            announcementActivity.getChallengeId(),
                            announcementActivity.getChallengeTitle(),
                            announcementActivity.getAssignee(),
                            announcementActivity.getComment(),
                            announcementActivity.getCreator(),
                            announcementActivity.getCreatedDate(),
                            announcementActivity.getActivityId());

  }

  public static AnnouncementActivity toAnnouncementActivity(Announcement announcement) {
    return new AnnouncementActivity(announcement.getId(),
                                    announcement.getChallengeId(),
                                    announcement.getChallengeTitle(),
                                    announcement.getAssignee(),
                                    announcement.getComment(),
                                    announcement.getCreator(),
                                    announcement.getCreatedDate(),
                                    announcement.getActivityId(),
                                    null);
  }

}