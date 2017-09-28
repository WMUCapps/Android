package wmuc_radio;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;


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

    public static int FM = 1;
    public static int DIGITAL = 2;

    private Schedule.Show[][] digSched = Splash.digSched;
    private Schedule.Show[][] fmSched = Splash.fmSched;
    private ArrayList<ListItem> myList;
    private View sun, mon, tue, wed, thu, fri, sat, today, prev;
    private View[] days = new View[7];
    private String[] dayNames = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private TextView currDay, fmToggle, digToggle;
    private ListView listView;
    private int channel;
    private float swipeX1, swipeY1, swipeX2, swipeY2;
    private DisplayMetrics dm = new DisplayMetrics();
    private int hourOfDay;
    private Show currShow;

    public static String CRAWLER_FRAG_TAG = "CRAWL_FRAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        today = initGui();
        //initCrawler();

        currShow = getCurrShow(DIGITAL);

        // Populating the list with today digital by default.
        myList = getScheduleData(today);
        today.setBackgroundColor(Color.parseColor("#ff6b6b"));
        prev = today;
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

                if(show.getText().equals(currShow.sName) && position == hourOfDay) {
                    row.setBackgroundColor(Color.LTGRAY);
                }

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
            return sName.equals(o.sName);
        }
    }

    private ArrayList<ListItem> getScheduleData(View view) {
        ArrayList<ListItem> schedByDay = new ArrayList<ListItem>();
        System.out.println("hello cruel " + view);
        for (int i = 0;i < 7; i++) {
            System.out.println("world is this " + i + " " + days[i] + "  " + dayNames[i]);
            if (view == days[i]) {
                System.out.println("real or just " + i);
                if (channel == DIGITAL) {

                    System.out.println("a dream");
                    for (int h = 0; h < 24; h++) {
                        if (h == 12) {
                            ListItem toAdd = new ListItem(this, "12:00pm", digSched[i][h].sName, digSched[i][h].host);
                            toAdd.setHost(digSched[i][h].host);
                            toAdd.setShow(digSched[i][h].sName);
                            toAdd.setTime("12:00pm");
                            schedByDay.add(new ListItem(this, "12:00pm", digSched[i][h].sName, digSched[i][h].host));
                        } else if (h == 0) {
                            ListItem toAdd = new ListItem(this, "12:00am", digSched[i][h].sName, digSched[i][h].host);
                            toAdd.setHost(digSched[i][h].host);
                            toAdd.setShow(digSched[i][h].sName);
                            toAdd.setTime("12:00am");
                            schedByDay.add(new ListItem(this, "12:00am", digSched[i][h].sName, digSched[i][h].host));
                        } else if (h > 11) {
                            ListItem toAdd = new ListItem(this, (h - 12) + ":00pm", digSched[i][h].sName, digSched[i][h].host);
                            toAdd.setHost(digSched[i][h].host);
                            toAdd.setShow(digSched[i][h].sName);
                            toAdd.setTime((h - 12) + ":00pm");
                            schedByDay.add(new ListItem(this, (h - 12) + ":00pm", digSched[i][h].sName, digSched[i][h].host));
                        } else {
                            ListItem toAdd = new ListItem(this, h + ":00am", digSched[i][h].sName, digSched[i][h].host);
                            toAdd.setHost(digSched[i][h].host);
                            toAdd.setShow(digSched[i][h].sName);
                            toAdd.setTime(h + ":00am");
                            schedByDay.add(new ListItem(this, h + ":00am", digSched[i][h].sName, digSched[i][h].host));
                        }
                    }
                } else {

                    System.out.println("because I");
                    for (int h = 0; h < 24; h++) {
                        if (h == 12) {
                            ListItem toAdd = new ListItem(this, "12:00pm", fmSched[i][h].sName, fmSched[i][h].host);
                            toAdd.setHost(fmSched[i][h].host);
                            toAdd.setShow(fmSched[i][h].sName);
                            toAdd.setTime("12:00pm");
                            schedByDay.add(new ListItem(this, "12:00pm", fmSched[i][h].sName, fmSched[i][h].host));
                        } else if (h == 0) {
                            ListItem toAdd = new ListItem(this, "12:00am", fmSched[i][h].sName, fmSched[i][h].host);
                            toAdd.setHost(fmSched[i][h].host);
                            toAdd.setShow(fmSched[i][h].sName);
                            toAdd.setTime("12:00am");
                            schedByDay.add(new ListItem(this, "12:00am", fmSched[i][h].sName, fmSched[i][h].host));
                        } else if (h > 11) {
                            ListItem toAdd = new ListItem(this, (h - 12) + ":00pm", fmSched[i][h].sName, fmSched[i][h].host);
                            toAdd.setHost(fmSched[i][h].host);
                            toAdd.setShow(fmSched[i][h].sName);
                            toAdd.setTime((h - 12) + ":00pm");
                            schedByDay.add(new ListItem(this, (h - 12) + ":00pm", fmSched[i][h].sName, fmSched[i][h].host));
                        } else {
                            ListItem toAdd = new ListItem(this, h + ":00am", fmSched[i][h].sName, fmSched[i][h].host);
                            toAdd.setHost(fmSched[i][h].host);
                            toAdd.setShow(fmSched[i][h].sName);
                            toAdd.setTime(h + ":00am");
                            schedByDay.add(new ListItem(this, h + ":00am", fmSched[i][h].sName, fmSched[i][h].host));
                        }
                    }
                }
                currDay.setText(dayNames[i]);
            }
        }
        return schedByDay;
    }

    @Override
    public void onClick(View v) {
        if (v == fmToggle) {
            if(channel == DIGITAL) {
                if(prev == today) {
                    currShow = getCurrShow(FM);
                }
                fmToggle.setTextColor(Color.RED);
                digToggle.setTextColor(Color.BLACK);
                channel = FM;
                myList = getScheduleData(prev);
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

                        if(currShow != null && show.getText().equals(currShow.sName) && position == hourOfDay) {
                            row.setBackgroundColor(Color.LTGRAY);
                        }

                        return row;
                    }
                });
            }
        } else if (v == digToggle) {
            if(channel == FM) {
                if (prev == today) {
                    currShow = getCurrShow(DIGITAL);
                }
                digToggle.setTextColor(Color.RED);
                fmToggle.setTextColor(Color.BLACK);
                channel = DIGITAL;
                myList = getScheduleData(prev);
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

                        if(currShow != null && show.getText().equals(currShow.sName) && position == hourOfDay) {
                            row.setBackgroundColor(Color.LTGRAY);
                        }

                        return row;
                    }
                });
            }
        } else {
            prev.setBackgroundColor(Color.parseColor("#fafafa"));
            prev = v;
            myList = getScheduleData(v);
            if( v == today ) {
                currShow = getCurrShow(channel);
            }
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

                    if(currShow != null && show.getText().equals(currShow.sName) && position == hourOfDay) {
                        row.setBackgroundColor(Color.LTGRAY);
                    }

                /* FOR UNDERLINE
                SpannableString content = new SpannableString(myList.get(position).getShow());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                show.setText(content);*/

                    return row;
                }
            });
        }
    }

    private View initGui() {
        View curDay = sun;

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        this.getWindowManager().getDefaultDisplay().getMetrics( dm );

        sun = findViewById(R.id.sunday);
        sun.setOnClickListener(this);
        sun.setOnTouchListener(new TouchHandler());
        mon = findViewById(R.id.monday);
        mon.setOnClickListener(this);
        mon.setOnTouchListener(new TouchHandler());
        tue = findViewById(R.id.tuesday);
        tue.setOnClickListener(this);
        tue.setOnTouchListener(new TouchHandler());
        wed = findViewById(R.id.wednesday);
        wed.setOnClickListener(this);
        wed.setOnTouchListener(new TouchHandler());
        thu = findViewById(R.id.thursday);
        thu.setOnClickListener(this);
        thu.setOnTouchListener(new TouchHandler());
        fri = findViewById(R.id.friday);
        fri.setOnClickListener(this);
        fri.setOnTouchListener(new TouchHandler());
        sat = findViewById(R.id.saturday);
        sat.setOnClickListener(this);
        sat.setOnTouchListener(new TouchHandler());
        fmToggle = (TextView) findViewById(R.id.fmToggle);
        fmToggle.setOnClickListener(this);
        fmToggle.setOnTouchListener(new TouchHandler());
        digToggle = (TextView) findViewById(R.id.digToggle);
        digToggle.setOnClickListener(this);
        digToggle.setOnTouchListener(new TouchHandler());
        digToggle.setTextColor(Color.RED);

        currDay = (TextView) findViewById(R.id.day);
        listView = (ListView) findViewById(R.id.list);
        listView.setOnTouchListener(new TouchHandler());
        currDay.setOnTouchListener(new TouchHandler());

        channel = DIGITAL;

        switch (day) {
            case Calendar.SUNDAY:
                curDay = sun;
                break;
            case Calendar.MONDAY:
                curDay = mon;
                break;
            case Calendar.TUESDAY:
                curDay = tue;
                break;
            case Calendar.WEDNESDAY:
                curDay = wed;
                break;
            case Calendar.THURSDAY:
                curDay = thu;
                break;
            case Calendar.FRIDAY:
                curDay = fri;
                break;
            case Calendar.SATURDAY:
                curDay = sat;
                break;
        }

        return curDay;
    }

    private Show getCurrShow(int channel) {
        Show show = null;

        if(channel == DIGITAL) {
            if (today == sun) {
                show = digSched[0][hourOfDay];
            } else if (today == mon) {
                show = digSched[1][hourOfDay];
            } else if (today == tue) {
                show = digSched[2][hourOfDay];
            } else if (today == wed) {
                show = digSched[3][hourOfDay];
            } else if (today == thu) {
                show = digSched[4][hourOfDay];
            } else if (today == fri) {
                show = digSched[5][hourOfDay];
            } else if (today == sat) {
                show = digSched[6][hourOfDay];
            }
        } else if(channel == FM) {
            if (today == sun) {
                show = fmSched[0][hourOfDay];
            } else if (today == mon) {
                show = fmSched[1][hourOfDay];
            } else if (today == tue) {
                show = fmSched[2][hourOfDay];
            } else if (today == wed) {
                show = fmSched[3][hourOfDay];
            } else if (today == thu) {
                show = fmSched[4][hourOfDay];
            } else if (today == fri) {
                show = fmSched[5][hourOfDay];
            } else if (today == sat) {
                show = fmSched[6][hourOfDay];
            }
        }
        days[0] = sun;
        days[1] = mon;
        days[2] = tue;
        days[3] = wed;
        days[4] = thu;
        days[5] = fri;
        days[6] = sat;
        return show;
    }

    private class TouchHandler implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent touchevent) {
            final int X = (int) touchevent.getRawX();
            final int Y = (int) touchevent.getRawY();
            switch (touchevent.getAction()) {
                // when user first touches the screen we get x and y coordinate
                case MotionEvent.ACTION_DOWN: {
 /*                   RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    _xDelta = X - lParams.leftMargin;
                    _yDelta = Y - lParams.topMargin;*/
                    swipeX1 = touchevent.getX();
                    swipeY1 = touchevent.getY();
                    break;
                }
               /* case MotionEvent.ACTION_MOVE: {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    if(view == FMButton)
                        layoutParams.leftMargin = Math.min(X - _xDelta, xDest);
                    else
                        layoutParams.leftMargin = Math.max(X - _xDelta, xDest);
                    view.setLayoutParams(layoutParams);
                    break;
                }*/
                case MotionEvent.ACTION_UP: {
                    swipeX2 = touchevent.getX();
                    swipeY2 = touchevent.getY();

                    // if left to right sweep event on screen
                    if ((swipeX2 - swipeX1) > (dm.widthPixels / 3)) {
                        onClick(fmToggle);
                    }

                    // if right to left sweep event on screen
                    if ((swipeX1 - swipeX2) > (dm.widthPixels / 3)) {
                        onClick(digToggle);

                    }

                    // if UP to Down sweep event on screen
                    if (swipeY1 < swipeY2) {
                        //do nothing.
                    }

                    // if Down to UP sweep event on screen
                    if (swipeY1 > swipeY2) {
                        //do nothing.
                    }
                    break;
                }
            }
            return false;
        }
    }

}