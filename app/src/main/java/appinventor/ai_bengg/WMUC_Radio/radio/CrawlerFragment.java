package appinventor.ai_bengg.WMUC_Radio.radio;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class CrawlerFragment extends Fragment {

    public CrawlerFragment() {

    }

    public void load() {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                //code to do the HTTP request
                Document doc;
                ArrayList<Element> shtuff;
                ArrayList<String> entireSchedule = new ArrayList<>();
                Show [] [] sched = new Show [24][7];
                try {
                    doc = Jsoup.connect("http://www.wmuc.umd.edu/station/schedule/0/2").get();
                    shtuff = doc.select("td");
                    for(Element n : shtuff){
                        if (!n.text().isEmpty()&&!(n.text().contains("Channel 2"))
                                &&!(n.text().contains(":30"))
                                &&!(n.text().contains("Get Involved Station History Donate"))
                                &&!(n.text().contains("Find us on Facebook Follow WMUC"))) {
                            entireSchedule.add(n.text());
                        }
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

                int row = -1;
                int col = 0;

                for(String s : entireSchedule) {

                    if(s.contains(":00")){
                        row++;
                    }
                    else{
                        String [] a = s.split("\\*\\*\\*");
                        if(a.length>1)
                            sched[row][col] = new Show (a);
                        col = (col+1)%7;
                    }
                }
                for(int r = 0; r <24; r++){
                    for(int c = 0; c < 1; c++){
                        //System.out.print(sched[r][c] + " | ");
                        System.out.println(sched[r][c].getShow() + " " + sched[r][c].getHost());
                    }
                    // System.out.println();
                }
            }
        });
        thread.start();
        //thread.stop();
    }




    static class Show {
        String sName;
        String host;

        Show(String [] s){
            sName = s[0];
            host = s[1];
        }

        String getShow(){
            return sName;
        }

        String getHost(){
            return host;
        }
        public String toString(){
            return sName + " - " + host;
        }
    }
}