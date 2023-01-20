package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GitHubSearchViewModel(

) : ViewModel() {

    lateinit var githubApi: GitHubService

    private val _repoList = MutableLiveData<List<Repository>>()
    val repoList: LiveData<List<Repository>>
        get() = _repoList

    companion object {
        const val BASE_URL = "https://api.github.com/"
    }

    fun setupRetrofit() {

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    fun saveUserLocal(context: Context, currentUser: String) {
        val editor = context.getSharedPreferences(
            GitHubSearchFragment.PREF_FILE_GITHUB_USER,
            Context.MODE_PRIVATE
        )
            .edit()

        editor.apply {
            putString(GitHubSearchFragment.PREF_CURRENT_USER, currentUser)
            apply()
        }
    }

    fun getLocalUser(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(
            GitHubSearchFragment.PREF_FILE_GITHUB_USER, Context.MODE_PRIVATE
        )
        return sharedPreferences.getString(GitHubSearchFragment.PREF_CURRENT_USER, "")
    }

    fun fetchAllPublicRepositories(fromUser: String) {
        githubApi.getAllRepositoriesByUser(fromUser).enqueue(object : Callback<List<Repository>> {
            override fun onResponse(
                call: Call<List<Repository>>,
                response: Response<List<Repository>>
            ) {
                if (response.isSuccessful) {
                    _repoList.value = response.body()
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {}
        })
    }

    fun shareRepositoryLink(context: Context, urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    fun openBrowser(context: Context, urlRepository: String) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )
    }
}