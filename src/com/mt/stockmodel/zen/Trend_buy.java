package com.mt.stockmodel.zen;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mt.sql.MysqlUtil;
import com.mt.stockcode.StockCodeURLUtil;

public class Trend_buy {
	public static List total_time=new ArrayList();
	public static List special_time =new ArrayList();
	public static List recent_time =new ArrayList();
	public static List date_list =new ArrayList();
	public static String lately="20141120";
	public static double principle=1;
	public static double special_pro = 0.08;
	public static int n_days=5;
	public static int after_n=5;	
	public static boolean between(float price,float last_price,float up_percent,float down_percent){
		boolean rst=false;
		if((price/last_price>=down_percent)&&(price/last_price<=up_percent)){
			return true;
		}
		return rst;
	}
	public static void hasQsBuy(final String stockCode,final String dateBegin ,final String dateEnd) throws SQLException{
		MysqlUtil sql = new MysqlUtil();
		sql.getConnection();
		//日k线
		List<Map<String, Object>> result = sql.findModeResult("select high,low,ma5,ma10,macd,d_date,close,percent,volume from gao_stock.stock_day_backup where stock_id = ? and d_date between ? and ? order by d_date  ",new ArrayList(){{
			add(stockCode);add(dateBegin);add(dateEnd);
		}});
		//30分钟线
//		List<Map<String, Object>> result = sql.findModeResult("select high,low,ma5,ma10,macd,t_time,close,percent,volume from gao_stock.stock_thirty where stock_id = ?  order by t_time",new ArrayList(){{
//			add(stockCode);
//		}});
		int n=0; //连续趋势的天数
		float close_n=(float) 0;
		float last_volume=(float) 0;
		float last_macd=(float) 0;
		double after_pro=0;
		boolean show_after =false;
		for(Iterator<Map<String, Object>> it=result.iterator();it.hasNext();){
			Map row = it.next();
			String time=(String) row.get("D_DATE");
			//String time=(String) row.get("T_TIME");
			BigDecimal ma5=(BigDecimal) row.get("MA5");
			BigDecimal ma10=(BigDecimal) row.get("MA10");
			BigDecimal high=(BigDecimal) row.get("HIGH");
			BigDecimal low=(BigDecimal) row.get("LOW");
			BigDecimal macd=(BigDecimal) row.get("MACD");
			BigDecimal close=(BigDecimal) row.get("CLOSE");
			BigDecimal percent=(BigDecimal) row.get("PERCENT");
			BigDecimal volume=(BigDecimal) row.get("VOLUME");
			if(last_volume!=0){
				//小碎步条件：涨幅在2个点之内，成交量在前一日的90%到130%之间，均线向上；
				//第一天无所谓
				if (between(percent.floatValue(),(float) 1,(float) 2,(float) 0)&&
					between(volume.floatValue(),last_volume,(float) 1.3,(float) 0.9)&&
					ma5.floatValue()>ma10.floatValue()&&
					n>0){
					n++;
				}else if (between(percent.floatValue(),(float) 1,(float) 2,(float) 0)&&
						n==0){
						n++;
				}else if (between(percent.floatValue(),(float) 1,(float) 2,(float) 0)&&
						n>0){
						n=1;
				}else{
					n=0;
				}
			}
			//如果需要计算收益，打印出买点出现之后的每一天的收益，并纳入计算
			if(show_after){
				System.out.println("-----"+stockCode+":"+time+":"+percent);
				after_pro=(1+after_pro)*(1+percent.floatValue()/100)-1;
				after_n--;
				if((after_n==0)){
					show_after=false;
					System.out.println("---total-----"+stockCode+":"+time+":"+after_pro);
					principle*=(1+after_pro);//模拟操作，看累计收益率
					List array=new ArrayList();
					array.add(stockCode);
					array.add(time);
					array.add(after_pro);
					total_time.add(array);
					//r如果满足极限要求
					if((after_pro>special_pro)||(after_pro<-special_pro)){
						special_time.add(array);
					}
					after_pro=0;
				}
			}
			if(n==n_days){
				System.out.println(stockCode+":"+time+":买点出现");
				date_list.add(Integer.parseInt(time));
				if (Integer.parseInt(time)>Integer.parseInt(lately)) {
					List array=new ArrayList();
					array.add(stockCode);
					array.add(time);
					array.add(after_pro);
					recent_time.add(array);
				}
				show_after=true;
				after_n=10;
				n=0;
			}
			last_macd=macd.floatValue();
			last_volume=volume.floatValue();
		}
	}
	
	public static int getSucTime(List array){
			int rst=0;
			for(int i=0;i<array.size();i++){
				double pro=Double.parseDouble(((List)array.get(i)).get(2).toString());
				if (pro>0){
					rst++;
				}
			}
			return rst;
	}
	public static int getFailTime(List array){
		int rst=0;
		for(int i=0;i<array.size();i++){
			double pro=Double.parseDouble(((List)array.get(i)).get(2).toString());
			if (pro<0){
				rst++;
			}
		}
		return rst;
}
	public static int getMaxSuc(List array){
		double profit=0;
		int rst=0;
		for(int i=0;i<array.size();i++){
			double pro=Double.parseDouble(((List)array.get(i)).get(2).toString());
			if (profit<pro){
				rst=i;
				profit=pro;
			}
		}
		return rst;
}
	public static int getMaxFail(List array){
		double profit=0;
		int rst=0;
		for(int i=0;i<array.size();i++){
			double pro=Double.parseDouble(((List)array.get(i)).get(2).toString());
			if (profit>pro){
				rst=i;
				profit=pro;
			}
		}
		return rst;
}
	public static double getMean(List array){
		double mean=0;
		for(int i=0;i<array.size();i++){
			double pro=Double.parseDouble(((List)array.get(i)).get(2).toString());
			mean+=pro;
		}
		return mean/array.size();
	}
	
	public static void main(String args[]) throws SQLException, IOException{
		
//		hasQsBuy("000430", "20141113", "20141201");
		//获取最新的全量股票代码
		MysqlUtil sql = new MysqlUtil();
		sql.getConnection();
		List<Map<String, Object>> result = sql.findModeResult("SELECT STOCK_ID FROM stock_info",null);
				for(Iterator<Map<String, Object>> it=result.iterator();it.hasNext();){
					Map row = it.next();
					String code=(String) row.get("stock_id");
					//System.out.println(code+"----------------------------------------");
					hasQsBuy(code, "20071031", "20141231");
				}
		
		//获取总数
				int total=total_time.size();
				System.out.println("总次数："+total);
				System.out.println("成功次数："+getSucTime(total_time));
				System.out.println("失败次数："+getFailTime(total_time));
				System.out.println("最成功的一次："+total_time.get(getMaxSuc(total_time)));
				System.out.println("最失败的一次："+total_time.get(getMaxFail(total_time)));
				System.out.println("均值："+getMean(total_time));
				System.out.println(total_time);
				
				System.out.println("----------------------------下面是极端情况，即涨跌幅较大的情况");
				System.out.println("总次数："+special_time.size());
				System.out.println("成功次数："+getSucTime(special_time));
				System.out.println("失败次数："+getFailTime(special_time));
				System.out.println("最成功的一次："+special_time.get(getMaxSuc(special_time)));
				System.out.println("最失败的一次："+special_time.get(getMaxFail(special_time)));
				System.out.println("均值："+getMean(special_time));
				//System.out.println(special_time);
				
				System.out.println("最后本金变为："+principle);
				System.out.println("近期符合条件的为："+recent_time);
				Collections.sort(date_list);
				System.out.println("日期为："+date_list);
	}
	
}
