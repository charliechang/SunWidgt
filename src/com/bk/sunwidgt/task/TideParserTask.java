
package com.bk.sunwidgt.task;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class TideParserTask extends AsyncTask<Void, Void, TideInformation[]> {
    public final static String TAG = "Sun" + TideParserTask.class.getSimpleName();
    public final static String LOCATION_FILE_PREFIX = TideParserTask.class.getName() + ".";

    protected final static Pattern datePattern = Pattern.compile("(\\d{1,2})/(\\d{1,2}).*");
    protected final static Pattern timePattern = Pattern.compile("(\\d{1,2}):(\\d{1,2}).*");

    protected final String m_tideLocation;
    protected final URL m_url;
    protected final Context m_context;

    public TideParserTask(Context context, String tideLocation, URL url) {
        super();
        m_context = context;
        m_tideLocation = tideLocation;
        m_url = url;
    }

    @Override
    protected TideInformation[] doInBackground(Void... params) {
        CleanerProperties props = new CleanerProperties();

        // set some properties to non-default values
        props.setTranslateSpecialEntities(true);
        props.setTransResCharsToNCR(true);
        props.setOmitComments(true);

        try {

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
                                        tideTableList.add(new TideInformation((int) value));
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
            
            return tideTableList.toArray(new TideInformation[0]);

        } catch (IOException e) {
            Log.e(TAG, "Unable to get url " + m_url,e);
        }

        return null;
    }

}
