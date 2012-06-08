package com.bk.sunwidgt.task;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
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

import com.bk.sunwidgt.activity.RainMapActivity;
import com.bk.sunwidgt.adapter.RainAdapterData;
import com.bk.sunwidgt.adapter.RainStoreUtil;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class RainParserTask extends AsyncTask<Void,Void,Void>{
    public final static String TAG = "Sun" + RainParserTask.class.getSimpleName();
    public final static String LOCATION_FILE_PREFIX = RainParserTask.class.getName() + ".";
    private final static SimpleDateFormat fmtDateTime = new SimpleDateFormat("MM/dd HH:mm");
    protected final Pattern titleDatePattern = Pattern.compile("(\\d{1,2})/(\\d{1,2})(\\d{2})\u2193(\\d{2})");
    protected final Pattern titleHourPattern = Pattern.compile("(\\d{2})\u2193(\\d{2})");
    
    protected final URL m_url;
    protected final Context m_context;
    protected Handler m_handler;
    protected final String m_progressConnectingMsg;
    protected final String m_progressHandlingMsg;
    protected final String m_progressSavingMsg;
    protected final String m_progressUpdateStatus;
    
    public RainParserTask(Context context, URL url,Handler handler) {
        super();
        m_context = context;
        m_url = url;
        m_handler = handler;
        
        m_progressConnectingMsg = context.getString(com.bk.sunwidgt.R.string.progress_message_connecting);
        m_progressHandlingMsg = context.getString(com.bk.sunwidgt.R.string.progress_message_handling);
        m_progressSavingMsg = context.getString(com.bk.sunwidgt.R.string.progress_message_saving);
        m_progressUpdateStatus = context.getString(com.bk.sunwidgt.R.string.progress_message_last_update);
    }


    @Override
    protected Void doInBackground(Void... arg0) {
        Log.d(TAG, "doInBackground");
        
        //if(m_handler != null) {
            //m_handler.obtainMessage(RainMapActivity.MESSAGE_SHOW_PROGRESS).sendToTarget();
        //    final long lastUpdateTimeInMS = RainStoreUtil.getLastUpdateTimeInMS(m_context);
        //    if(lastUpdateTimeInMS > 0L) {
        //        final Calendar cal = Calendar.getInstance();
        //        cal.setTimeInMillis(lastUpdateTimeInMS);
        //        m_handler.obtainMessage(RainMapActivity.MESSAGE_SET_UPDATE_STATUS, m_progressUpdateStatus+ " " + fmtDateTime.format(cal.getTime())).sendToTarget();
        //    }
        //}

        
        if(!RainStoreUtil.isPerfExpire(m_context)) {
            if(m_handler!=null) {
                m_handler.obtainMessage(RainMapActivity.MESSAGE_LOAD_RAINDATA).sendToTarget();
            }
            Log.i(TAG, "Read from Perference");
        }
        else {
            
            Log.i(TAG, "Perference expire");
            
            if(m_handler != null) {
                m_handler.obtainMessage(RainMapActivity.MESSAGE_LOAD_RAINDATA).sendToTarget();
            }
    
            CleanerProperties props = new CleanerProperties();
    
            // set some properties to non-default values
            props.setTranslateSpecialEntities(true);
            props.setTransResCharsToNCR(true);
            props.setOmitComments(true);
    
            // do parsing
            //final URL url = RainParser.class.getResource("22.htm");
            try {
                //m_handler.obtainMessage(RainMapActivity.MESSAGE_SET_PROGRESS_MESSAGE,m_progressConnectingMsg).sendToTarget();
                m_handler.obtainMessage(RainMapActivity.MESSAGE_SET_UPDATE_STATUS,m_progressConnectingMsg).sendToTarget();
                final TagNode node = new HtmlCleaner(props).clean(m_url);
                
                
                final List<List<RainInformation>> rainTable = new ArrayList<List<RainInformation>>();
                // traverse whole DOM and update images to absolute URLs
                node.traverse(new TagNodeVisitor() {
                    public boolean visit(TagNode tagNode, HtmlNode htmlNode) {
                        
                        //RainInformation[] titleRainInformation = null;
                        //List<RainInformation[]> rawdataRainInformationList = new ArrayList<RainInformation[]>(20);
                        if (htmlNode instanceof TagNode) {
                            TagNode tag = (TagNode) htmlNode;
                            String tagName = tag.getName();
                            
                            if ("tr".equals(tagName)) {
                                //RainInformation[] entry
                                List<RainInformation> rowTitleListRainInfromation = new ArrayList<RainInformation>(20);
                                for(TagNode sNode : tag.getAllElements(true)) {
                                    if("td".equals(sNode.getName()) || "th".equals(sNode.getName())) {
              
                                        final String rawData = sNode.getText().toString().replaceAll("&nbsp;"," ").trim();
        
                                        //try to match title
                                        final Matcher titleDateMatcher = titleDatePattern.matcher(rawData);
                                        final Matcher titleHourMatcher = titleHourPattern.matcher(rawData);
                                        final float value;
                                        boolean isValue = true;
                                        //check if it is a value
                                        try {
                                            value = Float.parseFloat(rawData);
                                            //System.out.print("v" + value + "\t");
                                            rowTitleListRainInfromation.add(new RainInformation(value));
                                        }
                                        catch(Exception e) {
                                            isValue = false;
                                        }
                                        if(!isValue) {
                                            if(titleDateMatcher.matches()) {
                                                //System.out.print("k" + titleDateMatcher.group(1) + "@" + titleDateMatcher.group(2) + "@" + titleDateMatcher.group(3) + "@" + titleDateMatcher.group(4)  + "\t");
                                                rowTitleListRainInfromation.add(new RainInformation(Integer.parseInt(titleDateMatcher.group(1)) - 1,Integer.parseInt(titleDateMatcher.group(2)),Integer.parseInt(titleDateMatcher.group(3)),Integer.parseInt(titleDateMatcher.group(4))));
                                            }
                                            else if(titleHourMatcher.matches()) {
                                                //System.out.print("g" + titleHourMatcher.group(1) + "@" + titleHourMatcher.group(2)  + "\t");
                                                rowTitleListRainInfromation.add(new RainInformation(Integer.parseInt(titleHourMatcher.group(1)),Integer.parseInt(titleHourMatcher.group(2))));
                                            }
                                            else if(rawData.length() < 9) {
                                                //System.out.print("s" + rawData + "\t");
                                                if(1 == rawData.length()) {
                                                    rowTitleListRainInfromation.add(new RainInformation());
                                                }
                                                else {
                                                    //m_handler.obtainMessage(RainMapActivity.MESSAGE_SET_PROGRESS_MESSAGE,m_progressHandlingMsg + " " + rawData).sendToTarget();
                                                    m_handler.obtainMessage(RainMapActivity.MESSAGE_SET_UPDATE_STATUS,m_progressHandlingMsg + " " + rawData).sendToTarget();
                                                    rowTitleListRainInfromation.add(new RainInformation(rawData));
                                                }
                                                
                                            }
                                        }
                                        
                                        
                                    }
                                }
                               
                                
                                rainTable.add(rowTitleListRainInfromation);
        
                            }
                            
                            //titleRainInformation = titleListRainInfromationList.toArray(new RainInformation[0]);
                            
                            //System.out.println(tag + " " + (tag.getText() != null ? tag.getText().toString() : ""));
                            
                        }
                        // tells visitor to continue traversing the DOM tree
                        return true;
                    }
               });
                
                final RainInformation[][] rainInformatinoArray = new RainInformation[rainTable.size()][];
                int i = 0;
                for(List<RainInformation> rowRainInfo : rainTable) {
                    rainInformatinoArray[i++] = rowRainInfo.toArray(new RainInformation[0]);
                }
                
                //debug print
                /*
                for(int r = 0;r < rainInformatinoArray.length;r++) {
                    StringBuffer lineSb = new StringBuffer();
                    for(int c = 0;c < rainInformatinoArray[r].length;c++) {
                        lineSb.append(rainInformatinoArray[r][c].toString()).append("\t");
                    }
                    lineSb.append(" rSize=").append(rainInformatinoArray[r].length).append("\n");
                    Log.d(TAG, lineSb.toString());
                    
                }
                */
                //m_handler.obtainMessage(RainMapActivity.MESSAGE_SET_PROGRESS_MESSAGE,m_progressSavingMsg).sendToTarget();
                RainStoreUtil.saveRains(m_context, rainInformatinoArray);
                
                if(m_handler != null) {
                    m_handler.obtainMessage(RainMapActivity.MESSAGE_LOAD_RAINDATA).sendToTarget();
                }
            
            }
            catch(IOException e) {
                Log.e(TAG, "Unable to get url " + m_url,e);
            }
        }
        
        //if(m_handler != null) {
        //    m_handler.obtainMessage(RainMapActivity.MESSAGE_CLOSE_PROGRESS).sendToTarget();
        //}
        if(m_handler != null) {
            //m_handler.obtainMessage(RainMapActivity.MESSAGE_SHOW_PROGRESS).sendToTarget();
            final long lastUpdateTimeInMS = RainStoreUtil.getLastUpdateTimeInMS(m_context);
            if(lastUpdateTimeInMS > 0L) {
                final Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(lastUpdateTimeInMS);
                m_handler.obtainMessage(RainMapActivity.MESSAGE_SET_UPDATE_STATUS, m_progressUpdateStatus+ " " + fmtDateTime.format(cal.getTime())).sendToTarget();
            }
        }
        
        return null;
    }
}
