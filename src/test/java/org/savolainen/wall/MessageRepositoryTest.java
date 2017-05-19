package org.savolainen.wall;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class MessageRepositoryTest {

    private MediaType contentTypeJson = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MediaType contentTypeHalJson = new MediaType(MediaType.APPLICATION_JSON.getType(),"hal+json",
            Charset.forName("utf8"));
    private MockMvc mockMvc;

    @SuppressWarnings("rawtypes")
	private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private List<Message> messageList = new ArrayList<>();
    
    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
            .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
            .findAny()
            .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        Message message = new Message();
        message.setAlias("foo_1");
        message.setContent("bar_1");
        message.setDate("10101010_1");
        this.messageList.add(this.messageRepository.save(message));
        Message message2 = new Message();
        message2.setAlias("foo_2");
        message2.setContent("bar_2");
        message2.setDate("10101010_2");
        this.messageList.add(this.messageRepository.save(message2));
        Message message3 = new Message();
        message3.setAlias("foo_3");
        message3.setContent("bar_3");
        message3.setDate("10101010_3");
        this.messageList.add(this.messageRepository.save(message3));
    }

    @Test
    public void messageIsCreated() throws Exception {
    	 Message message = new Message();
         message.setAlias("foo_x");
         message.setContent("bar_x");
         message.setDate("10101010_x");
        mockMvc.perform(post("/message/")
                .content(this.json(message))
                .contentType(contentTypeJson))
                .andExpect(status().isCreated());
    }
    
    @Test
    public void messageIsFound() throws Exception {
        mockMvc.perform(get("/message/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentTypeHalJson))
                .andExpect(jsonPath("$.alias", is(this.messageList.get(0).getAlias())))
                .andExpect(jsonPath("$.content", is(this.messageList.get(0).getContent())))
                .andExpect(jsonPath("$.date", is(this.messageList.get(0).getDate())));
    }
    
    @Test
    public void getMessages() throws Exception {
        mockMvc.perform(get("/message"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentTypeHalJson))
                .andExpect(jsonPath("$._embedded.message", hasSize(greaterThan(6))));             
    }
    
    @Test
    public void messageIsRemoved() throws Exception {
    	mockMvc.perform(get("/message/2"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentTypeHalJson))
        .andExpect(jsonPath("$.alias", is(this.messageList.get(1).getAlias())))
        .andExpect(jsonPath("$.content", is(this.messageList.get(1).getContent())))
        .andExpect(jsonPath("$.date", is(this.messageList.get(1).getDate())));
        mockMvc.perform(delete("/message/2"))
				.andExpect(status().isNoContent());
        mockMvc.perform(get("/message/2"))
        .andExpect(status().isNotFound());    
        }

    @SuppressWarnings("unchecked")
	protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
