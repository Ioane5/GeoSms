package utils;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.SettingsActivity;
import com.ioane.sharvadze.geosms.SettingsTestActivity;

/**
 * Created by Ioane on 3/5/2015.
 */
public class MyActivity extends ActionBarActivity {

    private static final String TAG = MyActivity.class.getSimpleName();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.animator.abc_slide_out_left, R.anim.abc_fade_out);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(MyActivity.this,SettingsTestActivity.class);
                startActivity(i);
                return true;
//            case android.R.id.home:
//                finish();
//                overridePendingTransition(R.animator.abc_slide_out_left, R.anim.abc_fade_out);
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
