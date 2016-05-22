package controllers;

import org.apache.commons.lang3.NotImplementedException;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vivia on 2016/5/5.
 */
public class HttpUtil {
    private String baseUrl;
    private URL url;
    private URLConnection connection;
    private String codec;
    private Map<String,String> paramPairs = new HashMap<>();
    private String method;
    private StringBuffer stringBuffer;
    private String responseStr;

    public HttpUtil(String targetUrl,String codecName){
        this.baseUrl = targetUrl;
        this.codec = codecName;
    }

    public boolean setupConnection(String method)
    {
        this.method = method;
        if(method.equals("POST"))
        {
            try {
                url = new URL(baseUrl);
                connection = url.openConnection();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(method.equals("GET"))
        {
            throw new NotImplementedException("Getg not implemented yet");
        }
        return false;
    }

    public String doPostRequest()
    {
        StringBuffer result = new StringBuffer();
        connection.setConnectTimeout(3000);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        if(stringBuffer != null && stringBuffer.length() > 0) {
            byte [] mydata = stringBuffer.toString().getBytes();
            // 设置请求体的类型
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Lenth", String.valueOf(mydata.length));
            try {
                OutputStream os = (OutputStream)connection.getOutputStream();
                os.write(mydata);
               //定义BufferedReader输入流来读取URL的响应
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = in .readLine()) != null) {
                    result.append(line);
                    result.append("\n");
                }
                result.deleteCharAt(result.length() - 1);
                responseStr = result.toString();
                return getResponseStr();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String doGetRequest()
    {
        return null;
    }

    public void feedPayload(String name,String value)
    {
        paramPairs.put(name,value);
        stringBuffer = new StringBuffer();
        for (Map.Entry<String, String> entry : paramPairs.entrySet()) {
            try {
                stringBuffer
                        .append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), codec))
                        .append("&");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // 删掉最后一个 & 字符
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        System.out.println("Payload updated to >> " + stringBuffer.toString());
    }

    public void feedHeader(String name,String value)
    {
            connection.setRequestProperty(name,value);
    }

    public Map<String,String> getResponsePair()
    {
        Map<String ,String> responsePair = new HashMap<>();
        if(getResponseStr() != null && !getResponseStr().isEmpty())
        {
            try {
                for(String pairStr : getResponseStr().split("&"))
                {
                        String decodedStr = URLDecoder.decode(pairStr,codec);
                        responsePair.put(decodedStr.split("=")[0],decodedStr.split("=")[1]);
                }
                return responsePair;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getResponseStr() {
        return responseStr;
    }
}
