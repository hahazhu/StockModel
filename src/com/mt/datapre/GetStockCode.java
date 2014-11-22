package com.mt.datapre;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetStockCode {
    // ����һ���Ʊ�����ַ���   ��code�а��������й�Ʊ�������List��  
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
      
    //   ���ص�ֵ��һ��js�����  ����ָ��urlҳ����������й�Ʊ����  
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
      
    // ��ȡ����38Ҳ�����й�Ʊ����  
    public static List<String> getAllStackCodes(String db) throws IOException{  
        List<String> codes = new ArrayList<String>() ;  
        int i =1 ;  
        URL url = null ;  
        for(i=1; i <= 40 ; i ++ ){  
             url = new URL("http://vip.stock.finance.sina.com.cn/q/go.php/vInvestConsult/kind/qgqp/index.phtml?s_i=&s_a=&s_c=&s_t=&s_z=&p="+i) ;   
             String code = getBatchStackCodes(url) ;  
             if(code.contains("sh")||code.contains("sz")){
            	 codes.addAll(handleStockCode(code)) ;
             }else{
            	 System.out.println("�����й�Ʊ����\n");
             }
        }  
        
    	for(i=1; i <= 40 ; i ++ ){  
             url = new URL("http://vip.stock.finance.sina.com.cn/q/go.php/vInvestConsult/kind/qgqp/index.phtml?s_i=&s_a=&s_c=&s_t=sz_a&s_z=&p="+i) ;   
//                 url = new URL("http://vip.stock.finance.sina.com.cn/q/go.php/vIR_CustomSearch/index.phtml?p="+i) ;
             String code = getBatchStackCodes(url) ;  
             codes.addAll(handleStockCode(code)) ;
        }  
        if(! ( new File(db) ).exists() )  
            saveStockCodes(codes,db) ;  
        return codes ;  
    }  
      
    //���������й�Ʊ������뱾���ļ�  
    public static void saveStockCodes(List<String> codes,String db ) throws IOException{  
        //�����й�Ʊ��������ļ���  
        File out = new File(db) ;  
        if(! out.exists())  
            out.createNewFile() ;  
        BufferedWriter bw = new BufferedWriter(new FileWriter(out)) ;  
        for(String code : codes ){  
            bw.write(code) ;  
            bw.newLine() ;  
        }  
        if(bw != null ){  
            bw.close() ;  
            bw = null ;  
        }  
    }  
    
    public static List<String> getAllStockCodesFromLocal(String db) throws IOException{  
        List<String> codes = new ArrayList<String>() ;  
        File in = new File(db) ;  
        if(! in.exists())  
            throw new IOException("ָ�������ļ�������!");  
        BufferedReader br = new BufferedReader(new FileReader(in)) ;  
        String line = null ;  
        while( ( line = br.readLine() ) != null ){  
        	  if(line.contains("sh")||line.contains("sz")){
        		  codes.add(line) ;  
        	  }
        }  
        return codes ;  
    }
}
