package org.proninyaroslav.opencomicvine.model.repo

import com.skydoves.sandwich.ApiResponse
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.proninyaroslav.opencomicvine.model.network.ComicVineService
import org.proninyaroslav.opencomicvine.types.MovieInfo
import org.proninyaroslav.opencomicvine.types.MoviesResponse
import org.proninyaroslav.opencomicvine.types.StatusCode
import org.proninyaroslav.opencomicvine.types.filter.MoviesFilter

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesRepositoryTest {
    lateinit var repo: MoviesRepository

    @MockK
    lateinit var apiKeyRepo: ApiKeyRepository

    @MockK
    lateinit var comicVineService: ComicVineService

    @MockK
    lateinit var moviesList: List<MovieInfo>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        repo = MoviesRepositoryImpl(comicVineService, apiKeyRepo)
    }

    @Test
    fun getMoviesList() = runTest {
        val apiKey = "123"
        val response = MoviesResponse(
            statusCode = StatusCode.OK,
            error = "OK",
            limit = 100,
            offset = 0,
            numberOfPageResults = 2,
            numberOfTotalResults = 2,
            results = moviesList,
        )
        val filters = listOf(MoviesFilter.Id(listOf(1)))

        every { apiKeyRepo.get() } returns flowOf(ApiKeyRepository.GetResult.Success(apiKey))
        coEvery {
            comicVineService.movies(
                apiKey = apiKey,
                offset = response.offset,
                limit = response.limit,
                sort = null,
                filter = filters,
            )
        } returns ApiResponse.Success(response)

        val res = repo.getItems(
            offset = response.offset,
            limit = response.limit,
            sort = null,
            filters = filters,
        )
        assertEquals(ComicVineResult.Success(response), res)

        verify { apiKeyRepo.get() }
        coVerify {
            comicVineService.movies(
                apiKey = apiKey,
                offset = response.offset,
                limit = response.limit,
                sort = null,
                filter = filters,
            )
        }
        confirmVerified(apiKeyRepo, comicVineService)
    }

    @Test
    fun `getMoviesList API key error`() = runTest {
        val error = ApiKeyRepository.GetResult.Failed.NoApiKey

        every { apiKeyRepo.get() } returns flowOf(error)

        val res = repo.getItems(
            offset = 0,
            limit = 0,
            sort = null,
            filters = emptyList(),
        )
        assertEquals(
            ComicVineResult.Failed.ApiKeyError(error),
            res
        )

        verify { apiKeyRepo.get() }
        confirmVerified(apiKeyRepo)
    }
}