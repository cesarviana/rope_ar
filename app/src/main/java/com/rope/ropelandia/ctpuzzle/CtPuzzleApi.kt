package com.rope.ropelandia.ctpuzzle

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.json.JSONObject
import java.net.URI
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread

private const val TAG = "CT_API"

object CtPuzzleApi {

    private lateinit var requestQueue: RequestQueue
    private val jackson by lazy { jacksonObjectMapper() }

    private val defaultSuccessCallback: (response: JSONObject) -> Unit = { response ->
        Log.d(TAG, response.toString())
    }
    private val defaultErrorCallback: (error: VolleyError) -> Unit = {
        Log.e(TAG, "Error: ${it.message}")
        try {
            val errorData = String(it.networkResponse.data, Charset.forName("UTF-8"))
            Log.e(TAG, "Error data: $errorData")
        } catch (e: Exception) {
            Log.e(TAG, "Error when reading error data. ${e.message}")
        }
    }

    fun initialize(context: Context) {
        requestQueue = Volley.newRequestQueue(context)
        requestQueue.start()
    }

    fun newParticipation(dataUrl: String, callback: (participation: Participation) -> Unit) {
        thread(start = true) {
            UUID.randomUUID().let {
                URI.create(dataUrl).resolve(it.toString())
            }.let {
                val type = object : TypeReference<Participation>() {}
                jackson.readValue(it.toURL(), type)
            }.let {
                callback.invoke(it)
            }
        }
    }

    fun registerProgress(participation: Participation, item: ItemWithId) {
        val progress = Progress(
            id = participation.participationId,
            lastVisitedItemId = item.id
        )

        val url = participation.urlToSendProgress.url

        jackson.writeValueAsString(progress)
            .let {
                JSONObject(it)
            }.let {
                JsonObjectRequest(
                    Request.Method.PUT, url, it,
                    defaultSuccessCallback, defaultErrorCallback
                )
            }.let {
                requestQueue.add(it)
            }
    }

    fun registerResponse(
        participation: Participation,
        item: ItemWithId,
        responseForItem: ResponseForItem
    ) {
        require(item.id > 0){
            "Throw invalid item id"
        }
        val url = participation.urlToSendResponses.url.replace("<item_id>", item.id.toString())
        Log.d(TAG, "URL: $url")

        jackson.writeValueAsString(responseForItem)
            .let {
                JSONObject(it)
            }.let {
                JsonObjectRequest(
                    Request.Method.POST, url, it,
                    defaultSuccessCallback, defaultErrorCallback
                )
            }.let {
                requestQueue.add(it)
            }
    }
}