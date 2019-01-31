package com.sap.hcp.cf.tutorials.redis;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sap.hcp.cf.tutorials.redis.model.RedisObject;
import com.sap.hcp.cf.tutorials.redis.model.Result;

@Controller
public class RootController {

    private static final Logger log = LoggerFactory.getLogger(RootController.class);

    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    @RequestMapping(method = RequestMethod.GET, path = "/cache")
    public @ResponseBody String onRootAccess() {

        RedisConnection redisConnection = redisConnectionFactory.getConnection();
        log.info("Starting to retrieve users from other API");
        String users = retrieveUsers();
        log.info("Setting key/value pair 'allusers'/'userlist'");
        redisConnection.set("allusers".getBytes(), users.getBytes());
//        log.info("Retrieving value for key 'allusers' from Redis: ");
//        final String value = new String(redisConnection.get("allusers".getBytes()));
//        log.info("Retrieve finished!!!");
//        Result result = new Result();
//        result.setStatus("Succesfully connected to Redis and retrieved the key/value pair that was inserted");
//        RedisObject redisObject = new RedisObject();
//        redisObject.setKey("grcdpp-test");
//        redisObject.setValue(value);
//        final ArrayList<RedisObject> redisObjects = new ArrayList<RedisObject>();
//        redisObjects.add(redisObject);
//        result.setRedisObjects(redisObjects);
        return "successfully cached!";
    }
    
    @RequestMapping(method = RequestMethod.GET, path = "/getFromCache")
    public @ResponseBody Result getFromCache() {
    	
    	RedisConnection redisConnection = redisConnectionFactory.getConnection();
        log.info("Retrieving value for key 'allusers' from Redis: ");
        final String value = new String(redisConnection.get("allusers".getBytes()));
        log.info("Retrieve finished!!!");
        Result result = new Result();
        result.setStatus("Succesfully connected to Redis and retrieved the key/value pair that was inserted");
        RedisObject redisObject = new RedisObject();
        redisObject.setKey("grcdpp-test");
        redisObject.setValue(value);
        final ArrayList<RedisObject> redisObjects = new ArrayList<RedisObject>();
        redisObjects.add(redisObject);
        result.setRedisObjects(redisObjects);
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET, path = "/hello")
    public @ResponseBody Result helloWorld() {

        RedisConnection redisConnection = redisConnectionFactory.getConnection();
        log.info("Setting key/value pair 'hello'/'world'");
        redisConnection.set("hello".getBytes(), "world".getBytes());

        log.info("Retrieving value for key 'hello': ");
        final String value = new String(redisConnection.get("hello".getBytes()));
        Result result = new Result();
        result.setStatus("Succesfully connected to Redis and retrieved the key/value pair that was inserted");
        RedisObject redisObject = new RedisObject();
        redisObject.setKey("hello");
        redisObject.setValue(value);
        final ArrayList<RedisObject> redisObjects = new ArrayList<RedisObject>();
        redisObjects.add(redisObject);
        result.setRedisObjects(redisObjects);
        return result;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/test")
    public @ResponseBody String test() {
    	return "Welcome!";
    }
    @RequestMapping(method = RequestMethod.GET, path = "/grcdpptest/users")
    public @ResponseBody String retrieveUsers() {
    	try {
            String webPage = "https://grcdpp-test.accounts400.ondemand.com/service/scim/Users/";
            String name = "T000001";
            String password = "7wOdWy6t2R";

            String authString = name + ":" + password;
            log.info("auth string: "+ authString);
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            log.info("Base64 encoded auth string: " + authStringEnc);

            URL url = new URL(webPage);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            int numCharsRead;
            char[] charArray = new char[1024];
            StringBuffer sb = new StringBuffer();
            while ((numCharsRead = isr.read(charArray)) > 0) {
                sb.append(charArray, 0, numCharsRead);
            }
            String result = sb.toString();
            System.out.println("result.length():"+result.length());
            System.out.println("*** BEGIN ***");
            System.out.println(result);
            System.out.println("*** END ***");
            System.out.println(result.length());
            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
