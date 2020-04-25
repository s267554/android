package it.polito.mad.team19lab2

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import org.json.JSONObject
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var u: UserInfo = UserInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home,R.id.showProfileFragment), drawerLayout) // decidiamo le schermate root e connettiamole al drawer
        setupActionBarWithNavController(navController, appBarConfiguration)//To add navigation support to the default action bar
        navView.setupWithNavController(navController)

        //Comment this to avoid the first page is the itemDetail
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        //destination means source!
        //Log.d("vittoz","${navController.currentDestination!!.id} vs detail ${R.id.nav_item_detail}")
        /*if(navController.currentDestination!!.id==R.id.nav_item_detail){//arrivo da item detail
            navController.navigate(R.id.action_itemDetailsFragment_to_nav_home)
            return false
        }
         */
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        //SHARED PREFERENCES
        Log.d("vittoz","onrestart()")

        val navView: NavigationView = findViewById(R.id.nav_view)
        val sharedPref = getSharedPreferences(
            "it.polito.mad.team19lab2.profile", 0)
        if(sharedPref!=null) {
            Log.d("vittoz","onrestart()")
            val profile = sharedPref.getString("profile", "notFound")
            if (profile != "notFound") {
                val jo = JSONObject(profile)
                nav_view.getHeaderView(0).findViewById<TextView>(R.id.header_title_textView).text = jo.getString("FULL_NAME")
                nav_view.getHeaderView(0).findViewById<TextView>(R.id.header_subtitle_textView).text = jo.get("NICK_NAME") as String
                val file = File(filesDir, "myimage.png")
                if (file.exists()) {
                    nav_view.getHeaderView(0).findViewById<ImageView>(R.id.header_imageView).setImageURI(file.toUri())
                }
            }
        }
    }

}
