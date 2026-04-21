package com.jyw.csp.util.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jyw.csp.exception.CommonRuntimeException;
import com.jyw.csp.util.Base64;
import com.jyw.csp.util.Utils;

/**
 * 文件操作工具类
 * @author deyang
 *
 */
public class FileUtils {

	protected static final String enCode = "UTF-8";
	public static final String log001 = "文件创建失败";
	public static final String log002 = "文件名找不到";
	public static final String log003 = "读取文件失败";
	public static final String log004 = "写人文件失败";

	/**
	 * 获取文件流
	 * @param uri 文件URI
	 * @return
	 * @throws IOException
	 */
	public static final InputStream getResource(String uri) throws IOException {
		try {
			return new URL(uri).openStream();
		} catch (MalformedURLException mue) {
		}
		return new FileInputStream(uri);
	}

	/**
	 * 检查文件备份文件路径是否存在，不存在新建备份路径
	 * 如：/home/ap/log/1.log 对应的备份路径为：/home/ap/log/backup/
	 * @param file
	 * @return 备份文件目录对象
	 */
	public static final File checkBackupDirectory(File file) {
		File backupDirectory = new File(file.getParent() + File.separator
				+ "backup");
		
		if (!(backupDirectory.exists()))
			backupDirectory.mkdirs();

		return backupDirectory;
	}
	
	/**
	 * 设置文件或文件夹权限，只针对文件服务器/home/tsmfile/tsmftpdir以下的目录或文件
	 * @param fileDir
	 */
	public static final void setFilePermissions(String fileDir) {
		/*此方法存在效率问题，改用 umask 022 命令自动设置文件权限*/
		
		/*String permissions = Config.getStrPara("CREATE_FILE_PERMISSIONS", "775");
		try {
			String osName = System.getProperty("os.name");
			boolean isWindows = osName.toLowerCase().startsWith("windows");
			if(isWindows) {
				return;
			}
			while(fileDir.indexOf(FTPRemoteFileRootPath)!=-1){
				Runtime.getRuntime().exec("chmod -R "+ permissions + " " + fileDir);
				File file = new File(fileDir);
				file.setExecutable(true,false);//设置可执行权限
				file.setReadable(true,false);//设置可读权限
				file.setWritable(true,false);//设置可写权限
				fileDir = file.getParent();
				if(fileDir==null || FTPRemoteFileRootPath.equals(fileDir)){
					break;
				}
				int idx = fileDir.indexOf("/");
				if(idx==-1){
					idx = fileDir.indexOf("\\");
				}
				if(idx==-1){
					break;
				}
			}
		} catch (IOException e) {
			LogUtil.error("chmod -R " + permissions,e);
		}*/
	}

	/**
	 * 新建文件路径
	 */
	public static final boolean createDirs(String path) {
		File file = new File(path);
		return file.mkdirs();
	}

	/**
	 * 新建文件
	 * @param path 文件路径
	 * @return
	 */
	public static final File createFile(String path) {
		File file = new File(path);
		try {
			file.createNewFile();
		} catch (IOException e) {
			throw new CommonRuntimeException("XATF300100AJ", log001 + ":"
					+ path, e);
		}
		return file;
	}

	/**
	 * 获取文件路径目录列表
	 * @param path 文件路径
	 * @return
	 */
	public static final String[] dirList(String path) {
		return dirList(new File(path));
	}

	public static final String[] dirList(File path) {
		String[] list = path.list();
		return list;
	}

	/**
	 * 读取文件内容
	 * @param file 文件路径
	 * @return
	 */
	public static final String readFile(String file) {
		return readFile(file, enCode);
	}

