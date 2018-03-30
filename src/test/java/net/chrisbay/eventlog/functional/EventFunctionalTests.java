package net.chrisbay.eventlog.functional;

import net.chrisbay.eventlog.functional.config.FunctionalTestConfig;
import net.chrisbay.eventlog.models.Event;
import net.chrisbay.eventlog.repositories.EventRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.text.StringContainsInOrder.stringContainsInOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by Chris Bay
 */
@RunWith(SpringRunner.class)
@FunctionalTestConfig
@ContextConfiguration
public class EventFunctionalTests extends AbstractBaseFunctionalTest {

    @Autowired
    EventRepository eventRepository;

    private Event createAndSaveEvent() {
        Date eventDate = new Date();
        Event newEvent = new Event("The Title", "The Description", eventDate, "The Location");
        eventRepository.save(newEvent);
        return newEvent;
    }

    @Test
    public void testCanViewNewEventForm() throws Exception {
        mockMvc.perform(get("/events/create")
                .with(user(TEST_USER_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Event")))
                .andExpect(xpath("//form//input[@name='title']").exists())
                .andExpect(xpath("//form//input[@name='startDate']").exists())
                .andExpect(xpath("//form//textarea[@name='description']").exists())
                .andExpect(xpath("//form//input[@name='location']").exists());
    }

    @Test
    public void testCanCreateEvent() throws Exception {
        mockMvc.perform(post("/events/create")
                .with(user(TEST_USER_EMAIL))
                .with(csrf())
                .param("title", "Test Event")
                .param("description", "This event will be great!")
                .param("startDate", "11/11/11")
                .param("location", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/events/*"));
    }

    @Test
    public void testCanViewEventDetails() throws Exception {
        Event event = createAndSaveEvent();
        List<String> eventFields = Arrays.asList(
                event.getTitle(),
                event.getStartDate().toString(),
                event.getLocation(),
                event.getDescription()
        );
        mockMvc.perform(get("/events/{uid}", event.getUid())
                .with(csrf())
                .with(user(TEST_USER_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(content().string(stringContainsInOrder(eventFields)));
    }

    @Test
    public void testCreateEventValidatesTitle() throws Exception {
        mockMvc.perform(post("/events/create")
                .with(csrf())
                .with(user(TEST_USER_EMAIL))
                .param("title", "")
                .param("description", "Test description")
                .param("startDate", "11/11/11"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("event", "title"));
    }

    @Test
    public void testCreateEventValidatesDescription() throws Exception {
        mockMvc.perform(post("/events/create")
                .with(csrf())
                .with(user(TEST_USER_EMAIL))
                .param("title", "Test title")
                .param("description", "")
                .param("startDate", "11/11/11"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("event", "description"));
    }

    @Test
    public void testCreateEventValidatesStartDate() throws Exception {
        mockMvc.perform(post("/events/create")
                .with(csrf())
                .with(user(TEST_USER_EMAIL))
                .param("title", "Test Title")
                .param("description", "Test description")
                .param("startDate", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("event", "startDate"));
    }

}
