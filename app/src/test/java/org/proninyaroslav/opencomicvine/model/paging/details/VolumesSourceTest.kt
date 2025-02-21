package org.proninyaroslav.opencomicvine.model.paging.details

import androidx.paging.PagingSource
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verifyAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.proninyaroslav.opencomicvine.model.AppPreferences
import org.proninyaroslav.opencomicvine.model.repo.ComicVineResult
import org.proninyaroslav.opencomicvine.model.repo.FavoriteFetchResult
import org.proninyaroslav.opencomicvine.model.repo.FavoritesRepository
import org.proninyaroslav.opencomicvine.model.repo.VolumesRepository
import org.proninyaroslav.opencomicvine.types.FavoriteInfo
import org.proninyaroslav.opencomicvine.types.StatusCode
import org.proninyaroslav.opencomicvine.types.VolumeInfo
import org.proninyaroslav.opencomicvine.types.VolumesResponse
import org.proninyaroslav.opencomicvine.types.filter.VolumesFilter
import org.proninyaroslav.opencomicvine.types.sort.ComicVineSortDirection
import org.proninyaroslav.opencomicvine.types.sort.VolumesSort

class VolumesSourceTest {
    lateinit var source: VolumesSource

    @MockK
    lateinit var volumesRepo: VolumesRepository

    @MockK
    lateinit var pref: AppPreferences

    @MockK
    lateinit var favoritesRepo: FavoritesRepository

    val idList = (1..10).toList()

    val sort = VolumesSort.DateAdded(direction = ComicVineSortDirection.Asc)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        source = VolumesSource(
            idList,
            flowOf(sort),
            volumesRepo,
            favoritesRepo,
        )
    }

    @Test
    fun `Load success result when more data is present`() = runTest {
        val pageSize = 5
        val response = mockk<VolumesResponse>()
        val volumesList = List(pageSize) {
            val info = mockk<VolumeInfo>()
            every { info.id } returns it
            every {
                favoritesRepo.observe(
                    entityId = it,
                    entityType = FavoriteInfo.EntityType.Volume,
                )
            } returns flowOf(FavoriteFetchResult.Success(isFavorite = true))
            info
        }

        every { response.statusCode } returns StatusCode.OK
        every { response.results } returns volumesList
        every { response.numberOfPageResults } returns pageSize
        every { response.error } returns "OK"
        every { response.limit } returns pageSize
        every { response.offset } returns 0
        every { response.numberOfTotalResults } returns pageSize
        coEvery {
            volumesRepo.getItems(
                offset = 0,
                limit = pageSize,
                sort = sort,
                filters = listOf(VolumesFilter.Id(idList.subList(0, pageSize))),
            )
        } returns ComicVineResult.Success(response)

        val result = source.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = pageSize,
                placeholdersEnabled = false,
            )
        )
        assertTrue(result is PagingSource.LoadResult.Page)
        assertNotNull((result as PagingSource.LoadResult.Page).nextKey)

        verifyAll {
            response.statusCode
            response.results
            response.numberOfPageResults
            response.error
            response.limit
            response.offset
            response.numberOfTotalResults
        }
        coVerify {
            volumesRepo.getItems(
                offset = 0,
                limit = pageSize,
                sort = sort,
                filters = listOf(VolumesFilter.Id(idList.subList(0, pageSize))),
            )
        }
        confirmVerified(volumesRepo, response)
    }

    @Test
    fun `Load success result and endOfPaginationReached when no more data`() = runTest {
        val pageSize = 10
        val response = mockk<VolumesResponse>()
        val volumesList = emptyList<VolumeInfo>()

        every { response.statusCode } returns StatusCode.OK
        every { response.results } returns volumesList
        every { response.error } returns "OK"
        every { response.limit } returns pageSize
        every { response.offset } returns 0
        every { response.numberOfPageResults } returns 0
        every { response.numberOfTotalResults } returns pageSize
        coEvery {
            volumesRepo.getItems(
                offset = 0,
                limit = pageSize,
                sort = sort,
                filters = listOf(VolumesFilter.Id(idList.subList(0, pageSize))),
            )
        } returns ComicVineResult.Success(response)

        val result = source.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = pageSize,
                placeholdersEnabled = false,
            )
        )
        assertTrue(result is PagingSource.LoadResult.Page)
        assertNull((result as PagingSource.LoadResult.Page).nextKey)

        verifyAll {
            response.statusCode
            response.results
            response.error
            response.limit
            response.offset
            response.numberOfPageResults
            response.numberOfTotalResults
        }
        coVerify {
            volumesRepo.getItems(
                offset = 0,
                limit = pageSize,
                sort = sort,
                filters = listOf(VolumesFilter.Id(idList.subList(0, pageSize))),
            )
        }
        confirmVerified(volumesRepo, response)
    }

    @Test
    fun `Service error`() = runTest {
        val pageSize = 10
        val response = mockk<VolumesResponse>()

        every { response.statusCode } returns StatusCode.InvalidAPIKey
        every { response.error } returns "Invalid API Key"
        coEvery {
            volumesRepo.getItems(
                offset = 0,
                limit = pageSize,
                sort = sort,
                filters = listOf(VolumesFilter.Id(idList.subList(0, pageSize))),
            )
        } returns ComicVineResult.Success(response)

        val result = source.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = pageSize,
                placeholdersEnabled = false,
            )
        )
        assertTrue(result is PagingSource.LoadResult.Error)
        (result as PagingSource.LoadResult.Error).run {
            val error = throwable as DetailsEntitySource.Error.Service
            assertEquals(StatusCode.InvalidAPIKey, error.statusCode)
            assertEquals("Invalid API Key", error.errorMessage)
        }

        verifyAll {
            response.statusCode
            response.error
        }
        coVerify {
            volumesRepo.getItems(
                offset = 0,
                limit = pageSize,
                sort = sort,
                filters = listOf(VolumesFilter.Id(idList.subList(0, pageSize))),
            )
        }
        confirmVerified(volumesRepo, response)
    }
}