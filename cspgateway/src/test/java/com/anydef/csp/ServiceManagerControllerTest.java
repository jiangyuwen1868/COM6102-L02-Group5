package com.anydef.csp;

import java.util.HashMap;
import java.util.Map;

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

import com.alibaba.fastjson.JSON;

@SpringBootTest
@AutoConfigureMockMvc
public class ServiceManagerControllerTest {

	@Autowired
    private MockMvc mockMvc;

    @Test
    public void testSysConfig() throws Exception {
    	
    	Map<String, String> data = new HashMap<String, String>();
    	
    	data.put("config_key", "sys.index.skinName");
    	data.put("config_value", "skin-purple");
    	
    	start("/cspGateway/serviceManager/sysConfig/get", JSON.toJSONString(data));
    }
    
    @Test
    public void testSysMonitor() throws Exception {
    	start("/cspGateway/serviceManager/sysMonitor", "");
    }
    
    @Test
    public void testHttpResource() throws Exception {
    	Map<String, String> data = new HashMap<String, String>();
    	
    	data.put("groupId", "01");
    	data.put("url", "http://192.168.1.2:8001/cspService");
    	start("/cspGateway/serviceManager/httpResource/addHttpResource", JSON.toJSONString(data));
    	
    	start("/cspGateway/serviceManager/httpResource/selectAll", "");
    }
    
    public void start(String path, String requestMsg) throws Exception {

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(path);
        requestBuilder.accept(MediaType.APPLICATION_JSON);
        requestBuilder.contentType(MediaType.APPLICATION_JSON);
        requestBuilder.content(requestMsg);

        ResultActions action = mockMvc.perform(requestBuilder);
        action.andDo(MockMvcResultHandlers.print());
        action.andExpect(MockMvcResultMatchers.status().isOk());
    }
}
