package com.anydef.csp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jyw.csp.datatransform.message.gw.GwRequestMsg;

@SpringBootTest
@AutoConfigureMockMvc
public class MainAccessControllerTest {

	@Autowired
    private MockMvc mockMvc;

    @Test
    public void testService() throws Exception {
    	try {
	        GwRequestMsg requestMsg = new GwRequestMsg();
	        requestMsg.setApp_id("123123");
	        requestMsg.setSignature("231321");
	        requestMsg.setSec_version("01");
	        requestMsg.setRequest_info("ewoJInR4X2hlYWRlciI6IHsKCQkic3lzX3BrZ192ZXJzaW9uIjogIjAxIiwKCQkic3lzX3JlcV90aW1lIjogIjIwMjIwMjE2MTczMDI4MTIzIiwKCQkic3lzX2V2dF90cmFjZV9pZCI6ICIyMDIyMDIxNjE3MzAyODEyMzIzNDY2NDY3IiwKCQkic3lzX3R4Y29kZSI6ICIxMDAwMDEiLAoJCSJzeXNfcGtnX3N0c190eXBlIjogIjAwIgoJfSwKCSJ0eF9ib2R5IjogewoJCSJjb20xIjogewoJCQkidGVuYW50SWQiOiAiMTAyNTAwMCIsCgkJCSJjaGFubmVsVHhDb2RlIjogIjYwMDAiCgkJfSwKCQkiZW50aXR5IjogewoJCQkiaWRfdHlwZSI6ICIxMDEwIiwKCQkJImlkX25vIjogIjQ0MDE4MTE5OTEwMTAxMTIzNCIsCgkJCSJuYW1lIjogIuadjuWbmyIsCgkJCSJtb2JpbGVfbm8iOiAiMTM3MTIzNDU2NzgiCgkJfQoJfQp9");
	
	        ObjectMapper objectMapper = new ObjectMapper();
	        String requestInfoJson = objectMapper.writeValueAsString(requestMsg);
	
	        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/cspGateway/mainAccess");
	        requestBuilder.accept(MediaType.APPLICATION_JSON);
	        requestBuilder.contentType(MediaType.APPLICATION_JSON);
	        requestBuilder.content(requestInfoJson);
	
	        ResultActions action = mockMvc.perform(requestBuilder);
	        action.andDo(MockMvcResultHandlers.print());
	        action.andExpect(MockMvcResultMatchers.status().isOk());
	        
	        Thread.sleep(5000);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
}
