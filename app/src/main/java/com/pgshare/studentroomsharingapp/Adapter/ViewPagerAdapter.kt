package com.pgshare.studentroomsharingapp.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.pgshare.studentroomsharingapp.R

class ViewPagerAdapter(private val context: Context?, var imageUrls: ArrayList<Uri?>) :
    PagerAdapter() {
    var layoutInflater: LayoutInflater? = null

    override fun getCount(): Int {
        return imageUrls.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.item_view_pager, container, false)
        val imageView = view.findViewById<ImageView>(R.id.UploadGallaryImage)
        imageView.setImageURI(imageUrls.get(position))
        container.addView(view)

        return view
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}
