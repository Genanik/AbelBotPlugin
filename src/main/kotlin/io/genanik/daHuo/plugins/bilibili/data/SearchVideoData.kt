package io.genanik.daHuo.plugins.bilibili.data

data class BiliSearchRequest(
    val data: BiliSearchData
)

data class BiliSearchData(
    val result: List<BliSearchResult>
)

data class BliSearchResult(
    val result_type: String,
    val data: List<SearchResultData>
)

data class SearchResultData(
    val id: Int // aid
)