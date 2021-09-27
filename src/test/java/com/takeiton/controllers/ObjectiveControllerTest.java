package com.takeiton.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeiton.models.AppUser;
import com.takeiton.models.Objective;
import com.takeiton.services.AppUserDetailsService;
import com.takeiton.services.OwnedObjectiveService;
import com.takeiton.services.TokenService;
import com.takeiton.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ObjectiveController.class)
public class ObjectiveControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    OwnedObjectiveService objectiveService;

    @MockBean
    JWTUtil jwtUtil;

    @MockBean
    AppUserDetailsService userDetailsService;
    @MockBean
    TokenService tokenService;

    String baseUrl = "/api/objective";

    AppUser appUser = AppUser.builder().username("user_1").password("").build();
    Objective OBJ_1 = Objective.builder().id(1L).name("obj_1").description("first").doneCriteria("dc1").dueDate(new Date()).status("not started").owner(appUser).build();
    Objective OBJ_2 = Objective.builder().id(2L).description("second").doneCriteria("dc2").dueDate(new Date()).status("not started").owner(appUser).build();
    Objective OBJ_3 = Objective.builder().id(3L).description("third").doneCriteria("dc3").dueDate(new Date()).status("not started").owner(appUser).build();
    User userDetails = new User("user_1", "", Collections.emptyList());
    String token = "dummy_token";

    @BeforeEach
    private void createMocks() {
        Mockito.when(jwtUtil.extractUsername(token)).thenReturn("user_1");
        Mockito.when(userDetailsService.loadUserByUsername("user_1")).thenReturn(userDetails);
        Mockito.when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);
        Mockito.when(tokenService.isActiveToken("user_1", token)).thenReturn(true);
    }

    @Test
    public void getAllObjectives_success() throws Exception {
        List<Objective> objectives = new ArrayList<>(Arrays.asList(OBJ_1, OBJ_2, OBJ_3));
        Mockito.when(objectiveService.findAll("user_1", true)).thenReturn(objectives);

        mockMvc.perform(MockMvcRequestBuilders
                        .get(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void getAllObjectives_noResult_success() throws Exception {
        List<Objective> objectives = new ArrayList<>();
        Mockito.when(objectiveService.findAll("user_1", true)).thenReturn(objectives);

        mockMvc.perform(MockMvcRequestBuilders.get(baseUrl).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void getObjective_byId_success() throws Exception {
        Mockito.when(objectiveService.findById(OBJ_1.getId(),"user_1")).thenReturn(Optional.of(OBJ_1));

        mockMvc.perform(MockMvcRequestBuilders
                        .get(baseUrl + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("obj_1")));
    }

    @Test
    public void createObjective() throws Exception {
        Objective OBJ_Create = Objective.builder()
                .id(1L)
                .name("OBJ_Create")
                .description("OBJ_create_desc")
                .doneCriteria("dc1")
                .dueDate(new Date())
//                .status("not started")
                .build();
        Mockito.when(objectiveService.createObjective(OBJ_Create, appUser.getUsername())).thenReturn(OBJ_Create);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(OBJ_Create));

        mockMvc.perform(mockRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("OBJ_Create")))
                .andExpect(jsonPath("$.description", is("OBJ_create_desc")));
    }


}
