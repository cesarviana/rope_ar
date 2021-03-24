package com.rope.ropelandia.ctpuzzle

data class Test(val id: Int, val name: String, val items: List<ItemWithId>)

data class ItemWithId(val id: Int, val item: Item)

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

open class UrlCtPuzzle(val method: String, val url: String, val help: String)

class UrlToSaveResponse(
    method: String,
    url: String,
    help: String,
    val responseClass: String
) : UrlCtPuzzle(method, url, help)

data class Progress(val id: Int, val lastVisitedItemId: Int)