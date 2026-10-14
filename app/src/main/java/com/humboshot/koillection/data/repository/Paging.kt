package com.humboshot.koillection.data.repository

import com.humboshot.koillection.data.hydra.HydraCollection

/** A single loaded page plus whether the server reports another one after it. */
data class Page<T>(
    val members: List<T>,
    val hasNext: Boolean,
    val totalItems: Int? = null,
)

fun <T> HydraCollection<T>.toPage(): Page<T> = Page(member, view?.next != null, totalItems)

/**
 * Follows Hydra `view.next` links, starting at page 1, until the server reports no further page.
 * Fails as soon as any page fails.
 */
suspend fun <T> getAllPages(
    maxPages: Int = 100,
    fetch: suspend (page: Int) -> Result<HydraCollection<T>>
): Result<List<T>> {
    val all = mutableListOf<T>()
    var page = 1
    while (page <= maxPages) {
        val result = fetch(page).getOrElse { return Result.failure(it) }
        all += result.member
        if (result.view?.next == null) break
        page++
    }
    return Result.success(all)
}
