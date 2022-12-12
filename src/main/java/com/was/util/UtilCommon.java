package com.was.util;

import java.text.SimpleDateFormat;
import java.util.*;


public class UtilCommon
{
    // 입력길이만큼 영문 대소문자 랜덤값 생성
	public static String getToken(int length) {

		StringBuffer buffer = new StringBuffer();
		Random random = new Random();

		String chars[] = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z".split(",");

		for (int i=0 ; i<length ; i++) {
			buffer.append(chars[random.nextInt(chars.length)]);
		}
		return buffer.toString();
	}
	
    // 시스템의 현재 일자를 얻는 메소드(yyyyMMdd)
    public static String getDate()
    {
        final int  millisPerHour = 60 * 60 * 1000;
        SimpleDateFormat sdf     = new SimpleDateFormat("yyyyMMdd");
        SimpleTimeZone timeZone  = new SimpleTimeZone( 9 * millisPerHour, "KST");
        sdf.setTimeZone(timeZone);

        Date             d       = new Date();
        String           sHHmmss = sdf.format(d);

        return sHHmmss;
    }
	
    //시스템의 현재시간을 얻는 메소드(HHmmss)
    public static String getHHmmss()
    {
        final int  millisPerHour = 60 * 60 * 1000;
        SimpleDateFormat sdf     = new SimpleDateFormat("HHmmss");
        SimpleTimeZone timeZone  = new SimpleTimeZone( 9 * millisPerHour, "KST");
        sdf.setTimeZone(timeZone);

        Date             d       = new Date();
        String           sHHmmss = sdf.format(d);

        return sHHmmss;
    }
    
    // 시스템의 현재 이전 날짜를 얻는 메소드(yyyyMMdd)
    public static String getBeforeDate(int day)
    {
        SimpleDateFormat dfd = new SimpleDateFormat("yyyyMMdd");
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DATE, -day);
		String date = dfd.format(cal.getTime());

        return date;
    }
    
    // 입력값의 추가된 분을 얻는 메소드(HHmmss)
	public static String addMinute(String tm, int minute) {

		SimpleDateFormat fmt = new SimpleDateFormat("HHmmss");
		Calendar cal = new GregorianCalendar();
		cal.setTime(dateParse(tm, "HHmmss"));
		cal.add(Calendar.MINUTE, minute);
		String strTime = fmt.format(new Date(cal.getTime().getTime()));

		return strTime;
	}
    
    public static Date dateParse(String date, String format)
    {
        if ( date == null )
            return null;

        try {
            return new SimpleDateFormat(format).parse(date);
        } catch(Exception e) {
            return null;
        }
    }
    
    // 주어진 길이에서 문자의 길이를 뺀 수만큼 '0'를 앞에 채워 문자를 리턴하는 메소드
    public static String fillZeros( int tLen, String source)
    {
        if (source.equals("null") || (source == null) )
            source = "";

        int mLen = source.trim().length();

        for ( int i=0 ; i < ( tLen - mLen ) ; i++ )
            source = "0" + source;

        return source;
    }
}