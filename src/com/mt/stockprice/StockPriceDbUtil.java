package com.mt.stockprice;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mt.common.Util;
import com.mt.sql.MysqlUtil;
import com.mt.sql.batchUpdate;

public class StockPriceDbUtil {
	private static String batchSql="insert into stock_day (stock_id,market,d_date,open,close,high,low,volume) values(?,?,?,?,?,?,?,?)";
	public static void importDailyPrice(List content) {
		// TODO Auto-generated method stub
		MysqlUtil jdbcUtils = new MysqlUtil();  
		jdbcUtils.getConnection();  
		String sql = "delete FROM stock_day where d_date=?"; 
		List<Object> params = new ArrayList<Object>(); 
		params.add(Util.GetNowDate()); 
		try {
			jdbcUtils.updateByPreparedStatement(sql, params);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		batchUpdate.batchUpdate(batchSql, content, 8);
		
		
	}
	public static void calDailyPrice(List codes) throws SQLException {
		// TODO Auto-generated method stub
		MysqlUtil jdbcUtils = new MysqlUtil();  
		jdbcUtils.getConnection();  
		for(int i=0;i<codes.size();i++){
			String call = "upDayStockInfo"; 
	        List list = new ArrayList();
	        list.add(codes.get(i).toString().substring(2));
	        list.add(Util.GetNowDate());
			boolean res = jdbcUtils.callProc(call, list);
			System.out.println(codes.get(i).toString()+","+res);
		}	
	}

}
