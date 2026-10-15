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
 * Fails as soon as any page fails, and when there are more than [maxPages] pages, so a cut list is never
 * shown as complete.
 */
suspend fun <T> getAllPages(
    maxPages: Int = 100,
    fetch: suspend (page: Int) -> Result<HydraCollection<T>>
): Result<List<T>> {
    val all = mutableListOf<T>()
    for (page in 1..maxPages) {
        val result = fetch(page).getOrElse { return Result.failure(it) }
        all += result.member
        if (result.view?.next == null) return Result.success(all)
    }
    return Result.failure(IllegalStateException("The list has more than $maxPages pages, so the app cannot show all of it."))
}
