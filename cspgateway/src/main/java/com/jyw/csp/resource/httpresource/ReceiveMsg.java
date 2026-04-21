package com.jyw.csp.resource.httpresource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;


public class ReceiveMsg implements Runnable {

	StringBuffer debugMsg = new StringBuffer("HttpCommService.ReceiveMsg:");
	String debugStr = null;
	public boolean isStop = false;

	public boolean readOK = false;

	private InputStream in;

	private OutputStream out;

	public Thread readingThread;

	/**
	 * 为读取输入流设定的缓冲区
	 */
	private byte[] buffer = new byte[2048];

	/**
	 * 当前的缓冲区大小
	 */
	private int bufferLen = 2048;

	/**
	 * 输入流的大小
	 */
	private int contentLen;

	/**
	 * 当前已读到的缓冲区大小
	 */
	private int readLen;

	int leftChar = -1;

	String msg = null;

	URLConnection conn = null;

	/**
	 * ReadThread constructor comment.
	 */
	public ReceiveMsg() {
		super();
	}

	/**
	 * ReadThread constructor comment.
	 */
	public ReceiveMsg(InputStream in) {
		super();
		this.in = in;
	}

	public ReceiveMsg(URLConnection conn1, String msg1) {
		super();
		this.conn = conn1;
		this.msg = msg1;
	}

	private int readLine(byte[] buf, java.io.InputStream is)
			throws java.io.IOException {

		byte[] buffer = new byte[1024];
		int bufLen = 1024;
		// int lineLength = 0;
		int off = 0;
		if (leftChar != -1) {
			buf[off++] = (byte) leftChar;
			leftChar = -1;
		}
		while (true) {
			int chr = is.read();
			if (chr == '\n') {
				buffer[off++] = (byte) chr;
				break;
			} else if (chr == '\r') {
				buffer[off++] = (byte) chr;
				int l = is.read();
				if (l != '\n') {
					leftChar = l;

				} else {
					buffer[off++] = (byte) l;
				}
				break;
			} else if (chr == -1) {
				break;
			} else {
				buffer[off++] = (byte) chr;
				if (off == bufLen - 2) {
					byte[] tmp = new byte[bufLen + 1024];
					System.arraycopy(buffer, 0, tmp, 0, bufLen);
					buffer = tmp;
					bufLen = bufLen + 1024;
				}
			}
		}
		if (buf.length < off) {
			buf = new byte[off + 1];

		}
		System.arraycopy(buffer, 0, buf, 0, off);

		return off;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-7-9 10:57:32)
	 */
	public String receive() throws Exception {
		String recMsg = null;
		try {
			byte[] buff = new byte[2048];
			int contentLen = 0;

			while (true) {
				int len = readLine(buff, in);

				if (len == 0) { // socket closed
					break;
				} else {
					String readBuf = new String(buff, 0, len);
					// Trace.trace(true,"->Debug","L","D","",readBuf);
					// System.out.println(readBuf);
					if (readBuf.trim().length() == 0) {
						if (contentLen > 0) {
							byte[] tmp = readFixedLenData(in, contentLen);
							// System.out.println(new String(tmp));
							recMsg = new String(tmp);
							return recMsg;
						}
					}
					if (readBuf.toLowerCase().indexOf("content-length:") != -1) {
						// System.out.print(readBuf);
						contentLen = Integer.parseInt(readBuf.substring(15)
								.trim());
					}
					// System.out.print(new String(buff, 0, len));
				}
			}
			if (contentLen <= 0) {
				ByteArrayOutputStream swapStream = null;
				byte[] tb = new byte[100];
				int rc = 0;
				while ((rc = in.read(tb, 0, 100)) > 0) {
					if (swapStream == null) {
						swapStream = new ByteArrayOutputStream();
					}
					swapStream.write(tb, 0, rc);
				}
				if (swapStream != null) {
					return new String(swapStream.toByteArray());
				}
			}
		} catch (java.io.IOException e) {
			throw e;
		}
		return recMsg;

	}

