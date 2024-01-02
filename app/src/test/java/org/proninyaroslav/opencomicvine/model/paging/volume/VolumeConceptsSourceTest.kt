package org.proninyaroslav.opencomicvine.model.paging.volume

import androidx.paging.PagingSource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.proninyaroslav.opencomicvine.types.*
import org.proninyaroslav.opencomicvine.types.filter.ConceptsFilter
import org.proninyaroslav.opencomicvine.model.AppPreferences
import org.proninyaroslav.opencomicvine.model.paging.details.DetailsEntitySource
import org.proninyaroslav.opencomicvine.model.paging.details.volume.VolumeConceptsSource
import org.proninyaroslav.opencomicvine.model.repo.ComicVineResult
import org.proninyaroslav.opencomicvine.model.repo.ConceptsRepository
import org.proninyaroslav.opencomicvine.model.repo.FavoriteFetchResult
import org.proninyaroslav.opencomicvine.model.repo.FavoritesRepository

@OptIn(ExperimentalCoroutinesApi::class)
class VolumeConceptsSourceTest {
    lateinit var source: VolumeConceptsSource

    @MockK
    lateinit var conceptsRepo: ConceptsRepository

    @MockK
    lateinit var pref: AppPreferences

    @MockK
    lateinit var favoritesRepo: FavoritesRepository

    val dispatcher = StandardTestDispatcher()

    val idList = (1..10).toList()

    val isFavorite = flowOf(FavoriteFetchResult.Success(isFavorite = true))

    private val concepts = idList.map {
        VolumeDetails.Concept(
            id = it,
            name = "$it",
            countOfAppearances = it,
        )
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        source = VolumeConceptsSource(
            concepts,
            conceptsRepo,
            favoritesRepo,
        )
        every {
            favoritesRepo.observe(
                entityId = any(),
                entityType = FavoriteInfo.EntityType.Concept,
            )
        } returns isFavorite
    }

    @Test
    fun `Load success result when more data is present`() = runTest {
        val pageSize = 5
        val response = mockk<ConceptsResponse>()
        val conceptsList = List(pageSize) { i ->
            mockk<ConceptInfo>().also {
                every { it.id } returns i + 1
            }
        }

        every { response.statusCode } returns StatusCode.OK
        every { response.results } returns conceptsList
        every { response.numberOfPageResults } returns pageSize
        every { response.numberOfTotalResults } returns idList.size
        every { response.error } returns "OK"
        every { response.offset } returns 0
        every { response.limit } returns pageSize
        coEvery {
            conceptsRepo.getItems(
                offset = 0,
                limit = pageSize,
                sort = null,
                filters = listOf(ConceptsFilter.Id(idList.subList(0, pageSize))),
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
            response.numberOfTotalResults
            response.error
            response.offset
            response.limit
        }
        coVerify {
            conceptsRepo.getItems(
                offset = 0,
                limit = pageSize,
                sort = null,
                filters = listOf(ConceptsFilter.Id(idList.subList(0, pageSize))),
            )
        }
        confirmVerified(conceptsRepo, response)
    }

    @Test
    fun `Load success result and endOfPaginationReached when no more data`() = runTest {
        val pageSize = 10
        val response = mockk<ConceptsResponse>()
        val conceptsList = emptyList<ConceptInfo>()

        every { response.statusCode } returns StatusCode.OK
        every { response.results } returns conceptsList
        every { response.numberOfPageResults } returns pageSize
        every { response.numberOfTotalResults } returns idList.size
        every { response.error } returns "OK"
        every { response.offset } returns 0
        every { response.limit } returns pageSize
        coEvery {
            conceptsRepo.getItems(
                offset = 0,
                limit = pageSize,
                sort = null,
                filters = listOf(ConceptsFilter.Id(idList.subList(0, pageSize))),
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
            response.numberOfPageResults
            response.numberOfTotalResults
            response.error
            response.offset
            response.limit
        }
        coVerify {
            conceptsRepo.getItems(
                offset = 0,
                limit = pageSize,
                sort = null,
                filters = listOf(ConceptsFilter.Id(idList.subList(0, pageSize))),
            )
        }
        confirmVerified(conceptsRepo, response)
    }

    @Test
    fun `Service error`() = runTest {
        val pageSize = 10
        val response = mockk<ConceptsResponse>()

        every { response.statusCode } returns StatusCode.InvalidAPIKey
        every { response.error } returns "Invalid API Key"
        coEvery {
            conceptsRepo.getItems(
                offset = 0,
                limit = pageSize,
                sort = null,
                filters = listOf(ConceptsFilter.Id(idList.subList(0, pageSize))),
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
            conceptsRepo.getItems(
                offset = 0,
                limit = pageSize,
                sort = null,
                filters = listOf(ConceptsFilter.Id(idList.subList(0, pageSize))),
            )
        }
        confirmVerified(conceptsRepo, response)
    }
}