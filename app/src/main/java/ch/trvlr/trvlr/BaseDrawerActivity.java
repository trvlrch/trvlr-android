package ch.trvlr.trvlr;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

public class BaseDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    protected DrawerLayout mDrawerLayout;
    protected FrameLayout mFrameLayout;
    protected NavigationView mNavigationView;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected int menuId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base_drawer);

        mFrameLayout = (FrameLayout) findViewById(R.id.content_frame);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        populateMenu(mNavigationView.getMenu());
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (item.isChecked()){
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return false;
        }

        uncheckItems(); // TODO maybe there's a better way...

        if (id == 0) {
            startActivity(new Intent(getApplicationContext(), FindConnectionActivity.class));
        } else {
            startActivity(new Intent(getApplicationContext(), ListPrivateChatsActivity.class));
        }
        finish();

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void uncheckItems() {
        Menu menu = mNavigationView.getMenu();
        for (int i = 0; i < menu.size(); i++)
            menu.getItem(i).setChecked(false);
    }

    public void populateMenu(Menu menu){
        menu.clear();
        menu.add(0, 0, 0, "Find connection");
        menu.add(0, 1, 0, "Private chats");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        uncheckItems();
        mNavigationView.setCheckedItem(this.getMenuId());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    protected int getMenuId() {
        return this.menuId;
    }

}

