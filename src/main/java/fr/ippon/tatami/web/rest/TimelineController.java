package fr.ippon.tatami.web.rest;

import fr.ippon.tatami.domain.Attachment;
import fr.ippon.tatami.domain.Status;
import fr.ippon.tatami.domain.StatusDetails;
import fr.ippon.tatami.service.StatusUpdateService;
import fr.ippon.tatami.service.TimelineService;
import fr.ippon.tatami.web.rest.dto.Reply;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Collection;

/**
 * REST controller for managing status.
 *
 * @author Julien Dubois
 */
@Controller
public class TimelineController {

    private final Log log = LogFactory.getLog(TimelineController.class);

    @Inject
    private TimelineService timelineService;

    @Inject
    private StatusUpdateService statusUpdateService;

    @ExceptionHandler(ConstraintViolationException.class)
    public void handleConstraintViolationException(ConstraintViolationException cve, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        if (log.isDebugEnabled()) {
            for (ConstraintViolation cv : cve.getConstraintViolations()) {
                log.debug("Violation : " + cv.getMessage());
            }
        }
    }

    /**
     * POST /statuses/update -> create a new Status
     */
    @RequestMapping(value = "/rest/statuses/update",
            method = RequestMethod.POST)
    public void postStatus(@RequestBody Status status) {
        if (log.isDebugEnabled()) {
            log.debug("REST request to add status : " + status.getContent());
        }
        String escapedContent = StringEscapeUtils.escapeHtml(status.getContent());
        statusUpdateService.postStatus(escapedContent);
    }

    /**
     * POST /statuses/update -> create a new Status
     */
    @RequestMapping(value = "/rest/statuses/attachment/update",
            method = RequestMethod.POST)
    public void postStatus(@RequestBody Attachment attach) {
        if (log.isDebugEnabled()) {
            log.debug("REST request to add status : " + attach.getContent());
        }
        //String escapedContent = StringEscapeUtils.escapeHtml(status.getContent());
        statusUpdateService.saveAttachment(attach);
        
    }
    
    
    /**
     * POST /statuses/discussion/:id -> reply to this Status
     */
    @RequestMapping(value = "/rest/statuses/discussion",
            method = RequestMethod.POST)
    public void replyToStatus(@RequestBody Reply reply) {
        if (log.isDebugEnabled()) {
            log.debug("REST request to reply to status : " + reply);
        }
        String escapedContent = StringEscapeUtils.escapeHtml(reply.getContent());
        statusUpdateService.replyToStatus(escapedContent, reply.getStatusId());
    }

    /**
     * POST /statuses/destroy/:id -> delete Status
     */
    @RequestMapping(value = "/rest/statuses/destroy/{statusId}",
            method = RequestMethod.POST)
    @ResponseBody
    public void removeStatus(@PathVariable("statusId") String statusId) {
        if (log.isDebugEnabled()) {
            log.debug("REST request to remove status : " + statusId);
        }
        timelineService.removeAttachment(statusId);
        timelineService.removeStatus(statusId);
    }

    /**
     * GET  /statuses/show/:id -> returns a single status, specified by the id parameter
     */
    @RequestMapping(value = "/rest/statuses/show/{statusId}",
            method = RequestMethod.GET,
            produces = "application/json")
    @ResponseBody
    public Status getStatus(@PathVariable("statusId") String statusId) {
        if (log.isDebugEnabled()) {
            log.debug("REST request to get status Id : " + statusId);
        }
        return timelineService.getStatus(statusId);
    }
    
    /**
     * GET  /statuses/show/:id -> returns a single attachment only if it exists, specified by the status id parameter
     */
    @RequestMapping(value = "/rest/statuses/attachment/show/{statusId}",
            method = RequestMethod.GET,
            produces = "application/json")
    @ResponseBody
    public Attachment getAttachment(@PathVariable("statusId") String statusId) {
        if (log.isDebugEnabled()) {
            log.debug("REST request to get status Id : " + statusId);
        }
        return timelineService.getAttachment(statusId);
    }
    

    /**
     * GET  /statuses/details/:id -> returns the details for a status, specified by the id parameter
     */
    @RequestMapping(value = "/rest/statuses/details/{statusId}",
            method = RequestMethod.GET,
            produces = "application/json")
    @ResponseBody
    public StatusDetails getStatusDetails(@PathVariable("statusId") String statusId) {
        if (log.isDebugEnabled()) {
            log.debug("REST request to get status details Id : " + statusId);
        }
        return timelineService.getStatusDetails(statusId);
    }

    /**
     * POST /statuses/share/:id -> Shares the status
     */
    @RequestMapping(value = "/rest/statuses/share/{statusId}",
            method = RequestMethod.POST)
    @ResponseBody
    public void favoriteStatus(@PathVariable("statusId") String statusId) {
        if (log.isDebugEnabled()) {
            log.debug("REST request to share status : " + statusId);
        }
        timelineService.shareStatus(statusId);
    }    
    
    /**
     * GET  /statuses/home_timeline -> get the latest statuses from the current user
     */
    @RequestMapping(value = "/rest/statuses/home_timeline",
            method = RequestMethod.GET,
            produces = "application/json")
    @ResponseBody
    public Collection<Status> listStatus(@RequestParam(required = false) Integer count,
                                         @RequestParam(required = false) String since_id,
                                         @RequestParam(required = false) String max_id) {
        if (count == null || count == 0) {
            count = 20; //Default value
        }
        return timelineService.getTimeline(count, since_id, max_id);
    }

    /**
     * GET  /statuses/user_timeline?screen_name=jdubois -> get the latest statuses from user "jdubois"
     */
    @RequestMapping(value = "/rest/statuses/user_timeline",
            method = RequestMethod.GET,
            produces = "application/json")
    @ResponseBody
    public Collection<Status> listStatusForUser(@RequestParam("screen_name") String username,
                                                @RequestParam(required = false) Integer count,
                                                @RequestParam(required = false) String since_id,
                                                @RequestParam(required = false) String max_id) {

        if (count == null || count == 0) {
            count = 20; //Default value
        }
        if (log.isDebugEnabled()) {
            log.debug("REST request to get someone's status (username=" + username + ").");
        }
        return timelineService.getUserline(username, count, since_id, max_id);
    }
}