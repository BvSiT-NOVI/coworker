package nl.bvsit.coworker.controller;

import nl.bvsit.coworker.domain.CpMenuItem;
import nl.bvsit.coworker.service.CpMenuItemService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes={CpMenuItemController.class})
public class CpMenuItemControllerIntegrationTest {
    private static final Logger logger =  LoggerFactory.getLogger(CpMenuItemControllerIntegrationTest.class);

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CpMenuItemService service;

    @Test
    public void whenNotAuthenticated_thenReturnsHttpStatusUnauthorized() throws Exception {

        CpMenuItem cpMenuItem = new CpMenuItem();
        cpMenuItem.setName("Koffie verkeerd");
        cpMenuItem.setPrice(new BigDecimal("2.10"));

        List<CpMenuItem> allCpMenuItems = Arrays.asList(cpMenuItem);

        given(service.getAll()).willReturn(allCpMenuItems);

        mvc.perform(get("/api/menuitems")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}