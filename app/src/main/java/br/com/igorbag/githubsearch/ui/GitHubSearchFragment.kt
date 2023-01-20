package br.com.igorbag.githubsearch.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter

class GitHubSearchFragment : Fragment() {

    companion object {

        // Preference keys
        const val PREF_FILE_GITHUB_USER = "GITHUB_USER"
        const val PREF_CURRENT_USER = "current_user"
    }

    private lateinit var repoListView: RecyclerView
    private lateinit var userName: EditText
    private lateinit var btnConfirm: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var resultText: TextView
    private val searchViewModel: GitHubSearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_git_hub_search, container, false)

        setupView(root); setUpButtonListener()
        searchViewModel.setupRetrofit()

        return root
    }

    private fun setupView(view: View) {
        repoListView = view.findViewById(R.id.rv_repo_list)
        userName = view.findViewById(R.id.et_user_name)
        btnConfirm = view.findViewById(R.id.btn_confirm)
        progressBar = view.findViewById(R.id.progress_circular)
        resultText = view.findViewById(R.id.tv_result_info)
    }

    private fun showUserName() {
        val currentUser = searchViewModel.getLocalUser(requireContext())
        if (!currentUser.isNullOrEmpty()) userName.setText(currentUser)
    }

    private fun setUpButtonListener() {
        btnConfirm.setOnClickListener {
            resultText.isVisible = false
            repoListView.isVisible = false
            progressBar.isVisible = true

            val currentUser = userName.text.trim().toString()
            if (currentUser.isNotBlank()) {

                searchViewModel.saveUserLocal(requireContext(), currentUser)
                searchViewModel.fetchAllPublicRepositories(currentUser)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) showUserName()

        searchViewModel.repoList.observe(this.viewLifecycleOwner) { newRepoList ->
            repoListView.isVisible = true
            setUpAdapter(newRepoList)

            progressBar.isVisible = false
            resultText.isVisible = true
        }
    }

    private fun setUpAdapter(repoList: List<Repository>) {
        val adapter = RepositoryAdapter(repoList)
        repoListView.adapter = adapter

        adapter.btnShareListener = { shareRepository ->
            searchViewModel.shareRepositoryLink(
                requireContext(),
                shareRepository.htmlUrl
            )
        }

        adapter.repoItemListener = { repository ->
            searchViewModel.openBrowser(
                requireContext(),
                repository.htmlUrl
            )
        }
    }
}