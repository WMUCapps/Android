package appinventor.ai_bengg.WMUC_Radio;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import appinventor.ai_bengg.WMUC_Radio.R;
import appinventor.ai_bengg.WMUC_Radio.radio.CrawlerFragment;

public class Schedule extends FragmentActivity implements View.OnClickListener {

    public static String CRAWLER_FRAG_TAG = "CRAWL_FRAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Document doc;
                ArrayList<Element> shtuff;
                Show [] [] sched = new Show [7][24];
                int row = 0;
                int col = 0;
                int rowspan = -1;							// how long a show is (2x)
                int rowIndex = -1;							// tracking the current row
                HashMap<Integer, Integer> colTrack = new HashMap<Integer, Integer>();
                String[] none = {"Off Air", "N/A"};			// "null" show
                Show offAir = new Show (none);

                //Initializes the current row of each col in the sched as they may get out of order.
                colTrack.put(0, 0);
                colTrack.put(1, 0);
                colTrack.put(2, 0);
                colTrack.put(3, 0);
                colTrack.put(4, 0);
                colTrack.put(5, 0);
                colTrack.put(6, 0);

                try {
                    doc = Jsoup.connect("http://www.wmuc.umd.edu/station/schedule/0/2").get();
                    shtuff = doc.select("td");
                    for(Element n : shtuff){
                        if (!n.text().isEmpty()&&!(n.text().contains("Channel 2"))
                                &&!(n.text().contains(":30"))
                                &&!(n.text().contains("Get Involved Station History Donate"))
                                &&!(n.text().contains("Find us on Facebook Follow WMUC"))) {
                            // sets the rowspan of the show
                            rowspan = n.toString().indexOf("rowspan=\"") + 9;
                            if(rowspan != 8) {
                                if(n.toString().charAt(rowspan + 1) != ('\"')) {
                                    rowspan = Integer.parseInt(n.toString().substring(rowspan, rowspan+2));
                                } else {
                                    rowspan = Integer.parseInt(n.toString().substring(rowspan, rowspan+1));
                                }
                            }

                            if(!n.text().contains(":00")){
                                try {
                                    while(sched[col][rowIndex].equals(offAir)) {
                                        col++;
                                    }
                                } catch (NullPointerException np) {
                                    //leave loop
                                }
                                if (col < 7) {
                                    row = colTrack.get(col);
                                    while(row > rowIndex) {
                                        col++;
                                        row = colTrack.get(col);
                                    }
                                    for(int i = 0; i < (rowspan/2); i++){
                                        String currShow = n.text();
                                        String [] curr = currShow.split("\\*\\*\\*");
                                        if(curr.length>1){
                                            sched[col][row++] = new Show (curr);
                                        } else {
                                            sched[col][row++] = offAir;
                                        }
                                    }
                                    colTrack.put(col, row);
                                    rowspan = -1;
                                    if (col == 6) {
                                        col = 0;
                                    } else {
                                        col++;
                                    }
                                    row = 0;
                                } else {
                                    col = 0;
                                }
                            }

                            if(n.text().contains(":00")) {
                                rowIndex++;
                                col = 0;
                            }
                        }
                    }
                } catch (MalformedURLException mue) {
                    mue.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

                for(int r = 0; r <24; r++){
                    for(int c = 0; c < 7; c++){
                        System.out.print(sched[c][r] + " | ");
                    }
                    System.out.println();
                }
            }
        });
        thread.start();
        // thread.stop();

}

    static class Show {
        String sName;
        String host;

        public Show(String [] s){
            sName = s[0];
            host = s[1];
        }

        public String toString(){
            return sName + " - " + host;
        }

        public boolean equals(Show o){
            if(sName.equals(o.sName)) {
                return true;
            } else {
                return false;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }
}
