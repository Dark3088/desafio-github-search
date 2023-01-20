package br.com.igorbag.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository

class RepositoryAdapter(private val repositories: List<Repository>) :
    RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    var repoItemListener: (Repository) -> Unit = {}
    var btnShareListener: (Repository) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.repository_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.repoRow.setOnClickListener {
            repoItemListener(repositories[position])
        }
        holder.repoName.text = repositories[position].name

        holder.shareRepo.setOnClickListener {
            btnShareListener(repositories[position])
        }
    }

    override fun getItemCount(): Int = repositories.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val repoRow: CardView
        val shareRepo: ImageView
        val repoName: TextView

        init {
            view.apply {
                repoRow = findViewById(R.id.cv_repo_row)
                shareRepo = findViewById(R.id.iv_share_repo)
                repoName = findViewById(R.id.tv_repo_name)
            }
        }
    }
}