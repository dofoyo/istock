package com.rhb.istock.comm.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhb.istock.kdata.KdataServiceImp;


public class FileTools {
	private static final String SEP = System.getProperty("line.separator");
	protected static final Logger logger = LoggerFactory.getLogger(FileTools.class);

	public static List<String> readAsLines(String fileName, String encoding) {
		List<String> lines = new ArrayList<String>();
		try {
			FileInputStream fis = new FileInputStream(fileName);
			InputStreamReader isr = new InputStreamReader(fis, encoding);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
			isr.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lines;
	}


	/**
	 * 判断文件的编码格式
	 * @param fileName :file
	 * @return 文件编码格式
	 * @throws Exception
	 */
	public static String getCharset(String fileName){
		String code = "GBK";

		try {
			BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));
			int p = (bin.read() << 8) + bin.read();
			
			switch (p) {
				case 0xefbb:
					code = "UTF-8";
					break;
				case 0xfffe:
					code = "Unicode";
					break;
				case 0xfeff:
					code = "UTF-16BE";
			}
			bin.close();
		}catch(Exception e) {}
		
		return code;
	}

	
	public static void write(String fileContent, String fileName, String encoding) {
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
			osw.write(fileContent);
			osw.flush();
			osw.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String read(String fileName, String encoding) {
		StringBuffer sb = new StringBuffer();
		try {
			FileInputStream fis = new FileInputStream(fileName);
			InputStreamReader isr = new InputStreamReader(fis, encoding);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append(SEP);
			}
			br.close();
			isr.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static boolean isExists(String path) {
		File file = new File(path);
		return file.exists();
	}
	
	public static void writeMapFile(String path, Map<LocalDate,List<String>> content, boolean append) {
		StringBuffer sb = new StringBuffer();
		for(Map.Entry<LocalDate, List<String>> entry : content.entrySet()) {
			sb.append(entry.getKey());
			sb.append(":");
			for(String str : entry.getValue()) {
				sb.append(str);
				sb.append(",");
			}
			sb.replace(sb.length()-1, sb.length(), "\n");
		}
		
		writeTextFile(path,sb.toString(),append);
	}

	public static void writeTextFile(String path, String content, boolean append) {
		try {
			File file = new File(path);

			if (!file.exists()) { // 如果文本文件不存在则创建它
				file.createNewFile();
				file = new File(path); // 重新实例化
			}
			
			FileWriter filewriter = new FileWriter(file, append);
			filewriter.write(content);
			filewriter.close();

			//logger.info("write: ");
			//logger.info(content);
		} catch (Exception d) {
			d.printStackTrace();
		}
		
		
	}

	public static Map<LocalDate, List<String>> readMapFile(String path) {
		Map<LocalDate, List<String>> results = new TreeMap<LocalDate, List<String>>();
		LocalDate date;
		List<String> ids;
		String[] lines = readTextFile(path).split("\n");
		String[] dateAndList;
		String[] list;
		for(String line : lines) {
			dateAndList = line.split(":");
			if(dateAndList.length>0 && dateAndList[0].length()>0) {
				date = LocalDate.parse(dateAndList[0]);
				list = dateAndList[1].split(",");
				ids = new ArrayList<String>();
				for(String id : list) {
					ids.add(id);
				}
				results.put(date, ids);
			}
		}
		return results;
	}
	
	public static String readTextFile(String path) {
		StringBuffer buffer = new StringBuffer();

		File file = new File(path);
		if(!file.exists()) {
			return buffer.toString();
		}
		
		try {
			InputStream is = new FileInputStream(path);
			String line; // 用来保存每行读取的内容
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			line = reader.readLine(); // 读取第一行
			while (line != null) { // 如果 line 为空说明读完了
				buffer.append(line); // 将读到的内容添加到 buffer 中
				buffer.append("\n"); // 添加换行符
				line = reader.readLine(); // 读取下一行
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}

	public static List<File> getFiles(String path, String suffix, boolean isdepth) {
		List<File> files = new ArrayList<File>();
		getListFiles(files, path, suffix, isdepth);
		// System.out.println(files.size());
		return files;
	}

	private static void getListFiles(List<File> list, String path, String suffix, boolean isdepth) {
		File dir = new File(path);
		listFile(list, dir, suffix, isdepth);
	}

	private static void listFile(List<File> list, File dir, String suffix, boolean isdepth) {
		// 是目录，同时需要遍历子目录
		if (dir.isDirectory() && isdepth == true) {
			File[] t = dir.listFiles();
			for (int i = 0; i < t.length; i++) {
				listFile(list, t[i], suffix, isdepth);
			}
		} else {
			String filePath = dir.getAbsolutePath();
			if (suffix != null) {
				int begIndex = filePath.lastIndexOf(".");// 最后一个.(即后缀名前面的.)的索引
				String tempsuffix = "";

				if (begIndex != -1) {// 防止是文件但却没有后缀名结束的文件
					tempsuffix = filePath.substring(begIndex + 1, filePath.length());
				}

				if (tempsuffix.equals(suffix)) {
					list.add(new File(filePath));
				}
			} else {
				// 后缀名为null则为所有文件
				list.add(new File(filePath));
			}
		}
	}

}
