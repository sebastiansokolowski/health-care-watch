package com.sebastiansokolowski.healthguard.model

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.GsonBuilder
import com.sebastiansokolowski.healthguard.BuildConfig
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.util.SingleEvent
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.rx.RxQuery
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.io.FileOutputStream


/**
 * Created by Sebastian Sokołowski on 23.08.19.
 */
class ShareDataModel(val context: Context, val boxStore: BoxStore) {

    private val healthEventEntityBox: Box<HealthEventEntity> = boxStore.boxFor(HealthEventEntity::class.java)

    val fileToShareObservable: PublishSubject<SingleEvent<Uri>> = PublishSubject.create()

    private val disposables = CompositeDisposable()

    fun shareMeasurementData() {
        val query = healthEventEntityBox.query().build()
        val disposable = RxQuery.observable(query)
                .take(1)
                .subscribeOn(Schedulers.io())
                .map {
                    val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                    return@map gson.toJson(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val uri = saveDataToFile(it)
                    fileToShareObservable.onNext(SingleEvent(uri))
                }
        disposables.add(disposable)
    }

    private fun saveDataToFile(data: String): Uri {
        val file = File(context.cacheDir, "measurement_data.json")
        file.createNewFile()
        if (file.exists()) {
            val outputStream = FileOutputStream(file)
            outputStream.write(data.toByteArray())
            outputStream.close()
        }

        return FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                file)
    }

    fun clear() {
        disposables.clear()
    }

}