	/**
	 * 读取文件内容
	 * @param file 文件路径
	 * @param charsetName 文件字符集编码
	 * @return
	 */
	public static final String readFile(String file, String charsetName) {
		StringBuffer sb = null;
		BufferedReader in = null;
		FileInputStream fis = null;
		try {
			sb = new StringBuffer();
			fis = new FileInputStream(file);
			in = new BufferedReader(new InputStreamReader(
					fis, charsetName));
			String str;
			while ((str = in.readLine()) != null) {

				sb.append(str).append("\r\n");
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			throw new CommonRuntimeException("XATF300100AG", log003 + ":"
					+ file + "[" + charsetName + "]", e);
		} catch (IOException e) {
			throw new CommonRuntimeException("XATF300100AH", log003 + ":"
					+ file + "[" + charsetName + "]", e);
		} finally {
			if(fis!=null){
				try{
					fis.close();
				}catch(Exception e){}
			}
			if(in!=null){
				try{
					in.close();
				}catch(Exception e){}
			}
		}
	}

	public static final Reader getReader(File file, String charsetName)
			throws Exception {
		return new BufferedReader(new InputStreamReader(new FileInputStream(
				file), charsetName));
	}

	public static final Reader getReader(File file) throws Exception {
		return getReader(file, enCode);
	}

	public static final DataInputStream getDataInputStream(String filePath)
			throws Exception {
		return new DataInputStream(new FileInputStream(filePath));
	}
	
	public static final DataOutputStream getDataOutputStream(String filePath)
			throws Exception {
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(filePath));
		return dos;
	}
	
	public static final String readBinaryFile2Hex(String file){
		DataInputStream dis = null;
		try{
			dis = getDataInputStream(file);
			byte[] b = new byte[dis.available()];
			dis.readFully(b);
			return Utils.bytes2Hex(b);
		}catch(Exception e){
			throw new CommonRuntimeException("XATF300100AG", log003 + ":"
					+ file , e);
		}finally{
			if(dis!=null){
				try{
					dis.close();
				}catch(Exception e){}
			}
		}
	}
	
	public static final String readBinaryFile2Base64(String file){
		DataInputStream dis = null;
		try{
			dis = getDataInputStream(file);
			byte[] b = new byte[dis.available()];
			dis.readFully(b);
			return new String(Base64.encode(b));
		}catch(Exception e){
			throw new RuntimeException("XATF300100AG" + log003 + ":"
					+ file , e);
		}finally{
			if(dis!=null){
				try{
					dis.close();
				}catch(Exception e){}
			}
		}
	}
	/**
	 * 写入文件内容
	 * @param file 文件对象
	 * @param content 内容
	 */
	public static final void write(File file, String content) {
		write(file, content, enCode);
	}

