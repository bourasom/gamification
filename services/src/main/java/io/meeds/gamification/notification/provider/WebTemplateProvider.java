/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
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
package io.meeds.gamification.notification.provider;

import static io.meeds.gamification.utils.Utils.RULE_ANNOUNCED_NOTIFICATION_ID;
import static io.meeds.gamification.utils.Utils.RULE_PUBLISHED_NOTIFICATION_ID;

import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.container.xml.InitParams;

import io.meeds.gamification.notification.provider.builder.ActionAnnouncedTemplateBuilder;
import io.meeds.gamification.notification.provider.builder.ActionPublishedTemplateBuilder;
import io.meeds.gamification.service.AnnouncementService;
import io.meeds.gamification.service.RuleService;
import io.meeds.social.translation.service.TranslationService;

@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = RULE_PUBLISHED_NOTIFICATION_ID, template = "classpath:/notification/gamification/RulePublishedWebTemplate.gtmpl"),
    @TemplateConfig(pluginId = RULE_ANNOUNCED_NOTIFICATION_ID, template = "classpath:/notification/gamification/RuleAnnouncedWebTemplate.gtmpl"),
})
public class WebTemplateProvider extends TemplateProvider {

  public WebTemplateProvider(RuleService ruleService,
                              AnnouncementService announcementService,
                              TranslationService translationService,
                              InitParams initParams) {
    super(initParams);
    this.templateBuilders.put(PluginKey.key(RULE_PUBLISHED_NOTIFICATION_ID),
                              new ActionPublishedTemplateBuilder(ruleService, translationService, this, false));
    this.templateBuilders.put(PluginKey.key(RULE_ANNOUNCED_NOTIFICATION_ID),
                              new ActionAnnouncedTemplateBuilder(ruleService,
                                                                 announcementService,
                                                                 translationService,
                                                                 this,
                                                                 false));
  }
}
