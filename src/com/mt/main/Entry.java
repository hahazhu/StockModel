package com.mt.main;

import java.util.Iterator;
import java.util.List;

import com.mt.stockcode.StockCodeDbUtil;
import com.mt.stockcode.StockCodeURLUtil;
import com.mt.stockprice.StockPriceDbUtil;
import com.mt.stockprice.StockPriceURLUtil;

public class Entry {
	
	public static void main(String[] args) throws Exception {
		
		String flag="history";
		
		//��ȡ���µ�ȫ����Ʊ����
		List stockCodeList = StockCodeURLUtil.getStockListFromSina();
		//���
		StockCodeDbUtil.refreshStockCodeInDB(stockCodeList);
		
		//����Ƿ����¹�,���¹������������
		List newStock = StockCodeDbUtil.getNewStock();
		if(newStock!=null&&newStock.size()>0){
			System.err.println("���¹�");
			for(Iterator it=newStock.iterator();it.hasNext();){
				System.err.println("�¹�:"+it.next());
			}
			StockPriceURLUtil.importHisFromListAfterIndex(0,newStock);
		}
		
		
		if(flag.equals("history")){
			//�������
			StockPriceURLUtil.importHisFromListAfterIndex(0,stockCodeList );
//			StockPriceURLUtil.importHisFromListAfterCode("sh600530",stockCodeList );
		}else if(flag.equals("daily")){
			//ÿ������
			List<List> content=StockPriceURLUtil.getTodayStockInfo(0, stockCodeList);
			//���뵱�ռ۸�
			StockPriceDbUtil.importDailyPrice(content);
			//����macd��ָ��
			StockPriceDbUtil.calDailyPrice(stockCodeList);
			
		}
		
				
	}

	

}
