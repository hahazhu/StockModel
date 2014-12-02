package com.mt.stockmodel.zen;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mt.sql.MysqlUtil;

public class ThirdBuy {
	public static void hasThirdBuy(final String stockCode,String dateBegin,String dateEnd) throws SQLException{
		MysqlUtil sql = new MysqlUtil();
		sql.getConnection();
		List<Map<String, Object>> result = sql.findModeResult("select high,low,ma5,ma10,macd,t_time from gao_stock.stock_thirty where stock_id = ? order by t_time  ",new ArrayList(){{
			add(stockCode);
		}});
		int flag=0;//0-跌势ma5<ma10;1涨势-ma5>ma10
		int last_flag=-1;//0-跌势ma5<ma10;1涨势-ma5>ma10
		float high1=(float) 0;
		Float low1=(float) 0, high2=(float) 0, low2=(float) 0;
		List highLows = new ArrayList();
		for(Iterator<Map<String, Object>> it=result.iterator();it.hasNext();){
			Map row = it.next();
			String time=(String) row.get("T_TIME");
			BigDecimal ma5=(BigDecimal) row.get("MA5");
			BigDecimal ma10=(BigDecimal) row.get("MA10");
			BigDecimal high=(BigDecimal) row.get("HIGH");
			BigDecimal low=(BigDecimal) row.get("LOW");
			high1 = high1>high.floatValue()?high1:high.floatValue();
			low1 = low1<low.floatValue()?low1:low.floatValue();
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
				tmp.put("high", high1);
				tmp.put("low", low1);
				tmp.put("time", time);
				highLows.add(tmp);
				System.err.println("time: "+time+" high:"+high1+" low:"+low1);
				last_flag=flag;
			}
			
		}
	}
	public static void main(String args[]) throws SQLException{
		hasThirdBuy("000055", null, null);
	}

}
