package com.rope.ropelandia.ctpuzzle

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Test(val id: Int, val name: String, val items: List<ItemWithId>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ItemWithId(val id: Int, val item: Item)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Participation(
    val participationId: Int,
    val lastVisitedId: Int,
    val test: Test,
    val lastVisitedItemId: Int,
    val urlToSendProgress: UrlCtPuzzle,
    val urlToSendUserData: UrlCtPuzzle,
    val urlToSendResponses: UrlToSaveResponse
) {
    fun getTestItem(index: Int) = test.items[index]
}

@JsonIgnoreProperties(ignoreUnknown = true)
open class UrlCtPuzzle(val method: String, val url: String, val help: String)

@JsonIgnoreProperties(ignoreUnknown = true)
class UrlToSaveResponse(
    method: String,
    url: String,
    help: String,
    val responseClass: String
) : UrlCtPuzzle(method, url, help)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Progress(val id: Int, val lastVisitedItemId: Int)