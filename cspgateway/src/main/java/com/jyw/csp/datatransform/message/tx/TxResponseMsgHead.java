package com.jyw.csp.datatransform.message.tx;

public class TxResponseMsgHead {
    private String sys_evt_trace_id;
    private String sys_snd_serial_no;
    private String sys_txcode;
    private String sys_recv_time;
    private String sys_resp_time;
    private String sys_pkg_sts_type;
    private String sys_resp_code;
    private String sys_resp_desc;

    public String getSys_evt_trace_id() {
        return sys_evt_trace_id;
    }

    public void setSys_evt_trace_id(String sys_evt_trace_id) {
        this.sys_evt_trace_id = sys_evt_trace_id;
    }

    public String getSys_snd_serial_no() {
        return sys_snd_serial_no;
    }

    public void setSys_snd_serial_no(String sys_snd_serial_no) {
        this.sys_snd_serial_no = sys_snd_serial_no;
    }

    public String getSys_txcode() {
        return sys_txcode;
    }

    public void setSys_txcode(String sys_txcode) {
        this.sys_txcode = sys_txcode;
    }

    public String getSys_recv_time() {
        return sys_recv_time;
    }

    public void setSys_recv_time(String sys_recv_time) {
        this.sys_recv_time = sys_recv_time;
    }

    public String getSys_resp_time() {
        return sys_resp_time;
    }

    public void setSys_resp_time(String sys_resp_time) {
        this.sys_resp_time = sys_resp_time;
    }

    public String getSys_pkg_sts_type() {
        return sys_pkg_sts_type;
    }

    public void setSys_pkg_sts_type(String sys_pkg_sts_type) {
        this.sys_pkg_sts_type = sys_pkg_sts_type;
    }

    public String getSys_resp_code() {
        return sys_resp_code;
    }

    public void setSys_resp_code(String sys_resp_code) {
        this.sys_resp_code = sys_resp_code;
    }

    public String getSys_resp_desc() {
        return sys_resp_desc;
    }

    public void setSys_resp_desc(String sys_resp_desc) {
        this.sys_resp_desc = sys_resp_desc;
    }
}
