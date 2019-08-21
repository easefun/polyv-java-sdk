package com.polyv.sdk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 视频列表使用的VO对象
 * @author TanQu 2015-9-14
 */
public class Video {
    private final String swfLink;
    private final long sourceFileSize;
    private final int status;
    private final String tag;
    private final int seed;
    private final int playerWidth;
    private final List<Long> fileSize;
    private final String duration;
    private final String title;
    private final int df;
    private final String firstImage;
    private final int times;
    private final String context;
    private final String originalDefinition;
    private final int playerHeight;
    private final String vid;
    private final String ptime;
    private final long cataid;
    public Video(String swfLink, long sourceFileSize, int status, String tag, int seed, int playerWidth,
            List<Long> fileSize, String duration, String title, int df, String firstImage, int times, String context,
            String originalDefinition, int playerHeight, String vid, String ptime, long cataid) {
        super();
        this.swfLink = swfLink;
        this.sourceFileSize = sourceFileSize;
        this.status = status;
        this.tag = tag;
        this.seed = seed;
        this.playerWidth = playerWidth;
        this.fileSize = fileSize;
        this.duration = duration;
        this.title = title;
        this.df = df;
        this.firstImage = firstImage;
        this.times = times;
        this.context = context;
        this.originalDefinition = originalDefinition;
        this.playerHeight = playerHeight;
        this.vid = vid;
        this.ptime = ptime;
        this.cataid = cataid;
    }
    public String getSwfLink() {
        return swfLink;
    }
    public long getSourceFileSize() {
        return sourceFileSize;
    }
    public int getStatus() {
        return status;
    }
    public String getTag() {
        return tag;
    }
    public int getSeed() {
        return seed;
    }
    public int getPlayerWidth() {
        return playerWidth;
    }
    public List<Long> getFileSize() {
        return fileSize;
    }
    public String getDuration() {
        return duration;
    }
    public String getTitle() {
        return title;
    }
    public int getDf() {
        return df;
    }
    public String getFirstImage() {
        return firstImage;
    }
    public int getTimes() {
        return times;
    }
    public String getContext() {
        return context;
    }
    public String getOriginalDefinition() {
        return originalDefinition;
    }
    public int getPlayerHeight() {
        return playerHeight;
    }
    public String getVid() {
        return vid;
    }
    public String getPtime() {
        return ptime;
    }
    public long getCataid() {
        return cataid;
    }
    
    public static Video formatJSONObject(JSONObject jsonObject) {
        int length = 0;
        
        String swfLink = jsonObject.optString("swf_link", "");
        long sourceFileSize = jsonObject.optLong("source_filesize", 0);
        int status = jsonObject.optInt("status", 0);
        String tag = jsonObject.optString("tag", "");
        int seed = jsonObject.optInt("seed", 0);
        int playerWidth = jsonObject.optInt("playerwidth", 0);
        
        List<Long> fileSize = new ArrayList<Long>();
        JSONArray childJSONArray = jsonObject.optJSONArray("filesize");
        if (childJSONArray != null && (length = childJSONArray.length()) != 0) {
            for (int i = 0 ; i < length ; i++) {
                fileSize.add(childJSONArray.optLong(i, 0));
            }
        }
        
        String duration = jsonObject.optString("duration", "");
        String title = jsonObject.optString("title", "");
        int df = jsonObject.optInt("df", 0);
        String firstImage = jsonObject.optString("first_image", "");
        int times =  jsonObject.optInt("times", 0);
        String context = jsonObject.optString("context", "");
        String originalDefinition = jsonObject.optString("original_definition", "");
        int playerHeight = jsonObject.optInt("playerheight", 0);
        String vid = jsonObject.optString("vid", "");
        String ptime = jsonObject.optString("ptime", "");
        long cataid = jsonObject.optLong("cataid", 0);
        
        Video Video = new Video(swfLink, sourceFileSize, status, tag, seed, playerWidth, fileSize, duration, title,
                df, firstImage, times, context, originalDefinition, playerHeight, vid, ptime, cataid);
        
        return Video;
    }
}
