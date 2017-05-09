package appinventor.ai_bengg.WMUC_Radio;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import appinventor.ai_bengg.WMUC_Radio.R;

public class Schedule extends Activity implements OnClickListener {



//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private Schedule.Show[][] sched = new Schedule.Show[7][24];
    private ArrayList<ListItem> myList;
    private View sun, mon, tue, wed, thu, fri, sat, prev;
    private TextView currDay;
    private ListView listView;

    public static String CRAWLER_FRAG_TAG = "CRAWL_FRAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        sun = findViewById(R.id.sunday);
        sun.setOnClickListener(this);
        mon = findViewById(R.id.monday);
        mon.setOnClickListener(this);
        tue = findViewById(R.id.tuesday);
        tue.setOnClickListener(this);
        wed = findViewById(R.id.wednesday);
        wed.setOnClickListener(this);
        thu = findViewById(R.id.thursday);
        thu.setOnClickListener(this);
        fri = findViewById(R.id.friday);
        fri.setOnClickListener(this);
        sat = findViewById(R.id.saturday);
        sat.setOnClickListener(this);
        currDay = (TextView) findViewById(R.id.day);
        listView = (ListView) findViewById(R.id.list);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Document doc;
                ArrayList<Element> shtuff;
                int row = 0;
                int col = 0;
                int rowspan = -1;                            // how long a show is (2x)
                int rowIndex = -1;                            // tracking the current row
                HashMap<Integer, Integer> colTrack = new HashMap<Integer, Integer>();
                String[] none = {"Off Air", "N/A"};            // "null" show
                Schedule.Show offAir = new Schedule.Show(none);

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
                    for (Element n : shtuff) {
                        if (!n.text().isEmpty() && !(n.text().contains("Channel 2"))
                                && !(n.text().contains(":30"))
                                && !(n.text().contains("Get Involved Station History Donate"))
                                && !(n.text().contains("Find us on Facebook Follow WMUC"))) {
                            // sets the rowspan of the show
                            rowspan = n.toString().indexOf("rowspan=\"") + 9;
                            if (rowspan != 8) {
                                if (n.toString().charAt(rowspan + 1) != ('\"')) {
                                    rowspan = Integer.parseInt(n.toString().substring(rowspan, rowspan + 2));
                                } else {
                                    rowspan = Integer.parseInt(n.toString().substring(rowspan, rowspan + 1));
                                }
                            }

                            if (!n.text().contains(":00")) {
                                try {
                                    while (sched[col][rowIndex].equals(offAir)) {
                                        col++;
                                    }
                                } catch (NullPointerException np) {
                                    //leave loop
                                }
                                if (col < 7) {
                                    row = colTrack.get(col);
                                    while (row > rowIndex) {
                                        col++;
                                        row = colTrack.get(col);
                                    }
                                    for (int i = 0; i < (rowspan / 2); i++) {
                                        String currShow = n.text();
                                        String[] curr = currShow.split("\\*\\*\\*");
                                        if (curr.length > 1) {
                                            sched[col][row++] = new Schedule.Show(curr);
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

                            if (n.text().contains(":00")) {
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

                for (int r = 0; r < 24; r++) {
                    for (int c = 0; c < 7; c++) {
                        System.out.print(sched[c][r] + " | ");
                    }
                    System.out.println();
                }
            }
        });
        thread.start();
        // thread.stop();

        try{
            thread.join();
        } catch (java.lang.InterruptedException e) {
            System.out.println("Uh oh.");
        }
        // Populating the list with sunday by default.
        myList = getScheduleData(sun);
        sun.setBackgroundColor(Color.parseColor("#7c7a7a"));
        prev = sun;
        listView.setAdapter(new ArrayAdapter<ListItem>(this, 0, myList) {
            private View row;
            private LayoutInflater inflater = getLayoutInflater();
            private TextView show;
            private TextView dj;
            private TextView time;

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                row = inflater.inflate(R.layout.schedule_item, parent, false);
                show = (TextView) row.findViewById(R.id.recycshow);
                show.setText(myList.get(position).getShow());

                dj = (TextView) row.findViewById(R.id.recychost);
                dj.setText(myList.get(position).getHost());

                time = (TextView) row.findViewById(R.id.time);
                time.setText(myList.get(position).getTime());
                return row;
            }
        });


    }

    static class Show {
        String sName;
        String host;

        public Show(String[] s) {
            sName = s[0];
            host = s[1];
        }

        public String toString() {
            return sName + " - " + host;
        }

        public boolean equals(Schedule.Show o) {
            if (sName.equals(o.sName)) {
                return true;
            } else {
                return false;
            }
        }
    }

    private ArrayList<ListItem> getScheduleData(View view) {
        ArrayList<ListItem> schedByDay = new ArrayList<ListItem>();
        if (view == sun) {
            for (int h = 0; h < 24; h++) {
                ListItem toAdd = new ListItem(this, h + ":00", sched[0][h].sName, sched[0][h].host);
                toAdd.setHost(sched[0][h].host);
                toAdd.setShow(sched[0][h].sName);
                toAdd.setTime(h + ":00");
                schedByDay.add(new ListItem(this, h + ":00", sched[0][h].sName, sched[0][h].host));
            }
            currDay.setText("Sunday");
        } else if (view == mon) {
            for (int h = 0; h < 24; h++) {
                ListItem toAdd = new ListItem(this, h + ":00", sched[1][h].sName, sched[1][h].host);
                toAdd.setHost(sched[1][h].host);
                toAdd.setShow(sched[1][h].sName);
                toAdd.setTime(h + ":00");
                schedByDay.add(new ListItem(this, h + ":00", sched[1][h].sName, sched[1][h].host));
            }
            currDay.setText("Monday");
        } else if (view == tue) {
            for (int h = 0; h < 24; h++) {
                ListItem toAdd = new ListItem(this, h + ":00", sched[2][h].sName, sched[2][h].host);
                toAdd.setHost(sched[2][h].host);
                toAdd.setShow(sched[2][h].sName);
                toAdd.setTime(h + ":00");
                schedByDay.add(new ListItem(this, h + ":00", sched[2][h].sName, sched[2][h].host));
            }
            currDay.setText("Tuesday");
        } else if (view == wed) {
            for (int h = 0; h < 24; h++) {
                ListItem toAdd = new ListItem(this, h + ":00", sched[3][h].sName, sched[3][h].host);
                toAdd.setHost(sched[3][h].host);
                toAdd.setShow(sched[3][h].sName);
                toAdd.setTime(h + ":00");
                schedByDay.add(new ListItem(this, h + ":00", sched[3][h].sName, sched[3][h].host));
            }
            currDay.setText("Wednesday");
        } else if (view == thu) {
            for (int h = 0; h < 24; h++) {
                ListItem toAdd = new ListItem(this, h + ":00", sched[4][h].sName, sched[4][h].host);
                toAdd.setHost(sched[4][h].host);
                toAdd.setShow(sched[4][h].sName);
                toAdd.setTime(h + ":00");
                schedByDay.add(new ListItem(this, h + ":00", sched[4][h].sName, sched[4][h].host));
            }
            currDay.setText("Thursday");
        } else if (view == fri) {
            for (int h = 0; h < 24; h++) {
                ListItem toAdd = new ListItem(this, h + ":00", sched[5][h].sName, sched[5][h].host);
                toAdd.setHost(sched[5][h].host);
                toAdd.setShow(sched[5][h].sName);
                toAdd.setTime(h + ":00");
                schedByDay.add(new ListItem(this, h + ":00", sched[5][h].sName, sched[5][h].host));
            }
            currDay.setText("Friday");
        } else if (view == sat) {
            for (int h = 0; h < 24; h++) {
                ListItem toAdd = new ListItem(this, h + ":00", sched[6][h].sName, sched[6][h].host);
                toAdd.setHost(sched[6][h].host);
                toAdd.setShow(sched[6][h].sName);
                toAdd.setTime(h + ":00");
                schedByDay.add(new ListItem(this, h + ":00", sched[6][h].sName, sched[6][h].host));
            }
            currDay.setText("Saturday");
        } else {
            return null;
        }
        return schedByDay;
    }

    @Override
    public void onClick(View v) {
        prev.setBackgroundColor(Color.parseColor("#fafafa"));
        prev = v;
        myList = getScheduleData(v);
        v.setBackgroundColor(Color.parseColor("#ff6b6b"));

        listView.setAdapter(new ArrayAdapter<ListItem>(this, 0, myList) {
            private View row;
            private LayoutInflater inflater = getLayoutInflater();
            private TextView show;
            private TextView dj;
            private TextView time;

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                row = inflater.inflate(R.layout.schedule_item, parent, false);

                show = (TextView) row.findViewById(R.id.recycshow);
                show.setText(myList.get(position).getShow());

                dj = (TextView) row.findViewById(R.id.recychost);
                dj.setText(myList.get(position).getHost());

                time = (TextView) row.findViewById(R.id.time);
                time.setText(myList.get(position).getTime());


                /* FOR UNDERLINE
                SpannableString content = new SpannableString(myList.get(position).getShow());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                show.setText(content);*/

                return row;
            }
        });
    }

}
