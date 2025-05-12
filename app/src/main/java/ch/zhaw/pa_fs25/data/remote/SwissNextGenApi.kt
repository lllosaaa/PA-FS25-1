package ch.zhaw.pa_fs25.data.remote
import ch.zhaw.pa_fs25.data.model.SwissAccountResponse
import ch.zhaw.pa_fs25.data.model.SwissTransactionResponse
import retrofit2.http.GET
import retrofit2.http.Url

interface SwissNextGenApi {

    @GET("/v1/accounts")
    suspend fun getAccounts(): SwissAccountResponse

    @GET
    suspend fun getTransactionsByUrl(@Url fullUrl: String): SwissTransactionResponse
}
