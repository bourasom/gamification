<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2022 Meeds Association
contact@meeds.io
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <exo-drawer
    ref="ruleFormDrawer"
    v-model="drawer"
    :confirm-close="ruleChanged"
    :confirm-close-labels="confirmCloseLabels"
    class="EngagementCenterDrawer"
    right
    allow-expand
    @expand-updated="expanded = $event"
    @opened="stepper = 1"
    @closed="clear">
    <template #title>
      {{ drawerTitle }}
    </template>
    <template v-if="drawer" #content>
      <v-form
        ref="RuleForm"
        v-model="isValidForm"
        class="form-horizontal pt-0 pb-4"
        flat
        @submit="saveRule">
        <v-stepper
          v-model="stepper"
          :class="expanded && 'flex-row' || 'flex-column'"
          class="ma-0 py-0 d-flex"
          vertical
          flat>
          <div :class="expanded && 'col-6'" class="flex-grow-1 flex-shrink-0">
            <v-stepper-step
              step="1"
              class="ma-0">
              <span class="font-weight-bold dark-grey-color text-subtitle-1">{{ $t('rule.form.label.stepOne') }}</span>
            </v-stepper-step>
            <v-slide-y-transition>
              <div v-show="expanded || stepper === 1" class="px-6">
                <v-card-text class="d-flex flex-grow-1 text-left text-subtitle-1 px-0 py-2">
                  {{ $t('rule.form.label.program') }}
                </v-card-text>
                <v-card-text class="d-flex pa-0">
                  <v-img
                    :src="programAvatar"
                    :height="programAvatarSize"
                    :width="programAvatarSize"
                    :max-height="programAvatarSize"
                    :max-width="programAvatarSize"
                    :style="programStyle"
                    class="rounded border-color" />
                  <span class="my-auto ms-3">{{ programTitle }}</span>
                </v-card-text>
                <v-card-text class="d-flex pa-0">
                  <translation-text-field
                    ref="ruleTitle"
                    id="ruleTitle"
                    v-model="ruleTitleTranslations"
                    :field-value.sync="ruleTitle"
                    :placeholder="$t('rule.form.label.rules.placeholder')"
                    :maxlength="maxTitleLength"
                    :object-id="ruleId"
                    :no-expand-icon="!expanded"
                    object-type="rule"
                    field-name="title"
                    drawer-title="rule.form.translateTitle"
                    class="width-auto flex-grow-1 pb-1 pt-4"
                    back-icon
                    autofocus
                    required
                    @initialized="setFormInitialized">
                    <template #title>
                      <div class="text-subtitle-1">
                        {{ $t('rule.form.label.rules') }}
                      </div>
                    </template>
                  </translation-text-field>
                </v-card-text>
                <v-card-text class="pa-0">
                  <translation-text-field
                    ref="ruleDescriptionTranslation"
                    v-model="ruleDescriptionTranslations"
                    :field-value.sync="ruleDescription"
                    :object-id="ruleId"
                    :maxlength="maxDescriptionLength"
                    :no-expand-icon="!expanded"
                    class="ma-1px mt-4"
                    object-type="rule"
                    field-name="description"
                    drawer-title="rule.form.translateDescription"
                    back-icon
                    rich-editor
                    @initialized="setFormInitialized">
                    <template #title>
                      <div class="text-subtitle-1">
                        {{ $t('rule.form.label.description') }}
                      </div>
                    </template>
                    <rich-editor
                      id="ruleDescription"
                      ref="ruleDescriptionEditor"
                      v-model="ruleDescription"
                      :placeholder="$t('rule.form.label.description.placeholder')"
                      :max-length="maxDescriptionLength"
                      :tag-enabled="false"
                      ck-editor-type="rule"
                      @validity-updated="validDescription = $event"
                      @ready="handleRichEditorReady" />
                  </translation-text-field>
                </v-card-text>
                <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left text-subtitle-1 px-0 pb-2">
                  {{ $t('rule.form.label.rewards') }}
                </v-card-text>
                <v-card
                  flat
                  width="120"
                  class="d-flex flex-grow-1">
                  <v-text-field
                    v-model="rule.score"
                    :rules="scoreRules"
                    class="mt-0 pt-0 me-2"
                    type="number"
                    hide-details
                    outlined
                    dense
                    required />
                  <label class="my-auto">{{ $t('rule.form.label.points') }}</label>
                </v-card>
                <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left text-subtitle-1 px-0 pb-2">
                  {{ $t('rule.form.label.type') }}
                </v-card-text>
                <div class="d-flex flex-row pb-4">
                  <v-btn
                    class="btn me-2 not-clickable"
                    :class="automaticType && 'btn-primary'"
                    @click="setAutomatic">
                    {{ $t('rule.form.label.type.automatic') }}
                  </v-btn>
                  <v-btn
                    class="btn not-clickable"
                    :class="manualType && 'btn-primary'"
                    @click="setManual">
                    {{ $t('rule.form.label.type.declarative') }}
                  </v-btn>
                </div>
                <div v-if="automaticType">
                  <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left text-subtitle-1 px-0 pb-2">
                    {{ $t('rule.form.label.selectEvent') }}
                  </v-card-text>
                  <v-card-text v-if="eventNames.length" class="pa-0">
                    <v-autocomplete
                      id="EventSelectAutoComplete"
                      ref="EventSelectAutoComplete"
                      v-model="value"
                      :placeholder="$t('rule.form.label.selectEvent.placeholder')"
                      :items="eventNames"
                      item-text="label"
                      class="pa-0"
                      filled
                      persistent-hint
                      dense>
                      <template #selection="data">
                        <v-chip
                          v-bind="data.attrs"
                          :input-value="data.selected"
                          :title="data.item && data.item.label || data.item"
                          @click="data.select">
                          {{ data.item && data.item.label || data.item }}
                        </v-chip>
                      </template>
                      <template #item="data">
                        <v-list-item-content v-text="data.item.label" />
                      </template>
                    </v-autocomplete>
                  </v-card-text>
                  <v-card-text v-if="eventExist" class="error--text pa-0">
                    {{ $t('rule.form.error.sameEventExistsInProgram') }}
                  </v-card-text>
                </div>
                <div v-if="ruleId">
                  <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left text-subtitle-1 px-0 pb-2">
                    {{ $t('rule.form.label.status') }}
                  </v-card-text>
                  <div class="d-flex flex-row">
                    <label class="subtitle-1 text-light-color mt-1 pe-3">{{ $t('rule.form.label.enabled') }}</label>
                    <v-switch
                      id="allowAttendeeToUpdateRef"
                      ref="allowAttendeeToUpdateRef"
                      v-model="rule.enabled"
                      class="mt-0 ms-4" />
                  </div>
                </div>
              </div>
            </v-slide-y-transition>
          </div>
          <div :class="expanded && 'col-6'" class="flex-grow-1 flex-shrink-0">
            <v-stepper-step
              :complete="stepper > 2"
              step="2"
              class="ma-0">
              <span class="font-weight-bold dark-grey-color text-subtitle-1">{{ $t('rule.form.label.stepTwo') }}</span>
            </v-stepper-step>
            <v-slide-y-transition>
              <div v-show="expanded || stepper > 1" class="px-6">
                <engagement-center-rule-publish-editor
                  v-if="enablePublication"
                  :enabled="!rule.id"
                  :rule="rule"
                  :program="program"
                  :publish.sync="rule.publish"
                  :space-id.sync="rule.spaceId"
                  :message.sync="rule.message"
                  :template-params="rule.templateParams"
                  :valid-message.sync="validMessage" />
                <div class="pt-4 text-subtitle-1">{{ $t('rule.form.ruleConditionsLabel') }}</div>
                <div class="ps-7">
                  <v-chip
                    class="ma-2"
                    :color="durationCondition && 'primary' || ''"
                    :outlined="!durationCondition"
                    :dark="durationCondition"
                    @click="updateDateCondition">
                    {{ $t('rule.form.label.duration') }}
                  </v-chip>
                  <v-chip
                    class="ma-2"
                    :color="recurrenceCondition && 'primary' || ''"
                    :outlined="!recurrenceCondition"
                    :dark="recurrenceCondition"
                    @click="updateRecurrenceCondition">
                    {{ $t('rule.form.label.recurrence') }}
                  </v-chip>
                  <v-tooltip :disabled="$root.isMobile" bottom>
                    <template #activator="{ on, attrs }">
                      <v-chip
                        class="ma-2"
                        :color="prerequisiteRuleCondition && 'primary' || ''"
                        :outlined="!prerequisiteRuleCondition"
                        :dark="prerequisiteRuleCondition"
                        v-bind="attrs"
                        v-on="on"
                        @click="updatePrerequisiteRuleCondition">
                        {{ $t('rule.form.label.action') }}
                      </v-chip>
                    </template>
                    <span>{{ $t('rule.form.label.actionTooltip') }}</span>
                  </v-tooltip>
                </div>
                <div v-if="durationCondition">
                  <engagement-center-rule-dates-input
                    v-model="validDatesInput"
                    :start-date.sync="rule.startDate"
                    :end-date.sync="rule.endDate" />
                </div>
                <div v-if="recurrenceCondition">
                  <engagement-center-rule-recurrence-input
                    v-model="rule.recurrence" />
                </div>
                <div v-if="prerequisiteRuleCondition">
                  <engagement-center-rule-lock-input
                    v-model="rule.prerequisiteRules"
                    :program-id="programId"
                    :excluded-ids="excludedRuleIds" />
                </div>
              </div>
            </v-slide-y-transition>
          </div>
        </v-stepper>
      </v-form>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          v-if="stepper === 2 && !expanded"
          class="btn me-2"
          @click="previousStep">
          {{ $t('rule.form.label.button.back') }}
        </v-btn>
        <v-btn
          v-else
          class="btn me-2"
          @click="close">
          {{ $t('rule.form.label.button.cancel') }}
        </v-btn>
        <v-btn
          v-if="stepper === 1"
          :disabled="disableNextButton"
          class="btn btn-primary"
          @click="nextStep">
          {{ $t('rule.form.label.button.next') }}
        </v-btn>
        <v-btn
          v-else
          :disabled="!ruleChanged || disableSaveButton"
          class="btn btn-primary"
          @click="saveRule">
          {{ saveButtonLabel }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    rule: {},
    program: null,
    ruleTitle: null,
    ruleDescription: null,
    originalRule: null,
    ruleToUpdate: {},
    ruleTitleTranslations: {},
    ruleDescriptionTranslations: {},
    saving: false,
    eventMapping: [],
    value: '',
    eventExist: false,
    validDescription: false,
    validEvent: false,
    stepper: 0,
    programAvatarSize: 40,
    isValidForm: true,
    maxTitleLength: 50,
    maxDescriptionLength: 1300,
    drawer: false,
    expanded: false,
    scoreRules: [
      v => ( v && v <= 10000 ),
    ],
    durationCondition: false,
    recurrenceCondition: false,
    prerequisiteRuleCondition: false,
    validDatesInput: false,
    validMessage: false,
    events: [],
    programEvents: [],
    defaultTemplateParams: {
      'previewHeight': '-',
      'previewWidth': '-',
      'link': '-',
      'description': '-',
      'title': '-',
      'comment': '-',
      'default_title': '-',
      'html': '-',
    },
  }),
  computed: {
    eventNames() {
      this.events.filter(event => event != null).forEach(event => {
        const eventObject = {};
        eventObject.name = event;
        let fieldLabelI18NKey = `exoplatform.gamification.gamificationinformation.rule.title.${event}`;
        let fieldLabelI18NValue = this.$t(fieldLabelI18NKey);
        if (fieldLabelI18NValue === fieldLabelI18NKey) {
          fieldLabelI18NKey = `exoplatform.gamification.gamificationinformation.rule.title.def_${event}`;
          fieldLabelI18NValue = this.$t(fieldLabelI18NKey);
        }
        eventObject.label = fieldLabelI18NValue === fieldLabelI18NKey ? event : fieldLabelI18NValue;
        this.eventMapping.push(eventObject);
      });
      return this.eventMapping;
    },
    programAvatar() {
      return this.program?.avatarUrl || '';
    },
    programTitle() {
      return this.program?.title;
    },
    programId() {
      return this.program?.id;
    },
    ruleId() {
      return this.rule?.id;
    },
    excludedRuleIds() {
      return this.ruleId && [this.ruleId] || [];
    },
    ruleTitleValid() {
      return this.ruleTitle?.length > 0;
    },
    ruleTypeValid() {
      return this.manualType || (this.automaticType && this.validEvent);
    },
    ruleStartDate() {
      return this.rule?.startDate;
    },
    ruleEndDate() {
      return this.rule?.endDate;
    },
    durationValid() {
      return !this.durationCondition || this.validDatesInput;
    },
    recurrenceValid() {
      return !this.recurrenceCondition || this.rule.recurrence;
    },
    prerequisiteRuleValid() {
      return !this.prerequisiteRuleCondition || this.rule.prerequisiteRules?.length;
    },
    disableNextButton() {
      return this.saving || this.eventExist || !this.ruleTitleValid || !this.validDescription || !this.ruleTypeValid || !this.isValidForm;
    },
    disableSaveButton() {
      return this.disableNextButton || !this.durationValid || !this.recurrenceValid || !this.prerequisiteRuleValid || (this.enablePublication && !this.validMessage);
    },
    drawerTitle() {
      return this.ruleId ? this.$t('rule.form.label.edit') : this.$t('rule.form.label.add');
    },
    saveButtonLabel() {
      return this.ruleId ? this.$t('rule.form.label.button.update') : this.$t('rule.form.label.button.add');
    },
    ruleType() {
      return this.rule?.type;
    },
    enablePublication() {
      return this.rule?.enabled
          && !this.rule?.deleted
          && this.program?.enabled
          && !this.program?.deleted
          && (!this.rule.id || !this.rule.published);
    },
    automaticType() {
      return this.ruleType === 'AUTOMATIC';
    },
    manualType() {
      return this.ruleType === 'MANUAL';
    },
    ruleToSave() {
      return this.computeRuleModel(this.rule, this.program, this.ruleDescription);
    },
    ruleChanged() {
      return this.originalRule && JSON.stringify(this.ruleToSave) !== JSON.stringify(this.originalRule);
    },
    confirmCloseLabels() {
      return {
        title: this.rule?.id && this.$t('rule.confirmCloseModificationTitle') || this.$t('rule.confirmCloseCreationTitle'),
        message: this.rule?.id && this.$t('rule.confirmCloseModificationMessage') || this.$t('rule.confirmCloseCreationMessage'),
        ok: this.$t('confirm.yes'),
        cancel: this.$t('confirm.no'),
      };
    },
    programStyle() {
      return this.program?.color && `border: 1px solid ${this.program.color} !important;` || '';
    },
  },
  watch: {
    value: {
      immediate: true,
      handler() {
        this.emitSelectedValue(this.value);
        this.validEvent = this.value && this.value !== '';
      }
    },
    ruleDescription() {
      if (this.$refs.ruleDescriptionTranslation) {
        this.$refs.ruleDescriptionTranslation.setValue(this.ruleDescription);
      }
    },
    saving() {
      if (this.saving) {
        this.$refs.ruleFormDrawer.startLoading();
      } else {
        this.$refs.ruleFormDrawer.endLoading();
      }
    },
    expanded() {
      if (this.expanded) {
        this.stepper = 2;
      } else {
        this.stepper = 1;
      }
    },
  },
  created() {
    this.$root.$on('rule-form-drawer-event', this.open);
  },
  methods: {
    open(rule, program) {
      this.retrieveEvents()
        .then(() => {
          this.rule = rule && JSON.parse(JSON.stringify(rule)) || {
            score: 20,
            enabled: true,
            publish: true,
            area: this.programTitle
          };
          if (!this.rule.templateParams) {
            this.rule.templateParams = Object.assign({}, this.defaultTemplateParams);
          }

          this.program = this.rule?.program || program;
          this.ruleTitle = this.rule?.title || '';
          this.ruleTitleTranslations = {};
          this.ruleDescription = this.rule?.description || '';
          this.validDescription = !!this.ruleDescription;
          this.ruleDescriptionTranslations = {};
          this.durationCondition = this.rule.startDate || this.rule.endDate;
          this.recurrenceCondition = !!this.rule.recurrence;
          this.prerequisiteRuleCondition = this.rule.prerequisiteRules?.length;
          this.eventExist = false;
          if (this.$refs.ruleFormDrawer) {
            this.$refs.ruleFormDrawer.open();
          }
          this.$nextTick().then(() => {
            this.$root.$emit('rule-form-drawer-opened', this.rule);
            this.value = this.eventMapping.find(event => event.name === rule?.event) || '';
          });
          return this.retrieveProgramRules();
        });
    },
    retrieveEvents() {
      if (!this.events?.length) {
        return this.$ruleService.getEvents()
          .then(events => this.events = events || []);
      } else {
        return Promise.resolve(null);
      }
    },
    close() {
      this.$refs.ruleFormDrawer.close();
    },
    clear() {
      this.$refs.ruleDescriptionEditor?.destroyCKEditor();
      this.stepper = 0;
      this.rule.enabled = true;
      this.rule.event = null;
      this.durationCondition = false;
      this.recurrenceCondition = false;
      this.prerequisiteRuleCondition = false;
      this.$set(this.rule,'title','');
      this.rule.description = '';
      this.value = {};
    },
    retrieveProgramRules() {
      return this.$ruleService.getRules({
        programId: this.programId,
        status: 'ENABLED',
        programStatus: 'ALL',
        type: 'AUTOMATIC',
        offset: 0,
        limit: this.events?.length || 10,
        returnSize: false,
        lang: eXo.env.portal.language,
      })
        .then(data => this.programEvents = data && data.rules.map(rule => ({
          ruleId: rule.id,
          event: rule.event,
        })) || []);
    },
    emitSelectedValue(value) {
      const eventObject = this.eventMapping.find(event => event.label === value);
      if (eventObject) {
        this.$set(this.rule,'event', eventObject.name);
        this.eventExist = this.programEvents.find(programEvent => eventObject.name === programEvent.event && programEvent.ruleId !== this.rule?.id);
      }
    },
    handleRichEditorReady() {
      this.setFormInitialized();
      this.ruleDescription = this.rule?.description || '';
    },
    saveRule() {
      this.saving = true;
      if (this.rule.id) {
        this.$translationService.saveTranslations('rule', this.rule.id, 'title', this.ruleTitleTranslations)
          .then(() => this.$translationService.saveTranslations('rule', this.rule.id, 'description', this.ruleDescriptionTranslations))
          .then(() => this.$ruleService.updateRule(this.ruleToSave))
          .then(rule => {
            this.$root.$emit('rule-updated-event', rule);
            if (this.ruleToSave.publish && rule.activityId) {
              document.dispatchEvent(new CustomEvent('alert-message-html', {detail: {
                alertType: 'success',
                alertMessage: `
                  <div class="d-flex flex-nowrap pt-1 justify-center">
                    ${this.$t('programs.details.ruleUpdateAndPublishSuccess')}
                  </div>
                `,
                alertLink: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${rule.activityId}`,
                alertLinkText: this.$t('rule.alert.see'),
                alertLinkTarget: '_self',
              }}));
            } else {
              this.$root.$emit('alert-message', this.$t('programs.details.ruleUpdateSuccess'), 'success');
            }
            this.saving = false; // To Keep to be able to close drawer
            this.originalRule = null;
            return this.$nextTick();
          })
          .then(() => this.close())
          .catch(e => {
            console.error(e);
            this.eventExist = e.message === '409';
          })
          .finally(() => this.saving = false);
      } else {
        this.$ruleService.createRule(this.ruleToSave)
          .then(rule => {
            this.originalRule = rule;
            this.$root.$emit('rule-created-event', rule);
            return this.$translationService.saveTranslations('rule', this.originalRule.id, 'title', this.ruleTitleTranslations);
          })
          .then(() => this.$translationService.saveTranslations('rule', this.originalRule.id, 'description', this.ruleDescriptionTranslations))
          .then(() => {
            if (this.ruleToSave.publish && this.originalRule.activityId) {
              document.dispatchEvent(new CustomEvent('alert-message-html', {detail: {
                alertType: 'success',
                alertMessage: `
                  <div class="d-flex flex-nowrap pt-1 justify-center">
                    ${this.$t('programs.details.ruleCreationAndPublishSuccess')}
                  </div>
                `,
                alertLink: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${this.originalRule.activityId}`,
                alertLinkText: this.$t('rule.alert.see'),
                alertLinkTarget: '_self',
              }}));
            } else {
              this.$root.$emit('alert-message', this.$t('programs.details.ruleCreationSuccess'), 'success');
            }
            this.saving = false; // To Keep to be able to close drawer
            this.originalRule = null;
            return this.$nextTick();
          })
          .then(() => this.close())
          .catch(e => {
            console.error(e);
            this.eventExist = e.message === '409';
          })
          .finally(() => this.saving = false);
      }
    },
    computeRuleModel(rule, program, description) {
      const ruleModel = {
        id: rule?.id,
        title: this.ruleTitle,
        description: description || this.ruleDescription,
        score: rule?.score,
        program: program && JSON.parse(JSON.stringify(program)),
        enabled: rule?.enabled,
        event: rule.type === 'AUTOMATIC' && rule?.event || null,
        startDate: rule?.startDate,
        endDate: rule?.endDate,
        publish: rule?.publish,
        message: rule?.message,
        spaceId: rule?.spaceId,
        templateParams: rule?.templateParams || Object.assign({}, this.defaultTemplateParams),
      };
      if (rule.recurrence) {
        ruleModel.recurrence = rule.recurrence;
      }
      if (rule.type) {
        ruleModel.type = rule.type;
      }
      if (rule.prerequisiteRules?.length) {
        ruleModel.prerequisiteRuleIds = rule.prerequisiteRules.map(r => r.id).filter(id => id);
      }
      return ruleModel;
    },
    setAutomatic() {
      this.$set(this.rule,'type', 'AUTOMATIC');
    },
    setManual() {
      this.$set(this.rule,'type', 'MANUAL');
    },
    updateDateCondition() {
      this.durationCondition = !this.durationCondition;
      this.$set(this.rule, 'startDate', null);
      this.$set(this.rule, 'endDate', null);
    },
    updateRecurrenceCondition() {
      this.recurrenceCondition = !this.recurrenceCondition;
      this.$set(this.rule,'recurrence', null);
    },
    updatePrerequisiteRuleCondition() {
      this.prerequisiteRuleCondition = !this.prerequisiteRuleCondition;
      this.$set(this.rule,'prerequisiteRules', []);
    },
    nextStep(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.stepper++;
    },
    setFormInitialized() {
      this.originalRule = this.computeRuleModel(this.rule, this.program);
    },
    previousStep(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.stepper--;
    },
  }
};
</script>
