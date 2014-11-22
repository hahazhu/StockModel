package com.mt.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {
	/*
	 * 
	 * 获取当前日期
	 * 
	 */
	public static String GetNowDate(){   
	    String temp_str="";   
	    Date dt = new Date();   
	    //最后的aa表示“上午”或“下午”    HH表示24小时制    如果换成hh表示12小时制   
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");   
	    temp_str=sdf.format(dt);   
	    return temp_str;   
	} 
	
	/*
	 * 
	 * 将雪球的日期转为8位标准数据
	 * 
	 */
	public static String getDate8FromString(String in){
		Map monthTrans= new HashMap(){{
			put("Jan","01");
			put("Feb","02");
			put("Mar","03");
			put("Apr","04");
			put("May","05");
			put("Jun","06");
			put("Jul","07");
			put("Aug","08");
			put("Sep","09");
			put("Oct","10");
			put("Nov","11");
			put("Dec","12");
		}};
		String[] arr=in.split(" ");
		String year = arr[arr.length-1];
		String month = (String) monthTrans.get(arr[1]);
		String day = arr[2];
		return year+month+day;
	}
	
	/**
	 * 
	 * 计算MACD中的ema12，ema26，dif，dea，macd
	 * 
	 * param1-3 前一天的ema12，ema26，dea
	 * param5 当天收盘价
	 * 
	 */
	public static List getMacd(double lastEma12,double lastEma26,double lastDea,double thisClose){
		List thisMacd = new ArrayList();
		double ema12,ema26,dif,dea,macd;
		ema12 = lastEma12*11/13+thisClose*2/13;
		ema26 = lastEma26*25/27+thisClose*2/27;
		dif = ema12-ema26;
		dea = lastDea*8/10+dif*2/10;
		macd = 2*(dif-dea);
		
		thisMacd.add(ema12); //ema12
		thisMacd.add(ema26); //ema26
		thisMacd.add(dif);     //dea
		thisMacd.add(dea);	  //macd
		thisMacd.add(macd);	  //dif
		return thisMacd;
	}
}
