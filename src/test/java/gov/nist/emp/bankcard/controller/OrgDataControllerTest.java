package gov.nist.emp.bankcard.controller;

import gov.nist.emp.bankcard.dto.DivisionDto;
import gov.nist.emp.bankcard.dto.GroupDto;
import gov.nist.emp.bankcard.dto.OuDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrgDataControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAllNistOus() throws Exception {
        mockMvc.perform(get("/api/org-data/ous").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetAllDivisions() throws Exception {
        mockMvc.perform(get("/api/org-data/divisions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetAllGroups() throws Exception {
        mockMvc.perform(get("/api/org-data/groups").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetActiveNistOus() throws Exception {
        mockMvc.perform(get("/api/org-data/ous/active").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetActiveDivisions() throws Exception {
        mockMvc.perform(get("/api/org-data/divisions/active").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetActiveGroups() throws Exception {
        mockMvc.perform(get("/api/org-data/groups/active").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
