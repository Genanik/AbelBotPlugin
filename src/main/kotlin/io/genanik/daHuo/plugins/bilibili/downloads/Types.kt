package io.genanik.daHuo.plugins.bilibili.downloads

data class TypesOptions (
    // Playlist indicates if we need to extract the whole playlist rather than the single video.
    val Playlist: Boolean,
    // Items defines wanted items from a playlist. Separated by commas like: 1,5,6,8-10.
    val Items: String,
    // ItemStart defines the starting item of a playlist.
    val ItemStart: Int,
    // ItemEnd defines the ending item of a playlist.
    val ItemEnd: Int,

    // ThreadNumber defines how many threads will use in the extraction, only works when Playlist is true.
    val ThreadNumber: Int,
    val Cookie: String,

    // EpisodeTitleOnly indicates file name of each bilibili episode doesn't include the playlist title
    val EpisodeTitleOnly: Boolean
)


data class TypesPart (
    val url: String,
    val size: Long,
    val ext: String
)

// Data is the main data structure for the whole video data.
data class TypesData (
    // URL is used to record the address of this download
    val url: String,
    val site: String,
    val title: String,
    val type: String,
    // each stream has it's own Parts and Quality
    val streams: Map<String, Stream>,
    // danmaku, subtitles, etc
    val caption: TypesPart,
    // Err is used to record whether an error occurred when extracting the list data
    val err: Error?
)