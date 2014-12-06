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

public class FirstBuy {
	public static float getLastFromList(List input,String typeName){
		float last =0;
		last = (Float) ((Map) input.get(input.size()-1)).get(typeName);
		return last;
	}
	public static float getHighFromList(List input,String typeName){
		float  high =0;
		for(int i =0 ;i<input.size();i++){
			float tmp= ((Float) ((Map) input.get(i)).get(typeName));
			high=high>tmp?high:tmp;
		}
		return high;
	}
	public static float getLowFromList(List input,String typeName){
		float  low =999;
		for(int i =0 ;i<input.size();i++){
			float tmp= ((Float) ((Map) input.get(i)).get(typeName));
			low=low<tmp?low:tmp;
		}
		return low;
	}
	public static void hasFirstBuy(final String stockCode,String dateBegin,String dateEnd) throws SQLException{
		MysqlUtil sql = new MysqlUtil();
		sql.getConnection();
		List<Map<String, Object>> result = sql.findModeResult("select high,low,ma5,ma10,macd,t_time from gao_stock.stock_thirty where stock_id = ? order by t_time  ",
				new ArrayList(){{
			add(stockCode);
		}});
		List trend_list= new ArrayList();//存放trend的list
		List bd_list= new ArrayList();//存放波段的list
		int trend_last_flag=-1;
		int n_trend = 1; //一个趋势中波段的次数
		int n_bd = 1; //一个趋势中波段的次数
		int flag=0;//0-跌势ma5<ma10;1涨势-ma5>ma10
		int last_flag=-1;//0-跌势ma5<ma10;1涨势-ma5>ma10； -1为初始值
		Float high_trend=(float) 0, high_bd=(float) 0 ; //前者为整个趋势中的最高点，后者为波段的最高点
		Float low_trend=(float) 9999, low_bd=(float) 9999; //前者为整个趋势中的最低点，后者为波段的最低点
		float macd_high_trend=(float) -999, macd_high_bd=(float) -999; //前者为整个趋势中的macd最高点，后者为波段的macd最高点
		float macd_low_trend=(float) 999, macd_low_bd=(float) 999; //前者为整个趋势中的macd最低点，后者为波段的macd最低点
		float last_macd_high= (float) 999, last_macd_low=-999;//用于记录上一次突破新高或者突破心底时候的macd值，用于和下一次创新高新低比较是否背离.初始值保证第一次绝对不会出现背离。
		for(Iterator<Map<String, Object>> it=result.iterator();it.hasNext();){
			Map trend = new HashMap();//存放整个趋势中的信息
			Map bd= new HashMap();//存放一个波段中的信息
			Map row = it.next();
			String time=(String) row.get("T_TIME");
			BigDecimal ma5=(BigDecimal) row.get("MA5");
			BigDecimal ma10=(BigDecimal) row.get("MA10");
			BigDecimal high=(BigDecimal) row.get("HIGH");
			BigDecimal low=(BigDecimal) row.get("LOW");
			BigDecimal macd=(BigDecimal) row.get("MACD");
			high_trend = high_trend>high.floatValue()?high_trend:high.floatValue();
			high_bd = high_bd>high.floatValue()?high_bd:high.floatValue();
			low_trend = low_trend<low.floatValue()?low_trend:low.floatValue();
			low_bd = low_bd<low.floatValue()?low_bd:low.floatValue();
			macd_high_trend = macd_high_trend>macd.floatValue()?macd_high_trend:macd.floatValue();
			macd_high_bd = macd_high_bd>macd.floatValue()?macd_high_bd:macd.floatValue();
			macd_low_trend = macd_low_trend<macd.floatValue()?macd_low_trend:macd.floatValue();
			macd_low_bd = macd_low_bd<macd.floatValue()?macd_low_bd:macd.floatValue();
			
			
			if(last_flag==-1){
				high_trend=high.floatValue();
				high_bd=high.floatValue();
				low_trend=low.floatValue();
				low_bd=low.floatValue();
				macd_high_trend=macd.floatValue();
				macd_low_trend = macd.floatValue();
				macd_high_bd=macd.floatValue();
				macd_low_bd=macd.floatValue();
				
				/*初始化中枢
				 * bgn
				 */
				trend.put("high", (float) 0);
				trend.put("low", (float) 9999);
				trend.put("date", time);
				trend.put("macd_high", (float) -999);
				trend.put("macd_low", (float) 999);
				trend.put("n",	 n_trend);
				if(ma5.floatValue()<ma10.floatValue()){
					last_flag=0;
					flag=0;
				}else{
					last_flag=1;
					flag=1;
				}
				trend.put("last_flag",last_flag);
				trend_list.add(trend);
				/*初始化中枢
				 * end
				 */
			}else{
				last_flag=flag;
				if(ma5.floatValue()<ma10.floatValue()){
					flag=0;
				}else{
					flag=1;
				}
			}
			if(flag!=last_flag){
				/*更新波段中的信息，并且置为初始值*bgn
				 */
				bd.put("high", high_bd);
				bd.put("low", low_bd);
				bd.put("date", time);
				bd.put("macd_high", macd_high_bd);
				bd.put("macd_low", macd_low_bd);
				bd.put("n",	 n_bd);
						/*如果创新高或者创新低，离开中枢（需要优化后续是否回落到中枢中）
						 * 		清楚原先bd和trend的list		
						 * 		trend_last_flag为中枢前的走势	
						 * 
						 * 		判断卖点和买点都是根据和上一次创新高或者新低的趋势的macd比较，看是否背离。
						 * 		当为创新高时，low变成初始化状态，
						 *		当为创新低时，high变成初始化状态。
						 */
						if(((last_flag==1)&&(high_bd>getHighFromList(bd_list, "high")))||((last_flag==0)&&(low_bd<getLowFromList(bd_list, "low")))){
							if((trend_last_flag==1)&&(last_flag==1)&&macd_high_bd<last_macd_high){
								System.out.println(time+" :卖点出现");
							}
							if((trend_last_flag==0)&&(last_flag==0)&&macd_low_bd>last_macd_low){
								System.out.println(time+" :买点出现");
							}
							if(last_flag==1){
								last_macd_high=macd_high_bd;
								last_macd_low=-999;
							}
							if(last_flag==0){
								last_macd_low=macd_low_bd;
								last_macd_high=999;
							}
							if(last_flag==trend_last_flag){
								n_trend++;
							}else if(trend_last_flag ==-1){
								n_trend=1;
							}else {
								
								trend.put("high", getHighFromList(bd_list, "high"));
								trend.put("low", getLowFromList(bd_list, "low"));
								trend.put("date", time);
								trend.put("macd_high", getHighFromList(bd_list, "macd_high"));
								trend.put("macd_low", getLowFromList(bd_list, "macd_low"));
								trend.put("n",	 n_trend);
								trend_list.add(trend);
								
								high_trend=(float) 0;low_trend=(float) -1; macd_high_trend=-999;macd_low_trend=999;
								bd_list.clear();
								n_bd=0;
							}
							trend_last_flag = last_flag;
						}else {
							n_bd++;
						}
				bd_list.add(bd);
				high_bd=(float) 0;low_bd=(float) 999; macd_high_bd=-999;macd_low_bd=999;
				/*更新波段中的信息，并且置为初始值*end
				 */
			}
			
		}
		System.out.println("End");;
	}

	public static void main(String args[]) throws SQLException, IOException{
			
		hasFirstBuy("300228", null, null);
		//获取最新的全量股票代码
//				List stockCodeList = StockCodeURLUtil.getStockListFromSina();
//				for(Iterator it=stockCodeList.iterator();it.hasNext();){
//					String code = (String) it.next();
//					System.out.println(code+"----------------------------------------");
//					hasFirstBuy(code.substring(2), null, null);
//				}
	}

}
