package utils;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Anna Kuranda on 5/7/2017.
 */
public class HttpUtil {

    // logger
    private static Logger logger = Logger.getLogger(HttpUtil.class);


    public Pair<Integer,String> getHttp(String url, int socketTimeOut){
        logger.debug("In HTTP util : Got request for http "+url);
        HttpResponse response = null;
        String result = null;
        MutablePair<Integer,String> httpResult = new MutablePair<Integer,String>();
        try {


            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (certificate, authType) -> true).build();

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "application/xml");
            // seconds
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (httpGet != null) {
                        httpGet.abort();
                        logger.debug("In HTTP util :Reach timeout in sec " + socketTimeOut + "   " +url);
                    }
                }
            };
            new Timer(true).schedule(task, socketTimeOut * 2000);
            response =  httpClient.execute(httpGet);

            if (response!=null ) {


                HttpEntity entity = response.getEntity();
                if (entity != null) {

                    // A Simple JSON Response Read
                    InputStream instream = entity.getContent();
                    result = EntityUtils.toString(entity);
                    // now you have the string representation of the HTML request

                    instream.close();
                    logger.debug("In HTTP util : response code " + response.getStatusLine().getStatusCode() + " url " + url);
                    logger.debug("In HTTP util : response data " + result + " url " + url);
                    httpResult.setLeft(response.getStatusLine().getStatusCode());
                    httpResult.setRight(result);
                }
                else{
                    logger.error("No content from "+url);
                }



            }
            else{
                logger.error("The api "+ url +" Failed response ");
            }

        } catch (Exception e) {
            logger.error("Failed get responce for url  " + url + "\n" + e);

        }
        return httpResult;
    }
    public  String getUrlWithProtocol(String url){
        url = url.replace("http://","");
        url = url.replace("https://","");
        return url;

    }
}
