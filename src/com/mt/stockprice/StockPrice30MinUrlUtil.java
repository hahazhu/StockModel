package com.mt.stockprice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mt.common.Util;
import com.mt.sql.batchUpdate;

public class StockPrice30MinUrlUtil {
	static String sql = "insert into stock_thirty(stock_id,market,open,high,close,low,volume,chg,percent,turnrate,ma5,ma10,ma20,ema12,ema26,dif,dea,macd,t_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public static void importHisFromListAfterIndex(int first,List stockList)
			throws Exception {
		first = first < 0 ? 0 : first;
		for (int i = first; i < stockList.size(); i++) {
			final String stockCode = (String) stockList.get(i);
			long start = System.currentTimeMillis();
			List<List> allHisStockInfoByCode = getAllHisStockInfoByCode(stockCode);
			long cost = System.currentTimeMillis()-start;
			System.err.println("get "+stockCode+ " cost:"+cost );
			start = System.currentTimeMillis();
			batchUpdate.batchUpdate(sql, allHisStockInfoByCode, 19);
			cost = System.currentTimeMillis()-start;
			System.err.println("insert "+stockCode+ " cost:"+cost );
		}
	}
	public static void importHisFromListAfterCode(String code,List stockList)
			throws Exception {
		importHisFromListAfterIndex(getIndexByCode(code,stockList),stockList);
	}
	private static int getIndexByCode(String stock,List stockList){
		for(int i=0;i<stockList.size();i++){
			if(((String) stockList.get(i)).contains(stock))
				return i+1;
		}
		return 0;
	}
	
	public static List<List> getAllHisStockInfoByCode(String stockCode) throws Exception {
		List<List> stockAllHisInfo = new ArrayList<List>();
		URL url = new URL(
				"http://api.finance.ifeng.com/index.php/akmin?scode="
						+ stockCode + "&type=30");

		int n=0;
		StringBuffer sb = new StringBuffer();
		while(n==0){
			try {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(10000);
				connection.setReadTimeout(5000);
				connection
				.setRequestProperty(
						"Cookie",
						"prov=cn021; city=021; weather_city=sh; region_ip=114.80.241.x; region_ver=1.2; vjuids=-34559dd01.149c84f40d7.0.6e2d2f92; userid=1416404878018_ufe8j03248; user_saw_channel_map=%2Cstock%3A%u80A1%u7968%3A1416404892949; user_saw_stock_map=%2Csh600718%3A%u4E1C%u8F6F%u96C6%u56E2%3A1416495311254%2Csz000055%3A%u65B9%u5927%u96C6%u56E2%3A1417185129381; vjlast=1416404878.1417185130.11; BIGipServerpool_caijing_flash=471474186.20480.0000");
				connection.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				connection.disconnect();
				n=1;
			} catch (Exception e) {
				System.out.println("connect lose.....sleeping 10s");
				Thread.sleep(10000);
			}
		}
		System.err.println(stockCode);
		System.err.println(sb);
		if(sb!=null&&!sb.toString().equals("")){
			JSONObject jb = new JSONObject(sb.toString());
			JSONArray object = jb.getJSONArray("record");
			double lastEma12=0;double lastEma26=0;double lastDea=0;
			for (int i = 0; i < object.length(); i++) {
				List stockInfo = new ArrayList();
				stockInfo.add(stockCode.substring(2));
				stockInfo.add(stockCode.substring(0, 2));
				JSONArray object2 = (JSONArray) object.get(i);
				String time = (String) object2.get(0);
				
				String open = (String) object2.get(1);
				stockInfo.add(open);
				String high = (String) object2.get(2);
				stockInfo.add(high);
				String close = (String) object2.get(3);
				stockInfo.add(close);
				String low = (String) object2.get(4);
				stockInfo.add(low);
				try{
					Double volume = (Double) object2.get(5);
					stockInfo.add(volume);
				}catch(ClassCastException e){
					//e.printStackTrace();
					Integer volume = (Integer) object2.get(5);
					stockInfo.add(volume);
				}
				String chg = (String) object2.get(6);
				stockInfo.add(chg);
				try{
					Double percent = (Double) object2.get(7);
					stockInfo.add(percent);
				}catch(ClassCastException e){
					//e.printStackTrace();
					Integer percent = (Integer ) object2.get(7);
					stockInfo.add(percent);
				}
				try{
					Double turnrate = (Double) object2.get(14);
					stockInfo.add(turnrate);
				}catch(ClassCastException e){
					//e.printStackTrace();
					Integer turnrate= (Integer ) object2.get(14);
					stockInfo.add(turnrate);
				}
				String ma5 = (String) object2.get(8);
				stockInfo.add(ma5);
				String ma10 = (String) object2.get(9);
				stockInfo.add(ma10);
				String ma20 = (String) object2.get(10);
				stockInfo.add(ma20);
				
				
				if(i==0){
					stockInfo.add(close);
					stockInfo.add(close);
					stockInfo.add("0");
					stockInfo.add("0");
					stockInfo.add("0");
					lastEma12 =Double.parseDouble(close);
					lastEma26 = Double.parseDouble(close);
					lastDea = 0;
				}else{
					List result=Util.getMacd(lastEma12, lastEma26, lastDea,Double.parseDouble(object2.get(3).toString()));
					stockInfo.add(result.get(0));
					stockInfo.add(result.get(1));
					stockInfo.add(result.get(2));
					stockInfo.add(result.get(3));
					stockInfo.add(result.get(4));
					lastEma12 =Double.parseDouble(result.get(0).toString());
					lastEma26 =Double.parseDouble(result.get(1).toString());
					lastDea = Double.parseDouble(result.get(3).toString());
				}
				stockInfo.add(time);
				stockAllHisInfo.add(stockInfo);
			}
		}
		System.out.println(stockCode+"::::::::::");
		return stockAllHisInfo;
	}
	
	//每日增量的
	
	
	public static List<List> getTodayStockInfo(int first,List stockList)
			throws Exception {
		List<List> stockInfo = new ArrayList<List>();
		first = first < 0 ? 0 : first;
		for (int i = first; i < stockList.size(); i++) {
			stockInfo.add(getStockInfoByCode((String)stockList.get(i)));
		}
		return stockInfo;
	}
	private static List getStockInfoByCode(String stockCode)
			throws IOException {
		// 仅仅打印
		List stockInfo =new ArrayList();
		stockInfo.add(stockCode.substring(2));
		stockInfo.add(stockCode.substring(0, 2));
		stockInfo.add(Util.GetNowDate());
		URL url = new URL("http://hq.sinajs.cn/?list=" + stockCode);
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(5000);
		BufferedReader br = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String line = null;
		StringBuffer sb = new StringBuffer();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		if (sb.length() > 0) {
			String rs = sb.toString();
			rs = rs.substring(rs.indexOf("\"") + 1, rs.lastIndexOf("\""));
			String[] rss = rs.split(",");
			for (int i = 0; i < rss.length; i++) {
				System.out.print(rss[i] + "\t|");
				if(i==1||i==3||i==4||i==5||i==8){
					stockInfo.add(rss[i]);
				}
			}
			System.out.println("\n------------------------------------");
		}
		return stockInfo;
	}
	public static void main(String[] args) throws Exception{
		importHisFromListAfterIndex(0,new ArrayList(){{add("sz000055");}});
	}
}
