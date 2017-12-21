package com.sjn.stamp.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.sjn.stamp.R;
import com.sjn.stamp.ui.DrawerMenu;
import com.sjn.stamp.utils.AnalyticsHelper;
import com.sjn.stamp.utils.LogHelper;

public abstract class DrawerActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    private static final String TAG = LogHelper.makeLogTag(DrawerActivity.class);
    public static final String FRAGMENT_TAG = "fragment_container";

    protected Toolbar mToolbar;

    protected Drawer mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    protected AccountHeader mAccountHeader;

    private long mNextDrawerMenu;
    protected long mSelectingDrawerMenu;
    private boolean mToolbarInitialized;

    public boolean onOptionsItemSelected(int itemId) {
        return false;
    }

    protected void changeFragmentByDrawer(long selectedDrawerMenu) {
        DrawerMenu drawerMenu = DrawerMenu.of(selectedDrawerMenu);
        if (drawerMenu == null) {
            return;
        }
        navigateToBrowser(drawerMenu.getFragment(), false, selectedDrawerMenu);
        setToolbarTitle(null);
    }

    public void setToolbarTitle(CharSequence title) {
        if (title == null) {
            title = getSelectingDrawerName();
        }
        setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        try {
            CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                    menu, R.id.media_route_menu_item);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onBackStackChanged() {
        updateDrawerToggleState();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG, "Activity onCreate");
        try {
            CastContext.getSharedInstance(this);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mToolbarInitialized) {
            throw new IllegalStateException("You must run super.initializeToolbar at " +
                    "the end of your onCreate method");
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Whenever the fragment back stack changes, we may need to update the
        // action bar toggle: only top level screens show the hamburger-like icon, inner
        // screens - either Activities or fragments - show the "Up" icon instead.
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    final public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // If not handled by drawerToggle, home needs to be handled by returning to previous
        if (item != null && item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item != null) {
            if (!onOptionsItemSelected(item.getItemId())) {
                return super.onOptionsItemSelected(item);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the drawer is open, back will close it
        if (mDrawer != null && mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
            return;
        }
        // Otherwise, it may return to the previous fragment stack
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            moveTaskToBack(true);
            // Lastly, it will rely on the system behavior for back
            // super.onBackPressed();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mToolbar.setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        mToolbar.setTitle(titleId);
    }

    public void navigateToBrowser(Fragment fragment, boolean addToBackStack, long selection) {
        navigateToBrowser(fragment, addToBackStack);
        mDrawer.setSelection(selection);
    }

    public void navigateToBrowser(Fragment fragment, boolean addToBackStack) {
        if (!addToBackStack) {
            getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, FRAGMENT_TAG);
        if (addToBackStack) {
            // If this is not the top level media (root), we add it to the fragment back stack,
            // so that actionbar toggle and Back will work appropriately:
            //transaction.setCustomAnimations(R.anim.fade_out, R.anim.fade_in);
            transaction.addToBackStack(null);
        }
        transaction.commit();
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.setExpanded(true, true);
    }

    protected void initializeToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            throw new IllegalStateException("Layout is required to include a Toolbar with id " +
                    "'toolbar'");
        }
        mToolbar.inflateMenu(R.menu.main);
        setSupportActionBar(mToolbar);
        mSelectingDrawerMenu = getCurrentMenuId();
        createDrawer();
        mToolbarInitialized = true;
    }

    protected long getCurrentMenuId() {
        if (mDrawer != null) {
            return mDrawer.getCurrentSelection();
        }
        return DrawerMenu.first().getMenuId();
    }

    private void updateDrawerToggleState() {
        if (mDrawerToggle == null) {
            return;
        }
        boolean isRoot = getSupportFragmentManager().getBackStackEntryCount() == 0;
        mDrawerToggle.setDrawerIndicatorEnabled(isRoot);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(!isRoot);
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isRoot);
            getSupportActionBar().setHomeButtonEnabled(!isRoot);
        }
        if (isRoot) {
            mDrawerToggle.syncState();
        }
    }

    private String getSelectingDrawerName() {
        if (mDrawer != null && mDrawer.getDrawerItem(mDrawer.getCurrentSelection()) instanceof Nameable) {
            return ((Nameable) mDrawer.getDrawerItem(mDrawer.getCurrentSelection())).getName().getText(this);
        }
        return getString(R.string.app_name);
    }

    private void createDrawer() {
        createAccountHeader();
        mDrawer = new DrawerBuilder().withActivity(this)
                .withAccountHeader(mAccountHeader)
                .withToolbar(mToolbar)
                .inflateMenu(R.menu.drawer)
                .withSelectedItem(mSelectingDrawerMenu)
                .withOnDrawerNavigationListener(new Drawer.OnDrawerNavigationListener() {
                    @Override
                    public boolean onNavigationClickListener(View clickedView) {
                        //this method is only called if the Arrow icon is shown. The hamburger is automatically managed by the MaterialDrawer
                        //if the back arrow is shown. close the activity
                        onBackPressed();
                        //return true if we have consumed the event
                        return true;
                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        AnalyticsHelper.trackScreen(DrawerActivity.this, DrawerMenu.of(drawerItem.getIdentifier()));
                        setNextDrawerMenu(drawerItem);
                        return false;
                    }
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View view) {
                    }

                    @Override
                    public void onDrawerClosed(View view) {
                        if (mSelectingDrawerMenu == mNextDrawerMenu) {
                            return;
                        }
                        mSelectingDrawerMenu = mNextDrawerMenu;
                        changeFragmentByDrawer(mNextDrawerMenu);
                    }

                    @Override
                    public void onDrawerSlide(View view, float v) {
                    }
                })
                .build();

        mDrawerToggle = mDrawer.getActionBarDrawerToggle();
    }

    private void setNextDrawerMenu(IDrawerItem drawerItem) {
        mNextDrawerMenu = drawerItem.getIdentifier();
    }

    private void createAccountHeader() {
        mAccountHeader = new AccountHeaderBuilder()
                .withSelectionListEnabledForSingleProfile(false)
                .withActivity(this)
                .withHeaderBackground(R.drawable.drawer_header).build();
    }
}
