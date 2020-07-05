package com.sebastiansokolowski.healthguard.model

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.BuildConfig
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.db.entity.SensorEventEntity
import com.sebastiansokolowski.healthguard.util.SingleEvent
import com.sebastiansokolowski.shared.dataModel.DataExport
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.rx.RxQuery
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.io.FileWriter


/**
 * Created by Sebastian Sokołowski on 23.08.19.
 */
class ShareDataModel(val context: Context, val boxStore: BoxStore) {

    private val healthEventEntityBox: Box<HealthEventEntity> = boxStore.boxFor(HealthEventEntity::class.java)
    private val sensorEventEntityBox: Box<SensorEventEntity> = boxStore.boxFor(SensorEventEntity::class.java)

    val filesToShareObservable: PublishSubject<SingleEvent<ArrayList<Uri>>> = PublishSubject.create()

    private val disposables = CompositeDisposable()

    fun shareDataForTesting(comment: String, testMode: DataExport.TestMode, counter: Int?) {
        val disposable = Single.zip(getHealthEventsObservable(), getSensorEventsObservable(), BiFunction { healthEvents: MutableList<HealthEvent>, sensorEvents: MutableList<SensorEvent> ->
            val exportData = DataExport(comment, testMode, counter, healthEvents, sensorEvents)

            val exportDataUri = saveDataToFile(exportData)
            val databaseUri = getDatabaseUri()
            filesToShareObservable.onNext(SingleEvent(arrayListOf(exportDataUri, databaseUri)))
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        disposables.add(disposable)
    }

    private fun getDatabaseUri(): Uri {
        val file = File(context.filesDir, "/objectbox/objectbox/data.mdb")
        return FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                file)
    }

    private fun getHealthEventsObservable(): Single<MutableList<HealthEvent>> {
        val query = healthEventEntityBox.query().build()
        return RxQuery.single(query)
                .map {
                    val healthEvents = mutableListOf<HealthEvent>()
                    it.forEach {
                        val healthEvent = createHealthEvent(it)
                        healthEvents.add(healthEvent)
                    }
                    healthEvents
                }
    }

    private fun getSensorEventsObservable(): Single<MutableList<SensorEvent>> {
        val query = sensorEventEntityBox.query().build()
        return RxQuery.single(query)
                .map {
                    val sensorEvents = mutableListOf<SensorEvent>()
                    it.forEach {
                        val sensorEntity = createSensorEvent(it)
                        sensorEvents.add(sensorEntity)
                    }
                    sensorEvents
                }
    }

    private fun createHealthEvent(healthEventEntity: HealthEventEntity): HealthEvent {
        healthEventEntity.apply {
            val sensorEntity = createSensorEvent(sensorEventEntity.target)
            return HealthEvent(event, sensorEntity, value, emptyList(), details, measurementEventEntity.targetId)
        }
    }

    private fun createSensorEvent(sensorEventEntity: SensorEventEntity): SensorEvent {
        sensorEventEntity.apply {
            return SensorEvent(type, value, accuracy, measurementEventEntity.targetId, timestamp)
        }
    }

    private fun saveDataToFile(data: DataExport): Uri {
        val file = File(context.cacheDir, "data.json")
        val writer = FileWriter(file)
        Gson().toJson(data, writer)
        writer.close()

        return FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                file)
    }

    fun clear() {
        disposables.clear()
    }

}