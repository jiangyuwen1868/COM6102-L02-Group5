package com.jyw.csp.datatransform.message.tx;

public class TxRequestMsgHead {

    private String sys_pkg_version;
    private String sys_req_time;
    private String sys_evt_trace_id;
    private String sys_snd_serial_no;
    private String sys_txcode;
    private String sys_pkg_sts_type;

    public String getSys_pkg_version() {
        return sys_pkg_version;
    }

    public void setSys_pkg_version(String sys_pkg_version) {
        this.sys_pkg_version = sys_pkg_version;
    }

    public String getSys_req_time() {
        return sys_req_time;
    }

    public void setSys_req_time(String sys_req_time) {
        this.sys_req_time = sys_req_time;
    }

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

    public String getSys_pkg_sts_type() {
        return sys_pkg_sts_type;
    }

    public void setSys_pkg_sts_type(String sys_pkg_sts_type) {
        this.sys_pkg_sts_type = sys_pkg_sts_type;
    }
}
