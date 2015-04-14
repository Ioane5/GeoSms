package utils;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.SettingsActivity;

/**
 * Created by Ioane on 3/5/2015.
 */
public abstract class MyActivity extends ActionBarActivity {

    private static final String TAG = MyActivity.class.getSimpleName();

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
                Intent i = new Intent(MyActivity.this,SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return false;
        }

    }

}
