/**
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
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
package io.meeds.gamification.service;

import java.io.InputStream;
import java.util.List;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.security.Identity;

import io.meeds.gamification.model.ProgramDTO;
import io.meeds.gamification.model.filter.ProgramFilter;

public interface ProgramService {

  public static final String GAMIFICATION_DOMAIN_UPDATE_LISTENER  = "gamification.domain.update";

  public static final String GAMIFICATION_DOMAIN_CREATE_LISTENER  = "gamification.domain.create";

  public static final String GAMIFICATION_DOMAIN_DELETE_LISTENER  = "exo.gamification.domain.delete";

  public static final String GAMIFICATION_DOMAIN_DISABLE_LISTENER = "exo.gamification.domain.disable";

  public static final String GAMIFICATION_DOMAIN_ENABLE_LISTENER  = "exo.gamification.domain.enable";

  /**
   * Gets programs by filter.
   *
   * @param  programFilter          {@link ProgramFilter} used to filter results
   * @param  username               User name accessing programs
   * @param  offset                 index of the search
   * @param  limit                  limit of results to return
   * @return                        A {@link List &lt;ProgramDTO&gt;} object
   * @throws IllegalAccessException when user is not authorized to get another
   *                                  owner's programs list
   */
  List<ProgramDTO> getProgramsByFilter(ProgramFilter programFilter, String username, int offset,
                                       int limit) throws IllegalAccessException;

  /**
   * Gets Program Ids by filter.
   *
   * @param  programFilter          {@link ProgramFilter} used to filter results
   * @param  username               User name accessing Programs
   * @param  offset                 index of the search
   * @param  limit                  limit of results to return
   * @return                        A {@link List &lt;ProgramDTO&gt;} object
   * @throws IllegalAccessException when user is not authorized to get another
   *                                  owner's Programs list
   */
  List<Long> getProgramIdsByFilter(ProgramFilter programFilter, String username, int offset,
                                   int limit) throws IllegalAccessException;

  /**
   * Return enabled Programs within the DB
   *
   * @return A {@link List &lt;ProgramDTO&gt;} object
   */
  List<ProgramDTO> getEnabledPrograms();

  /**
   * Find a Program by title
   * 
   * @param  programTitle : Program title
   * @return              found {@link ProgramDTO}
   */
  ProgramDTO getProgramByTitle(String programTitle);

  /**
   * Creates a new Program
   * 
   * @param  program                : an object of type ProgramDTO
   * @param  aclIdentity            Security identity of user attempting to
   *                                  create a program
   * @return                        created {@link ProgramDTO}
   * @throws IllegalAccessException when user is not authorized to create a
   *                                  Program for the designated owner defined
   *                                  in object
   */
  ProgramDTO createProgram(ProgramDTO program, Identity aclIdentity) throws IllegalAccessException;

  /**
   * Creates a new Program
   * 
   * @param  program : an object of type ProgramDTO
   * @return         created {@link ProgramDTO}
   */
  ProgramDTO createProgram(ProgramDTO program);

  /**
   * Update an existing Program
   * 
   * @param  program                  : an instance of type ProgramDTO
   * @param  aclIdentity              Security identity of user attempting to
   *                                    update a program
   * @return                          updated object {@link ProgramDTO}
   * @throws IllegalArgumentException when user is not authorized to update the
   *                                    Program
   * @throws ObjectNotFoundException  when the Program identified by its
   *                                    technical identifier is not found
   * @throws IllegalAccessException   when user is not authorized to create a
   *                                    Program for the designated owner defined
   *                                    in object
   */
  ProgramDTO updateProgram(ProgramDTO program, Identity aclIdentity) throws ObjectNotFoundException, IllegalAccessException;

  /**
   * Deletes an existing Program by id
   *
   * @param  programId               Program technical identifier to delete
   * @param  aclIdentity             Security identity of user attempting to
   *                                   delete a program
   * @return                         deleted {@link ProgramDTO}
   * @throws IllegalAccessException  when user is not authorized to delete
   *                                   program
   * @throws ObjectNotFoundException program not found
   */
  ProgramDTO deleteProgramById(long programId, Identity aclIdentity) throws ObjectNotFoundException, IllegalAccessException; // NOSONAR

  /**
   * Retrieves a program identified by its technical identifier.
   * 
   * @param  programId : program id
   * @return           found {@link ProgramDTO}
   */
  ProgramDTO getProgramById(long programId);

  /**
   * Retrieves a program identified by its technical identifier accessed by a
   * user
   * 
   * @param  programId
   * @param  username
   * @return                         found {@link ProgramDTO}
   * @throws IllegalAccessException  when user is not authorized to access
   *                                   program
   * @throws ObjectNotFoundException program not found
   */
  ProgramDTO getProgramById(long programId, String username) throws IllegalAccessException, ObjectNotFoundException;

  /**
   * Count all Programs by filter
   *
   * @param  programFilter          {@link ProgramFilter} used to filter
   *                                  Programs
   * @param  username               User name accessing Programs
   * @return                        Programs count
   * @throws IllegalAccessException when user is not authorized to get another
   *                                  owner's Programs list
   */
  int countPrograms(ProgramFilter programFilter, String username) throws IllegalAccessException;

  /**
   * Retrieves a cover identified by Program technical identifier.
   *
   * @param  programId               Program unique identifier
   * @return                         found {@link InputStream}
   * @throws ObjectNotFoundException Program not found
   */
  InputStream getFileDetailAsStream(long programId) throws ObjectNotFoundException;

  /**
   * Check whether user can add programs or not
   * 
   * @param  aclIdentity Security identity of user
   * @return             true if user has enough privileges to create a program,
   *                     else false
   */
  boolean canAddProgram(Identity aclIdentity);

  /**
   * Check whether user can add programs or not
   * 
   * @param  programId technical identifier of program
   * @param  username  user name
   * @return           true if user has enough privileges to create a program,
   *                   else false
   */
  boolean isProgramOwner(long programId, String username);

  /**
   * Check whether user is member of program or not
   * 
   * @param  programId technical identifier of program
   * @param  username  user name
   * @return           true if user has enough privileges to see a program, else
   *                   false
   */
  boolean isProgramMember(long programId, String username);

}