	/**
	 * 写入文件内容
	 * @param file 文件对象
	 * @param content 内容
	 * @param charsetName 文件字符集编码
	 */
	public static final void write(File file, String content, String charsetName) {
		Writer out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), charsetName));

			out.write(content);
			out.flush();
		} catch (FileNotFoundException e) {
			throw new CommonRuntimeException("XATF300100AG", log002 + ":"
					+ file + "[" + charsetName + "]", e);
		} catch (IOException e) {
			throw new CommonRuntimeException("XATF300100AI", log004 + ":"
					+ file + "[" + charsetName + "]", e);
		} finally {
			if(out!=null){
				try{
					out.close();
				}catch(Exception e){}
			}
		}
	}

	public static final Writer getWriter(File file) throws Exception {
		return getWriter(file, enCode);
	}

	public static final Writer getWriter(File file, String charsetName)
			throws Exception {
		Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
				file), charsetName));
		return w;
	}

	
	/**
	 * 写入文件内容并且备份原文件至备份目录，备份文件名：fileName.yyyyMMdd_hhmmss.*
	 * @param file 文件对象
	 * @param content 内容
	 */
	public static final void writeAndBackup(File file, String content) {
		DateFormat backupDF = null;
		BufferedWriter out = null;
		try {
			backupDF = new SimpleDateFormat("yyyyMMdd_hhmmss");
			File backupDirectory = checkBackupDirectory(file);
			File original = new File(file.getAbsolutePath());
			File backup = new File(backupDirectory, original.getName() + "."
					+ backupDF.format(new Date()));

			original.renameTo(backup);
			out = new BufferedWriter(new FileWriter(file));
			out.write(content);
			out.flush();
		} catch (FileNotFoundException e) {
			throw new CommonRuntimeException("XATF300100AG", log002 + ":"
					+ file, e);
		} catch (IOException e) {
			throw new CommonRuntimeException("XATF300100AI", log004 + ":"
					+ file, e);
		} finally {
			if(out!=null){
				try{
					out.close();
				}catch(Exception e){}
			}
		}
	}

	/**
	 * 判断文件路径是否存在，不存在新建文件路径
	 * 
	 * @param dir
	 *            文件路径
	 * @return
	 */
	public static boolean checkDir(String dir) {
		File f = new File(dir);
		if (f.exists())
			return true;

		if (!(f.isDirectory())) {
			return f.mkdirs();
		}
		return false;
	}

	/**
	 * 检查文件是否存在
	 * @param path
	 * @return
	 */
	public static boolean checkFile(String path) {
		File f = new File(path);
		boolean res = false;
		if (f.exists())
			res = true;
		return res;
	}

	/**
	 * 复制目录所有文件至目标文件目录
	 * @param source 源文件目录
	 * @param destination 目标文件目录
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public static boolean copyFiles(String source, String destination)
			throws IOException {
		File file = new File(source);
		if(!checkDir(destination)){
			return false;
		}
		FileChannel in = null;
		FileChannel out = null;
		boolean res = false;
		try {
			in = new FileInputStream(file).getChannel();
			File outFile = new File(destination, file.getName());
			out = new FileOutputStream(outFile).getChannel();
			in.transferTo(0L, in.size(), out);
			res = true;
		} finally {
			try{
				if (in != null)
					in.close();
			}catch(Exception e){}
			try{
				if (out != null)
					out.close();
			}catch(Exception e){}
		}
		return res;
	}

	/**
	 * 复制指定文件至指定目录
	 * @param s 源文件
	 * @param t 目标文件
	 * @return
	 * @throws Exception
	 */
	public static boolean copyFile(String s, String t) throws Exception {
		File source = new File(s);
		File target = new File(t);
		
		if(!checkDir(target.getParent())){
			return false;
		}
		
		boolean result = false;
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new BufferedInputStream(new FileInputStream(source));
			out = new BufferedOutputStream(new FileOutputStream(target),
					(int) source.length());
			byte[] file = new byte[(int) source.length()];
			while (in.read(file) > 0)
				out.write(file);
			result = true;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception ex) {
				result = false;
			}
			try {
				if (out != null)
					out.close();
			} catch (Exception ex) {
				result = false;
			}
		}
		return result;
	}
	
	/**
	 * 删除文件或目录
	 * 
	 * @param fileName
	 *            要删除的文件或目录
	 * @return
	 */
	public static boolean deleteFile(String fileName) {
		boolean result = true;
		try {
			File file = new File(fileName);
			if (file.isFile()) {
				file.delete();
			} else {
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					deleteFile(fileName + "/" + filelist[i]);
				}
				file.delete();
			}
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	/**
	 * 移动文件
	 * 
	 * @param source
	 *            源文件
	 * @param target
	 *            目标文件
	 */
	public static void moveFile(String source, String target) throws Exception {
		target = target.replaceAll("\\\\", "/");
		String targetDirPath = target.substring(0, target.lastIndexOf("/"));
		File targetDir = new File(targetDirPath);
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}
		boolean flag = copyFile(source, target);
		if (flag) {
			deleteFile(source);
		}
	}
	
	/**
	 * 将InputStream保存到文件
	 * @param filePath 文件路径
	 * @param inputStream
	 * @return
	 */
	public static boolean inputStream2File(String filePath,
			InputStream inputStream) {
		if (filePath == null || filePath.isEmpty()) {
			return false;
		}
		OutputStream os = null;
		try {
			os = new FileOutputStream(filePath);
			int nBytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((nBytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, nBytesRead);
			}
			os.flush();
			inputStream.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if(os!=null){
				try{
					os.close();
				}catch(Exception e){}
			}
			if(inputStream!=null){
				try{
					inputStream.close();
				}catch(Exception e){}
			}
		}
	}
}
