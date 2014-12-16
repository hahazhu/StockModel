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

public class v_buy {
	public static List total_time=new ArrayList();
	public static List special_time =new ArrayList();
	public static List recent_time =new ArrayList();
	public static List date_list =new ArrayList();
	public static String lately="20141201";
	public static double principle=1;
	public static double special_pro = 0.08;
	public static double big_drop = -8.0;
	public static double vol_time = 1;
	public static int n_days=5;
	public static int n_after=10;
	public static int after_n=n_after;	
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
		//��k��
		List<Map<String, Object>> result = sql.findModeResult("select high,low,ma5,ma10,macd,d_date,close,percent,volume from gao_stock.stock_day where stock_id = ? and d_date between ? and ? order by d_date  ",new ArrayList(){{
			add(stockCode);add(dateBegin);add(dateEnd);
		}});
		//30������
//		List<Map<String, Object>> result = sql.findModeResult("select high,low,ma5,ma10,macd,t_time,close,percent,volume from gao_stock.stock_thirty where stock_id = ?  order by t_time",new ArrayList(){{
//			add(stockCode);
//		}});
		int n=0; //�������Ƶ�����
		float last_volume=(float) 0;
		float last_percent=(float) 0;
		boolean last_big_drop=false;
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
			if((ma5.floatValue()>ma10.floatValue())
					&&percent.floatValue()<9.8
					&&last_big_drop&&(percent.floatValue()>-last_percent/2)
					&&(volume.floatValue()>vol_time*last_volume)){
				//v�ͷ�����ǰһ��������һ���ǻ�x���ϣ����ɽ�����y��
				if (last_big_drop){
					System.out.println(stockCode+":"+time+":������");
					date_list.add(Integer.parseInt(time));
					if (Integer.parseInt(time)>Integer.parseInt(lately)) {
						List array=new ArrayList();
						array.add(stockCode);
						array.add(time);
						array.add(after_pro);
						recent_time.add(array);
					}
					show_after=true;
					after_n=n_after;
				}
			}
			//�����Ҫ�������棬��ӡ��������֮���ÿһ������棬���������
			if(show_after){
				System.out.println("-----"+stockCode+":"+time+":"+percent);
				after_pro=(1+after_pro)*(1+percent.floatValue()/100)-1;
				after_n--;
				if((after_n==0)){
					show_after=false;
					System.out.println("---total-----"+stockCode+":"+time+":"+after_pro);
					principle*=(1+after_pro);//ģ����������ۼ�������
					List array=new ArrayList();
					array.add(stockCode);
					array.add(time);
					array.add(after_pro);
					total_time.add(array);
					//r������㼫��Ҫ��
					if((after_pro>special_pro)||(after_pro<-special_pro)){
						special_time.add(array);
					}
					after_pro=0;
				}
			}
			if(percent.floatValue()<=big_drop){
				 last_big_drop=true;
			}else {
				last_big_drop=false;
			}
			last_volume=volume.floatValue();
			last_percent=percent.floatValue();
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
		//��ȡ���µ�ȫ����Ʊ����
		MysqlUtil sql = new MysqlUtil();
		sql.getConnection();
		List<Map<String, Object>> result = sql.findModeResult("SELECT STOCK_ID FROM stock_info",null);
				for(Iterator<Map<String, Object>> it=result.iterator();it.hasNext();){
					Map row = it.next();
					String code=(String) row.get("stock_id");
					//System.out.println(code+"----------------------------------------");
					hasQsBuy(code, "20100101", "20121231");
				}
		
		//��ȡ����
				int total=total_time.size();
				System.out.println("�ܴ�����"+total);
				System.out.println("�ɹ�������"+getSucTime(total_time));
				System.out.println("ʧ�ܴ�����"+getFailTime(total_time));
				System.out.println("��ɹ���һ�Σ�"+total_time.get(getMaxSuc(total_time)));
				System.out.println("��ʧ�ܵ�һ�Σ�"+total_time.get(getMaxFail(total_time)));
				System.out.println("��ֵ��"+getMean(total_time));
			//	System.out.println(total_time);
				
				System.out.println("----------------------------�����Ǽ�����������ǵ����ϴ�����");
				System.out.println("�ܴ�����"+special_time.size());
				System.out.println("�ɹ�������"+getSucTime(special_time));
				System.out.println("ʧ�ܴ�����"+getFailTime(special_time));
				System.out.println("��ɹ���һ�Σ�"+special_time.get(getMaxSuc(special_time)));
				System.out.println("��ʧ�ܵ�һ�Σ�"+special_time.get(getMaxFail(special_time)));
				System.out.println("��ֵ��"+getMean(special_time));
				//System.out.println(special_time);
				
				System.out.println("��󱾽��Ϊ��"+principle);
				System.out.println("���ڷ���������Ϊ��"+recent_time);
				Collections.sort(date_list);
				System.out.println("����Ϊ��"+date_list);
	}
	
}
