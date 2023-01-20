package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    lateinit var userName: EditText
    lateinit var btnConfirm: Button
    lateinit var repoList: RecyclerView
    lateinit var githubApi: GitHubService
    lateinit var progressBar: ProgressBar
    lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        setupListeners()
        showUserName()
        setupRetrofit()
    }

    private fun setupView() {
        userName = findViewById(R.id.et_user_name)
        btnConfirm = findViewById(R.id.btn_confirm)
        repoList = findViewById(R.id.rv_repo_list)
        progressBar = findViewById(R.id.progress_circular)
        resultText = findViewById(R.id.tv_result_info)
    }

    private fun setupListeners() {
        btnConfirm.setOnClickListener {
            val currentUser = userName.text.trim().toString()

            if (currentUser.isNotBlank()) {
                progressBar.isVisible = true

                saveUserLocal(currentUser)
                getAllReposByUserName(currentUser)
            }
        }
    }

    private fun saveUserLocal(currentUser: String) {
        val editor = getSharedPreferences("GITHUB_USER", Context.MODE_PRIVATE).edit()
        editor.apply {
            putString("current_user", currentUser)
            apply()
        }
    }

    private fun getUserLocal(): String? {
        val sharedPreferences = getSharedPreferences("GITHUB_USER", Context.MODE_PRIVATE)
        return sharedPreferences.getString("current_user", "")
    }

    private fun showUserName() {
        val currentUser = getUserLocal()

        if (!currentUser.isNullOrEmpty())
            userName.setText(currentUser)
    }

    private fun setupRetrofit() {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    private fun getAllReposByUserName(user: String) {

        githubApi.getAllRepositoriesByUser(user).enqueue(object : Callback<List<Repository>> {
            override fun onResponse(
                call: Call<List<Repository>>,
                response: Response<List<Repository>>
            ) {
                if (response.isSuccessful) {
                    setupAdapter(response.body()!!)
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                resultText.isVisible = false
            }
        })
    }

    private fun setupAdapter(list: List<Repository>) {

        val repoAdapter = RepositoryAdapter(list)
        repoList.adapter = repoAdapter

        progressBar.isVisible = false
        if (!resultText.isVisible) resultText.isVisible = true

        repoAdapter.repoItemListener = { repoCard ->
            openBrowser(repoCard.htmlUrl)
        }

        repoAdapter.btnShareListener = { repoUrl ->
            shareRepositoryLink(repoUrl.htmlUrl)
        }
    }

    private fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )
    }
}