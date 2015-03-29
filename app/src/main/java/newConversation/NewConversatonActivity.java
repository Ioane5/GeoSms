package newConversation;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ioane.sharvadze.geosms.R;


public class NewConversatonActivity extends FragmentActivity {


    private ViewPager viewPager;
    private TabsPagerAdapter pagerAdapter;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversaton);
        viewPager = (ViewPager)findViewById(R.id.pager);

        pagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());

    }

    private class TabsPagerAdapter extends FragmentPagerAdapter{

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: return null;
                case 1: return null;
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }


    private class FavoriteContactsFragment extends Fragment{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }

    private class AllContactsFragment extends Fragment{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }


}
