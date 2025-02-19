package com.example.posapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import com.example.posapp.fragments.CashierFragment
import com.example.posapp.Databases
import com.example.posapp.fragments.HistoryFragment
import com.example.posapp.R
import com.example.posapp.fragments.ReportFragment
import com.example.posapp.fragments.StoreFragment
import com.example.posapp.databinding.ActivityMainBinding
import com.example.posapp.fragments.SettingsFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        val navigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(this)

        // Toggle to show menu navigation
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.drawerArrowDrawable.color = getColor(R.color.main_color)
        toggle.syncState()

        // Default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CashierFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_cashier)
            updateToolbarTitle("Cashier")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {

            R.id.nav_cashier -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, CashierFragment()).commit()
                updateToolbarTitle("Cashier")  // Set title for Cashier fragment
            }
            R.id.nav_history_order -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HistoryFragment()).commit()
                updateToolbarTitle("History Orders")  // Set title for History Orders fragment
            }
            R.id.nav_report -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ReportFragment()).commit()
                updateToolbarTitle("Reports")  // Set title for Reports fragment
            }
            R.id.nav_manage_store -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, StoreFragment()).commit()
                updateToolbarTitle("Manage Store")  // Set title for Manage Store fragment
            }
            R.id.nav_account -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SettingsFragment()).commit()
                updateToolbarTitle("Account")
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun updateToolbarTitle(title: String) {
        val toolbarTitle = binding.toolbar.findViewById<TextView>(R.id.toolbar_title)
        toolbarTitle.text = title
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}