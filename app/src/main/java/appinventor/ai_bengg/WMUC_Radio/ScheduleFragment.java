package appinventor.ai_bengg.WMUC_Radio;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by Evan on 3/6/17.
 */

public class ScheduleFragment extends FragmentActivity implements View.OnClickListener {

    private Show[][] sched = new Show[7][24];
    private View sun = findViewById(R.id.sunday);
    private View mon = findViewById(R.id.monday);
    private View tue = findViewById(R.id.tuesday);
    private View wed = findViewById(R.id.wednesday);
    private View thu = findViewById(R.id.thursday);
    private View fri = findViewById(R.id.friday);
    private View sat = findViewById(R.id.saturday);
    private TextView currDay = (TextView) findViewById(R.id.day);
    private ListView listView = (ListView) findViewById(R.id.list);

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
                int row = 0;
                int col = 0;
                int rowspan = -1;                            // how long a show is (2x)
                int rowIndex = -1;                            // tracking the current row
                HashMap<Integer, Integer> colTrack = new HashMap<Integer, Integer>();
                String[] none = {"Off Air", "N/A"};            // "null" show
                Show offAir = new Show(none);

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
                                            sched[col][row++] = new Show(curr);
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
        //listView.addView(new ListItem(this, 1 + ":00", sched[0][0].sName, sched[0][0].host));
        //getScheduleData(mon);
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

        public boolean equals(Show o) {
            if (sName.equals(o.sName)) {
                return true;
            } else {
                return false;
            }
        }
    }

    private List<ListItem> getScheduleData(View view) {
        List<ListItem> schedByDay = new ArrayList<ListItem>();
        if (view == sun) {
            for (int h = 0; h < 24; h++) {
                ListItem toAdd = new ListItem(this, h + ":00", sched[0][h].sName, sched[0][h].host);
                toAdd.setHost(sched[0][h].host);
                toAdd.setShow(sched[0][h].sName);
                toAdd.setTime(h + ":00");
                listView.addView(toAdd);
                //schedByDay.add(new ListItem());
            }
            currDay.setText("Sunday");
        } else if (view == mon) {
            for (int h = 0; h < 24; h++) {
                ListItem toAdd = new ListItem(this, h + ":00", sched[1][h].sName, sched[1][h].host);
                toAdd.setHost(sched[1][h].host);
                toAdd.setShow(sched[1][h].sName);
                toAdd.setTime(h + ":00");
                listView.addView(toAdd);
                //schedByDay.add(toAdd);
            }
            currDay.setText("Monday");
        } else if (view == tue) {
            for (int h = 0; h < 24; h++) {
                ListItem toAdd = new ListItem(this, h + ":00", sched[2][h].sName, sched[2][h].host);
                toAdd.setHost(sched[2][h].host);
                toAdd.setShow(sched[2][h].sName);
                toAdd.setTime(h + ":00");
                listView.addView(toAdd);
                //schedByDay.add(new ListItem());
            }
            currDay.setText("Tuesday");
        } else if (view == wed) {
            for (int h = 0; h < 24; h++) {
                ListItem toAdd = new ListItem(this, h + ":00", sched[3][h].sName, sched[3][h].host);
                toAdd.setHost(sched[3][h].host);
                toAdd.setShow(sched[3][h].sName);
                toAdd.setTime(h + ":00");
                listView.addView(toAdd);
                //schedByDay.add(new ListItem());
            }
            currDay.setText("Wednesday");
        } else if (view == thu) {
            for (int h = 0; h < 24; h++) {
                ListItem toAdd = new ListItem(this, h + ":00", sched[4][h].sName, sched[4][h].host);
                toAdd.setHost(sched[4][h].host);
                toAdd.setShow(sched[4][h].sName);
                toAdd.setTime(h + ":00");
                listView.addView(toAdd);
                //schedByDay.add(new ListItem());
            }
            currDay.setText("Thursday");
        } else if (view == fri) {
            for (int h = 0; h < 24; h++) {
                ListItem toAdd = new ListItem(this, h + ":00", sched[5][h].sName, sched[5][h].host);
                toAdd.setHost(sched[5][h].host);
                toAdd.setShow(sched[5][h].sName);
                toAdd.setTime(h + ":00");
                listView.addView(toAdd);
                //schedByDay.add(new ListItem());
            }
            currDay.setText("Friday");
        } else if (view == sat) {
            for (int h = 0; h < 24; h++) {
                ListItem toAdd = new ListItem(this, h + ":00", sched[6][h].sName, sched[6][h].host);
                toAdd.setHost(sched[6][h].host);
                toAdd.setShow(sched[6][h].sName);
                toAdd.setTime(h + ":00");
                listView.addView(toAdd);
                //schedByDay.add(new ListItem());
            }
            currDay.setText("Saturday");
        } else {
            return null;
        }
        return schedByDay;
    }

    @Override
    public void onClick(View v) {

    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancesState) {
        View view = inflater.inflate(R.layout.schedule_fragment, container, false);
        return view;
    }
}
