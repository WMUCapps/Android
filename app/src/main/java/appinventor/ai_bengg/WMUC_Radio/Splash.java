package appinventor.ai_bengg.WMUC_Radio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import appinventor.ai_bengg.WMUC_Radio.R;

public class Splash extends Activity {

    static public Schedule.Show[][] digSched = new Schedule.Show[7][24];
    static public Schedule.Show[][] fmSched = new Schedule.Show[7][24];
    private String digUrl = "http://wmuc.umd.edu/station/schedule/0/2";
    private String fmUrl = "http://wmuc.umd.edu/station/schedule";
    private Document docDig;
    private Document docFM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Thread myThread = new Thread() {
            public void run() {
                initCrawler();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        };
        myThread.start();
    }

    private void initCrawler() {
        Thread digThread = new Thread(new Runnable() {
            @Override
            public void run() {
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
                    docDig = Jsoup.connect(digUrl).get();
                } catch (MalformedURLException mue) {
                    mue.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                shtuff = docDig.select("td");
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
                                while (digSched[col][rowIndex].equals(offAir)) {
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
                                        digSched[col][row++] = new Schedule.Show(curr);
                                    } else {
                                        digSched[col][row++] = offAir;
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


                for (int r = 0; r < 24; r++) {
                    for (int c = 0; c < 7; c++) {
                        System.out.print(digSched[c][r] + " | ");
                    }
                    System.out.println();
                }
            }
        });
        digThread.start();

        Thread fmThread = new Thread(new Runnable() {
            @Override
            public void run() {
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
                    docFM = Jsoup.connect(fmUrl).get();
                } catch (MalformedURLException mue) {
                    mue.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                shtuff = docFM.select("td");
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
                                while (fmSched[col][rowIndex].equals(offAir)) {
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
                                        fmSched[col][row++] = new Schedule.Show(curr);
                                    } else {
                                        fmSched[col][row++] = offAir;
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
            }

        });
        fmThread.start();
        // thread.stop();

        try {
            digThread.join();
            fmThread.join();
        } catch (java.lang.InterruptedException e) {
            System.out.println("Uh oh.");
        }
    }
}
