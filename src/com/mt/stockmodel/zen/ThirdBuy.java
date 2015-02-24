package com.mt.stockmodel.zen;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Days;

import com.mt.sql.MysqlUtil;

public class ThirdBuy {

	private static final String HIGH = "high";
	private static final String LOW = "low";
	private static final String TIME = "time";
	private static final String TREND = "trend";
	private static final String PERIOD_LENGTH = "periodLength";
	private static final double HIGH_LOW_RATIO = 1.3;
	private static final int BACKWARD_DAYS = 5;
	private static final int RECENT_BULL_DAYS = 3;
	private static final int RECENT_BEAR_DAYS = 6;
	private static final int BULL_FLAG = 1;

	public static boolean hasThirdBuy(final String stockCode,final String dateBegin,final String dateEnd) throws SQLException{
		List highLows = splitStockToPeriod(stockCode, dateBegin, dateEnd);
		if(highLows==null){
			return false;
		}
		//��ת�б�ʹ���б�ʱ�䵹�����У�
		Collections.reverse(highLows);
		/*
		 * ����ɸѡ��2��������̬��
		 * 1������֮����䣬ma5��δͻ��ma10����macdֵΪ���Ҿ���ֵ��С
		 * 2����������䣬ma5ͻ��ma10����ʱ�䲻�������2��
		 * ����ж�����ͻ��ǰ�����ࣿʹ�����¼��裺
		 * ������ε���ߵ�����͵���Ҫ30%����
		 * ����ǰ5�λ���50�������ղ���ƽ���������µ�������ߵ㲻���������εĵ͵�
		 */
		if(highLows.size()<8){
			return false;
		}
		Map fir = (Map) highLows.get(0);
		Map sec = (Map) highLows.get(1);
		Map third = (Map) highLows.get(2);
		//��������̬2��ma5ͻ��ma10��Ƚ�������
		//��̬2
		Float lowInThird;
		if((Integer) fir.get(TREND)==BULL_FLAG){//ĿǰΪ��
			if((Integer) fir.get(PERIOD_LENGTH)<RECENT_BULL_DAYS){//ֻ����2������
				if((Integer)sec.get(PERIOD_LENGTH)<RECENT_BEAR_DAYS){//֮ǰ���˲���6��
					Float high = (Float) third.get(HIGH);
					Float low = (Float) third.get(LOW);
					if((high/low)>HIGH_LOW_RATIO){
						lowInThird = (Float) sec.get(LOW);
						//��ǰ����3�����λ���50�������գ�֮ǰ����ߵ㲻��ͻ�������ص���ĵ͵�
						if(isBackwardSmooth(highLows, lowInThird,RECENT_BULL_DAYS)){
							return true;
						}else{
							System.err.println("��ǰ����3���� ��ߵ�������ص��ĵ͵��");
							return false;
						}
					}
					System.err.println("�����Ⲩ����30%");
				}
				System.err.println("��ֹ����5��");
			}
			System.err.println("��ֹ����2��"+"����"+(Integer) fir.get(PERIOD_LENGTH)+" "+stockCode);
		} 
		else{
			if((Integer) fir.get(PERIOD_LENGTH)<5){//ֻ����5������
				Float high = (Float) sec.get(HIGH);
				Float low = (Float) sec.get(LOW);
				
				if((high/low)>HIGH_LOW_RATIO){
					lowInThird = (Float) fir.get(LOW);
					//��ǰ����3�����λ���50�������գ�֮ǰ����ߵ㲻��ͻ�������ĵ͵�
					if(isBackwardSmooth(highLows, lowInThird,2)){
						return true;
					}else{
						System.err.println("��ǰ����3���� ��ߵ�������ص��ĵ͵��");
						return false;
					}
				}
				System.err.println("�����Ⲩ����30%");
			}
			System.err.println("��ֹ����5��"+"����"+(Integer) fir.get(PERIOD_LENGTH)+" "+stockCode);
		}
//		System.err.println("ĿǰΪ��");
		return false;
	}
	private static List splitStockToPeriod(final String stockCode,
			final String dateBegin, final String dateEnd) throws SQLException {
		MysqlUtil sql =	new MysqlUtil();
		sql.getConnection();
		List<Map<String, Object>> result = sql.findModeResult("select high,low,ma5,ma10,macd,d_date from gao_stock.stock_day"
				+ " where stock_id = ? and d_date between ? and ? order by d_date  ",new ArrayList(){{
			add(stockCode);
			add(dateBegin);
			add(dateEnd);
		}});
		if(result==null||result.size()<1){
			return null;
		}
		int flag=0;//0-����ma5<ma10;1����-ma5>ma10
		int last_flag=-1;//0-����ma5<ma10;1����-ma5>ma10
		float high1=(float) 0;
		Float low1=(float) 0, high2=(float) 0, low2=(float) 0;
		List highLows = new LinkedList();
		String startDate = dateBegin;
		int daycounts=0;
		for(Iterator<Map<String, Object>> it=result.iterator();it.hasNext();){
			Map row = it.next();
			String time=(String) row.get("D_DATE");
			BigDecimal ma5=(BigDecimal) row.get("MA5");
			BigDecimal ma10=(BigDecimal) row.get("MA10");
			BigDecimal high=(BigDecimal) row.get("HIGH");
			BigDecimal low=(BigDecimal) row.get("LOW");
			BigDecimal macd=(BigDecimal) row.get("MACD");
			
			high1 = high1>high.floatValue()?high1:high.floatValue();
			low1 = low1<low.floatValue()?low1:low.floatValue();
			daycounts++;
			if(last_flag==-1){
				high1=high.floatValue();
				low1=low.floatValue();
				if(ma5.floatValue()<ma10.floatValue()){
					last_flag=0;
					flag=0;
				}else{
					last_flag=1;
					flag=1;
				}
			}else{
				if(ma5.floatValue()<ma10.floatValue()){
					flag=0;
				}else{
					flag=1;
				}
			}
			if(flag!=last_flag){
				Map tmp = new HashMap();
				int periodLength = daycounts;
//				calDatePeriod(startDate,time);
				tmp.put(HIGH, high1);
				tmp.put(LOW, low1);
				tmp.put(TIME, time);
				tmp.put(TREND, flag);
				tmp.put(PERIOD_LENGTH, periodLength);
				highLows.add(tmp);
//				System.err.println("endtime: "+time+" high:"+high1+" low:"+low1+" trend:"+flag+" periodLength:"+periodLength);
				high1=0;
				low1=(float) 999;
				last_flag=flag;
				startDate = time;
				daycounts=0;
			}
			
		}
		Map tmp = new HashMap();
		tmp.put(HIGH, high1);
		tmp.put(LOW, low1);
		tmp.put(TIME, dateEnd);
		tmp.put(TREND, flag);
		tmp.put(PERIOD_LENGTH, daycounts);
		highLows.add(tmp);
		return highLows;
	}
	private static boolean isBackwardSmooth(List highLows, Float lowInThird,int start) {
		int daysBefore=0;
		for(int i=start;i<start+BACKWARD_DAYS;i++){
			Map tmp = (Map) highLows.get(i);
			if(((Float)tmp.get(HIGH))<lowInThird){
				daysBefore+=(Integer)tmp.get(PERIOD_LENGTH);
				if(daysBefore>50){
					return false;
				}
			}else{
				return false;
			}
		}
		return true;
	}
	public static int calDatePeriod(String dateBegin, String dateEnd){
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		try {
			DateTime begin = new DateTime(format.parse(dateBegin));
            DateTime end = new DateTime(format.parse(dateEnd));
            System.err.println(dateBegin+dateEnd);
			return Days.daysBetween(begin,end).getDays();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	public static void main(String args[]) throws SQLException{
		MysqlUtil sql =	new MysqlUtil();
		sql.getConnection();
		List<Map<String, Object>> result = sql.findModeResult("SELECT stock_id FROM gao_stock.stock_info ",null);
		List thirdStock = new LinkedList();
		for(Iterator it = result.iterator();it.hasNext();){
			Map tmp = (Map) it.next();
			if(hasThirdBuy((String)tmp.get("stock_id"),"20140101","20150228")){
//				System.err.println(tmp.get("stock_id")+":true:true:true:true:true");
				thirdStock.add(tmp.get("stock_id"));
			}else{
//				System.err.println(tmp.get("stock_id")+":false");
			}
		}
//		hasThirdBuy("600030", "20140101", "20141122");
		System.err.println(thirdStock);
	}

}
