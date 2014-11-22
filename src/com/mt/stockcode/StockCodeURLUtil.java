package com.mt.stockcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StockCodeURLUtil {
	final static int PAGE_NUM = 40;
	public static List getStockListFromSina() throws IOException {
		// TODO Auto-generated method stub
		List<String> codes = new ArrayList<String>() ;  
        URL url = null ;  
		for(int i=1; i <= PAGE_NUM ; i ++ ){  
             url = new URL("http://vip.stock.finance.sina.com.cn/q/go.php/vInvestConsult/kind/qgqp/index.phtml?s_i=&s_a=&s_c=&s_t=&s_z=&p="+i) ;   
             String code = getBatchStackCodes(url) ;  
             if(code.contains("sh")||code.contains("sz")){
            	 codes.addAll(handleStockCode(code)) ;
             }else{
             }
        }  
        
    	for(int i=1; i <= PAGE_NUM ; i ++ ){  
             url = new URL("http://vip.stock.finance.sina.com.cn/q/go.php/vInvestConsult/kind/qgqp/index.phtml?s_i=&s_a=&s_c=&s_t=sz_a&s_z=&p="+i) ;   
             String code = getBatchStackCodes(url) ;  
             codes.addAll(handleStockCode(code)) ;
        }  

        return codes ;  
		
	}
//  返回的值是一个js代码段  包括指定url页面包含的所有股票代码  
   public static  String getBatchStackCodes(URL url) throws IOException{  
        URLConnection connection = url.openConnection() ;  
        connection.setConnectTimeout(30000) ;  
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream())) ;  
        String line = null ;  
        StringBuffer sb = new StringBuffer() ;  
        boolean flag =false ;  
        while((line = br.readLine()) != null ){  
            if(line.contains("<script language=\"JavaScript\" id=\"hq_data_id\"") || flag){  
                sb.append(line) ;  
                flag = true ;  
            }  
            if(line.contains("</script>")){  
                flag =false ;  
                if(sb.length() > 0 ){  
                    if(sb.toString().contains("hq_data_id") && sb.toString().contains("list=")){  
                        break ;  
                    }else{  
                        sb.setLength(0) ;  
                    }  
                }  
            }  
        }  
        if(br != null ){  
            br.close() ;  
            br= null ;  
        }  
       return sb.toString() ;  
   } 
// 解析一组股票代码字符串   把code中包括的所有股票代码放入List中  
   public static List<String> handleStockCode(String code){  
       List<String> codes = null ;  
       int end = code.lastIndexOf("\"") ;  
           code = code.substring(0,end) ;  
       int start = code.lastIndexOf("list=") ;  
          code = code.substring(start) ;  
          code = code.substring(5) ;  
          codes = Arrays.asList(code.split(",")) ;  
       return codes ;  
   }
}
