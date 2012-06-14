
package com.bk.sunwidgt.task;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;

import com.bk.sunwidgt.activity.TideMapActivity;
import com.bk.sunwidgt.adapter.RainStoreUtil;
import com.bk.sunwidgt.adapter.TideStoreUtil;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class TideParserTask extends AsyncTask<Void, Void, Void> {
    public final static String TAG = "Sun" + TideParserTask.class.getSimpleName();
    public final static String LOCATION_FILE_PREFIX = TideParserTask.class.getName() + ".";

    protected final static Pattern datePattern = Pattern.compile("(\\d{1,2})/(\\d{1,2}).*");
    protected final static Pattern timePattern = Pattern.compile("(\\d{1,2}):(\\d{1,2}).*");

    protected final Location m_tideLocation;
    protected final URL m_url;
    protected final Context m_context;
    protected final Handler m_handler;
    protected final String m_progressConnectingMsg;
    protected final String m_progressHandlingMsg;
    protected final String m_progressSavingMsg;
    protected final String m_progressUpdateStatus;
    
    public TideParserTask(Context context, Handler handler,Location tideLocation, URL url) {
        super();
        m_context = context;
        m_tideLocation = new Location(tideLocation);
        m_url = url;
        m_handler = handler;
        
        m_progressConnectingMsg = context.getString(com.bk.sunwidgt.R.string.progress_message_connecting);
        m_progressHandlingMsg = context.getString(com.bk.sunwidgt.R.string.progress_message_handling);
        m_progressSavingMsg = context.getString(com.bk.sunwidgt.R.string.progress_message_saving);
        m_progressUpdateStatus = context.getString(com.bk.sunwidgt.R.string.progress_message_last_update);
    }

    @Override
    protected Void doInBackground(Void... params) {
        final int lastSlashPos = m_url.getPath().lastIndexOf('/');
        final String tideLocationName = (lastSlashPos < m_url.getPath().length() - 5 ? m_url.getPath().substring(lastSlashPos + 1) : "") + locationToString();
        Log.d(TAG, "doInBackground tideLocationName=" + tideLocationName + " url=" + m_url);
        
        if(m_handler != null) {
            m_handler.obtainMessage(TideMapActivity.MESSAGE_LOAD_TIDEATA, tideLocationName).sendToTarget();
        }
        
        if(!TideStoreUtil.isPerfExpire(m_context, tideLocationName)) {
            Log.i(TAG, "Read from Perference");
        }
        else {
            CleanerProperties props = new CleanerProperties();
    
            // set some properties to non-default values
            props.setTranslateSpecialEntities(true);
            props.setTransResCharsToNCR(true);
            props.setOmitComments(true);
    
            try {
                m_handler.obtainMessage(TideMapActivity.MESSAGE_SET_UPDATE_STATUS,m_progressConnectingMsg).sendToTarget();
                
                final TagNode node = new HtmlCleaner(props).clean(m_url);
    
                final List<TideInformation> tideTableList = new ArrayList<TideInformation>();
                // traverse whole DOM and update images to absolute URLs
                node.traverse(new TagNodeVisitor() {
                    public boolean visit(TagNode tagNode, HtmlNode htmlNode) {
    
                        if (htmlNode instanceof TagNode) {
                            TagNode tag = (TagNode) htmlNode;
                            String tagName = tag.getName();
                            if ("tr".equals(tagName)) {
    
                                for (TagNode sNode : tag.getAllElements(true)) {
                                    if ("td".equals(sNode.getName()) || "th".equals(sNode.getName())) {
                                        final String rawData = sNode.getText().toString()
                                                .replaceAll("&nbsp;", " ").trim();
    
                                        boolean isAValue;
                                        try {
                                            final float value = Float.parseFloat(rawData);
                                            tideTableList.add(new TideInformation(Math.round(value)));
                                            isAValue = true;
                                        }
                                        catch (Exception e) {
                                            isAValue = false;
                                        }
                                        if (!isAValue) {
                                            final Matcher dateMatcher = datePattern.matcher(rawData);
                                            final Matcher timeMatcher = timePattern.matcher(rawData);
    
                                            if (dateMatcher.matches()) {
                                                tideTableList.add(new TideInformation(Integer
                                                        .parseInt(dateMatcher.group(1)) - 1, Integer
                                                        .parseInt(dateMatcher.group(2)), true));
                                            }
                                            else if (timeMatcher.matches()) {
      
                                                tideTableList.add(new TideInformation(Integer
                                                        .parseInt(timeMatcher.group(1)), Integer
                                                        .parseInt(timeMatcher.group(2)), false));
                                            }
                                            else {
                                                //Nothing match! Skip
                                            }
                                        }
    
                                    }
                                }
    
                            }
    
                            // System.out.println(tag + " " + (tag.getText() != null
                            // ? tag.getText().toString() : ""));
    
                        }
                        // tells visitor to continue traversing the DOM tree
                        return true;
                    }
                });
                TideStoreUtil.saveTides(m_context, tideLocationName, m_tideLocation,tideTableList.toArray(new TideInformation[0]));
                
                if(m_handler != null) {
                    m_handler.obtainMessage(TideMapActivity.MESSAGE_LOAD_TIDEATA, tideLocationName).sendToTarget();
                }
            
            } catch (IOException e) {
                Log.e(TAG, "Unable to get url " + m_url,e);
            }
        }
        
        final long lastUpdateTimeInMS = TideStoreUtil.getLastUpdateTimeInMS(m_context,tideLocationName);
        if(lastUpdateTimeInMS > 0L) {
            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lastUpdateTimeInMS);
            m_handler.obtainMessage(TideMapActivity.MESSAGE_SET_UPDATE_STATUS, m_progressUpdateStatus+ " " + TideStoreUtil.fmtDateTime.format(cal.getTime())).sendToTarget();
        }
        
        return null;
    }
    
    private String locationToString() {
        return String.valueOf(m_tideLocation.getLatitude()) + "_" + String.valueOf(m_tideLocation.getLongitude());
    }

}
