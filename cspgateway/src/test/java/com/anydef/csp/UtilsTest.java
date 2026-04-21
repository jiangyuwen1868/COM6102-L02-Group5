package com.anydef.csp;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.anydef.gwapi.sdk.core.constant.SdkConstant;
import com.jyw.csp.constant.CspConstants;
import com.jyw.csp.datatransform.message.tx.TxResponseMsg;
import com.jyw.csp.enums.ErrorInfo;
import com.jyw.csp.monitor.MonitorServer;
import com.jyw.csp.util.string.StringUtils;
import com.jyw.csp.vo.CSP000000OutVo;

public class UtilsTest {

	@Test
	public void test1() {
		boolean b = StringUtils.isMatch( "127.*.12.12","127.0.12.12");
		System.out.println(System.currentTimeMillis());
	}
	
	@Test
	public void test2() {
		String responseMsg = "{\n" + 
				"        \"tx_header\":{\n" + 
				"                \"sys_evt_trace_id\":\"c33a879b236b4a408097ecba0095ba68\",\n" + 
				"                \"sys_recv_time\":\"20220613111934571\",\n" + 
				"                \"sys_tx_code\":\"CSP000000\",\n" + 
				"                \"sys_resp_time\":\"20220613111934583\",\n" + 
				"                \"sys_pkg_sts_type\":\"01\",\n" + 
				"                \"sys_resp_desc\":\"成功\",\n" + 
				"                \"sys_resp_code\":\"000000000000\"\n" + 
				"        },\n" + 
				"        \"tx_body\":{\n" + 
				"                \"com1\":{\n" + 
				"                        \"appId\":\"1001\",\n" + 
				"                        \"tenantId\":\"cps\",\n" + 
				"                        \"channelTxCode\":\"CspOpenApi\",\n" + 
				"                        \"nodeId\":\"csp\"\n" + 
				"                },\n" + 
				"                \"entity\":{\n" + 
				"                        \"hsmrspdata\":\"{\\\"errcode\\\":\\\"00\\\",\\\"hsmIp\\\":\\\"192.168.20.20\\\",\\\"hsmPort\\\":\\\"8016\\\",\\\"hsmType\\\":\\\"SJJ1309\\\",\\\"methodName\\\":\\\"HsmSM2SignByKeyt\\\",\\\"r\\\":\\\"50A85DF931FE9AF32EE805A6931907B737E93CDDFA10B5EE493C85AE798CD797\\\",\\\"rspcode\\\":\\\"K4\\\",\\\"s\\\":\\\"73C9F33BB89488856FF7159A524DE85A134592F34C50269756FAA23A3C571EF3\\\"}\"\n" + 
				"                }\n" + 
				"        }\n" + 
				"}";
		TxResponseMsg txResponseMsg = new TxResponseMsg();
		txResponseMsg = JSONObject.parseObject(responseMsg, TxResponseMsg.class);
		if(ErrorInfo.SUCCESS.getCode().equals(txResponseMsg.getMsgHead().getSys_resp_code())) {
			JSONObject jsonObj = JSON.parseObject(responseMsg);
			JSONObject rspHsmDataJson = jsonObj.getJSONObject(CspConstants.TX_BODY).getJSONObject(CspConstants.ENTITY).getJSONObject(CspConstants.HSMRSPDATA);
			
			CSP000000OutVo outVo = new CSP000000OutVo();
			outVo.setHsmrspdataJson(rspHsmDataJson);
			outVo.setHsmrspdata(rspHsmDataJson.toJSONString());
			txResponseMsg.getMsgBody().setEntity(outVo);
			
		}
		
		CSP000000OutVo outVo = (CSP000000OutVo)txResponseMsg.getMsgBody().getEntity();
		if (outVo!=null && CspConstants.SUCCESS_HSM_CODE.equals(outVo.getHsmrspdataJson().getString(CspConstants.ERRCODE))) {
			String r = outVo.getHsmrspdataJson().getString("r");
            String s = outVo.getHsmrspdataJson().getString("s");
            System.out.println(r+s);
		}
	}
	
	@Test
	public void test3() throws Exception{
		MonitorServer ms = new MonitorServer();
		ms.copyToCpuAndMem();
		
		System.out.println(JSON.toJSONString(ms));
	}
	
	@Test
	public void testHmac() throws Exception{
		String appSecret = "197A74511492B5EACBF5FB2FCF5D5CA6";
		String signString = "POST\n" + 
				"application/json; charset=UTF-8\n" + 
				"7gBytcT8wv5GBaKkK1ChHA==\n" + 
				"application/json; charset=UTF-8\n" + 
				"Fri, 09 Jun 2023 17:01:40 GMT\n" + 
				"X-Ca-AppKey:TESTORG_YZW_APP_001\n" + 
				"X-Ca-Nonce:c1adf1ac-ca6b-4365-a403-7ee26ae542f4\n" + 
				"X-Ca-Timestamp:1686301300661\n" + 
				"/cspGateway/mainAccess";
		Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        byte[] keyBytes = appSecret.getBytes(SdkConstant.CLOUDAPI_ENCODING);
        hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256"));
        byte[] signResult = hmacSha256.doFinal(signString.getBytes(SdkConstant.CLOUDAPI_ENCODING));
        String signMsg = Base64.encodeBase64String(signResult);
        System.out.println(signMsg);
	}
}
