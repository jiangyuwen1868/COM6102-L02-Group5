package com.jyw.csp.util.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class DateUtils {

	private static GregorianCalendar now;
	private int year = 0;
	private int month = 0;
	private int day = 0;
	private int hour = 0;
	private int minute = 0;
	private int second = 0;
	private int millisecond = 0;
	private String[] daysOfWeek = { "日", "一", "二", "三", "四", "五", "六" };

	public DateUtils() {
		String[] ids = TimeZone.getAvailableIDs(28800000);
		if (ids.length == 0) {
			now = new GregorianCalendar();
		} else {
			SimpleTimeZone pdt = new SimpleTimeZone(28800000, ids[0]);

			now = new GregorianCalendar(pdt);
		}

		this.year = now.get(GregorianCalendar.YEAR);
		this.month = (now.get(GregorianCalendar.MONTH) + 1);
		this.day = now.get(GregorianCalendar.DAY_OF_MONTH);
		this.hour = now.get(GregorianCalendar.HOUR_OF_DAY);
		this.minute = now.get(GregorianCalendar.MINUTE);
		this.second = now.get(GregorianCalendar.SECOND);
		this.millisecond = now.get(GregorianCalendar.MILLISECOND);
	}

	public String getMMDDhhmm() {
		String dateString = new String();

		if (this.month < 10)
			dateString = dateString + "0" + Integer.toString(this.month);
		else
			dateString = dateString + Integer.toString(this.month);

		if (this.day < 10)
			dateString = dateString + "0" + Integer.toString(this.day);
		else
			dateString = dateString + Integer.toString(this.day);

		if (this.hour < 10)
			dateString = dateString + "0" + Integer.toString(this.hour);
		else
			dateString = dateString + Integer.toString(this.hour);

		if (this.minute < 10)
			dateString = dateString + "0" + Integer.toString(this.minute);
		else {
			dateString = dateString + Integer.toString(this.minute);
		}

		return dateString;
	}

	public String getMMDDhhmmss() {
		String dateString = getMMDDhhmm();

		if (this.second < 10)
			dateString = dateString + "0" + Integer.toString(this.second);
		else {
			dateString = dateString + Integer.toString(this.second);
		}

		return dateString;
	}

	public String getYYYYMMDD() {
		String dateString = new String();

		dateString = Integer.toString(this.year);

		if (this.month < 10)
			dateString = dateString + "0" + Integer.toString(this.month);
		else
			dateString = dateString + Integer.toString(this.month);

		if (this.day < 10)
			dateString = dateString + "0" + Integer.toString(this.day);
		else {
			dateString = dateString + Integer.toString(this.day);
		}

		return dateString;
	}
	
	public String getHHMMSS(){
		String dateString = new String();
		if (this.hour  < 10)
			dateString = dateString + "0" + Integer.toString(this.hour );
		else
			dateString = dateString + Integer.toString(this.hour);

		if (this.minute < 10)
			dateString = dateString + "0" + Integer.toString(this.minute);
		else
			dateString = dateString + Integer.toString(this.minute);

		if (this.second < 10)
			dateString = dateString + "0" + Integer.toString(this.second);
		else {
			dateString = dateString + Integer.toString(this.second);
		}
		return dateString;
	}

	public String getYYYY_MM_DD() {
		String dateString = new String();

		dateString = Integer.toString(this.year) + "-";

		if (this.month < 10)
			dateString = dateString + "0" + Integer.toString(this.month);
		else
			dateString = dateString + Integer.toString(this.month);

		dateString = dateString + "-";
		if (this.day < 10)
			dateString = dateString + "0" + Integer.toString(this.day);
		else {
			dateString = dateString + Integer.toString(this.day);
		}

		return dateString;
	}

	public String getYYYYMMDDhhmmss() {
		return Integer.toString(this.year) + getMMDDhhmmss();
	}
	
	public String getYYYYMMDDhhmmssmis() {
		String dateString = getYYYYMMDDhhmmss();
		
		if(this.millisecond < 10){
			dateString = dateString + "00" + Integer.toString(this.millisecond);
		}else if(this.millisecond>=10 && this.millisecond<100){
			dateString = dateString + "0" + Integer.toString(this.millisecond);
		}else{
			dateString = dateString + Integer.toString(this.millisecond);
		}
		return dateString;
	}
	
	public String getYYMMDDHH() {
		
		String dateString = new String();

		dateString = Integer.toString(this.year).substring(2,4);

		if (this.month < 10)
			dateString = dateString + "0" + Integer.toString(this.month);
		else
			dateString = dateString + Integer.toString(this.month);

		if (this.day < 10)
			dateString = dateString + "0" + Integer.toString(this.day);
		else {
			dateString = dateString + Integer.toString(this.day);
		}
		
		if (this.hour  < 10)
			dateString = dateString + "0" + Integer.toString(this.hour);
		else
			dateString = dateString + Integer.toString(this.hour);

		return dateString;
	}
	
	public String getYYYY() {
		return Integer.toString(this.year);
	}

	public String getChineseDate() {
		String dateString = this.year + "年";
		dateString = dateString + this.month + "月";
		dateString = dateString + this.day + "日，";
		dateString = dateString + "星期" + this.daysOfWeek[(this.now.get(GregorianCalendar.DAY_OF_WEEK) - 1)];

		return dateString;
	}
	

	/**
	 * 获取当前日期时间：2007-01-02 12:00:00
	 */
	
	/**
	 * 获取当前日期时间
	 * @return yyyy-MM-dd HH:mm:ss
	 */
	public static String getTodaytime() {
		GregorianCalendar cl = new GregorianCalendar();
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		try {
			return sdf.format(cl.getTime());
		} catch (Exception ex) {
			return null;
		}

	}
	
	public static String getTodaytime(String splitDate) {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy"+splitDate+"MM"+splitDate+"dd HH:mm:ss");
        Calendar cl = new GregorianCalendar();
        try {
            return sdf.format(cl.getTime());
       } catch(Exception ex) {
            return null;
       }        
	}

	/**
	 * 获取当前日期 ：2007-01-02
	 */
	public static String getToday() {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyy-MM-dd");

		Calendar cl = new GregorianCalendar();
		try {
			return sdf.format(cl.getTime());
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * 获取当前日期 ：20070102
	 */
	public static String getdayNumber() {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyyMMdd");

		Calendar cl = new GregorianCalendar();
		try {
			return sdf.format(cl.getTime());
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * 获取当前时间 ：12:00:00
	 */
	public static String getTime() {
		Calendar cl = new GregorianCalendar();
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"HH:mm:ss");

		try {
			return sdf.format(cl.getTime());
		} catch (Exception ex) {
			return null;
		}
	}
	/**
	 * 获取当前时间 ：20070102120000
	 */
	public static String getNumberDate() {
		Calendar cl = new GregorianCalendar();
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyyMMddHHmmss");

		try {
			return sdf.format(cl.getTime());
		} catch (Exception ex) {
			return null;
		}
	}
	/**
	 * 根据字符串日期返回日期对象
	 * 
	 * @param date
	 *            日期标示 2007-01-02
	 */
	public static Calendar getDate(String Date) {
		if (Date == null)
			return null;

		Date = Date.trim();
		if (Date.length() == 7) {
			Date += "-01";
		}

		StringTokenizer st = new StringTokenizer(Date, "-");
		int year = 2100;
		int month = 0;
		int day = 1;

		try {
			year = Integer.parseInt(st.nextToken());
			month = Integer.parseInt(st.nextToken()) - 1;
			day = Integer.parseInt(st.nextToken());
		} catch (Exception e) {
			return null;
		}
		return new GregorianCalendar(year, month, day);
	}

	/**
	 * 根据日期对象返回字符串日期
	 *  @param date  日期对象
	 *  @return yyyy-MM-dd;
	 */
	public static String getDateStr(Calendar Date) {
		if (Date == null)
			return "";
		return (new java.text.SimpleDateFormat("yyyy-MM-dd")).format(Date
				.getTime());
	}
	/**
	 * 根据日期对象返回字符串日期
	 *  @param date  日期对象
	 *  @return yyyy-MM-dd HH:mm:ss;
	 */
	public static String getDateTimeStr(Date Date) {
		if (Date == null)
			return "";
		return (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Date
				.getTime());
	}

	/**
	 * 根据日期字符串返回上月1号日期
	 * 
	 * @param date
	 *            日期字符串 2007-01-11
	 * @return 调整过得日期时间 2007-01-01
	 */
	public static String preMonthFirstDay() {
		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.MONTH, -1);
		calendar.set(Calendar.DATE, 1);
		return getDateStr(calendar);
	}

	/**
	 * 返回上月最后一天日期
	 * 
	 * @return 调整过得日期时间 2007-01-31
	 */
	public static String preMonthEndDay() {
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.DATE, 0);
		return getDateStr(calendar);
	}

	/**
	 * 根据日期字符串返回当前月1号日期
	 * 
	 * @param date
	 *            日期字符串 2007-01-11
	 * @return 调整过得日期时间 2007-01-01
	 */
	public static String monthFirstDay(String Date) {
		Calendar calendar = getDate(Date);
		calendar.set(Calendar.DATE, 1);
		return getDateStr(calendar);
	}

	/**
	 * 返回当前月1号日期
	 * 
	 * @return 调整过得日期时间 2007-01-01
	 */
	public static String currMonthFirstDay() {
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.DATE, 1);
		return getDateStr(calendar);
	}

	/**
	 * 返回上周周日日期
	 * 
	 * @return 调整过得日期时间 2007-01-01
	 */
	public static String preWeekFirstDay() {
		Calendar calendar = new GregorianCalendar();

		calendar.add(Calendar.DATE,
				(calendar.get(Calendar.DAY_OF_WEEK) * -1) - 6);
		return getDateStr(calendar);
	}

	/**
	 * 返回上周周六日期
	 * 
	 * @return 调整过得日期时间 2007-01-01
	 */
	public static String preWeekEndDay() {
		Calendar calendar = new GregorianCalendar();

		calendar.add(Calendar.DATE, calendar.get(Calendar.DAY_OF_WEEK) * -1);
		return getDateStr(calendar);
	}

	/**
	 * 返回当前周周日日期
	 * 
	 * @return 调整过得日期时间 2007-01-01
	 */
	public static String currWeekFirstDay() {
		Calendar calendar = new GregorianCalendar();

		calendar
				.add(Calendar.DATE, 1 + calendar.get(Calendar.DAY_OF_WEEK) * -1);
		return getDateStr(calendar);
	}

	/**
	 * 返回当前周周六日期
	 * 
	 * @return 调整过得日期时间 2007-01-01
	 */
	public static String currWeekEndDay() {
		Calendar calendar = new GregorianCalendar();

		calendar
				.add(Calendar.DATE, 7 + calendar.get(Calendar.DAY_OF_WEEK) * -1);
		return getDateStr(calendar);
	}
	
	/**
	 * 根据日期字符串返回当前月最后一天
	 * 
	 * @param date
	 *            日期字符串 2007-01-11
	 * @return 调整过得日期时间 2007-01-31
	 */
	public static String monthEndDay(String Date) {
		Calendar calendar = getDate(Date);
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DATE, 0);
		return getDateStr(calendar);
	}

	/**
	 * 返回当前月最后一天日期
	 * 
	 * @return 调整过得日期时间 2007-01-31
	 */
	public static String currMonthEndDay() {
		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DATE, 0);
		return getDateStr(calendar);
	}

	/**
	 * 在当天日期增加N年N月N日
	 * 
	 * @param year
	 *            增加N年
	 * @param month
	 *            增加N月
	 * @param day
	 *            增加N日
	 * @return 调整过得日期时间 2009-01-02
	 */
	public static String todayPlusDays(int year, int month, int day) {
		return datePlusDays(getToday(), year, month, day);
	}

	/**
	 * 在现有日期时间增加N年N月N日
	 * 
	 * @param date
	 *            日期标示 2007-01-02
	 * @param year
	 *            增加N年
	 * @param month
	 *            增加N月
	 * @param day
	 *            增加N日
	 * 
	 * @return 调整过得日期时间 2009-01-02
	 */
	public static String datePlusDays(String date, int year, int month, int day) {
		StringTokenizer datest = new StringTokenizer(plusDays(date + " "
				+ getTime(), year, month, day), " ");
		return datest.nextToken();
	}

	/**
	 * 在现有日期时间增加N年N月N日
	 * 
	 * @param dateTime
	 *            时间标示 2007-01-02 12:00:00
	 * @param year
	 *            增加N年
	 * @param month
	 *            增加N月
	 * @param day
	 *            增加N日
	 * 
	 * @return 调整过得日期时间 2009-01-02 12:00:00
	 */
	public static String plusDays(String dateTime, int year, int month, int day) {
		StringTokenizer datest = new StringTokenizer(dateTime, " ");

		int newyear = 2100;
		int newmonth = 0;
		int newday = 1;

		int newhour = 1;
		int newmin = 0;
		int newsed = 0;

		try {

			StringTokenizer dates = new StringTokenizer(datest.nextToken(), "-");

			newyear = Integer.parseInt(dates.nextToken());
			newmonth = Integer.parseInt(dates.nextToken()) - 1;
			newday = Integer.parseInt(dates.nextToken());

			StringTokenizer hours = new StringTokenizer(datest.nextToken(), ":");

			newhour = Integer.parseInt(hours.nextToken());
			newmin = Integer.parseInt(hours.nextToken());
			newsed = Integer.parseInt(hours.nextToken());

		} catch (Exception e) {
			e.printStackTrace();
		}

		Calendar calendar = new GregorianCalendar(newyear, newmonth, newday,
				newhour, newmin, newsed);

		calendar.add(Calendar.YEAR, year);
		calendar.add(Calendar.MONTH, month);
		calendar.add(Calendar.DATE, day);

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		try {
			return sdf.format(calendar.getTime());
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * 在现有日期时间增加N年N月N日
	 * 
	 * @param dateTime
	 *            时间标示 2007-01-02
	 * @param year
	 *            增加N年
	 * @param month
	 *            增加N月
	 * @param day
	 *            增加N日
	 * 
	 * @return 调整过得日期 2009-01-02 (不带时间)
	 */
	public static String plusDates(String dateTime, int year, int month, int day) {
		StringTokenizer datest = new StringTokenizer(dateTime, " ");

		int newyear = 2100;
		int newmonth = 0;
		int newday = 1;

		try {

			StringTokenizer dates = new StringTokenizer(datest.nextToken(), "-");

			newyear = Integer.parseInt(dates.nextToken());
			newmonth = Integer.parseInt(dates.nextToken()) - 1;
			newday = Integer.parseInt(dates.nextToken());

		} catch (Exception e) {
			e.printStackTrace();
		}

		Calendar calendar = new GregorianCalendar(newyear, newmonth, newday);

		calendar.add(Calendar.YEAR, year);
		calendar.add(Calendar.MONTH, month);
		calendar.add(Calendar.DATE, day);

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyy-MM-dd");
		try {
			return sdf.format(calendar.getTime());
		} catch (Exception ex) {
			return null;
		}
	}

	
	/**
	 * 判断是否 月底
	 */
	public   static   boolean   isMonthEnd(String date)
	{   
		Calendar   cal=DateUtils.getDate(date);  
        if(cal.get(Calendar.DATE)==cal.getActualMaximum(Calendar.DAY_OF_MONTH)) 
        {
        	return   true;  
        }      
        else
        {	
            return   false;   
        }    
}
	/**
	 * 
	 * @param sDate
	 *            :指定日期
	 * @param month
	 *            :加量的月
	 * @return
	 */
	public static String plusMonth(String sDate, int month) {
		Calendar cl = getDate(sDate);
		cl.add(Calendar.MONTH, month);
		return getDateStr(cl);
	}
	
	public static String getStartTime(String sDate) 
	{
		if(sDate == null || "".equals(sDate))
		{
			return "";
		}
		
		String[] temp	=	sDate.split(" ");
		
		return temp[0]+" 00:00:00";
	}
	
	public static String getEndTime(String sDate) 
	{
		
		if(sDate == null || "".equals(sDate))
		{
			return "";
		}
		
		String[] temp	=	sDate.split(" ");
		
		return temp[0]+" 23:59:59";
	}

	
	/**
	 * 
	 * @param bgdate
	 * @param endate
	 * @return
	 */
	public static boolean isAfterDate(String bgdate, String endate) {
		String[] bg = bgdate.split(" ", -2);
		String[] end = endate.split(" ", -2);

		Calendar bgCal = getDate(bg[0]);
		Calendar edCal = getDate(end[0]);

		return bgCal.after(edCal);
	}
	/**
	 * 
	 * @param bgdate
	 * @param endate
	 * @return
	 */
	public static boolean isAfterToDay(String date) {
		String[] end = date.split(" ", -2);

		Calendar bgCal = getDate(getToday());
		Calendar edCal = getDate(end[0]);

		return bgCal.after(edCal);
	}
	
	public static boolean isEqualsDate(String bgdate, String endate) {
		String[] bg = bgdate.split(" ", -2);
		String[] end = endate.split(" ", -2);

		Calendar bgCal = DateUtils.getDate(bg[0]);
		Calendar edCal = DateUtils.getDate(end[0]);

		return bgCal.equals(edCal);
	}
	/**
	 * 返回中文星期
	 * @return
	 */
	public static String getWeekDay()
	{
		Calendar calendar = new GregorianCalendar();

		int week = calendar.get(Calendar.DAY_OF_WEEK)-1;
		
		String[] weeks = {"日","一","二","三","四","五","六"};
		
		String weekDay = "星期" + weeks[week];
		
		return weekDay;
	}
	/**
	 * 返回日期上午或下午
	 * @return
	 */
	public static String getTimeSplite()
	{
		String timesp ="";
		
		Calendar calendar = new GregorianCalendar();

		int hour = calendar.get(Calendar.HOUR);
		
		if (hour>=0 && hour<8)
			timesp ="早上";
		
		if (hour>=8 && hour<12)
			timesp ="上午";
		
		if (hour>=12 && hour<18)
			timesp ="下午";
		
		if (hour>=18 && hour<24)
			timesp ="晚上";
			
		return timesp;
	}
	
	/**
	 * 根据毫秒数获取时间，获取的是短日期格式
	 * @param mills
	 * @return
	 */
	public static String getDateInMillis(long mills) {
		String pattern = "yyyy-MM-dd";
		return getTimeInMillis(pattern,mills);
	}
	
	/**
	 * 根据毫秒数获取时间，获取的是长日期格式
	 * @param mills
	 * @return
	 */
	public static String getDateTimeInMillis(long mills) {
		String pattern = "yyyy-MM-dd hh:mm:ss";
		return getTimeInMillis(pattern,mills);
	}
	
	/**
	 * 根据毫秒数和日期格式获取时间
	 * @param pattern
	 * @param mills
	 * @return
	 */
	private static String getTimeInMillis(String pattern,long mills){
		String strRetDeat = "";
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(mills);
		Date date = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		strRetDeat = format.format(date);
		return strRetDeat;
	}
	
	/**
	 * 根据毫秒数转 Date
	 * @param mills
	 * @return
	 */
	public static Date toDateInMillis(long mills) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(mills);
		Date date = calendar.getTime();
		return date;
	}
	
	/**
	 * 
	 * @param s yyyyMMdd HH:mm:ss
	 * @return
	 */
	public static Date getYYYYMMDDDate(String s) {
		// 2007-1-26 23:59:12
		try {
			if (s==null||"".equals(s))
				return null;
			if (s.length() < 11) {
				s = s + " 01:01:01";
			}
			SimpleDateFormat simpledateformat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date date = simpledateformat.parse(s);
			return date;
		} catch (Exception _ex) {
			_ex.printStackTrace();
			return null;
		}
	}

	/**
	 * is the number of hours since the  start of January 1 of the current year (first 
	   hour of January 1 = 0001) expressed as digits.
	 * @return
	 */
	public int getNumber_January1_of_hours() {
		
		try {
			SimpleDateFormat simpledateformat = new SimpleDateFormat(
					"yyyyMMdd HH:mm:ss");
			Date currDate = new Date();
			
			Date sDate = simpledateformat.parse(this.year + "0101 00:00:00");
			
			 //毫秒ms
            long diff = currDate.getTime() - sDate.getTime();
            
            return (int) (diff / (60 * 60 * 1000));
           
		} catch (ParseException e) {
			return -1;
		}
	}

	public int getHour() {
		return this.hour;
	}

	public int getMinute() {
		return this.minute;
	}

	public int getDay() {
		return this.day;
	}

	public int getMonth() {
		return this.month;
	}

	public int getYear() {
		return this.year;
	}
	
	public int getMillisecond() {
		return this.millisecond;
	}

	public static Date parseDate(String s, String pattern) {
		try {
			if (s == null || "".equals(s)) {
				return null;
			}
			SimpleDateFormat simpledateformat = new SimpleDateFormat(pattern);
			Date date = simpledateformat.parse(s);
			return date;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 计算两个时间差
	 */
	public static String getDatePoor(Date endDate, Date nowDate)
	{
		long nd = 1000 * 24 * 60 * 60;
		long nh = 1000 * 60 * 60;
		long nm = 1000 * 60;
		// long ns = 1000;
		// 获得两个时间的毫秒时间差异
		long diff = endDate.getTime() - nowDate.getTime();
		// 计算差多少天
		long day = diff / nd;
		// 计算差多少小时
		long hour = diff % nd / nh;
		// 计算差多少分钟
		long min = diff % nd % nh / nm;
		// 计算差多少秒//输出结果
		// long sec = diff % nd % nh % nm / ns;
		return day + "天" + hour + "小时" + min + "分钟";
	}

	/**
	 * @param timeValue
	 * 返回格式时间 yyyy-MM-dd hh:mm:ss：000
	 * @return
	 */
	public static Date getNewDateTime(long timeValue) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeValue);
		calendar.set(Calendar.MILLISECOND,0);
		Date date = calendar.getTime();
		return date;
	}
}
