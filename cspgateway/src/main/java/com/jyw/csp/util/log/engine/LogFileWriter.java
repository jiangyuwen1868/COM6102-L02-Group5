package com.jyw.csp.util.log.engine;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jyw.csp.util.log.base.LogConfig;
import com.jyw.csp.util.log.vo.PlainLogVO;


/**
 * 日志文件操作类
 * 1.记录Error日志至(文件序号.err)文件中
 * 2.记录非Error日志至(文件序号.log)文件中
 * 3.根据交易码记录交易报文日志至(交易码.log)文件中
 * <p>Title: 日志文件操作类</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: anydef.com.cn</p>
 * @author pengdy
 * @version 1.0
 */
public class LogFileWriter {
	private final boolean isErrorLog;
	// 输入日志文件流
	private OutputStream out;
	
	private SimpleDateFormat rollingModel;

	// 当前普通日志文件对应目录的日期
	private String logDate;
	
	//当前交易日志文件对应目录日期
	private String lastRolling;
	
	private String logRootPath=LogConfig.logFilePath;

	/**
	 * 当前日志文件大小
	 */
	private long totalSize = 0;
	
	/**
	 * 日志文件扩展名
	 */
	private final String fileExtName;

	public LogFileWriter(String fileExtName) {
		this(fileExtName,LogConfig.logFilePath);
	}
	
	
	
