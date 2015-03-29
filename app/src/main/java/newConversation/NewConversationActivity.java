package newConversation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ioane.sharvadze.geosms.R;


public class NewConversationActivity extends ActionBarActivity {


    private ViewPager viewPager;
    private TabsPagerAdapter pagerAdapter;
    private static final String TAG = NewConversationActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversaton);
        viewPager = (ViewPager)findViewById(R.id.pager);


        pagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

    }

    private class TabsPagerAdapter extends FragmentPagerAdapter{

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: return new FavoriteContactsFragment();
                case 1: return new AllContactsFragment();
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            final CharSequence first = getResources().getString(R.string.favorites);
            final CharSequence second = getResources().getString(R.string.all);

            switch (position){
                case 0: return first;
                case 1: return second;
                default: return null;
            }
        }
    }


    public static class FavoriteContactsFragment extends Fragment{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            return inflater.inflate(
                    R.layout.fragment_favorite_contacts, container, false);
        }
    }


    public static class AllContactsFragment extends Fragment{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            Log.i(TAG, "here onCreateView");
            return inflater.inflate(
                    R.layout.fragment_favorite_contacts, container, false);
        }
    }


}
