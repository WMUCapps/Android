package wmuc.radio;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class Schedule extends Activity implements View.OnClickListener {

    private Button DIGButton;
    private Button FMButton;
    private TextView sh;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
 
        DIGButton = (Button) findViewById(R.id.DIG);
        DIGButton.setOnClickListener(this);
        FMButton = (Button) findViewById(R.id.FM);
        FMButton.setOnClickListener(this);

        sh = (TextView) findViewById(R.id.sh);

        webView = (WebView)findViewById(R.id.webView);
        webView.loadUrl("http://wmuc.umd.edu/station/schedule?#now");
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
        if (v == DIGButton && sh.getText().equals("FM")) {
            sh.setText("Digital");
            webView.loadUrl("http://wmuc.umd.edu/station/schedule/0/2?#now");
        }
        if (v == FMButton && sh.getText().equals("Digital")) {
            sh.setText("FM");
            webView.loadUrl("http://wmuc.umd.edu/station/schedule?#now");
        }
    }
}
