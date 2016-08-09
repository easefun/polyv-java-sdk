package com.polyv.sdk;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;




public class SDKUtil {
	public static Video convertJsonToVideo(String jsonbody) throws JSONException{
		JSONObject jsonObj = new JSONObject(jsonbody);
		JSONObject data = jsonObj.getJSONArray("data").getJSONObject(0);
		return Video.formatJSONObject(data);
		
	}
	

    public static List<Video> convertJsonToVideoList(String jsonBody) throws JSONException {
        ArrayList<Video> videolist = new ArrayList<Video>();
        if (jsonBody == null || jsonBody.equals("")) return videolist;
        JSONObject jsonObj = new JSONObject(jsonBody);
        JSONArray datalist = jsonObj.getJSONArray("data");
        int length = datalist.length();
        for (int i = 0; i < length; i++) {
            JSONObject data = datalist.getJSONObject(i);
            videolist.add(Video.formatJSONObject(data));
        }
        
        return videolist;
    }
	
	public static String sha1(String str){
		 String sha1 = "";
		    try
		    {
		        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
		        crypt.reset();
		        crypt.update(str.getBytes("UTF-8"));
		        sha1 = byteToHex(crypt.digest());
		    }
		    catch(NoSuchAlgorithmException e)
		    {
		        e.printStackTrace();
		    }
		    catch(UnsupportedEncodingException e)
		    {
		        e.printStackTrace();
		    }
		    return sha1;
	}
	private static String byteToHex(final byte[] hash)
	{
	    Formatter formatter = new Formatter();
	    for (byte b : hash)
	    {
	        formatter.format("%02x", b);
	    }
	    String result = formatter.toString();
	    formatter.close();
	    return result;
	}
	public static int getErrorCodeFromJson(String jsonbody) throws JSONException{
	    JSONObject jsonObj = new JSONObject(jsonbody);
		return jsonObj.getInt("error");
		
		
	}
	public static void main(String[] arsg) throws JSONException{
		String json = "{\"images_b\":\"[\"http://v.polyv.net/img/processing.png\",\"http://v.polyv.net/img/processing.png\",\"http://v.polyv.net/img/processing.png\",\"http://v.polyv.net/img/processing.png\",\"http://v.polyv.net/img/processing.png\",\"http://v.polyv.net/img/processing.png\",\"http://v.polyv.net/img/processing.png\",\"http://v.polyv.net/img/processing.png\"]\",\"tag\":\"tag\",\"mp4\":\"http://mpv.videocc.net/sl8da4jjbx/e/sl8da4jjbx155339ddac9d2b62a76eee_1.mp4\",\"title\":\"我的标题\",\"df\":1,\"times\":\"0\",\"mp4_1\":\"http://mpv.videocc.net/sl8da4jjbx/e/sl8da4jjbx155339ddac9d2b62a76eee_1.mp4\",\"vid\":\"sl8da4jjbx155339ddac9d2b62a76eee_s\",\"cataid\":\"1\",\"swf_link\":\"http://play.polyv.net/videos/sl8da4jjbx155339ddac9d2b62a76eee_s.swf\",\"status\":\"10\",\"seed\":0,\"flv1\":\"http://plvod01.videocc.net/sl8da4jjbx/e/sl8da4jjbx155339ddac9d2b62a76eee_1.flv\",\"sourcefile\":\"http://mpv.videocc.net/sl8da4jjbx/e/sl8da4jjbx155339ddac9d2b62a76eee.mp3\",\"playerwidth\":\"600\",\"hlsIndex\":\"http://v.polyv.net/hlsIndex/sl8da4jjbx155339ddac9d2b62a76eee_s.m3u8\",\"hls1\":\"http://v.polyv.net/hls/sl8da4jjbx155339ddac9d2b62a76eee_s.m3u8?df=1\",\"default_video\":\"http://plvod01.videocc.net/sl8da4jjbx/e/sl8da4jjbx155339ddac9d2b62a76eee_1.flv\",\"duration\":\"00:04:40\",\"first_image\":\"http://v.polyv.net/img/processing.png\",\"original_definition\":\"240x240\",\"context\":\"\",\"images\":\"[\"http://v.polyv.net/img/processing.png\",\"http://v.polyv.net/img/processing.png\",\"http://v.polyv.net/img/processing.png\",\"http://v.polyv.net/img/processing.png\",\"http://v.polyv.net/img/processing.png\",\"http://v.polyv.net/img/processing.png\",\"http://v.polyv.net/img/processing.png\",\"http://v.polyv.net/img/processing.png\"]\",\"playerheight\":\"490\",\"ptime\":\"2014-10-09 16:01:57\"}";
		
		Video v = (Video)SDKUtil.convertJsonToVideo(json);
		System.out.println(v);
	}
	
}
