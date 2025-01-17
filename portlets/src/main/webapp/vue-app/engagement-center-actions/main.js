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
import './initComponents.js';
import '../engagement-center/initComponents.js';
import '../engagement-center/services.js';

// get overridden components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('EngagementCenterActions');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const vuetify = Vue.prototype.vuetifyOptions;

const appId = 'EngagementCenterActions';

//getting language of the PLF
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const urls = [
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Challenges-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.addon.Gamification-${lang}.json`
];

export function init(isAdministrator, isProgramManager) {
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

  exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
    // init Vue app when locale ressources are ready
    Vue.createApp({
      data: {
        now: Date.now(),
      },
      computed: {
        isMobile() {
          return this.$vuetify.breakpoint.xsOnly;
        },
      },
      created() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        window.setInterval(() => this.now = Date.now(), 1000);
      },
      template: `<engagement-center-rules id="${appId}" :is-administrator="${isAdministrator}" :is-program-manager="${isProgramManager}" />`,
      vuetify,
      i18n
    }, `#${appId}`, 'EngagementCenterActions');
  }).finally(() => Vue.prototype.$utils?.includeExtensions('engagementCenterActions'));
}