	public LogFileWriter(String fileExtName,String logpath) {
		this.logRootPath=logpath;
		this.fileExtName = fileExtName;
		if(this.rollingModel==null){
			this.rollingModel = new SimpleDateFormat(LogConfig.getLogRollDirectoryTime());
		}
		isErrorLog = ".err".equals(fileExtName);
		try {
			init(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * 日志记录加入ip
	 * @param ip
	 * @param txNo
	 * @param logVO
	 * @param isTest
	 */
	public void log(String ip,String txNo, PlainLogVO logVO, boolean isTest) throws Exception{
		StringBuilder buffer = new StringBuilder();

		if (isTest) {
			buffer.append("test");
			buffer.append("|");
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String dateTime = sdf.format(logVO.getLogTime());
		buffer.append(dateTime);

//		buffer.append("|");
//		buffer.append(LogConfig.APP_CODE);
		buffer.append("|");
		buffer.append(LogConfig.SERVER_INSTANCE_CODE);

		buffer.append("|");
		if (ip != null) {
			buffer.append(ip);
		}
		
		buffer.append("|");
		if (txNo != null) {
			buffer.append(txNo);
		}

		buffer.append("|");
		if (logVO.getLogType() != null) {
			buffer.append(logVO.getLogType());
		}

		buffer.append("|");
		if (logVO != null) {
			buffer.append(logVO.getLogContent());
		}

		String msg = buffer.toString();
		writeToFile(msg, logVO.getLogTime());
		if (LogConfig.logConsoleEnabled && !isErrorLog) {
			System.out.println(msg);
		}
	}


	/**
	 * 写日志信息到文件中
	 * 
	 * @param msg
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private void writeToFile(String msg, Date date) throws IOException,
			UnsupportedEncodingException,Exception {
		String ecoding = LogConfig.encoding_CharSet;
		ecoding = ecoding==null?"UTF-8":ecoding;
		byte[] bytes = msg.getBytes(ecoding);

		// 如果日志的日期与当前目录文件的日期不同，则调用初始化创建
		if (!this.rollingModel.format(date).equals(logDate)) {
			init(true);
		}

		// 以三个制表符开头，表示一个新的日志开始
		out.write(bytes);
		out.write('\n');
		totalSize += bytes.length + 1;

		if (totalSize > LogConfig.logFileMaxSize) {
			init(true);
		}
		
		if(LogConfig.logFlushEnabled){
			flush();
		}
	}
	
	/**
	 * 同一个交易记录同一个文件，调用方式必须先调用LogSynUtil.beginLog();
	 * 再调用该方法，最后调用LogSynUtil.endLog();
	 * @param ip IP地址
	 * @param txNo 交易流水
	 * @param txcode 交易码
	 * @param logVO log对象
	 * @throws Exception
	 */
	public void log(String ip,String txNo,String txcode, PlainLogVO logVO) throws Exception{
		
		String date = this.rollingModel.format(new Date());
		String fullPath = this.getFullPath(date);
		
		if (!(fullPath.equals(this.lastRolling))) {
			File dir = new File(fullPath);
			if (!(dir.exists()))
				try {
					dir.mkdirs();
				} catch (Exception e) {
				}

			this.lastRolling = fullPath;
		}
		String logFilePath = fullPath + txcode + ".log";
		StringBuilder buffer = new StringBuilder();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String dateTime = sdf.format(logVO.getLogTime());
		buffer.append(dateTime);

//		buffer.append("|");
//		buffer.append(LogConfig.APP_CODE);
		buffer.append("|");
		buffer.append(LogConfig.SERVER_INSTANCE_CODE);

		buffer.append("|");
		if (ip != null) {
			buffer.append(ip);
		}
		
		buffer.append("|");
		if (txNo != null) {
			buffer.append(txNo);
		}

		buffer.append("|");
		if (logVO.getLogType() != null) {
			buffer.append(logVO.getLogType());
		}

		buffer.append("|");
		if (logVO != null) {
			buffer.append(logVO.getLogContent());
		}

		String msg = buffer.toString();
		writeToFile(logFilePath, msg);
		if (LogConfig.logConsoleEnabled && !isErrorLog) {
			System.out.println(msg);
		}
	}
	

    /**
     * 将信息放进指定的文件
     * 
     * @param filename 文件名
     * @param addData
     */
    private void writeToFile(String filename, String addData) {
    	 RandomAccessFile myFileStream=null;
        try {
            boolean ifFirst = false;
            File file = new File(filename);
            // 如果指定的文件不存在
            if (!file.exists()) {
                // 创建一个文件
                file.createNewFile();
                ifFirst = true;
            }
            myFileStream = new RandomAccessFile(filename, "rw");
            // 如果不是第一次写入
            if (!ifFirst) {
                // 跳到文件的末尾
                myFileStream.seek(myFileStream.length());
                // 文件换行
                myFileStream.write("\n".getBytes());
            }
            // 将信息写入
            myFileStream.write(addData.getBytes());

            // 关闭文件流
            myFileStream.close();
            	
            myFileStream = null;
        } catch (IOException ioe) {
            System.out.println("Error:" + ioe.getMessage());
        }finally{
        	try{if(myFileStream!=null)myFileStream.close();}catch(Exception e){}
        }
    }

	// 是否立即刷新？
	public void flush() throws IOException{
		if (out!=null)
			out.flush();
	}

	public void closeFile() throws IOException{
		if (out!=null)
			out.close();
	}

	public void init(boolean isCreate) throws Exception {
		String date = this.rollingModel.format(new Date());
		String path = this.getFullPath(date);

		File file = new File(path);
		file.mkdirs();
		String fileNo = FileNoGenerator.getFileNoFromDisk(file, this.fileExtName, isCreate)+"";
		String fileName = getFullFileName(path, fileNo);
		
		if (out != null) {
			out.close();
		}

		file = new File(fileName);
		
		totalSize = file.length();

		out = new FileOutputStream(file, true);
		out = new BufferedOutputStream(out, LogConfig.logFileBufferSize);

		// 设置当前日志文件对应的日期
		logDate = date;

	}

	/**
	 * 得到完整文件名 log/syscode/date/fileno.log(.err)
	 * 文件名添加日期时间戳记hhmmss.log(.err)
	 * @param date
	 * @param path
	 * @param fileNo
	 * @return
	 */
	protected String getFullFileName(String path, String fileNo) {
//		SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
//		return path +sdf.format(new Date())+fileExtName;
		return path + fileNo + fileExtName;
	}

	protected String getFullPath(String date) {
		return this.logRootPath + LogConfig.APP_CODE + File.separatorChar
				+ LogConfig.SERVER_INSTANCE_CODE + File.separatorChar + date
				+ File.separatorChar;
	}


	
	
}