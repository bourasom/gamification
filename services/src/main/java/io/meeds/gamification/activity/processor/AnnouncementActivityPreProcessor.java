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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.gamification.activity.processor;

import static io.meeds.gamification.activity.processor.AnnouncementActivityProcessor.ACTIVITY_TYPE;
import static io.meeds.gamification.activity.processor.AnnouncementActivityProcessor.ANNOUNCEMENT_COMMENT_PARAM;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

public class AnnouncementActivityPreProcessor extends BaseActivityProcessorPlugin {

  public AnnouncementActivityPreProcessor(InitParams params) {
    super(params);
  }

  @Override
  public boolean isPreActivityProcessor() {
    return true;
  }

  @Override
  public void processActivity(ExoSocialActivity activity) {
    if (!ACTIVITY_TYPE.equals(activity.getType())) {
      return;
    }
    if (activity.isComment() || activity.getType() == null) {
      return;
    }
    if (activity.getTemplateParams().containsKey(ANNOUNCEMENT_COMMENT_PARAM)) {
      activity.getTemplateParams().put(ANNOUNCEMENT_COMMENT_PARAM, null);
    }
  }

}