	public void run() {
		debugMsg.append("begin running....#");
		int len;
		byte[] buf = new byte[256];
		readLen = 0;
		String beginTime = "";
		try {
			try {
				debugMsg.append("begin getOutputStream....#");
				beginTime = "begin send data["+System.currentTimeMillis()+"],";
				out = conn.getOutputStream();
				debugMsg.append("end getOutputStream,begin send msg....#");
				if (msg != null && msg.trim().length() != 0) {
					out.write(msg.getBytes("UTF-8"));
				}
				debugMsg.append("send msg over,begin getInputStream...#");
				beginTime = beginTime +"end send data,begin read["+System.currentTimeMillis()+"],";
				this.in = conn.getInputStream();
				debugMsg.append("getInputStream over#");
			} catch (Exception ee) {
				debugMsg.append("into Exception1#["+ee+"]");
				throw ee;
			}
			this.contentLen = conn.getContentLength();
			debugMsg.append("getContentLength:" + contentLen + "#");
			if (contentLen > 0) {
				debugMsg.append("into contentLen cycle#");
				if (bufferLen < contentLen) {

					buffer = new byte[contentLen];

					bufferLen = contentLen;
				}
				try {
					debugMsg.append("begin read data#" + !isStop + "#");
					while (!isStop) {

						len = in.read(buffer, readLen, contentLen - readLen);
						if (len <= 0) {
							readInputOk(this);
							break;

						}
						readLen = readLen + len;
						if (readLen >= contentLen) {
							readInputOk(this);
							break;
						}
					}
					debugMsg.append("read data over#" + !isStop + "#");
				} catch (Exception e) {
					debugMsg.append("into Exception2#["+e+"]");
					throw e;
				}
			} else {
				try {
					debugMsg.append("into no content cycle#" + !isStop + "#");
					while (!isStop) {
						len = in.read(buf);
						debugMsg.append("read data len#" + len + "#");
						if (len <= 0) {
							readInputOk(this);
							break;
						}

						if (len + readLen >= this.bufferLen) {
							byte[] tmp = new byte[len + readLen + 1024];
							System.arraycopy(buffer, 0, tmp, 0, readLen);
							buffer = tmp;
						}

						System.arraycopy(buf, 0, buffer, readLen, len);
						this.readLen += len;

						if (contentLen > 0 && readLen >= contentLen) {
							readInputOk(this);
							break;
						}

					}
					debugMsg.append("read data over" + !isStop + "#");
				} catch (Exception e) {
					debugMsg.append("into Exception3#");
					throw e;
				}
			}
		} catch (Throwable ee) {
			System.err.println("Time:["+beginTime+"endTime["+System.currentTimeMillis()+"];\r\ndebugMsg["+debugMsg.toString()+"];\r\nException:["+ee+"]");
			debugMsg = null;
			readInputNo(this);
		}
	}

	public void beginRun(int timeout) {
		try{
			this.readingThread = new Thread(this);
			readingThread.setName("ReceiveMsg Read thread");
			debugStr = ""+readingThread.toString();
			readingThread.start();
			waitForData(timeout);
		}catch(Exception e){
			readOK = false;
			notifyAll();//异常时重新唤起等待线程
		}
	}

	public String getMessage() throws Exception {
		try {
			out.close();
		} catch (Exception ee) {
		}
		try {
			in.close();
		} catch (Exception ee) {
		}
		try {
			readingThread.interrupt();
		} catch (Exception ee) {
		}
		if (!readOK) // time Out
		{
			String emsg = "Send and Wait for Data timeout!";

			try {
				emsg = emsg + "DebugMsg:" + new String(debugMsg);
			} catch (Exception ee) {
			}

			throw new Exception(emsg);
		}
		if (readLen <= 0 || buffer == null || buffer.length == 0) {
			return "";
		}
		return new String(buffer, 0, readLen,"UTF-8");
	}

	public synchronized void readInputOk(ReceiveMsg rec) {
		rec.readOK = true;
		notifyAll();
	}
	
	public synchronized void readInputNo(ReceiveMsg rec) {
		rec.readOK = false;
		notifyAll();
	}

	private void waitForData(int timeout) {
		if (readOK) {
			return;
		}
		synchronized (this) {
			try {
				wait(timeout);// sleep(timeOut);
				// //Thread.currentThread().wait(timeOut);
			} catch (Exception e) {
				System.err.println("[HttpCommService]Error in waiting: " + e);
			}
			if (!readOK) {
				isStop = true;
				try {
					readingThread.interrupt();
				} catch (Exception ee) {
				}
				try {
					out.close();
				} catch (Exception ee) {
				}
				try {
					in.close();
				} catch (Exception ee) {
				}
				try {
					notifyAll();
				} catch (Exception ee) {
				}
				try {
					readingThread.notify();
				} catch (Exception ee) {
				}
			}
		}
	}

	private static byte[] readFixedLenData(InputStream in, int len)
			throws IOException {

		int tmpLen1 = 0;
		int tmpoffset = 0;
		byte[] b = new byte[len];
		while (true) {
			tmpLen1 = in.read(b, tmpoffset, len - tmpoffset);
			if (tmpLen1 == -1) {
				break;
			}
			tmpoffset += tmpLen1;
			if (tmpoffset >= len) {
				break;
			}
		}
		return b;
	}

}