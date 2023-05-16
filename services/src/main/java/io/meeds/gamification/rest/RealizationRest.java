package io.meeds.gamification.rest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

import io.meeds.gamification.constant.HistoryStatus;
import io.meeds.gamification.constant.IdentityType;
import io.meeds.gamification.model.RealizationDTO;
import io.meeds.gamification.model.filter.RealizationFilter;
import io.meeds.gamification.rest.model.RealizationList;
import io.meeds.gamification.rest.model.RealizationRestEntity;
import io.meeds.gamification.service.RealizationService;
import io.meeds.gamification.service.RuleService;
import io.meeds.gamification.utils.RealizationBuilder;
import io.meeds.gamification.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.exoplatform.social.core.manager.IdentityManager;

@Path("/gamification/realizations/api")
@Tag(name = "/gamification/realizations/api", description = "Manages users realizations")
@RolesAllowed("administrators")
public class RealizationRest implements ResourceContainer {

  private static final Log   LOG = ExoLogger.getLogger(RealizationRest.class);

  private RuleService        ruleService;

  private RealizationService realizationService;

  private IdentityManager    identityManager;

  public RealizationRest(RuleService ruleService,
                         RealizationService realizationService,
                         IdentityManager identityManager) {
    this.ruleService = ruleService;
    this.realizationService = realizationService;
    this.identityManager = identityManager;
  }

