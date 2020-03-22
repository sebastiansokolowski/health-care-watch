package com.sebastiansokolowski.healthguard.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.BaseSwipeAdapter
import com.sebastiansokolowski.healthguard.R
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.util.HealthEventHelper
import kotlinx.android.synthetic.main.health_event_item.view.*

/**
 * Created by Sebastian Sokołowski on 24.06.19.
 */
class HealthEventAdapter(val context: Context, private val healthEventEntities: List<HealthEventEntity>, private val healthEventAdapterItemListener: HealthEventAdapterItemListener) : BaseSwipeAdapter(), SwipeLayout.SwipeListener {
    private val TAG = javaClass.canonicalName

    private val healthEventHelper = HealthEventHelper(context)

    fun setEmptyView(view: View) {
        view.visibility = if (count > 0) View.INVISIBLE else View.VISIBLE
    }

    override fun getItem(position: Int): Any {
        return healthEventEntities[position]
    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }

    override fun getItemId(position: Int): Long {
        return healthEventEntities[position].id
    }

    override fun getCount(): Int {
        return healthEventEntities.size
    }

    override fun generateView(position: Int, parent: ViewGroup?): View {
        val swipeLayout = LayoutInflater.from(context).inflate(R.layout.health_event_item, null) as SwipeLayout

        swipeLayout.showMode = SwipeLayout.ShowMode.LayDown
        swipeLayout.addSwipeListener(this@HealthEventAdapter)

        return swipeLayout
    }

    override fun fillValues(position: Int, convertView: View?) {
        val item = healthEventEntities[position]
        convertView?.apply {
            tag = item
            health_event_item_title.text = healthEventHelper.getTitle(item)
            health_event_item_date.text = healthEventHelper.getDate(item)
            health_event_item_event_info.text = healthEventHelper.getEventInfo(item)
            health_event_item_message.text = healthEventHelper.getMessage(item)
            foreground_container.setOnClickListener {
                healthEventAdapterItemListener.onClickItem(item)
            }
            foreground_container.setOnLongClickListener {
                healthEventAdapterItemListener.onLongClickItem(item)
                true
            }
        }
    }

    interface HealthEventAdapterItemListener {
        fun onClickItem(healthEventEntity: HealthEventEntity)
        fun onLongClickItem(healthEventEntity: HealthEventEntity)
        fun onDeleteItem(healthEventEntity: HealthEventEntity)
    }

    //SwipeListener

    override fun onOpen(layout: SwipeLayout?) {
        layout?.let {
            val healthEvent = it.tag as HealthEventEntity
            healthEventAdapterItemListener.onDeleteItem(healthEvent)
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