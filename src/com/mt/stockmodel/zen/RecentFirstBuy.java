package com.mt.stockmodel.zen;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mt.sql.MysqlUtil;
import com.mt.stockcode.StockCodeURLUtil;

public class RecentFirstBuy {
	public static boolean hasFirstBuy(final String stockCode) throws SQLException{
		MysqlUtil sql = new MysqlUtil();
		sql.getConnection();
		List<Map<String, Object>> result = sql.findModeResult("select high,low,ma5,ma10,macd,t_time from gao_stock.stock_thirty where stock_id = ? order by t_time desc ",
				new ArrayList(){{
			add(stockCode);
		}});
		if(result==null){
			return false;
		}
		int flag=0;//0-跌势ma5<ma10;1涨势-ma5>ma10
		int last_flag=-1;//0-跌势ma5<ma10;1涨势-ma5>ma10
		float high1=(float) 0;
		Float low1=(float) 0, high2=(float) 0, low2=(float) 0;
		List<Map> highLows = new ArrayList();
		int updown_flag=-1;
		int segment_count=0;//往前计算的波段数
		//一类买点，2种情况：1、目前处于跌势，本跌势波段有近期最低点。2、目前涨势，但X天前为跌势，且前几天的跌势波段为近期最低点。
		//近期为可选参数，x为可选参数。
		//近期也可为 最近Y个波段。
		float macd=0;
		float macd_avg=0;
		float macd_sum=0;
		int macd_count=0;
		String tmp_endtime="";
		for(Iterator<Map<String, Object>> it=result.iterator();it.hasNext();){
			Map row = it.next();
			String time=(String) row.get("T_TIME");
			BigDecimal ma5=(BigDecimal) row.get("MA5");
			BigDecimal ma10=(BigDecimal) row.get("MA10");
			BigDecimal high=(BigDecimal) row.get("HIGH");
			BigDecimal low=(BigDecimal) row.get("LOW");
			BigDecimal macdBD=(BigDecimal) row.get("MACD");
			high1 = high1>high.floatValue()?high1:high.floatValue();
			low1 = low1<low.floatValue()?low1:low.floatValue();


			if(last_flag==-1){//第一个时间点
				tmp_endtime=time;
				high1=high.floatValue();
				low1=low.floatValue();
				if(ma5.floatValue()<ma10.floatValue()){
					updown_flag=0;//当天为跌势
					last_flag=0;
					flag=0;
				}else {
					updown_flag=1;//当天为涨势
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
				tmp.put("high", high1);
				tmp.put("low", low1);
				tmp.put("endtime", tmp_endtime);
				tmp.put("starttime", time);
				tmp.put("flag", ""+flag);
				tmp.put("macd_avg", macd_avg);
				highLows.add(tmp);
//				System.err.println("starttime: "+time+" endtime: "+tmp_endtime+
//						" high:"+high1+" low:"+low1+" flag:"+last_flag+
//						" avg_macd:"+macd_avg);
				last_flag=flag;
//				System.err.println(macd_count);
//				System.err.println(macd_sum);
				macd_count=1;
				macd_sum=macdBD.floatValue();
				segment_count++;
				tmp_endtime=time;
			} else{
				macd_count++;
				macd=macdBD.floatValue();
				macd_sum+=macd;
				macd_avg=macd_sum/macd_count;
			}
			 
		}
		if(highLows==null||highLows.size()==0){
			return false;
		}
		Float l=(float) 0,macdF=(float) 0;
		if(updown_flag==0){
			Map lowest = highLows.get(0);
			l=(Float) lowest.get("low");
			macdF=(Float) lowest.get("macd_avg");
		}else{
			Map lowest = highLows.get(1);
			l=(Float) lowest.get("low");
			macdF=(Float) lowest.get("macd_avg");
		}
		for(int i=0;i<(highLows.size()>10?10:highLows.size());i++){
			Map tmp = highLows.get(i);
			Float l_tmp = (Float) tmp.get("low");
			Float macd_tmp = (Float) tmp.get("macd_avg");
			String flag_tmp = (String)tmp.get("flag");
			if(l_tmp<l||(Math.abs(macd_tmp)<0.6*Math.abs(macdF)&&"0".equals(flag_tmp))){
				return false;
			}
		}
		return true;
	}
	public static void main(String args[]) throws SQLException, IOException{
		//获取最新的全量股票代码
		List stockCodeList = StockCodeURLUtil.getStockListFromSina();
		for(Iterator it=stockCodeList.iterator();it.hasNext();){
			String code = (String) it.next();
			
			final boolean hasFirstBuy = hasFirstBuy(code.substring(2));
			if(hasFirstBuy){
				System.err.println("******************"+code);
			}
			//System.err.println(code+" "+hasFirstBuy);
		}
	}

}