  @GET
  @Produces({
      MediaType.APPLICATION_JSON, "application/vnd.ms-excel", MediaType.TEXT_PLAIN
  })
  @Path("allRealizations")
  @Operation(summary = "Retrieves the list of achievements switch a filter. The returned format can be of type JSON or XLSX", method = "GET", description = "Retrieves the list of achievements switch a filter. The returned format can be of type JSON or XLSX")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
  })
  @RolesAllowed("users")
  public Response getAllRealizations(
                                     @Context
                                     HttpServletRequest httpRequest,
                                     @Parameter(description = "result fromDate", required = true)
                                     @QueryParam("fromDate")
                                     String fromDate,
                                     @Parameter(description = "result toDate", required = true)
                                     @QueryParam("toDate")
                                     String toDate,
                                     @Parameter(description = "Sort field. Possible values: date or actionType.")
                                     @QueryParam("sortBy")
                                     @DefaultValue("date")
                                     String sortField,
                                     @Parameter(description = "Whether to retrieve results sorted descending or not")
                                     @QueryParam("sortDescending")
                                     @DefaultValue("true")
                                     boolean sortDescending,
                                     @Parameter(description = "earnerIds, that will be used to filter achievements", required = false)
                                     @QueryParam("earnerIds")
                                     List<String> earnerIds,
                                     @Parameter(description = "Offset of result")
                                     @DefaultValue("0")
                                     @QueryParam("offset")
                                     int offset,
                                     @Parameter(description = "Limit of result")
                                     @QueryParam("limit")
                                     int limit,
                                     @Parameter(description = "Response Type")
                                     @DefaultValue("")
                                     @QueryParam("returnType")
                                     String returnType,
                                     @Parameter(description = "identity Type")
                                     @QueryParam("identityType")
                                     String identityType,
                                     @Parameter(description = "domainIds. that will be used to filter achievements", required = false)
                                     @QueryParam("domainIds")
                                     List<Long> domainIds,
                                     @Parameter(description = "If true, this will return the total count of filtered realizations. Possible values = true or false. Default value = false.", required = false)
                                     @QueryParam("returnSize")
                                     @DefaultValue("false")
                                     boolean returnSize) {
    if (StringUtils.isBlank(fromDate) || StringUtils.isBlank(toDate)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Dates must not be blank").build();
    }

    Identity identity = ConversationState.getCurrent().getIdentity();
    Date dateFrom = Utils.parseRFC3339Date(fromDate);
    Date dateTo = Utils.parseRFC3339Date(toDate);

    if (domainIds == null) {
      domainIds = new ArrayList<>();
    }

    RealizationFilter filter = new RealizationFilter(earnerIds,
                                                     sortField,
                                                     sortDescending,
                                                     dateFrom,
                                                     dateTo,
                                                     IdentityType.getType(identityType),
                                                     domainIds);

    boolean isXlsx = StringUtils.isNotBlank(returnType) && returnType.equals("xlsx");
    if (StringUtils.isNotBlank(returnType) && !returnType.equals("json") && !isXlsx) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Unsupported returnType, possible values: xlsx or json").build();
    }
    if (!isXlsx && limit <= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Limit must be positive").build();
    }
    if (offset < 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Offset must be 0 or positive").build();
    }
    try {
      if (isXlsx) {
        String filename = "report_Actions";
        InputStream xlsxInputStream = realizationService.exportXlsx(filter, identity, filename, httpRequest.getLocale());
        return Response.ok(xlsxInputStream)
                       .header("Content-Disposition", "attachment; filename=" + filename + ".xlsx")
                       .header("Content-Type", "application/vnd.ms-excel")
                       .build();
      } else {
        List<RealizationDTO> realizations = realizationService.getRealizationsByFilter(filter,
                                                                                       identity,
                                                                                       offset,
                                                                                       limit);
        List<RealizationRestEntity> realizationRestEntities = RealizationBuilder.toRestEntities(ruleService,
                                                                                                identityManager,
                                                                                                realizations);
        RealizationList realizationList = new RealizationList();
        if (returnSize) {
          int realizationsSize = realizationService.countRealizationsByFilter(filter, identity);
          realizationList.setSize(realizationsSize);
        }
        realizationList.setRealizations(realizationRestEntities);
        realizationList.setOffset(offset);
        realizationList.setLimit(limit);
        return Response.ok(realizationList).build();
      }
    } catch (IllegalAccessException e) {
      LOG.debug("User '{}' isn't authorized to access achievements with parameter : earnerId = {}",
                identity.getUserId(),
                earnerIds,
                e);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
    }
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Path("updateRealizations")
  @Operation(summary = "Updates an existing realization", method = "PUT", description = "Updates an existing realization")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Request fulfilled"),
      @ApiResponse(responseCode = "404", description = "Object not found"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
  })
  public Response updateRealization(
                                    @Parameter(description = "id of realization", required = true)
                                    @QueryParam("realizationId")
                                    String realizationId,
                                    @Parameter(description = "new status of realization", required = true)
                                    @QueryParam("status")
                                    String status,
                                    @Parameter(description = "new action Label of realization")
                                    @QueryParam("actionLabel")
                                    String actionLabel,
                                    @Parameter(description = "new points of realization")
                                    @QueryParam("points")
                                    Long points) {

    Identity currenIdentity = ConversationState.getCurrent().getIdentity();
    String currentUserName = currenIdentity.getUserId();

    try {
      RealizationDTO realization = realizationService.getRealizationById(Long.parseLong(realizationId), currenIdentity);
      if (!actionLabel.isEmpty()) {
        realization.setActionTitle(actionLabel);
      }
      if (points != 0) {
        realization.setGlobalScore(realization.getGlobalScore() - realization.getActionScore() + points);
        realization.setActionScore(points);
      }
      realization.setStatus(HistoryStatus.valueOf(status).name());
      realization = realizationService.updateRealization(realization, currenIdentity);
      return Response.ok(RealizationBuilder.toRestEntity(ruleService, identityManager, realization)).build();
    } catch (IllegalAccessException e) {
      LOG.debug("User '{}' doesn't have enough privileges to update realization with id {}", currentUserName, realizationId, e);
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } catch (ObjectNotFoundException e) {
      LOG.debug("User '{}' attempts to update a not existing realization '{}'", currentUserName, e);
      return Response.status(Response.Status.NOT_FOUND).entity("realization not found").build();
    }
  }
}