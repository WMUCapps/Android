package appinventor.ai_bengg.WMUC_Radio;

import android.content.Context;
import android.view.View;

/**
 * Created by Evan on 4/16/17.
 */

public class ListItem extends View {
    private String time, show, host;

    public ListItem(Context context, String time, String show, String host) {
        super(context);
        this.time = time;
        this.show = show;
        this.host = host;
    }

    public String getTime() { return time; }

    public String getShow() {
        return show;
    }

    public String getHost() {
        return host;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setShow(String show) {
        this.show = show;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
