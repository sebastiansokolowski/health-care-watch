package com.sebastiansokolowski.healthcarewatch.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.BaseSwipeAdapter
import com.sebastiansokolowski.healthcarewatch.R
import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEvent
import com.sebastiansokolowski.healthcarewatch.util.HealthCareEventHelper
import kotlinx.android.synthetic.main.health_care_event_item.view.*

/**
 * Created by Sebastian Sokołowski on 24.06.19.
 */
class HealthCareEventAdapter(val context: Context, private val healthCareEvents: List<HealthCareEvent>, private val healthCareEventAdapterItemListener: HealthCareEventAdapterItemListener) : BaseSwipeAdapter(), SwipeLayout.SwipeListener {
    private val TAG = javaClass.canonicalName

    private val healthCareEventHelper = HealthCareEventHelper(context)

    fun setEmptyView(view: View) {
        view.visibility = if (count > 0) View.INVISIBLE else View.VISIBLE
    }

    override fun getItem(position: Int): Any {
        return healthCareEvents[position]
    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }

    override fun getItemId(position: Int): Long {
        return healthCareEvents[position].id
    }

    override fun getCount(): Int {
        return healthCareEvents.size
    }

    override fun generateView(position: Int, parent: ViewGroup?): View {
        val swipeLayout = LayoutInflater.from(context).inflate(R.layout.health_care_event_item, null) as SwipeLayout

        swipeLayout.showMode = SwipeLayout.ShowMode.LayDown
        swipeLayout.addSwipeListener(this@HealthCareEventAdapter)

        return swipeLayout
    }

    override fun fillValues(position: Int, convertView: View?) {
        val item = healthCareEvents[position]
        convertView?.apply {
            tag = item
            health_care_event_item_title.text = healthCareEventHelper.getTitle(item)
            health_care_event_item_date.text = healthCareEventHelper.getDate(item)
            health_care_event_item_event_info.text = healthCareEventHelper.getEventInfo(item)
            health_care_event_item_message.text = healthCareEventHelper.getMessage(item)
            foreground_container.setOnClickListener {
                healthCareEventAdapterItemListener.onClickItem(item)
            }
        }
    }

    interface HealthCareEventAdapterItemListener {
        fun onClickItem(healthCareEvent: HealthCareEvent)
        fun onDeleteItem(healthCareEvent: HealthCareEvent)
    }

    //SwipeListener

    override fun onOpen(layout: SwipeLayout?) {
        layout?.let {
            val healthCareEvent = it.tag as HealthCareEvent
            healthCareEventAdapterItemListener.onDeleteItem(healthCareEvent)
        }
    }

    override fun onUpdate(layout: SwipeLayout?, leftOffset: Int, topOffset: Int) {
    }

    override fun onStartOpen(layout: SwipeLayout?) {
    }

    override fun onStartClose(layout: SwipeLayout?) {
    }

    override fun onHandRelease(layout: SwipeLayout?, xvel: Float, yvel: Float) {
    }

    override fun onClose(layout: SwipeLayout?) {
    }
}