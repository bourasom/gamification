package io.meeds.gamification.rest.model;

import org.exoplatform.social.rest.entity.IdentityEntity;

import io.meeds.gamification.model.ProgramDTO;
import io.meeds.gamification.model.RuleDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RealizationRestEntity implements Cloneable {

  private Long           id;

  private IdentityEntity earner;

  private RuleDTO        action;

  private ProgramDTO     domain;

  private String         domainLabel;

  private String         actionLabel;

  private Long           score;

  private String         creator;

  private String         createdDate;

  private String         status;

  private String         space;

  private String         objectId;

  private String         objectType;

  @Override
  public RealizationRestEntity clone() { // NOSONAR
    return new RealizationRestEntity(id,
                                     earner,
                                     action,
                                     domain,
                                     domainLabel,
                                     actionLabel,
                                     score,
                                     creator,
                                     createdDate,
                                     status,
                                     space,
                                     objectId,
                                     objectType);
  }
}