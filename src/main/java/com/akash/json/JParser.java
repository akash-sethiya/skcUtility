package com.akash.json;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JParser {
    private static Logger logger = LoggerFactory.getLogger(JParser.class);
    public static Map<String,Object> parseAndFilter(String jSonString){
        Map<String,Object> values=new HashMap<String,Object>();
        try{
            logger.debug("Parsing of received JSon object is starting.....");
            JSONObject jSon=new JSONObject(jSonString);
            JSONObject list=jSon.getJSONObject("list");
        
            JSONArray resources=list.getJSONArray("resources");
            for(int i=0;i<resources.length();i++)
            {
                JSONObject object=(JSONObject)resources.get(i);
                JSONObject resource=object.getJSONObject("resource");
                JSONObject fields = resource.getJSONObject("fields");
                String name=fields.getString("name");
                String price=fields.getString("price");
                String symbol=fields.getString("symbol");
                Long timestamp=Long.parseLong((String)fields.getString("ts"));
                values.put("name", name);
                values.put("price", price);
                values.put("symbol", symbol);
                values.put("ts",timestamp);
            }
        }
        catch(Exception e){
            logger.error("Exception occured during parsing of JSon String."+e);
            e.printStackTrace();
        }
        return values;
    }

}