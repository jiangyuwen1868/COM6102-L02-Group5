package com.jyw.csp.context;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StopWatch;

import com.jyw.csp.constant.CspConstants;
import com.jyw.csp.datatransform.message.gw.GwRequestMsg;
import com.jyw.csp.datatransform.message.gw.GwResponseMsg;


public class SessionContext extends ConcurrentHashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    protected static Class<? extends SessionContext> contextClass = SessionContext.class;

    protected static final ThreadLocal<? extends SessionContext> threadLocal = new ThreadLocal<SessionContext>() {
        @Override
        protected SessionContext initialValue() {
            try {
                return contextClass.newInstance();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    };

    public SessionContext() {
        super();
    }

    public static void setContextClass(Class<? extends SessionContext> contextClass) {
        SessionContext.contextClass = contextClass;
    }

    public static SessionContext getCurrentContext() {
        return threadLocal.get();
    }

    public void release() {
        threadLocal.remove();
    }

    public void set(String key, Object value) {
        if (value != null) {
            put(key, value);
        } else {
            remove(key);
        }
    }

    public GwRequestMsg getGwRequestMsg() {
        return (GwRequestMsg) get("gwRequestMsg");
    }

    public void setGwRequestMsg(GwRequestMsg gwRequestMsg) {
        set("gwRequestMsg", gwRequestMsg);
    }

    public GwResponseMsg getGwResponseMsg() {
        return (GwResponseMsg) get("gwResponseMsg");
    }

    public void setGwResponseMsg(GwResponseMsg gwResponseMsg) {
        set("gwResponseMsg", gwResponseMsg);
    }

    public long getSysRecvTime() {
        return (long) get("sys_recv_time");
    }

    public void setSysRecvTime(long sysRecvTime) {
        set("sys_recv_time", sysRecvTime);
    }

    public long getSysRespTime() {
        return (long) get("sys_resp_time");
    }

    public void setSysRespTime(long sysRespTime) {
        set("sys_resp_time", sysRespTime);
    }
    
    public StopWatch getStopWatch() {
    	return (StopWatch) get(CspConstants.CSP_COSTTIME_INFO_STOPWATCH);
    }
}
