
package com.custom.acertvlauncher

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.*
import android.view.View
import android.view.ViewGroup
import java.io.File

class MainActivity : Activity() {

    private lateinit var wallpaperView: ImageView
    private lateinit var appsGrid: GridView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wallpaperView = findViewById(R.id.wallpaper)
        appsGrid = findViewById(R.id.appsGrid)

        loadWallpaper()
        loadApps()
    }

    private fun loadApps() {
        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(pm).toString().lowercase() }


        appsGrid.adapter = object : BaseAdapter() {

            override fun getCount() = apps.size

            override fun getItem(position: Int) = apps[position]

            override fun getItemId(position: Int) = position.toLong()

            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {

                val view = convertView ?: layoutInflater.inflate(
                    R.layout.app_tile,
                    parent,
                    false
                )

                val app = apps[position]

                val icon = view.findViewById<ImageView>(R.id.appIcon)
                val name = view.findViewById<TextView>(R.id.appName)

                icon.setImageDrawable(app.loadIcon(pm))
                name.text = app.loadLabel(pm)

                // Highlight currently selected app
                view.isActivated =
                    position == appsGrid.selectedItemPosition

                return view
            }
        }

// IMPORTANT: This listener is OUTSIDE the adapter.
        appsGrid.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    for (i in 0 until appsGrid.childCount) {
                        val child = appsGrid.getChildAt(i)

                        child.isActivated =
                            (appsGrid.firstVisiblePosition + i == position)
                    }
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {
                    for (i in 0 until appsGrid.childCount) {
                        appsGrid.getChildAt(i).isActivated = false
                    }
                }
            }



        appsGrid.setOnItemClickListener { _, _, position, _ ->
            val app = apps[position]
            val info = app.activityInfo

            val launchIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                setClassName(info.packageName, info.name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                startActivity(launchIntent)
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Unable to open app",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        appsGrid.requestFocus()
    }

    private fun loadWallpaper() {
        val file = File(filesDir, "wallpaper.jpg")

        if (!file.exists()) {
            wallpaperView.setImageDrawable(null)
            return
        }

        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeFile(file.absolutePath, bounds)

        var sampleSize = 1
        while (
            bounds.outWidth / sampleSize > 1280 ||
            bounds.outHeight / sampleSize > 720
        ) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }

        val bitmap = BitmapFactory.decodeFile(
            file.absolutePath,
            options
        )

        if (bitmap != null) {
            wallpaperView.setImageBitmap(bitmap)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        loadWallpaper()
    }

    override fun onResume() {
        super.onResume()
        if (::wallpaperView.isInitialized) {
            loadWallpaper()
        }
    }
}
