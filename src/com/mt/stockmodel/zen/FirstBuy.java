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
		List trend_list= new ArrayList();//���trend��list
		List bd_list= new ArrayList();//��Ų��ε�list
		int trend_last_flag=-1;
		int n_trend = 1; //һ�������в��εĴ���
		int n_bd = 1; //һ�������в��εĴ���
		int flag=0;//0-����ma5<ma10;1����-ma5>ma10
		int last_flag=-1;//0-����ma5<ma10;1����-ma5>ma10�� -1Ϊ��ʼֵ
		Float high_trend=(float) 0, high_bd=(float) 0 ; //ǰ��Ϊ���������е���ߵ㣬����Ϊ���ε���ߵ�
		Float low_trend=(float) 9999, low_bd=(float) 9999; //ǰ��Ϊ���������е���͵㣬����Ϊ���ε���͵�
		float macd_high_trend=(float) -999, macd_high_bd=(float) -999; //ǰ��Ϊ���������е�macd��ߵ㣬����Ϊ���ε�macd��ߵ�
		float macd_low_trend=(float) 999, macd_low_bd=(float) 999; //ǰ��Ϊ���������е�macd��͵㣬����Ϊ���ε�macd��͵�
		float last_macd_high= (float) 999, last_macd_low=-999;//���ڼ�¼��һ��ͻ���¸߻���ͻ���ĵ�ʱ���macdֵ�����ں���һ�δ��¸��µͱȽ��Ƿ���.��ʼֵ��֤��һ�ξ��Բ�����ֱ��롣
		for(Iterator<Map<String, Object>> it=result.iterator();it.hasNext();){
			Map trend = new HashMap();//������������е���Ϣ
			Map bd= new HashMap();//���һ�������е���Ϣ
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
				
				/*��ʼ������
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
				/*��ʼ������
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
				/*���²����е���Ϣ��������Ϊ��ʼֵ*bgn
				 */
				bd.put("high", high_bd);
				bd.put("low", low_bd);
				bd.put("date", time);
				bd.put("macd_high", macd_high_bd);
				bd.put("macd_low", macd_low_bd);
				bd.put("n",	 n_bd);
						/*������¸߻��ߴ��µͣ��뿪���ࣨ��Ҫ�Ż������Ƿ���䵽�����У�
						 * 		���ԭ��bd��trend��list		
						 * 		trend_last_flagΪ����ǰ������	
						 * 
						 * 		�ж��������㶼�Ǹ��ݺ���һ�δ��¸߻����µ͵����Ƶ�macd�Ƚϣ����Ƿ��롣
						 * 		��Ϊ���¸�ʱ��low��ɳ�ʼ��״̬��
						 *		��Ϊ���µ�ʱ��high��ɳ�ʼ��״̬��
						 */
						if(((last_flag==1)&&(high_bd>getHighFromList(bd_list, "high")))||((last_flag==0)&&(low_bd<getLowFromList(bd_list, "low")))){
							if((trend_last_flag==1)&&(last_flag==1)&&macd_high_bd<last_macd_high){
								System.out.println(time+" :�������");
							}
							if((trend_last_flag==0)&&(last_flag==0)&&macd_low_bd>last_macd_low){
								System.out.println(time+" :������");
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
				/*���²����е���Ϣ��������Ϊ��ʼֵ*end
				 */
			}
			
		}
		System.out.println("End");;
	}

	public static void main(String args[]) throws SQLException, IOException{
			
		hasFirstBuy("300228", null, null);
		//��ȡ���µ�ȫ����Ʊ����
//				List stockCodeList = StockCodeURLUtil.getStockListFromSina();
//				for(Iterator it=stockCodeList.iterator();it.hasNext();){
//					String code = (String) it.next();
//					System.out.println(code+"----------------------------------------");
//					hasFirstBuy(code.substring(2), null, null);
//				}
	}

}
